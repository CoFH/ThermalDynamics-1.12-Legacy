package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public class DuctUnitEnergy<E extends DuctUnitEnergy<E, G>, G extends EnergyGrid<E>> extends DuctUnit<E, G, IEnergyReceiver> implements IEnergyDuctInternal<E, G> {

	public int energyForGrid = 0;
	public int lastStoredValue = 0;
	byte internalSideCounter;

	public DuctUnitEnergy(TileGrid parent) {

		super(parent);
	}

	@Override
	public DuctToken<E, G, IEnergyReceiver> getToken() {
		return null;
	}

	@Override
	public G createGrid() {

		return (G) new EnergyGrid<E>(world(), 0);
	}

	@Override
	@Nullable
	public IEnergyReceiver cacheTile(TileEntity tile, byte side) {
		if (tile instanceof IEnergyReceiver) {
			IEnergyReceiver energyReceiver = (IEnergyReceiver) tile;
			if (energyReceiver.canConnectEnergy(EnumFacing.values()[side ^ 1])) {
				return energyReceiver;
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

		if (!super.tickPass(pass)) {
			return false;
		}

		int power = this.grid.getSendableEnergy();

		int usedPower = transmitEnergy(power, false);

		this.grid.useEnergy(usedPower);
		return true;
	}

	@Override
	public int getEnergyForGrid() {

		return energyForGrid;
	}

	@Override
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
				lastStoredValue = grid.getNodeShare((E) this);
				nbt.setInteger("Energy", lastStoredValue);
			}
		} else if (energyForGrid > 0) {
			nbt.setInteger("Energy", energyForGrid);
		} else {
			energyForGrid = 0;
		}
		return nbt;
	}
}
