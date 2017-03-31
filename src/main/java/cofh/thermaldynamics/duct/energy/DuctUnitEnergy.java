package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitEnergy extends DuctUnit<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> {

	public int energyForGrid = 0;
	public int lastStoredValue = 0;
	byte internalSideCounter;
	int type;

	public DuctUnitEnergy(TileGrid parent, Duct duct) {

		super(parent, duct);
	}

	@Override
	public DuctToken<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> getToken() {
		return DuctToken.ENERGY;
	}

	@Override
	public EnergyGrid createGrid() {
		return new EnergyGrid(world(), getTransferLimit(), getCapacity());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> adjDuct, byte side) {
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

			if (tileCaches[i] != null) {
				IEnergyReceiver receiver = tileCaches[i];
				if (receiver.canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
					usedEnergy += sendEnergy(receiver, energy - usedEnergy, i, simulate);
				}
				if (!simulate && usedEnergy >= energy) {
					this.tickInternalSideCounter(i + 1);
					break;
				}
			}
		}

		for (byte i = 0; i < this.internalSideCounter && usedEnergy < energy; i++) {
			if (tileCaches[i] != null) {
				IEnergyReceiver receiver = tileCaches[i];
				if (receiver.canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
					usedEnergy += sendEnergy(receiver, energy - usedEnergy, i, simulate);
				}
				if (!simulate && usedEnergy >= energy) {
					this.tickInternalSideCounter(i + 1);
					break;
				}
			}
		}
		return usedEnergy;
	}

	public int getTransferLimit() {
		return EnergyGrid.NODE_TRANSFER[type];
	}

	public int getCapacity() {
		return EnergyGrid.NODE_STORAGE[type];
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


	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		if(grid == null) return 0;
		return grid.receiveEnergy(maxReceive, simulate);
	}

	public boolean canConnectEnergy(EnumFacing facing){
		return tileCaches[facing.ordinal()] != null;
	}
}
