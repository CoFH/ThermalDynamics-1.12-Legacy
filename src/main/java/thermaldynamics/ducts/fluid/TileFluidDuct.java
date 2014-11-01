package thermaldynamics.ducts.fluid;

import cofh.lib.util.helpers.FluidHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class TileFluidDuct extends TileMultiBlock {
    public static void initialize() {
        //guiId = ThermalDynamics.proxy.registerGui("FluidFilter", "conduit", "TEBase", null, true);
    }

    public TileFluidDuct() {

    }

    public TileFluidDuct(int type, boolean opaque) {

    }

    @Override
    public MultiBlockGrid getNewGrid() {
        return new FluidGrid(worldObj, 0);
    }

    protected BlockDuct.ConnectionTypes getDefaultConnection() {
        return BlockDuct.ConnectionTypes.FLUID_NORMAL;
    }

    protected static int guiId;

    /* Filtering Variables */
    public boolean isWhitelist;
    public boolean useNBT;
    public FluidStack[] filterStacks;

    IFluidHandler[] importantCache = new IFluidHandler[6];
    FluidGrid myGrid;
    public FluidStack mySavedFluid;
    public FluidStack myRenderFluid = new FluidStack(0, 0);
    public FluidStack fluidForGrid;
    public FluidStack myConnectionFluid;

    @Override
    public boolean isSignificantTile(TileEntity theTile, int side) {
        return FluidHelper.isFluidHandler(theTile) && !(theTile instanceof IMultiBlock);
    }

    @Override
    public void tickPass(int pass) {
        super.tickPass(pass);
    }

    public void updateFluid() {

    }

    @Override
    public boolean isConnectable(TileEntity theTile, int side) {
        return super.isConnectable(theTile, side);
    }
}
