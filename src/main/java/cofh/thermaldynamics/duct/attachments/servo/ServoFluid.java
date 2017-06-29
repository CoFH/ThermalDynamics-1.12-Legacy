package cofh.thermaldynamics.duct.attachments.servo;

import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluid;
import cofh.thermaldynamics.duct.fluid.FluidTankGrid;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ServoFluid extends ServoBase {

	public static float[] throttle = { 0.5F, 0.75F, 1F, 1.5F, 2F };

	public DuctUnitFluid fluidDuct;

	@Override
	public int getId() {

		return AttachmentRegistry.SERVO_FLUID;
	}

	public ServoFluid(TileGrid tile, byte side) {

		super(tile, side);
		fluidDuct = tile.getDuct(DuctToken.FLUID);
	}

	public ServoFluid(TileGrid tile, byte side, int type) {

		super(tile, side, type);
		fluidDuct = tile.getDuct(DuctToken.FLUID);
	}

	@Override
	public DuctToken tickUnit() {

		return DuctToken.FLUID;
	}

	@Override
	public void clearCache() {

		myTile = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		myTile = tile;
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public boolean canAddToTile(TileGrid tile) {

		return fluidDuct != null;
	}

	@Override
	public void tick(int pass) {

		super.tick(pass);

		if (pass != 1 || fluidDuct.getGrid() == null || !isPowered || !isValidInput) {
			return;
		}
		FluidTankGrid myTank = fluidDuct.getGrid().myTank;
		int maxInput = (int) Math.ceil(myTank.fluidThroughput * throttle[type]);
		IFluidHandler ductHandler = fluidDuct.getFluidCapability(EnumFacing.VALUES[side]);

		if (ductHandler == null) {
			return;
		}
		IFluidHandler tileHandler = getFluidHandler();

		if (tileHandler == null) {
			return;
		}
		maxInput = myTank.fill(tileHandler.drain(maxInput, false), false);
		myTank.fill(tileHandler.drain(maxInput, true), true);
	}

	public boolean fluidPassesFiltering(FluidStack theFluid) {

		return theFluid != null && filter.allowFluid(theFluid);
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.FLUID, this);
	}

	public IFluidHandler getFluidHandler() {

		if (myTile == null) {
			return null;
		}
		return myTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

}
