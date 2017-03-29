package cofh.thermaldynamics.duct.fluid;

import cofh.thermaldynamics.multiblock.IGridTile;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidDuctInternal extends IGridTile<IFluidDuctInternal, FluidGrid> {

	void updateFluid();

	FluidStack getFluidForGrid();

	void setFluidForGrid(FluidStack fluidForGrid);

	boolean isOpaque();

	void updateLighting();

	FluidStack getConnectionFluid();

	boolean canStoreFluid();
}
