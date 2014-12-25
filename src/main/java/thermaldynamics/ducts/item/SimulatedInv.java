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

    public SimulatedInv(IInventory target) {
        super(target.getInventoryName(), false, target.getSizeInventory());
        this.target = target;
    }

    IInventory target;
    int curReadSlot = -1;

    public void ensureSlotRead(int newMax) {
        ItemStack stackInSlot;
        for (curReadSlot++; curReadSlot <= newMax && curReadSlot < target.getSizeInventory(); curReadSlot++) {
            stackInSlot = target.getStackInSlot(curReadSlot);
            this.setInventorySlotContents(curReadSlot, stackInSlot != null ? stackInSlot.copy() : null);
        }
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot > curReadSlot) ensureSlotRead(slot);
        return super.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int size) {
        if (slot > curReadSlot) ensureSlotRead(slot);
        return super.decrStackSize(slot, size);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (slot > curReadSlot) ensureSlotRead(slot);
        return super.getStackInSlotOnClosing(slot);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot > curReadSlot) ensureSlotRead(slot);
        super.setInventorySlotContents(slot, stack);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot > curReadSlot) ensureSlotRead(slot);
        return slot < target.getSizeInventory() && target.isItemValidForSlot(slot, stack);
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
            return slot < target.getSizeInventory() && sided.canInsertItem(slot, item, side);
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack item, int side) {
            return slot < target.getSizeInventory() && sided.canExtractItem(slot, item, side);
        }

    }
}
