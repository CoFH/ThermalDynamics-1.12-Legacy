package cofh.thermaldynamics.duct.item;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class SimulatedInv implements IItemHandler {

	public static SimulatedInv INSTANCE = new SimulatedInv();
	public static final int REBUILD_THRESHOLD = 128;

	public static SimulatedInv wrapHandler(IItemHandler handler) {
        return INSTANCE.setTarget(handler);
    }

	public static SimulatedInv wrapInv(IInventory inventory) {
		return INSTANCE.setTarget(new InvWrapper(inventory));
	}

	public SimulatedInv() {

	}

	public SimulatedInv(IInventory target) {
		setTarget(new InvWrapper(target));
	}

	public void clear() {
		this.target = null;
	}

	public SimulatedInv setTarget(IItemHandler target) {

		//this.target = target;
		size = target.getSlots();

		if (items == null || items.length < size || (size < REBUILD_THRESHOLD && items.length >= REBUILD_THRESHOLD)) {
			items = new ItemStack[target.getSlots()];
		}
		ItemStack stackInSlot;
		for (int i = 0; i < size; i++) {
			stackInSlot = target.getStackInSlot(i);
			items[i] = stackInSlot != null ? stackInSlot.copy() : null;
		}
		this.target = new ItemStackHandler(items);
		return this;
	}
    IItemHandler target;
	//IInventory target;
	ItemStack[] items;
	int size;

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
        return target.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return target.extractItem(slot, amount, simulate);
    }

    //Left here for reasons.
    /*public static class SimulatedInvSided extends SimulatedInv implements ISidedInventory {

		ISidedInventory sided;

		public SimulatedInvSided(ISidedInventory target) {

			super(target);
			this.sided = target;
		}

		public SimulatedInvSided() {

		}

		@Override
		public int[] getSlotsForFace(EnumFacing side) {

			return sided.getSlotsForFace(side);
		}

		@Override
		public boolean canInsertItem(int slot, ItemStack item, EnumFacing side) {

			return slot < target.getSizeInventory() && sided.canInsertItem(slot, item, side);
		}

		@Override
		public boolean canExtractItem(int slot, ItemStack item, EnumFacing side) {

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
	}*/

}
