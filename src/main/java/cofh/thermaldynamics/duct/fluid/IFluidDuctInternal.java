package cofh.thermaldynamics.duct.fluid;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import net.minecraftforge.fluids.FluidStack;

public interface IFluidDuctInternal extends IMultiBlock {

	void updateFluid();

	FluidStack getFluidForGrid();

	void setFluidForGrid(FluidStack fluidForGrid);

	boolean isOpaque();

	void updateLighting();

	FluidStack getConnectionFluid();

	boolean canStoreFluid();
}
