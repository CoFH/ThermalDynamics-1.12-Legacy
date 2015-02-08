package cofh.thermaldynamics.ducts.attachments.retriever;

import cofh.core.render.RenderUtils;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.ducts.attachments.servo.ServoFluid;
import cofh.thermaldynamics.ducts.fluid.TileFluidDuct;
import cofh.thermaldynamics.render.RenderDuct;
import java.util.Iterator;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class RetrieverFluid extends ServoFluid {
    public RetrieverFluid(TileMultiBlock tile, byte side) {
        super(tile, side);
    }

    public RetrieverFluid(TileMultiBlock tile, byte side, int type) {
        super(tile, side, type);
    }

    @Override
    public int getId() {
        return AttachmentRegistry.RETRIEVER_FLUID;
    }

    @Override
    public void tick(int pass) {
        if (pass != 1 || fluidDuct.fluidGrid == null || !isPowered || !isValidInput) {
            return;
        }

        int maxInput = Math.min(fluidDuct.fluidGrid.myTank.getSpace(), (int) Math.ceil(fluidDuct.fluidGrid.myTank.fluidThroughput * throttle[type]));
        if (maxInput == 0) {
            return;
        }

        if (fluidDuct.fluidGrid.myTank.getFluid() != null){
            if(!fluidPassesFiltering(fluidDuct.fluidGrid.myTank.getFluid()))
                return;
        }
        for (Iterator iterator = fluidDuct.fluidGrid.nodeSet.iterator(); iterator.hasNext(); ) {
            TileFluidDuct fluidDuct = (TileFluidDuct) iterator.next();
            for (int k = 0; k < 6; k++) {
                int i = (k + fluidDuct.internalSideCounter) % 6;
                if (fluidDuct.cache[i] == null || (fluidDuct.neighborTypes[i] != TileMultiBlock.NeighborTypes.OUTPUT &&
                        fluidDuct.neighborTypes[i] != TileMultiBlock.NeighborTypes.INPUT))
                    continue;

                if (fluidDuct.attachments[i] != null)
                    if(fluidDuct.attachments[i].getId() == this.getId())
                    continue;

                FluidStack fluid = fluidDuct.cache[i].drain(ForgeDirection.VALID_DIRECTIONS[i ^ 1], maxInput, false);

                if (fluid != null && fluid.amount > 0 && fluidPassesFiltering(fluid) && fluidDuct.cache[i].canDrain(ForgeDirection.VALID_DIRECTIONS[i ^ 1], fluid.getFluid())) {
                    if (fluidDuct.fluidGrid.myTank.getFluid() == null || fluidDuct.fluidGrid.myTank.getFluid().fluidID == 0) {
                        fluidDuct.fluidGrid.myTank.setFluid(fluidDuct.cache[i].drain(ForgeDirection.VALID_DIRECTIONS[i ^ 1], maxInput, true));
                    } else if (fluidDuct.fluidGrid.myTank.getFluid().isFluidEqual(fluid)) {
                        fluidDuct.fluidGrid.myTank.getFluid().amount += fluidDuct.cache[i].drain(ForgeDirection.VALID_DIRECTIONS[i ^ 1], maxInput, true).amount;
                    }

                    if(fluidDuct.fluidGrid.toDistribute > 0 && fluidDuct.fluidGrid.myTank.getFluid() != null) {
                        fluidDuct.transfer(side, Math.min(fluidDuct.fluidGrid.myTank.getFluid().amount, fluidDuct.fluidGrid.toDistribute));
                    }

                    return;
                }
            }
        }
    }

    @Override
    public ItemStack getPickBlock() {

        return new ItemStack(ThermalDynamics.itemRetriever, 1, type);
    }

    @Override
    public String getName() {

        return "item.thermaldynamics.retriever." + type + ".name";
    }

    @Override
    public boolean render(int pass, RenderBlocks renderBlocks) {
        if (pass == 1) {
            return false;
        }
        Translation trans = RenderUtils.getRenderVector(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5).translation();
        RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(trans,
                RenderUtils.getIconTransformation(RenderDuct.retrieverTexture[type * 2 + (stuffed ? 1 : 0)]));
        return true;
    }

    @Override
    public TileMultiBlock.NeighborTypes getNeighborType() {

        return isValidInput ? TileMultiBlock.NeighborTypes.OUTPUT : TileMultiBlock.NeighborTypes.DUCT_ATTACHMENT;
    }
}
