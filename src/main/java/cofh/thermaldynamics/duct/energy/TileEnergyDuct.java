package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEnergyDuct extends TileTDBase implements IEnergyReceiver, IEnergyProvider, IEnergyDuctInternal {

	public int energyForGrid = 0;
	public int lastStoredValue = 0;
	EnergyGrid internalGrid;

	public TileEnergyDuct() {

	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (EnergyGrid) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergyGrid(worldObj, getDuctType().type);
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {

		return connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

		return (this.internalGrid != null && canConnectEnergy(from)) ? this.internalGrid.myStorage.receiveEnergy(maxReceive, simulate) : 0;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		return (this.internalGrid != null && canConnectEnergy(from)) ? this.internalGrid.myStorage.extractEnergy(maxExtract, simulate) : 0;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {

		return this.internalGrid != null ? this.internalGrid.myStorage.getEnergyStored() : 0;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {

		return this.internalGrid != null ? this.internalGrid.myStorage.getMaxEnergyStored() : 0;
	}

	@Override
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileEnergyDuct && ((TileEnergyDuct) theTile).getDuctType().type == getDuctType().type;
	}

	@Override
	public boolean isSignificantTile(TileEntity theTile, int side) {

		return theTile instanceof IEnergyConnection && ((IEnergyConnection) theTile).canConnectEnergy(EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public void tileUnloading() {

		if (isNode) {
			internalGrid.myStorage.extractEnergy(lastStoredValue, false);
		}
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}

		int power = this.internalGrid.getSendableEnergy();

		int usedPower = transmitEnergy(power, false);

		this.internalGrid.useEnergy(usedPower);
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

		if (!cachesExist()) {
			return usedEnergy;
		}
		for (byte i = this.internalSideCounter; i < this.neighborTypes.length && usedEnergy < energy; i++) {
			if (this.neighborTypes[i] == NeighborTypes.OUTPUT && this.connectionTypes[i] == ConnectionTypes.NORMAL) {
				if (cache[i] != null) {
					if (cache[i].canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
						usedEnergy += sendEnergy(cache[i], energy - usedEnergy, i, simulate);
					}
					if (!simulate && usedEnergy >= energy) {
						this.tickInternalSideCounter(i + 1);
						break;
					}
				}
			}
		}

		for (byte i = 0; i < this.internalSideCounter && usedEnergy < energy; i++) {
			if (this.neighborTypes[i] == NeighborTypes.OUTPUT && this.connectionTypes[i] == ConnectionTypes.NORMAL) {
				if (cache[i] != null) {
					if (cache[i].canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
						usedEnergy += sendEnergy(cache[i], energy - usedEnergy, i, simulate);
					}
					if (!simulate && usedEnergy >= energy) {
						this.tickInternalSideCounter(i + 1);
						break;
					}
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

		if (internalGrid != null) {
			if (isNode) {
				lastStoredValue = internalGrid.getNodeShare(this);
				nbt.setInteger("Energy", lastStoredValue);
			}
		} else if (energyForGrid > 0) {
			nbt.setInteger("Energy", energyForGrid);
		} else {
			energyForGrid = 0;
		}
		return nbt;
	}

	@Override
	public boolean cachesExist() {

		return cache != null;
	}

	IEnergyReceiver[] cache;

	@Override
	public void cacheImportant(TileEntity tile, int side) {

		if (tile instanceof IEnergyReceiver) {
			cache[side] = (IEnergyReceiver) tile;
		}
	}

	@Override
	public void createCaches() {

		cache = new IEnergyReceiver[6];
	}

	@Override
	public void clearCache(int side) {

		cache[side] = null;
	}

}
