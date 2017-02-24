package cofh.thermaldynamics.gui.slot;

import cofh.lib.gui.slot.SlotFalseCopy;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class SlotIcon extends SlotFalseCopy {

	private static final IInventory INV = new InventoryBasic("[FALSE]", false, 0);

	private final TileTransportDuct duct;

	public SlotIcon(int x, int y, TileTransportDuct duct) {

		super(INV, 0, x, y);
		this.duct = duct;
	}

	@Override
	public ItemStack getStack() {

		return ItemStack.copyItemStack(duct.data.item);
	}

	@Override
	public void putStack(ItemStack stack) {

		if (stack != null) {
			stack.stackSize = 1;
		}
		duct.setIcon(stack);
	}

	@Override
	public void onSlotChanged() {

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
	public boolean isHere(IInventory inv, int slotIn) {

		return false;
	}

}
