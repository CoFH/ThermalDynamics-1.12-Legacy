package cofh.thermaldynamics.ducts.attachments;

import cofh.api.tileentity.IPortableData;
import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.RedstoneControlHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.ducts.attachments.filter.FilterLogic;
import cofh.thermaldynamics.ducts.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.ducts.attachments.filter.IFilterFluid;
import cofh.thermaldynamics.ducts.attachments.filter.IFilterItems;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.gui.client.GuiDuctConnection;
import cofh.thermaldynamics.gui.container.ContainerDuctConnection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class ConnectionBase extends Attachment implements IStuffable, IRedstoneControl, IFilterAttachment, IPortableData {

	public boolean stuffed = false;
	public int type = 0;
	public boolean isValidInput;
	public boolean isPowered = false;
	public ControlMode rsMode = ControlMode.HIGH;
	public FilterLogic filter;

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
	public TileMultiBlock.NeighborTypes getNeighborType() {

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
	public void onNeighborChange() {

		super.onNeighborChange();

		TileEntity adjacentTileEntity = tile.getAdjTileEntitySafe(side);

		clearCache();
		boolean wasValidInput = isValidInput;
		isValidInput = isValidTile(adjacentTileEntity);
		if (isValidInput) {
			cacheTile(adjacentTileEntity);
		}

		boolean wasPowered = isPowered;
		isPowered = rsMode.isDisabled() || rsMode.getState() == tile.getWorldObj().isBlockIndirectlyGettingPowered(tile.xCoord, tile.yCoord, tile.zCoord);
		if (wasPowered != isPowered || isValidInput != wasValidInput) {
			tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
		}
	}

	@Override
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

		return true;
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
			onNeighborChange();
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

		if (filter == null)
			filter = createFilterLogic();
		return filter;
	}

	public static class NETWORK_ID {

		public final static byte GUI = 0;
		public final static byte RSCONTROL = 1;
		public final static byte FILTERFLAG = 2;
		public final static byte FILTERLEVEL = 3;

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
		} else if (a == NETWORK_ID.FILTERLEVEL) {
			byte b = payload.getByte();
			int c = payload.getShort();
            filter.setLevel(b, c);
			filter.recalc = true;
		}
	}

	public void sendFilterConfigPacketFlag(int flagType, boolean flag) {

		PacketTileInfo packet = getNewPacket(NETWORK_ID.FILTERFLAG);
		packet.addByte(flagType << 1 | (flag ? 1 : 0));
		PacketHandler.sendToServer(packet);
	}

	public void sendFilterConfigPacketLevel(int levelType, int level) {

		PacketTileInfo packet = getNewPacket(NETWORK_ID.FILTERLEVEL);
		packet.addByte(levelType);
		packet.addShort(level);
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

		if (filter.levelsChanged || newGuy) {
			for (int i = 0; i < FilterLogic.defaultLevels.length; i++) {
				for (Object player : players) {
					((ICrafting) player).sendProgressBarUpdate(container, 1 + i, filter.getLevel(i));
				}
			}
			filter.levelsChanged = false;
		}
	}

	@Override
	public void receiveGuiNetworkData(int i, int j) {

		super.receiveGuiNetworkData(i, j);
		if (i == 0)
			filter.handleFlagByte(j);
		else
			filter.setLevel(i - 1, j);
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

	@Override
	public void stuffItem(ItemStack item) {

	}

	@Override
	public boolean canStuff() {

		return false;
	}

	@Override
	public BlockDuct.ConnectionTypes getRenderConnectionType() {

		return BlockDuct.ConnectionTypes.DUCT;
	}

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


    @Override
    public String getDataType() {
        return "ConnectionBase";
    }

    @Override
    public void readPortableData(EntityPlayer player, NBTTagCompound tag) {
        if (canAlterRS() && tag.hasKey("RSControl"))
            setControl(RedstoneControlHelper.getControlFromNBT(tag));

        filter.readFromNBT(tag);

        onNeighborChange();
    }

    @Override
    public void writePortableData(EntityPlayer player, NBTTagCompound tag) {
        if (canAlterRS())
            RedstoneControlHelper.setItemStackTagRS(tag, this);

        filter.writeToNBT(tag);
    }
}
