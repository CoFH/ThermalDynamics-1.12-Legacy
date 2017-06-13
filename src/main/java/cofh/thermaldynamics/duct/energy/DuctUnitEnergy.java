package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitEnergy extends DuctUnit<DuctUnitEnergy, GridEnergy, IEnergyReceiver> implements cofh.api.energy.IEnergyStorage {

	public int energyForGrid = 0;
	public int lastStoredValue = 0;
	byte internalSideCounter;
	private int transferLimit;
	private int capacity;

	public DuctUnitEnergy(TileGrid parent, Duct duct, int transferLimit, int capacity) {

		super(parent, duct);
		this.transferLimit = transferLimit;
		this.capacity = capacity;
	}

	public DuctUnitEnergy(TileDuctEnergy parent, Duct duct) {

		super(parent, duct);
		transferLimit = GridEnergy.NODE_TRANSFER[duct.type];
		capacity = GridEnergy.NODE_STORAGE[duct.type];
	}

	@Override
	protected IEnergyReceiver[] createTileCache() {

		return new IEnergyReceiver[6];
	}

	@Override
	protected DuctUnitEnergy[] createDuctCache() {

		return new DuctUnitEnergy[6];
	}

	@Nonnull
	@Override
	public DuctToken<DuctUnitEnergy, GridEnergy, IEnergyReceiver> getToken() {

		return DuctToken.ENERGY;
	}

	@Override
	public GridEnergy createGrid() {

		return new GridEnergy(world(), getTransferLimit(), getCapacity());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, GridEnergy, IEnergyReceiver> adjDuct, byte side, byte oppositeSide) {

		return getTransferLimit() == adjDuct.cast().getTransferLimit();
	}

	@Override
	@Nullable
	public IEnergyReceiver cacheTile(@Nonnull TileEntity tile, byte side) {

		EnumFacing facing = EnumFacing.values()[side ^ 1];
		if (tile instanceof IEnergyReceiver) {
			IEnergyReceiver energyReceiver = (IEnergyReceiver) tile;
			if (energyReceiver.canConnectEnergy(facing)) {
				return energyReceiver;
			} else if (tile instanceof IDuctHolder) {
				return energyReceiver;
			}
		}
		if (tile.hasCapability(CapabilityEnergy.ENERGY, facing)) {
			IEnergyStorage capability = tile.getCapability(CapabilityEnergy.ENERGY, facing);
			if (capability != null && capability.canReceive()) {
				return new IEnergyReceiver() {

					public IEnergyStorage getStorage(EnumFacing facing1) {

						if (tile.hasCapability(CapabilityEnergy.ENERGY, facing1)) {
							return tile.getCapability(CapabilityEnergy.ENERGY, facing1);
						}
						return null;
					}

					@Override
					public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

						IEnergyStorage storage = getStorage(from);
						return storage == null ? 0 : storage.receiveEnergy(maxReceive, simulate);
					}

					@Override
					public int getEnergyStored(EnumFacing from) {

						IEnergyStorage storage = getStorage(from);
						return storage == null ? 0 : storage.getEnergyStored();
					}

					@Override
					public int getMaxEnergyStored(EnumFacing from) {

						IEnergyStorage storage = getStorage(from);
						return storage == null ? 0 : storage.getMaxEnergyStored();
					}

					@Override
					public boolean canConnectEnergy(EnumFacing from) {

						return true;
					}
				};
			}
		}
		return null;
	}

	@Override
	public void tileUnloading() {

		if (isNode() && grid != null) {
			grid.myStorage.extractEnergy(lastStoredValue, false);
		}
	}

	@Override
	public boolean tickPass(int pass) {

		return super.tickPass(pass) && sendEnergy();

	}

	public boolean sendEnergy() {

		int power = this.grid.getSendableEnergy();

		int usedPower = transmitEnergy(power, false);

		this.grid.useEnergy(usedPower);
		return true;
	}

	public int getEnergyForGrid() {

		return energyForGrid;
	}

	public void setEnergyForGrid(int energy) {

		energyForGrid = energy;
	}

	public int transmitEnergy(int energy, boolean simulate) {

		int usedEnergy = 0;

		for (byte i = this.internalSideCounter; i < 6 && usedEnergy < energy; i++) {

			if (tileCache[i] != null) {
				IEnergyReceiver receiver = tileCache[i];
				if (receiver.canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
					usedEnergy += sendEnergy(receiver, energy - usedEnergy, i, simulate);
				}
				if (!simulate && usedEnergy >= energy) {
					internalSideCounter = tickInternalSideCounter(i + 1);
					break;
				}
			}
		}
		for (byte i = 0; i < this.internalSideCounter && usedEnergy < energy; i++) {
			if (tileCache[i] != null) {
				IEnergyReceiver receiver = tileCache[i];
				if (receiver.canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
					usedEnergy += sendEnergy(receiver, energy - usedEnergy, i, simulate);
				}
				if (!simulate && usedEnergy >= energy) {
					internalSideCounter = tickInternalSideCounter(i + 1);
					break;
				}
			}
		}
		return usedEnergy;
	}

	public int getTransferLimit() {

		return transferLimit;
	}

	public int getCapacity() {

		return capacity;
	}

	protected int sendEnergy(IEnergyReceiver receiver, int maxReceive, byte side, boolean simulate) {

		return receiver.receiveEnergy(EnumFacing.VALUES[side ^ 1], maxReceive, simulate);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		energyForGrid = nbt.getInteger("Energy");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		if (grid != null) {
			if (isNode()) {
				lastStoredValue = grid.getNodeShare(this);
				nbt.setInteger("Energy", lastStoredValue);
			}
		} else if (energyForGrid > 0) {
			nbt.setInteger("Energy", energyForGrid);
		} else {
			energyForGrid = 0;
		}
		return nbt;
	}

	public boolean canExtract() {

		return true;
	}

	/* IEnergyStorage */
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {

		return grid != null ? grid.receiveEnergy(maxReceive, simulate) : 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {

		return (canExtract() && grid != null) ? grid.myStorage.extractEnergy(maxExtract, simulate) : 0;
	}

	public int getEnergyStored() {

		return grid != null ? grid.myStorage.getEnergyStored() : 0;
	}

	@Override
	public int getMaxEnergyStored() {

		return grid != null ? grid.myStorage.getMaxEnergyStored() : 0;
	}

	/* CAPABILITIES */
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

		return CapabilityEnergy.ENERGY.cast(new IEnergyStorage() {

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {

				return DuctUnitEnergy.this.receiveEnergy(maxReceive, simulate);
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {

				return DuctUnitEnergy.this.extractEnergy(maxExtract, simulate);
			}

			@Override
			public int getEnergyStored() {

				return DuctUnitEnergy.this.getEnergyStored();
			}

			@Override
			public int getMaxEnergyStored() {

				return DuctUnitEnergy.this.getMaxEnergyStored();
			}

			@Override
			public boolean canExtract() {

				return DuctUnitEnergy.this.canExtract();
			}

			@Override
			public boolean canReceive() {

				return true;
			}
		});
	}

}
