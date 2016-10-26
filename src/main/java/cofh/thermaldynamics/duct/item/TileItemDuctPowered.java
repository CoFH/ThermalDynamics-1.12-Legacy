package cofh.thermaldynamics.duct.item;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.block.SubTileMultiBlock;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.energy.subgrid.SubTileEnergy;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public abstract class TileItemDuctPowered extends TileItemDuct implements IEnergyHandler, IEnergyProvider, IEnergyReceiver {

	SubTileEnergy energy;

	public TileItemDuctPowered() {

		super();
	}

	public void setSubEnergy(SubTileEnergy energy) {

		this.energy = energy;
		this.subTiles = new SubTileMultiBlock[] { energy };
	}

	@Override
	public BlockDuct.ConnectionTypes getRenderConnectionType(int side) {

		if (attachments[side] != null) {
			return attachments[side].getRenderConnectionType();
		}

		if (neighborTypes[side] == NeighborTypes.STRUCTURE) {
			return connectionTypes[side] != ConnectionTypes.BLOCKED ? BlockDuct.ConnectionTypes.STRUCTURE : BlockDuct.ConnectionTypes.NONE;
		} else {
			return super.getRenderConnectionType(side);
		}
	}

	@Override
	public boolean isStructureTile(TileEntity theTile, int side) {

		return theTile instanceof IEnergyProvider && ((IEnergyProvider) theTile).canConnectEnergy(EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

		return energy.energyGrid != null && canConnectEnergy(from) ? energy.energyGrid.myStorage.receiveEnergy(maxReceive, simulate) : 0;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		return 0;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {

		return energy.energyGrid != null ? energy.energyGrid.myStorage.getEnergyStored() : 0;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {

		return energy.energyGrid != null ? energy.energyGrid.myStorage.getMaxEnergyStored() : 0;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {

		return connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED;
		// && (neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT || neighborTypes[from.ordinal()] == NeighborTypes.STRUCTURE);
	}

}
