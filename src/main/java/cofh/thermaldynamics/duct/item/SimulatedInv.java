package cofh.thermaldynamics.duct.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;

public class SimulatedInv implements IInventory {

	public static SimulatedInv INSTANCE = new SimulatedInv();
	public static SimulatedInvSided INSTANCE_SIDED = new SimulatedInvSided();
	public static final int REBUILD_THRESHOLD = 128;

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

		if (items == null || items.length < size || (size < REBUILD_THRESHOLD && items.length >= REBUILD_THRESHOLD)) {
			items = new ItemStack[target.getSizeInventory()];
		}
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
	public ItemStack decrStackSize(int slot, int amount) {

		return null;
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {

		return items[i];
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		items[i] = stack;
	}

	@Override
	public String getName() {

		return "[Simulated]";
	}

	@Override
	public boolean hasCustomName() {

		return false;
	}

    @Override
    public ITextComponent getDisplayName() {
        return null;
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
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return slot < target.getSizeInventory() && target.isItemValidForSlot(slot, stack);
	}

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
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
	}

}
