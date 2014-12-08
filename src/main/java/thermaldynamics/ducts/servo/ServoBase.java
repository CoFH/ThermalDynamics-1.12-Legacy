package thermaldynamics.ducts.servo;

import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.core.render.RenderUtils;
import cofh.lib.util.helpers.ServerHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Translation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.gui.GuiHandler;
import thermaldynamics.render.RenderDuct;

import java.util.LinkedList;
import java.util.List;

public abstract class ServoBase extends Attachment implements IRedstoneControl {
    public ServoBase(TileMultiBlock tile, byte side) {
        super(tile, side);
    }

    public ServoBase(TileMultiBlock tile, byte side, int type) {
        super(tile, side);
        this.type = type;
    }

    boolean isPowered = false;
    boolean stuffed = false;

    ControlMode rsMode = ControlMode.HIGH;

    int type = 0;


    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setBoolean("power", isPowered);
        tag.setByte("type", (byte) type);
        if (canAlterRS())
            tag.setByte("rsMode", (byte) rsMode.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        isPowered = tag.getBoolean("power");
        type = tag.getByte("type") % 5;
        if (canAlterRS())
            rsMode = ControlMode.values()[tag.getByte("rsMode")];
    }

    @Override
    public void onNeighbourChange() {
        super.onNeighbourChange();
        boolean wasPowered = isPowered;

        isPowered = rsMode.isDisabled() || rsMode.getState() == tile.getWorldObj().isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);


        TileEntity adjacentTileEntity = tile.getAdjTileEntitySafe(side);

        clearCache();
        boolean wasValidInput = isValidInput;
        isValidInput = isValidTile(adjacentTileEntity);
        if (isValidInput) {
            cacheTile(adjacentTileEntity);
        }


        if (wasPowered != isPowered || isValidInput != wasValidInput)
            tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ATTACHMENT_ID + side, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        return true;
    }

    boolean isValidInput;

    public abstract void clearCache();

    public abstract void cacheTile(TileEntity tile);

    @Override
    public TileMultiBlock.NeighborTypes getNeighbourType() {
        return isValidInput ? TileMultiBlock.NeighborTypes.INPUT : TileMultiBlock.NeighborTypes.DUCT_ATTACHMENT;
    }

    public abstract boolean isValidTile(TileEntity tile);

    @Override
    public List<ItemStack> getDrops() {
        LinkedList<ItemStack> drops = new LinkedList<ItemStack>();
        drops.add(new ItemStack(ThermalDynamics.itemServo, 1, type));
        return drops;
    }

    @Override
    public ItemStack getPickBlock() {
        return new ItemStack(ThermalDynamics.itemServo, 1, type);
    }

    @Override
    public void addDescriptionToPacket(PacketCoFHBase packet) {
        packet.addBool(isPowered);
        packet.addBool(stuffed);
        packet.addByte(type);
        if (canAlterRS())
            packet.addByte(rsMode.ordinal());
    }

    @Override
    public void getDescriptionFromPacket(PacketCoFHBase packet) {
        isPowered = packet.getBool();
        stuffed = packet.getBool();
        type = packet.getByte();
        if (canAlterRS())
            rsMode = ControlMode.values()[packet.getByte()];
    }

    @Override
    public Cuboid6 getCuboid() {
        return TileMultiBlock.subSelection[side].copy();
    }

    @Override
    public boolean onWrenched() {
        tile.removeAttachment(this);
        for (ItemStack stack : getDrops()) {
            dropItemStack(stack);
        }
        return true;
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

    @Override
    public void setControl(ControlMode control) {
        if (!canAlterRS())
            return;
        rsMode = control;
        if (ServerHelper.isClientWorld(tile.world())) {
            PacketTileInfo packet = PacketTileInfo.newPacket(tile);
            packet.addByte(1 + side);
            packet.addByte(NETWORK_ID.RSCONTROL);
            packet.addByte(rsMode.ordinal());
            PacketHandler.sendToServer(packet);
        } else {
            onNeighbourChange();
        }
    }

    @Override
    public ControlMode getControl() {
        return rsMode;
    }

    @Override
    public void setPowered(boolean isPowered) {
        this.isPowered = isPowered;
    }

    @Override
    public boolean isPowered() {
        return isPowered;
    }

    @Override
    public void sendGuiNetworkData(Container container, ICrafting player) {
        super.sendGuiNetworkData(container, player);
    }

    @Override
    public void receiveGuiNetworkData(int i, int j) {
        super.receiveGuiNetworkData(i, j);
    }

    @Override
    public boolean doesTick() {
        return true;
    }

    @Override
    public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {
        super.handleInfoPacket(payload, isServer, thePlayer);
        byte a = payload.getByte();
        switch (a) {
            case NETWORK_ID.RSCONTROL:
                if (canAlterRS()) {
                    setControl(ControlMode.values()[payload.getByte()]);
                }
                break;
        }
    }

    public boolean canAlterRS() {
        return type >= 2;
    }

    public static class NETWORK_ID {
        public final static int GUI = 0;
        public final static int RSCONTROL = 1;
    }
}
