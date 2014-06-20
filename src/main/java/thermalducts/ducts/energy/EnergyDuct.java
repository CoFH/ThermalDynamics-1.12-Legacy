package thermalducts.ducts.energy;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class EnergyDuct implements IEnergyHandler {

	private final TileEnergyDuct parentTile;

	public EnergyDuct(TileEnergyDuct parentTile) {

		this.parentTile = parentTile;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {

		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {

		System.out.println("Energy In Possible: " + maxReceive + " - Sim: " + simulate + (parentTile.internalGrid != null ? " - EnergyHeld: " + parentTile.internalGrid.myStorage.getEnergyStored() + " - EnergyMax: " + parentTile.internalGrid.myStorage.getMaxEnergyStored() : ""));
		return parentTile.internalGrid != null ? parentTile.internalGrid.myStorage.receiveEnergy(maxReceive, simulate) : 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {

		return parentTile.internalGrid != null ? parentTile.internalGrid.myStorage.extractEnergy(maxExtract, simulate) : 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {

		return parentTile.internalGrid != null ? parentTile.internalGrid.myStorage.getEnergyStored() : 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {

		return parentTile.internalGrid != null ? parentTile.internalGrid.myStorage.getMaxEnergyStored() : 0;
	}

	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileEnergyDuct && ((TileEnergyDuct) theTile).internalDuct instanceof EnergyDuct;
	}

	public boolean isSignificantTile(TileEntity theTile, int side) {

		return theTile instanceof IEnergyConnection && ((IEnergyConnection) theTile).canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[side]);
	}

	public void tickPass(int pass) {

	}
}
