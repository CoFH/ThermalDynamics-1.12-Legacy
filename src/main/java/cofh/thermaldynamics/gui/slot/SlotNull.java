package cofh.thermaldynamics.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class SlotNull extends Slot {

	private static final IInventory INV = new InventoryBasic("[Null]", true, 1);

	public SlotNull(int x, int y) {

		super(INV, 0, x, y);
	}

	@Override
	public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {

	}

	@Override
	protected void onCrafting(ItemStack stack, int amount) {

	}

	@Override
	protected void onCrafting(ItemStack stack) {

	}

	@Override
	public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {

	}

	@Override
	public boolean isItemValid(@Nullable ItemStack stack) {

		return false;
	}

	@Override
	public ItemStack getStack() {

		return null;
	}

	@Override
	public boolean getHasStack() {

		return false;
	}

	@Override
	public void putStack(@Nullable ItemStack stack) {

	}

	@Override
	public void onSlotChanged() {

	}

	@Override
	public int getSlotStackLimit() {

		return 64;
	}

	@Override
	public ItemStack decrStackSize(int amount) {

		return null;
	}

	@Override
	public boolean isHere(IInventory inv, int slotIn) {

		return false;
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn) {

		return false;
	}

}
