package cofh.thermaldynamics.duct.attachments.servo;

import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ServoFluid extends ServoBase {

	public TileFluidDuct fluidDuct;

	public static float[] throttle = { 0.5F, 0.75F, 1F, 1.5F, 2F };

	@Override
	public int getId() {

		return AttachmentRegistry.SERVO_FLUID;
	}

	public ServoFluid(TileTDBase tile, byte side) {

		super(tile, side);
		fluidDuct = (TileFluidDuct) tile;
	}

	public ServoFluid(TileTDBase tile, byte side, int type) {

		super(tile, side, type);
		fluidDuct = (TileFluidDuct) tile;
	}

	public IFluidHandler theTile;

	@Override
	public void clearCache() {

		theTile = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		theTile = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public boolean canAddToTile(TileTDBase tileMultiBlock) {

		return tileMultiBlock instanceof TileFluidDuct;
	}

	@Override
	public void tick(int pass) {

		super.tick(pass);

		if (pass != 1 || fluidDuct.fluidGrid == null || !isPowered || !isValidInput) {
			return;
		}

		int maxInput = (int) Math.ceil(fluidDuct.fluidGrid.myTank.fluidThroughput * throttle[type]);
		IFluidHandler ductHandler = fluidDuct.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side]);

		maxInput = ductHandler.fill(theTile.drain(maxInput, false), false);

		FluidStack returned = theTile.drain(maxInput, true);
        ductHandler.fill(returned, true);
	}

	public boolean fluidPassesFiltering(FluidStack theFluid) {

		return theFluid != null && filter.allowFluid(theFluid);
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.FLUID, this);
	}

}
