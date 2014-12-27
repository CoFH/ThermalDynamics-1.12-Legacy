package thermaldynamics.ducts.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public class SimulatedInv implements IInventory {
    public static SimulatedInv INSTANCE = new SimulatedInv();
    public static SimulatedInvSided INSTANCE_SIDED = new SimulatedInvSided();
    public int REBUILD_THRESHOLD = 512;

    public static SimulatedInv wrapInv(IInventory inventory) {
        INSTANCE.setTarget(inventory);
        return INSTANCE;
    }

    public static SimulatedInvSided wrapInvSided(ISidedInventory inventory) {
        INSTANCE_SIDED.setTargetSided(inventory);
        return INSTANCE_SIDED;
    }

    public SimulatedInv() {

    }


    public SimulatedInv(IInventory target) {
        setTarget(target);
    }

    public void clear() {
        this.target = null;
    }

    public void setTarget(IInventory target) {
        this.target = target;

        size = target.getSizeInventory();

        if (items == null || items.length < size || (size < REBUILD_THRESHOLD && items.length >= REBUILD_THRESHOLD))
            items = new ItemStack[target.getSizeInventory()];

        ItemStack stackInSlot;
        for (int i = 0; i < size; i++) {
            stackInSlot = target.getStackInSlot(i);
            items[i] = stackInSlot != null ? stackInSlot.copy() : null;
        }
    }


    IInventory target;
    ItemStack[] items;
    int size;


    @Override
    public int getSizeInventory() {
        return items.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return items[i];
    }

    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        return items[i];
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        items[i] = stack;
    }

    @Override
    public String getInventoryName() {
        return "[Simulated]";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return target.getInventoryStackLimit();
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
        return false;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot < target.getSizeInventory() && target.isItemValidForSlot(slot, stack);
    }


    public static class SimulatedInvSided extends SimulatedInv implements ISidedInventory {
        ISidedInventory sided;

        public SimulatedInvSided(ISidedInventory target) {
            super(target);
            this.sided = target;
        }

        public SimulatedInvSided() {

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


        public void setTargetSided(ISidedInventory target) {
            setTarget(target);
            sided = target;
        }

        @Override
        public void clear() {
            super.clear();
            sided = null;
        }
    }
}
