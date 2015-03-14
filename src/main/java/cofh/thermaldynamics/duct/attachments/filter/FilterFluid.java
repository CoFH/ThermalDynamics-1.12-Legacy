package cofh.thermaldynamics.duct.attachments.filter;

import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.Duct;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.IFluidHandler;

public class FilterFluid extends FilterBase {

	public FilterFluid(TileTDBase tile, byte side, int type) {

		super(tile, side, type);
	}

	public FilterFluid(TileTDBase tile, byte side) {

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

		return new FilterLogic(type, Duct.Type.FLUID, this);
	}

}
