package thermaldynamics.ducts.attachments.servo;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.Duct;
import thermaldynamics.ducts.attachments.filter.FilterLogic;
import thermaldynamics.ducts.fluid.TileFluidDuct;

public class ServoFluid extends ServoBase {

	TileFluidDuct fluidDuct;

	public static float[] throttle = { 0.2F, 0.5F, 1F, 1F, 2F };

	@Override
	public int getId() {

		return AttachmentRegistry.SERVO_FLUID;
	}

	public ServoFluid(TileMultiBlock tile, byte side) {

		super(tile, side);
		fluidDuct = (TileFluidDuct) tile;
	}

	public ServoFluid(TileMultiBlock tile, byte side, int type) {

		super(tile, side, type);
		fluidDuct = (TileFluidDuct) tile;
	}

	IFluidHandler theTile;

	@Override
	public void clearCache() {

		theTile = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		theTile = (IFluidHandler) tile;
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile instanceof IFluidHandler;
	}

	@Override
	public boolean canAddToTile(TileMultiBlock tileMultiBlock) {

		return tileMultiBlock instanceof TileFluidDuct;
	}

	@Override
	public void tick(int pass) {

		super.tick(pass);
		if (pass != 1 || fluidDuct.fluidGrid == null || !isPowered || !isValidInput) {
			return;
		}

		int maxInput = Math.min(fluidDuct.fluidGrid.myTank.getSpace(), (int) Math.ceil(fluidDuct.fluidGrid.myTank.fluidThroughput * throttle[type]));
		if (maxInput == 0)
			return;

		FluidStack returned = theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, false);

		if (fluidPassesFiltering(returned)) {
			if (fluidDuct.fluidGrid.myTank.getFluid() == null || fluidDuct.fluidGrid.myTank.getFluid().fluidID == 0) {
				fluidDuct.fluidGrid.myTank.setFluid(theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true));
			} else if (fluidDuct.fluidGrid.myTank.getFluid().isFluidEqual(returned)) {
				fluidDuct.fluidGrid.myTank.getFluid().amount += theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true).amount;
			}
		}
	}

	private boolean fluidPassesFiltering(FluidStack theFluid) {

		return theFluid != null && theFluid.fluidID != 0 && filter.allowFluid(theFluid) ;
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.FLUID, this);
	}

}
