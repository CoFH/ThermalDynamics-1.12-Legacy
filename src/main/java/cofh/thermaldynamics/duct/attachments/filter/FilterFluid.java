package cofh.thermaldynamics.duct.attachments.filter;

import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FilterFluid extends FilterBase {

	public FilterFluid(TileGrid tile, byte side, int type) {

		super(tile, side, type);
	}

	public FilterFluid(TileGrid tile, byte side) {

		super(tile, side);
	}

	IFluidHandler tank;

	@Override
	public void clearCache() {

		tank = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		tank = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public ResourceLocation getId() {

		return AttachmentRegistry.FILTER_FLUID;
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.FLUID, this);
	}

}
