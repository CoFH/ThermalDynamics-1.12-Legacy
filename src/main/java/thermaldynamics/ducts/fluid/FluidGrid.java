package thermaldynamics.ducts.fluid;

import cofh.lib.util.TimeTracker;
import cofh.lib.util.helpers.FluidHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import thermaldynamics.ducts.item.PropsConduit;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class FluidGrid extends MultiBlockGrid {


    public FluidTankGrid myTank = new FluidTankGrid(1000, this);
    public int toDistribute = 0;

    /* Render Stuff */
    TimeTracker myTracker = new TimeTracker();
    boolean recentRenderUpdate = false;
    int renderFluidLevel = 0;
    FluidStack myRenderFluid;


    private int type;

    public FluidGrid(World world, int type) {
        super(world);
        myTank = new FluidTankGrid(1000, this);
        this.type = type;
    }

    @Override
    public void addNode(IMultiBlock aMultiBlock) {
        TileFluidDuct theCondF = (TileFluidDuct) aMultiBlock;
        if (theCondF.fluidForGrid != null) {
            if (myTank.getFluid() == null) {
                myTank.setFluid(theCondF.fluidForGrid);
            } else {
                myTank.fill(theCondF.fluidForGrid, true);
            }
            theCondF.fluidForGrid = null;
        }
        super.addNode(aMultiBlock);
    }

    @Override
    public void balanceGrid() {
        myTank.setCapacity(nodeSet.size() * myTank.fluidPerConduit);
    }

    @Override
    public void destroyNode(IMultiBlock node) {
        if (node.isNode() && hasValidFluid()) {
            ((TileFluidDuct) node).fluidForGrid = getNodeShare((TileFluidDuct) node);
        }
        super.destroyNode(node);
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {
        return aBlock instanceof TileFluidDuct;
    }

    @Override
    public void tickGrid() {

        if (worldGrid.worldObj.getTotalWorldTime() % PropsConduit.FLUID_UPDATE_DELAY == 0) {
            updateAllRenders();
        }
        if (myTank.getFluid() != null && nodeSet.size() > 0) {
            toDistribute = Math.min(myTank.getFluidAmount() / nodeSet.size(), getFluidThroughput());

            if (toDistribute <= 0) {
                toDistribute = Math.min(myTank.getFluidAmount() % nodeSet.size(), getFluidThroughput());
            }
        }
        super.tickGrid();
    }

    @Override
    public void mergeGrids(MultiBlockGrid theGrid) {

        super.mergeGrids(theGrid);
        myTank.fill(((FluidGrid) theGrid).getFluid(), true);
    }


    @Override
    public boolean canGridsMerge(MultiBlockGrid grid) {
        return super.canGridsMerge(grid) && ((FluidGrid) grid).type == this.type && FluidHelper.isFluidEqualOrNull(((FluidGrid) grid).getFluid(), getFluid());
    }

    public int getFluidThroughput() {

        if (myTank.getFluid() == null) {
            return 100;
        }
        int capacity = myTank.getCapacity();
        // if over 3/4 full, full throughput
        if (myTank.getFluid().amount >= capacity * 3 / 4) {
            return myTank.fluidThroughput;
        }
        // if under 1/4 full, half throughput
        if (myTank.getFluid().amount <= capacity / 4) {
            return myTank.fluidThroughput >> 1;
        }
        // otherwise scale between half and full
        return (myTank.fluidThroughput >> 1) + (myTank.fluidThroughput >> 1) * (myTank.getFluid().amount - (capacity >> 2)) / (capacity >> 1);
    }

    public void fluidChanged() {
        balanceGrid();
    }

    public FluidStack getNodeShare(TileFluidDuct theCond) {

        FluidStack toReturn = myTank.getFluid().copy();
        toReturn.amount = getNodeAmount(theCond);
        return toReturn;
    }

    public int getNodeAmount(TileFluidDuct theCond) {
        return nodeSet.size() == 1 ? myTank.getFluidAmount() :
                isFirstMultiblock(theCond) ? myTank.getFluidAmount() / nodeSet.size() + myTank.getFluidAmount() % nodeSet.size() :
                        myTank.getFluidAmount() / nodeSet.size();
    }


    public FluidStack getFluid() {

        return myTank.getFluid();
    }

    public boolean hasValidFluid() {

        return myTank.getFluid() != null;
    }

    /* Renders */
    public void updateAllRenders() {

        int fl = renderFluidLevel;
        if (updateRender()) {
            if (fl != renderFluidLevel) {
                if (myTank.getFluid() != null) {
                    myRenderFluid = myTank.getFluid().copy();
                    myRenderFluid.amount = renderFluidLevel;
                } else {
                    myRenderFluid = null;
                }
                for (IMultiBlock curTile : nodeSet) {
                    ((TileFluidDuct) curTile).updateFluid();
                }
                for (IMultiBlock curTile : idleSet) {
                    ((TileFluidDuct) curTile).updateFluid();
                }
            }
        }
    }

    public boolean updateRender() {

        if (recentRenderUpdate && myTracker.hasDelayPassed(worldGrid.worldObj, PropsConduit.FLUID_EMPTY_UPDATE_DELAY)) {
            recentRenderUpdate = false;
        }
        if (myTank.getFluid() != null && myTank.getCapacity() > 0) {

            double fullPercent = 10000 * myTank.getFluid().amount / myTank.getCapacity();

            if (fullPercent >= 0 && fullPercent <= (renderFluidLevel == FluidRenderType.LOW_MED ? 500 : 700)) {
                renderFluidLevel = FluidRenderType.LOW;
                return true;
            }
            if (fullPercent >= 500 && fullPercent <= (renderFluidLevel == FluidRenderType.MEDIUM ? 2000 : 2500)) {
                renderFluidLevel = FluidRenderType.LOW_MED;
                return true;
            }
            if (fullPercent >= 2000 && fullPercent <= (renderFluidLevel == FluidRenderType.MED_HIGH ? 4000 : 4500)) {
                renderFluidLevel = FluidRenderType.MEDIUM;
                return true;
            }
            if (fullPercent >= 4000 && fullPercent <= (renderFluidLevel == FluidRenderType.HIGH ? 6000 : 6500)) {
                renderFluidLevel = FluidRenderType.MED_HIGH;
                return true;
            }
            if (fullPercent >= 6000 && fullPercent <= (renderFluidLevel == FluidRenderType.FULL ? 8000 : 8500)) {
                renderFluidLevel = FluidRenderType.HIGH;
                return true;
            }
            renderFluidLevel = FluidRenderType.FULL;
            return true;
        } else if (renderFluidLevel != FluidRenderType.EMPTY && !recentRenderUpdate) {
            renderFluidLevel = FluidRenderType.EMPTY;
            recentRenderUpdate = true;
            myTracker.markTime(worldGrid.worldObj);
            return true;
        }
        return false;
    }

    public FluidStack getRenderFluid() {

        return myRenderFluid;
    }

    /* FLUID RENDER TYPE */
    public static final class FluidRenderType {

        public static final byte EMPTY = 0;
        public static final byte LOW = 1;
        public static final byte LOW_MED = 2;
        public static final byte MEDIUM = 3;
        public static final byte MED_HIGH = 4;
        public static final byte HIGH = 5;
        public static final byte FULL = 6;
    }


}
