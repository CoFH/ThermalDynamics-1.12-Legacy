package thermaldynamics.ducts.facades;

import cofh.core.network.PacketCoFHBase;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Rotation;
import cofh.repack.codechicken.lib.vec.Vector3;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;

public class Facade extends Attachment {
    static Cuboid6 bound = new Cuboid6(0, 0, 0, 1, 0.1, 1);

    static Cuboid6[] bounds = {
            bound,
            bound.copy().apply(Rotation.sideRotations[1].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[2].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[3].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[4].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[5].at(Vector3.center))
    };

    Block block;
    int meta;

    public Facade(TileMultiBlock tile, byte side, Block block, int meta) {
        super(tile, side);
        this.block = block;
        this.meta = meta;
    }

    public Facade(TileMultiBlock tile, byte side) {
        super(tile, side);
    }

    @Override
    public int getID() {
        return 0;
    }

    @Override
    public Cuboid6 getCuboid() {
        return bounds[side].copy();
    }

    @Override
    public boolean onWrenched() {
        return false;
    }

    @Override
    public TileMultiBlock.NeighborTypes getNeighbourType() {
        return TileMultiBlock.NeighborTypes.STRUCTURE;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    public boolean render(int pass, RenderBlocks renderBlocks) {
        if (!block.canRenderInPass(pass))
            return false;

        return FacadeRenderer.renderFacade(renderBlocks, tile.xCoord, tile.yCoord, tile.zCoord, side, block, meta, getCuboid());
    }

    @Override
    public boolean makesSideSolid() {
        return true;
    }

    @Override
    public void addDescriptionToPacket(PacketCoFHBase packet) {
        packet.addShort(Block.getIdFromBlock(block));
        packet.addByte(meta);
    }

    @Override
    public void getDescriptionFromPacket(PacketCoFHBase packet) {
        block = Block.getBlockById(packet.getShort());
        meta = packet.getByte();
    }
}
