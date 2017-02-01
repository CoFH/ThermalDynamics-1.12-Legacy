package cofh.thermaldynamics.duct.fluid;

import cofh.core.fluid.FluidTankCore;
import cofh.lib.util.helpers.MathHelper;

import gnu.trove.map.hash.TObjectIntHashMap;

import net.minecraftforge.fluids.FluidStack;

public class FluidTankGrid extends FluidTankCore {

	public int fluidThroughput = 120;
	public int fluidPerDuct = 3000;
	public FluidGrid myMaster;
	static TObjectIntHashMap<String> fluidFlowrate = new TObjectIntHashMap<String>();

	static {
		fluidFlowrate.put("steam", 600);
	}

	public FluidTankGrid(int capacity, FluidGrid theGrid) {

		super(capacity);
		myMaster = theGrid;
	}

	@Override
	public void setFluid(FluidStack fluid) {

		if (fluid != null) {
			setFluidData(fluid);
		}
		super.setFluid(fluid);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {

		if (fluid == null && resource != null) {
			setFluidData(resource);
		}
		return myMaster.trackIn(super.fill(resource, doFill), !doFill);
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {

		FluidStack drain = super.drain(maxDrain, doDrain);
		if (doDrain && drain != null) {
			myMaster.trackOut(drain.amount, false);
		}
		return drain;
	}

	public void setFluidData(FluidStack fluid) {

		int viscosity = 0;

		if (fluid != null) {
			viscosity = Math.max(fluid.getFluid().getViscosity(), 100);
			fluidThroughput = MathHelper.clamp(120000 / viscosity, 80, 600);

			if (fluidFlowrate.containsKey(fluid.getFluid().getName())) {
				fluidThroughput = fluidFlowrate.get(fluid.getFluid().getName());
			}
			fluidThroughput *= myMaster.getThroughPutModifier();
			fluidPerDuct = Math.min(25 * fluidThroughput, myMaster.getMaxFluidPerDuct());
		}
		myMaster.fluidChanged();
	}

}
