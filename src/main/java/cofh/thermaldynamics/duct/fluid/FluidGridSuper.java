package cofh.thermaldynamics.duct.fluid;

import cofh.lib.util.helpers.FluidHelper;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidGridSuper extends FluidGrid {

	public FluidGridSuper(World world) {

		super(world);
	}

	int nodeTracker;
	boolean isSendingFluid;

	TileFluidDuct[] nodeList = null;

	@Override
	public void tickGrid() {

		super.tickGrid();
		int i = 0;
		if (nodeList == null) {
			nodeList = new TileFluidDuct[nodeSet.size()];
			for (IGridTile multiBlock : nodeSet) {
				nodeList[i] = (TileFluidDuct) multiBlock;
				i++;
			}
		}
	}

	public int sendFluid(FluidStack fluid, boolean simulate) {

		if (fluid == null || !FluidHelper.isFluidEqualOrNull(myTank.getFluid(), fluid) || isSendingFluid) {
			return 0;
		}
		int fluidToSend = fluid.amount;
		int addedToGrid = myTank.fill(fluid, !simulate);

		fluidToSend -= addedToGrid;

		if (fluidToSend <= 0) {
			return fluid.amount;
		}
		int tempTracker = nodeTracker;

		TileFluidDuct[] list = nodeList;
		if (list == null || list.length == 0) {
			return fluid.amount - fluidToSend;
		}
		isSendingFluid = true;
		for (int i = nodeTracker; i < list.length && fluidToSend > 0; i++) {
			fluidToSend -= trackInOut(list[i].transfer(fluidToSend, simulate, fluid, false), simulate);
			if (fluidToSend == 0) {
				nodeTracker = i + 1;
			}
		}
		for (int i = 0; i < list.length && i < nodeTracker && fluidToSend > 0; i++) {
			fluidToSend -= trackInOut(list[i].transfer(fluidToSend, simulate, fluid, false), simulate);
			if (fluidToSend == 0) {
				nodeTracker = i + 1;
			}
		}
		if (fluidToSend > 0) {
			nodeTracker++;
		}
		if (nodeTracker >= list.length) {
			nodeTracker = 0;
		}
		if (simulate) {
			nodeTracker = tempTracker;
		}
		isSendingFluid = false;

		return fluid.amount - fluidToSend;
	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		nodeList = null;
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return grid instanceof FluidGridSuper;
	}

	@Override
	public void destroy() {

		nodeList = null;
		super.destroy();
	}

}
