package cofh.thermaldynamics.duct.item;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class SimulatedInv implements IItemHandler {

	public static final int REBUILD_THRESHOLD = 128;
	public static SimulatedInv INSTANCE = new SimulatedInv();
	IItemHandler originalLogic;
	IItemHandler slotHandler;
	NonNullList<ItemStack> items;
	int size;

	public SimulatedInv() {

	}

	public SimulatedInv(IInventory target) {

		setTarget(new InvWrapper(target));
	}

	public static SimulatedInv wrapHandler(IItemHandler handler) {

		return INSTANCE.setTarget(handler);
	}

	public static SimulatedInv wrapInv(IInventory inventory) {

		return INSTANCE.setTarget(new InvWrapper(inventory));
	}

	public void clear() {

		this.originalLogic = null;
	}

	public SimulatedInv setTarget(IItemHandler target) {

		originalLogic = target;

		size = target.getSlots();

		if (items == null || items.size() < size || (size < REBUILD_THRESHOLD && items.size() >= REBUILD_THRESHOLD)) {
			items = NonNullList.withSize(target.getSlots(), ItemStack.EMPTY);
			this.slotHandler = new ItemStackHandler(items);
		}
		ItemStack stackInSlot;
		for (int i = 0; i < size; i++) {
			stackInSlot = target.getStackInSlot(i);
			items.set(i, !stackInSlot.isEmpty() ? stackInSlot.copy() : ItemStack.EMPTY);
		}
		return this;
	}

	@Override
	public int getSlots() {

		return size;
	}

	@Override
	public ItemStack getStackInSlot(int i) {

		return items.get(i);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

		if (stack.isEmpty() || stack.getCount() == 0) {
			return ItemStack.EMPTY;
		}

		int originalStackSize = stack.getCount();
		ItemStack copy = stack.copy();
		int maxStackSize = copy.getMaxStackSize();
		copy.setCount(maxStackSize);
		ItemStack insertItem = originalLogic.insertItem(slot, copy, true);

		// rejected
		if (insertItem == copy) {
			return stack;
		}

		int insertable = maxStackSize - (!insertItem.isEmpty() ? insertItem.getCount() : 0);

		if (insertable == 0) {
			return stack; // rejected
		}

		if (insertable >= originalStackSize) // whole stack would have been accepted
		{
			return slotHandler.insertItem(slot, stack, simulate);
		}

		// only partial stack would have been accepted
		copy.setCount(insertable);

		int remainderStackSize = originalStackSize - insertable;

		ItemStack simInsertStack = slotHandler.insertItem(slot, copy, simulate);

		if (simInsertStack.isEmpty() || simInsertStack.getCount() == 0) {
			copy.setCount(remainderStackSize);
		} else {
			copy.setCount(remainderStackSize + simInsertStack.getCount());
		}
		return copy;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {

		throw new UnsupportedOperationException();
	}

	@Override
	public int getSlotLimit(int slot) {

		return originalLogic.getSlotLimit(slot);
	}
}
