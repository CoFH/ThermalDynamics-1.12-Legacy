package cofh.thermaldynamics.duct.attachments.filter;

import net.minecraftforge.fluids.FluidStack;

public interface IFilterFluid {

	boolean allowFluid(FluidStack fluid);

	IFilterFluid nullFilter = new IFilterFluid() {

		@Override
		public boolean allowFluid(FluidStack fluid) {

			return true;
		}
	};

}
