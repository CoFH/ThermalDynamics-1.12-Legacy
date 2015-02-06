package cofh.thermaldynamics.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotNull extends Slot {
    private static final IInventory inv = new InventoryBasic("[Null]", true, 1);

    public SlotNull(int x, int y) {
        super(inv, 0, x, y);
    }

    @Override
    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {

    }

    @Override
    protected void onCrafting(ItemStack p_75210_1_, int p_75210_2_) {

    }

    @Override
    protected void onCrafting(ItemStack p_75208_1_) {

    }

    @Override
    public void onPickupFromSlot(EntityPlayer p_82870_1_, ItemStack p_82870_2_) {

    }

    @Override
    public boolean isItemValid(ItemStack p_75214_1_) {
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
    public void putStack(ItemStack p_75215_1_) {

    }

    @Override
    public void onSlotChanged() {

    }

    @Override
    public int getSlotStackLimit() {
        return 64;
    }

    @Override
    public ItemStack decrStackSize(int p_75209_1_) {
        return null;
    }

    @Override
    public boolean isSlotInInventory(IInventory p_75217_1_, int p_75217_2_) {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer p_82869_1_) {
        return false;
    }
}
