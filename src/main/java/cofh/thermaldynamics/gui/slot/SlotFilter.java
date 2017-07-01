package cofh.thermaldynamics.gui.slot;

import cofh.core.gui.slot.SlotFalseCopy;
import cofh.thermaldynamics.duct.attachments.filter.IFilterConfig;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class SlotFilter extends SlotFalseCopy {

	IFilterConfig filter;

	private static final IInventory INV = new InventoryBasic("[FALSE]", false, 0);

	public SlotFilter(IFilterConfig tile, int slotIndex, int x, int y) {

		super(INV, slotIndex, x, y);
		filter = tile;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {

		return !stack.isEmpty();
	}

	@Override
	public ItemStack getStack() {

		return filter.getFilterStacks()[getSlotIndex()];
	}

	@Override
	public void putStack(ItemStack stack) {

		synchronized (filter.getFilterStacks()) {
			if (!stack.isEmpty()) {
				stack.setCount(1);
			}
			filter.getFilterStacks()[getSlotIndex()] = stack;
			onSlotChanged();
		}
	}

	@Override
	public void onSlotChanged() {

		filter.onChange();
	}

	@Override
	public int getSlotStackLimit() {

		return 1;
	}

	@Override
	public ItemStack decrStackSize(int amount) {

		return ItemStack.EMPTY;
	}

	@Override
	public boolean isHere(IInventory inv, int slotIn) {

		return false;
	}

}
