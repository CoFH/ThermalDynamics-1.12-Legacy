package thermaldynamics.ducts.attachments.filter;

import net.minecraftforge.fluids.FluidStack;

public interface IFilterFluid {
    public boolean allowFluid(FluidStack fluid);

    final static IFilterFluid nullFilter = new IFilterFluid() {

        @Override
        public boolean allowFluid(FluidStack fluid) {
            return true;
        }
    };
}
