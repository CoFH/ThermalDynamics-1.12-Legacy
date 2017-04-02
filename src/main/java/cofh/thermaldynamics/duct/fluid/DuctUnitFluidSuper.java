package cofh.thermaldynamics.duct.fluid;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nullable;

public class DuctUnitFluidSuper extends DuctUnitFluid {

	public DuctUnitFluidSuper(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public FluidGridSuper createGrid() {

		return new FluidGridSuper(world());
	}

	@Override
	public IFluidHandler getFluidCapability(EnumFacing from) {
		FluidGridSuper fluidGridSuper = (FluidGridSuper) this.grid;
		if(fluidGridSuper == null){
			return EmptyFluidHandler.INSTANCE;
		}

		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {

			@Override
			public IFluidTankProperties[] getTankProperties() {

				FluidStack info = grid != null ? grid.myTank.getInfo().fluid : null;
				int capacity = grid != null ? grid.myTank.getInfo().capacity : 0;
				return new IFluidTankProperties[] { new FluidTankProperties(info, capacity, isOpen(from), isOpen(from)) };
			}

			@Override
			public int fill(FluidStack resource, boolean doFill) {

				if (resource != null && isOpen(from) && matchesFilter(from, resource)) {
					return fluidGridSuper.sendFluid(resource, !doFill);
				}
				return 0;
			}

			@Nullable
			@Override
			public FluidStack drain(FluidStack resource, boolean doDrain) {

				if (isOpen(from)) {
					return grid.myTank.drain(resource, doDrain);
				}
				return null;
			}

			@Nullable
			@Override
			public FluidStack drain(int maxDrain, boolean doDrain) {

				if (isOpen(from)) {
					return grid.myTank.drain(maxDrain, doDrain);
				}
				return null;
			}
		});
	}
//
//	@Override
//	@SideOnly (Side.CLIENT)
//	public CoverHoleRender.ITransformer[] getHollowMask(byte side) {
//
//		BlockDuct.ConnectionType connectionType = getRenderConnectionType(side);
//		if (connectionType == BlockDuct.ConnectionType.TILECONNECTION) {
//			return CoverHoleRender.hollowDuctTile;
//		} else if (connectionType == BlockDuct.ConnectionType.NONE) {
//			return null;
//		} else {
//			return CoverHoleRender.hollowDuctLarge;
//		}
//	}
}
