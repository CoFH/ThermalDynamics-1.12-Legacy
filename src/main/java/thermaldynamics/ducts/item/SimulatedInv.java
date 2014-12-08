package thermaldynamics.ducts.item;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class SimulatedInv extends InventoryBasic {
    public static SimulatedInv wrapInv(IInventory inventory) {
        if (inventory instanceof ISidedInventory)
            return new SimulatedInvSided((ISidedInventory) inventory);
        else
            return new SimulatedInv(inventory);
    }

    IInventory target;

    public SimulatedInv(IInventory target) {
        super(target.getInventoryName(), false, target.getSizeInventory());
        this.target = target;
        for (int i = 0; i < target.getSizeInventory(); i++) {
            this.setInventorySlotContents(i, target.getStackInSlot(i));
        }
    }

    @Override
    public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
        return target.isItemValidForSlot(p_94041_1_, p_94041_2_);
    }

    @Override
    public void markDirty() {

    }

    public static class SimulatedInvSided extends SimulatedInv implements ISidedInventory {
        ISidedInventory sided;

        public SimulatedInvSided(ISidedInventory target) {
            super(target);
            this.sided = target;
        }


        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return sided.getAccessibleSlotsFromSide(side);
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack item, int side) {
            return sided.canInsertItem(slot, item, side);
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack item, int side) {
            return sided.canExtractItem(slot, item, side);
        }
    }
}
