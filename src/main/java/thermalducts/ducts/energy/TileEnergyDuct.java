package thermalducts.ducts.energy;

import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import thermalducts.block.TileMultiBlock;
import thermalducts.multiblock.MultiBlockGrid;

public class TileEnergyDuct extends TileMultiBlock implements IEnergyHandler {

	EnergyDuct internalDuct;
	EnergyGrid internalGrid;

	static {
		GameRegistry.registerTileEntity(TileEnergyDuct.class, "thermalducts.ducts.energy.TileEnergyDuct");
	}

	public TileEnergyDuct() {

		internalDuct = new EnergyDuct(this);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {

		return internalDuct != null ? internalDuct.canConnectEnergy(from) : false;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {

		return internalDuct != null ? internalDuct.receiveEnergy(from, maxReceive, simulate) : 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {

		return internalDuct != null ? internalDuct.extractEnergy(from, maxExtract, simulate) : 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {

		return internalDuct != null ? internalDuct.getEnergyStored(from) : 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {

		return internalDuct != null ? internalDuct.getMaxEnergyStored(from) : 0;
	}

	/*
	 * Should return true if theTile is an instance of this multiblock.
	 * 
	 * This must also be an instance of IMultiBlock
	 */
	@Override
	public boolean isConnectable(TileEntity theTile, int side) {

		return internalDuct != null ? internalDuct.isConnectable(theTile, side) : false;
	}

	/*
	 * Should return true if theTile is significant to this multiblock
	 * 
	 * IE: Inventory's to ItemDuct's
	 */
	@Override
	public boolean isSignificantTile(TileEntity theTile, int side) {

		return internalDuct != null ? internalDuct.isSignificantTile(theTile, side) : false;
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (EnergyGrid) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergyGrid();
	}

	@Override
	public void tickPass(int pass) {

		if (internalDuct != null)
			internalDuct.tickPass(pass);
	}
}
