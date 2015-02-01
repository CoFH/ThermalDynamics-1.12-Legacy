package thermaldynamics.ducts.attachments.filter;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidHandler;

import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.Ducts;

public class FilterFluid extends FilterBase {

	public FilterFluid(TileMultiBlock tile, byte side, int type) {

		super(tile, side, type);
	}

	public FilterFluid(TileMultiBlock tile, byte side) {

		super(tile, side);
	}

	IFluidHandler tank;

	@Override
	public void clearCache() {

		tank = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		tank = (IFluidHandler) tile;
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile instanceof IFluidHandler;
	}

	@Override
	public int getId() {

		return AttachmentRegistry.FILTER_FLUID;
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Ducts.Type.FLUID, this);
	}

}
