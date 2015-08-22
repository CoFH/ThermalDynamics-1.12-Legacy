package cofh.thermaldynamics.duct.fluid;

import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleQuad;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class TileFluidDuctSuper extends TileFluidDuct {

	private FluidGridSuper internalGridFS;

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGridFS = (FluidGridSuper) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new FluidGridSuper(worldObj);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {

		if (resource != null && isOpen(from) && matchesFilter(from, resource)) {
			return internalGridFS.sendFluid(resource, !doFill);
		}
		return 0;
	}

	@Override
	public CoverHoleQuad.ITransformer[] getHollowMask(byte side) {

		BlockDuct.ConnectionTypes connectionType = getRenderConnectionType(side);
		if(connectionType == BlockDuct.ConnectionTypes.TILECONNECTION)
			return CoverHoleQuad.hollowDuctTile;
		else if(connectionType == BlockDuct.ConnectionTypes.NONE){
			return null;
		} else {
			return CoverHoleQuad.hollowDuctLarge;
		}
	}
}
