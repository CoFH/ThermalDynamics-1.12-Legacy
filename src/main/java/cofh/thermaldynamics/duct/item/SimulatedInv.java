package cofh.thermaldynamics.duct.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SimulatedInv implements IItemHandler {

	public static final int REBUILD_THRESHOLD = 128;
	public static SimulatedInv INSTANCE = new SimulatedInv();
	IItemHandler originalLogic;
	IItemHandler slotHandler;
	ItemStack[] items;
	int size;

	public SimulatedInv() {

	}

	public static SimulatedInv wrapHandler(IItemHandler handler) {

		return INSTANCE.setTarget(handler);
	}

	public SimulatedInv setTarget(IItemHandler target) {

		originalLogic = target;
		size = target.getSlots();

		if (items == null || items.length < size || (size < REBUILD_THRESHOLD && items.length >= REBUILD_THRESHOLD)) {
			items = new ItemStack[target.getSlots()];
			this.slotHandler = new ItemStackHandler(items);
		}
		ItemStack stackInSlot;
		for (int i = 0; i < size; i++) {
			stackInSlot = target.getStackInSlot(i);
			items[i] = stackInSlot != null ? stackInSlot.copy() : null;
		}
		return this;
	}

	@Override
	public int getSlots() {

		return size;
	}

	@Override
	public ItemStack getStackInSlot(int i) {

		return items[i];
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

		if (stack == null || stack.stackSize == 0) {
			return null;
		}
		if (originalLogic == null) {
			return stack;
		}
		int originalStackSize = stack.stackSize;
		ItemStack copy = stack.copy();
		int maxStackSize = copy.getMaxStackSize();
		copy.stackSize = maxStackSize;
		ItemStack insertItem = originalLogic.insertItem(slot, copy, true);

		// rejected
		if (insertItem == copy) {
			return stack;
		}
		int insertable = maxStackSize - (insertItem != null ? insertItem.stackSize : 0);

		if (insertable == 0) {
			return stack; // rejected
		}
		if (insertable >= originalStackSize) { // whole stack would have been accepted
			return slotHandler.insertItem(slot, stack, simulate);
		}
		// only partial stack would have been accepted
		copy.stackSize = insertable;
		int remainderStackSize = originalStackSize - insertable;
		ItemStack simInsertStack = slotHandler.insertItem(slot, copy, simulate);

		if (simInsertStack == null || simInsertStack.stackSize == 0) {
			copy.stackSize = remainderStackSize;
		} else {
			copy.stackSize = remainderStackSize + simInsertStack.stackSize;
		}
		return copy;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {

		throw new UnsupportedOperationException();
	}

}
