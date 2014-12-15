package thermaldynamics.ducts.attachments;

import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.ServerHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.attachments.filter.FilterLogic;
import thermaldynamics.ducts.attachments.filter.IFilterAttachment;
import thermaldynamics.ducts.attachments.filter.IFilterFluid;
import thermaldynamics.ducts.attachments.filter.IFilterItems;
import thermaldynamics.gui.GuiHandler;
import thermaldynamics.gui.containers.ContainerDuctConnection;
import thermaldynamics.gui.gui.GuiDuctConnection;

import java.util.LinkedList;
import java.util.List;

public abstract class ConnectionBase extends Attachment implements IStuffable, IRedstoneControl, IFilterAttachment {
    public boolean stuffed = false;
    public int type = 0;
    public boolean isValidInput;
    public boolean isPowered = false;
    public ControlMode rsMode = ControlMode.HIGH;

    public ConnectionBase(TileMultiBlock tile, byte side) {
        super(tile, side);
    }

    public ConnectionBase(TileMultiBlock tile, byte side, int type) {
        this(tile, side);
        this.type = type;
        filter = createFilterLogic();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setByte("type", (byte) type);
        filter.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        type = tag.getByte("type") % 5;
        filter = createFilterLogic();
        filter.readFromNBT(tag);
    }

    @Override
    public TileMultiBlock.NeighborTypes getNeighbourType() {
        return isValidInput ? TileMultiBlock.NeighborTypes.INPUT : TileMultiBlock.NeighborTypes.DUCT_ATTACHMENT;
    }

    @Override
    public List<ItemStack> getDrops() {
        LinkedList<ItemStack> drops = new LinkedList<ItemStack>();
        drops.add(getPickBlock());
        return drops;
    }

    @Override
    public void addDescriptionToPacket(PacketCoFHBase packet) {
        packet.addBool(stuffed);
        packet.addByte(type);
    }

    @Override
    public void getDescriptionFromPacket(PacketCoFHBase packet) {
        stuffed = packet.getBool();
        type = packet.getByte();
    }

    @Override
    public void onNeighbourChange() {
        super.onNeighbourChange();

        TileEntity adjacentTileEntity = tile.getAdjTileEntitySafe(side);

        clearCache();
        boolean wasValidInput = isValidInput;
        isValidInput = isValidTile(adjacentTileEntity);
        if (isValidInput) {
            cacheTile(adjacentTileEntity);
        }

        boolean wasPowered = isPowered;
        isPowered = rsMode.isDisabled() || rsMode.getState() == tile.getWorldObj().isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);
        if (wasPowered != isPowered || isValidInput != wasValidInput)
            tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
    }

    public boolean isStuffed() {
        return false;
    }

    public abstract void clearCache();

    public abstract void cacheTile(TileEntity tile);

    public abstract boolean isValidTile(TileEntity tile);

    @Override
    public Cuboid6 getCuboid() {
        return TileMultiBlock.subSelection[side].copy();
    }

    @Override
    public boolean isNode() {
        return isValidInput;
    }


    @Override
    public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {
        super.handleInfoPacket(payload, isServer, thePlayer);
        byte a = payload.getByte();

        handleInfoPacketType(a, payload, isServer, thePlayer);
    }

    public PacketTileInfo getNewPacket(byte type) {
        PacketTileInfo packet = PacketTileInfo.newPacket(tile);
        packet.addByte(1 + side);
        packet.addByte(type);
        return packet;
    }

    @Override
    public void setControl(ControlMode control) {
        if (!canAlterRS())
            return;
        rsMode = control;
        if (ServerHelper.isClientWorld(tile.world())) {
            PacketTileInfo packet = getNewPacket(NETWORK_ID.RSCONTROL);
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

    public boolean canAlterRS() {
        return false;
    }

    public FilterLogic getFilter() {
        if (filter == null) filter = createFilterLogic();
        return filter;
    }


    public static class NETWORK_ID {
        public final static byte GUI = 0;
        public final static byte RSCONTROL = 1;
        public final static byte FILTERFLAG = 2;

    }

    public void handleInfoPacketType(byte a, PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {
        if (a == NETWORK_ID.RSCONTROL) {
            if (canAlterRS()) {
                setControl(ControlMode.values()[payload.getByte()]);
            }
        } else if (a == NETWORK_ID.FILTERFLAG) {
            byte aByte = payload.getByte();
            filter.setFlag(aByte >> 1, (aByte & 1) == 1);
            filter.recalc = true;
        }
    }

    public void sendFilterConfigPacket(int flagType, boolean flag) {
        PacketTileInfo packet = getNewPacket(NETWORK_ID.FILTERFLAG);
        packet.addByte(flagType << 1 | (flag ? 1 : 0));
        PacketHandler.sendToServer(packet);
    }

    int prevFlag = -1;

    @Override
    public void sendGuiNetworkData(Container container, List players, boolean newGuy) {
        super.sendGuiNetworkData(container, players, newGuy);
        int flagByte = filter.getFlagByte();
        if (flagByte != prevFlag || newGuy) {
            for (Object player : players) {
                ((ICrafting) player).sendProgressBarUpdate(container, 0, flagByte);
            }
        }
        prevFlag = flagByte;
    }

    @Override
    public void receiveGuiNetworkData(int i, int j) {
        super.receiveGuiNetworkData(i, j);
        if (i == 0) filter.handleFlagByte(j);
    }


    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerDuctConnection(inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiDuctConnection(inventory, this);
    }


    public void stuffItem(ItemStack item) {

    }

    public boolean canStuff() {
        return false;
    }

    @Override
    public BlockDuct.ConnectionTypes getRenderConnectionType() {
        return BlockDuct.ConnectionTypes.DUCT;
    }


    public FilterLogic filter;

    @Override
    public IFilterItems getItemFilter() {
        return filter;
    }

    @Override
    public IFilterFluid getFluidFilter() {
        return filter;
    }

    public abstract FilterLogic createFilterLogic();

    @Override
    public boolean openGui(EntityPlayer player) {
        player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ATTACHMENT_ID + side, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        return true;
    }
}
