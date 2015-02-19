package cofh.thermaldynamics.ducts.fluid;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.ducts.energy.subgrid.SubTileEnergyRedstone;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileFluidDuctFlux extends TileFluidDuctPowered {

	public final SubTileEnergyRedstone redstoneEnergy;
	boolean isSubNode = false;

	@Override
	public void onNeighborBlockChange() {

		super.onNeighborBlockChange();
		checkSubNode();
	}

	public TileFluidDuctFlux() {

		super();
		setSubEnergy(redstoneEnergy = new SubTileEnergyRedstone(this));
	}

	public void checkSubNode() {

		boolean newSubNode = false;
		for (int i = 0; i < 6; i++) {
			if (energyCache[i] != null) {
				newSubNode = true;
				break;
			}
		}

		if (isSubNode != newSubNode) {
			isSubNode = newSubNode;
			if (energy.energyGrid != null) {
				energy.energyGrid.addBlock(energy);
			}

		}
	}

	@Override
	public boolean isSubNode() {

		return isSubNode;
	}

	@Override
	public void onNeighborTileChange(int tileX, int tileY, int tileZ) {

		super.onNeighborTileChange(tileX, tileY, tileZ);
		checkSubNode();
	}

	@Override
	public BlockDuct.ConnectionTypes getConnectionType(int side) {

		BlockDuct.ConnectionTypes connectionType = super.getConnectionType(side);
		if (connectionType == BlockDuct.ConnectionTypes.DUCT) {
			if (!(neighborMultiBlocks[side] instanceof TileFluidDuctPowered)) {
				return BlockDuct.ConnectionTypes.CLEANDUCT;
			}
		}
		return connectionType;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		FluidGrid grid = new FluidGrid(worldObj, getDuctType().type);
		grid.doesPassiveTicking = true;
		return grid;
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass))
			return false;
		if (pass == 2 && isSubNode) {
			int maxSend = redstoneEnergy.internalGrid.toDistribute;
			redstoneEnergy.internalGrid.myStorage.modifyEnergyStored(-transmitEnergy(maxSend));
		}
		return true;
	}

	public int transmitEnergy(int power) {

		int usedPower = 0;
        if(!cachesExist()) return 0;

		for (byte i = this.internalSideCounter; i < this.neighborTypes.length && usedPower < power; i++) {
			if (this.connectionTypes[i] == ConnectionTypes.NORMAL) {
				if (energyCache[i] != null) {
					if (energyCache[i].canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1])) {
						usedPower += energyCache[i].receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], power - usedPower, false);
					}
					if (usedPower >= power) {
						this.tickInternalSideCounter(i + 1);
						break;
					}
				}
			}
		}

		for (byte i = 0; i < this.internalSideCounter && usedPower < power; i++) {
			if (this.connectionTypes[i] == ConnectionTypes.NORMAL) {
				if (energyCache[i] != null) {
					if (energyCache[i].canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1])) {
						usedPower += energyCache[i].receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], power - usedPower, false);
					}
					if (usedPower >= power) {
						this.tickInternalSideCounter(i + 1);
						break;
					}
				}
			}
		}
		return usedPower;
	}

	IEnergyReceiver[] energyCache;

	@Override
	public void clearCache(int side) {

		super.clearCache(side);
		energyCache[side] = null;
	}

    @Override
    public void createCaches() {
        super.createCaches();
        energyCache = new IEnergyReceiver[6];
    }

    @Override
    public void cacheImportant(TileEntity tile, int side) {

		super.cacheImportant(tile, side);
		if (tile instanceof IEnergyReceiver)
			energyCache[side] = (IEnergyReceiver) tile;
	}

	@Override
	public void cacheInputTile(TileEntity tile, int side) {

		super.cacheInputTile(tile, side);
		if (tile instanceof IEnergyReceiver)
			energyCache[side] = (IEnergyReceiver) tile;
	}

	@Override
	public void cacheStructural(TileEntity tile, int side) {

		energyCache[side] = (IEnergyReceiver) tile;
		isOutput = true;
	}

}
