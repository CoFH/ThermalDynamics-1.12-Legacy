package cofh.thermaldynamics.duct.fluid;

import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class TileFluidDuctSuper extends TileFluidDuct {

	private FluidGridSuper internalGridFS;

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGridFS = (FluidGridSuper) newGrid;
	}

	@Override
	public MultiBlockGrid createGrid() {

		return new FluidGridSuper(worldObj);
	}

	@Override
	//TODO Rewrite this a bit so there isnt as much duplicated code.
	public <T> T getCapability(Capability<T> capability, final EnumFacing from) {

		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {

				@Override
				public IFluidTankProperties[] getTankProperties() {

					FluidStack info = fluidGrid != null ? fluidGrid.myTank.getInfo().fluid : null;
					int capacity = fluidGrid != null ? fluidGrid.myTank.getInfo().capacity : 0;
					return new IFluidTankProperties[] { new FluidTankProperties(info, capacity, isOpen(from), isOpen(from)) };
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {

					if (resource != null && isOpen(from) && matchesFilter(from, resource)) {
						return internalGridFS.sendFluid(resource, !doFill);
					}
					return 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {

					if (isOpen(from)) {
						return fluidGrid.myTank.drain(resource, doDrain);
					}
					return null;
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {

					if (isOpen(from)) {
						return fluidGrid.myTank.drain(maxDrain, doDrain);
					}
					return null;
				}
			});
		}
		return super.getCapability(capability, from);
	}

	@Override
	@SideOnly (Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask(byte side) {

		BlockDuct.ConnectionTypes connectionType = getRenderConnectionType(side);
		if (connectionType == BlockDuct.ConnectionTypes.TILECONNECTION) {
			return CoverHoleRender.hollowDuctTile;
		} else if (connectionType == BlockDuct.ConnectionTypes.NONE) {
			return null;
		} else {
			return CoverHoleRender.hollowDuctLarge;
		}
	}
}
