package thermaldynamics.gui.container;

import cofh.lib.gui.slot.SlotFalseCopy;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

import thermaldynamics.ducts.attachments.filter.IFilterConfig;

public class SlotFilter extends SlotFalseCopy {

	IFilterConfig filter;

	public final static IInventory dummy = new InventoryBasic("Dummy", true, 0);

	public SlotFilter(IFilterConfig tile, int slotIndex, int x, int y) {

		super(dummy, slotIndex, x, y);
		filter = tile;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {

		return stack != null;
	}

	@Override
	public ItemStack getStack() {

		return filter.getFilterStacks()[getSlotIndex()];
	}

	@Override
	public void putStack(ItemStack stack) {

		synchronized (filter.getFilterStacks()) {
			if (stack != null)
				stack.stackSize = 1;
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

		return null;
	}

	@Override
	public boolean isSlotInInventory(IInventory inventory, int slot) {

		return false;
	}

}
