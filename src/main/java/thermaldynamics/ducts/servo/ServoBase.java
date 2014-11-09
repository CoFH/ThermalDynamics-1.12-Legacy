package thermaldynamics.ducts.servo;

import cofh.core.network.PacketCoFHBase;
import cofh.core.render.RenderUtils;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Translation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.render.RenderDuct;

import java.util.LinkedList;
import java.util.List;

public abstract class ServoBase extends Attachment {
    public ServoBase(TileMultiBlock tile, byte side) {
        super(tile, side);
    }

    public ServoBase(TileMultiBlock tile, byte side, int type) {
        super(tile, side);
        this.type = type;
    }

    boolean isPowered = false;
    boolean stuffed = false;

    int type = 0;

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setBoolean("power", isPowered);
        tag.setByte("type", (byte) type);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        isPowered = tag.getBoolean("power");
        type = tag.getByte("type") % 4;
    }

    @Override
    public void onNeighbourChange() {
        super.onNeighbourChange();
        boolean wasPowered = isPowered;
        isPowered = tile.getWorldObj().isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);

        if (wasPowered != isPowered)
            tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
    }

    @Override
    public List<ItemStack> getDrops() {
        LinkedList<ItemStack> drops = new LinkedList<ItemStack>();
        drops.add(new ItemStack(ThermalDynamics.itemServo, 1, type));
        return drops;
    }

    @Override
    public void addDescriptionToPacket(PacketCoFHBase packet) {
        packet.addBool(isPowered);
        packet.addBool(stuffed);
        packet.addByte(type);
    }

    @Override
    public void getDescriptionFromPacket(PacketCoFHBase packet) {
        isPowered = packet.getBool();
        stuffed = packet.getBool();
        type = packet.getByte();
    }

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public Cuboid6 getCuboid() {
        return TileMultiBlock.subSelection[side];
    }

    @Override
    public boolean onWrenched() {
        tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
        return true;
    }

    @Override
    public TileMultiBlock.NeighborTypes getNeighbourType() {
        return TileMultiBlock.NeighborTypes.DUCT_ATTACHMENT;
    }

    @Override
    public boolean isNode() {
        return true;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean render(int pass, RenderBlocks renderBlocks) {
        if (pass == 1)
            return false;

        Translation trans = RenderUtils.getRenderVector(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5).translation();
        RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(trans, RenderUtils.getIconTransformation(RenderDuct.servoTexture[type * 2 + (stuffed ? 1 : 0)]));

        return true;
    }
}
