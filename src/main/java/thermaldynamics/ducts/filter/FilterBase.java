package thermaldynamics.ducts.filter;

import cofh.repack.codechicken.lib.vec.Cuboid6;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;

import java.util.List;

public class FilterBase extends Attachment {
    public FilterBase(TileMultiBlock tile, byte side) {
        super(tile, side);
    }

    @Override
    public int getID() {
        return AttachmentRegistry.FILTER_FLUID;
    }

    @Override
    public Cuboid6 getCuboid() {
        return TileMultiBlock.subSelection[6+side].copy();
    }

    @Override
    public boolean onWrenched() {
        return false;
    }

    @Override
    public TileMultiBlock.NeighborTypes getNeighbourType() {
        return TileMultiBlock.NeighborTypes.TILE;
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean render(int pass, RenderBlocks renderBlocks) {
        return false;
    }

    @Override
    public List<ItemStack> getDrops() {
        return null;
    }
}
