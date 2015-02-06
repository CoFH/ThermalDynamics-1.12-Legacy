package cofh.thermaldynamics.ducts.item;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.ducts.energy.subgrid.SubTileEnergyRedstone;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileItemDuctFlux extends TileItemDuctPower {

	public final SubTileEnergyRedstone redstoneEnergy;
	boolean isSubNode = false;

	@Override
	public void onNeighborBlockChange() {

		super.onNeighborBlockChange();
		checkSubNode();
	}

	public TileItemDuctFlux() {

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
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}
		if (pass == 0 && isSubNode) {
			int maxSend = redstoneEnergy.internalGrid.toDistribute;
			redstoneEnergy.internalGrid.myStorage.modifyEnergyStored(-transmitEnergy(maxSend));
		}
		return true;
	}

	public int transmitEnergy(int power) {

		int usedPower = 0;

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

	IEnergyReceiver[] energyCache = new IEnergyReceiver[6];

	@Override
	public void clearCache(int side) {

		super.clearCache(side);
		energyCache[side] = null;
	}

	@Override
	public void cacheImportant(TileEntity tile, int side) {

		super.cacheImportant(tile, side);
		if (tile instanceof IEnergyReceiver) {
			energyCache[side] = (IEnergyReceiver) tile;
		}
	}

	@Override
	public void cacheInputTile(TileEntity tile, int side) {

		super.cacheInputTile(tile, side);
		if (tile instanceof IEnergyReceiver) {
			energyCache[side] = (IEnergyReceiver) tile;
		}
	}

	@Override
	public void cacheStructural(TileEntity tile, int side) {

		if (tile instanceof IEnergyReceiver) {
			energyCache[side] = (IEnergyReceiver) tile;
		}
		isOutput = true;
	}

	@Override
	public boolean isStructureTile(TileEntity theTile, int side) {

		return theTile instanceof IEnergyConnection && ((IEnergyConnection) theTile).canConnectEnergy(ForgeDirection.getOrientation(side ^ 1));
	}

}
