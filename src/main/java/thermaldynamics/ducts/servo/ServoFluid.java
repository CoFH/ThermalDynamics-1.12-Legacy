package thermaldynamics.ducts.servo;

import cofh.lib.util.helpers.BlockHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.fluid.TileFluidDuct;

public class ServoFluid extends ServoBase {
    TileFluidDuct fluidDuct;

    int[] maxthroughput = {50, 200, 1000, 10000};
    float[] throttle = {0.2F, 0.5F, 1F, 10F};

    public ServoFluid(TileMultiBlock tile, byte side) {
        super(tile, side);
        fluidDuct = (TileFluidDuct) tile;
    }

    public ServoFluid(TileMultiBlock tile, byte side, int type) {
        super(tile, side, type);
        fluidDuct = (TileFluidDuct) tile;
    }

    @Override
    public boolean canAddToTile(TileMultiBlock tileMultiBlock) {
        return tileMultiBlock instanceof TileFluidDuct;
    }

    @Override
    public boolean doesTick() {
        return true;
    }

    @Override
    public void tick(int pass) {
        super.tick(pass);
        if (pass != 1 || fluidDuct.fluidGrid == null || !isPowered) {
            return;
        }

        TileEntity adjacentTileEntity = BlockHelper.getAdjacentTileEntity(fluidDuct, side);
        if (!(adjacentTileEntity instanceof IFluidHandler))
            return;
        IFluidHandler theTile = (IFluidHandler) adjacentTileEntity;

        int maxInput = Math.min(Math.min(fluidDuct.fluidGrid.myTank.getSpace(), (int) Math.ceil(fluidDuct.fluidGrid.myTank.fluidThroughput * throttle[type])), maxthroughput[type]);
        if (maxInput == 0)
            return;

        FluidStack returned = theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, false);

        if (fluidPassesFiltering(returned)) {
            if (fluidDuct.fluidGrid.myTank.getFluid() == null || fluidDuct.fluidGrid.myTank.getFluid().fluidID == 0) {
                fluidDuct.fluidGrid.myTank.setFluid(theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true));
            } else if (fluidDuct.fluidGrid.myTank.getFluid().isFluidEqual(returned)) {
                fluidDuct.fluidGrid.myTank.getFluid().amount += theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true).amount;
            }
        }
    }

    private boolean fluidPassesFiltering(FluidStack theFluid) {
        return theFluid != null && theFluid.fluidID != 0;
    }


}
