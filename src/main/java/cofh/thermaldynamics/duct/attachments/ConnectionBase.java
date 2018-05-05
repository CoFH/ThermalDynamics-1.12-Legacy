package cofh.thermaldynamics.duct.attachments;

import codechicken.lib.vec.Cuboid6;
import cofh.api.core.IPortableData;
import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.core.util.helpers.BlockHelper;
import cofh.core.util.helpers.RedstoneControlHelper;
import cofh.core.util.helpers.ServerHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender.CoverTransformer;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.duct.attachments.filter.IFilterFluid;
import cofh.thermaldynamics.duct.attachments.filter.IFilterItems;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.gui.client.GuiDuctConnection;
import cofh.thermaldynamics.gui.container.ContainerDuctConnection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public abstract class ConnectionBase extends Attachment implements IStuffable, IRedstoneControl, IFilterAttachment, IPortableData {

	public boolean stuffed = false;
	public int type = 0;
	public boolean isValidInput;
	public boolean isPowered = false;
	public ControlMode rsMode = ControlMode.HIGH;
	public FilterLogic filter;

	int prevFlag = -1;

	public ConnectionBase(TileGrid tile, byte side) {

		super(tile, side);
	}

	public ConnectionBase(TileGrid tile, byte side, int type) {

		this(tile, side);
		this.type = type;
		filter = createFilterLogic();
	}

	public abstract void cacheTile(TileEntity tile);

	public abstract void clearCache();

	public abstract boolean isValidTile(TileEntity tile);

	public abstract FilterLogic createFilterLogic();

	/**
	 * Enables maximum stack size and item routing in the gui
	 */
	public abstract boolean isServo();

	/**
	 * Enables over-sending in the gui
	 */
	public abstract boolean isFilter();

	/**
	 * Whether or not retrievers can pull from the attached inventory
	 */
	public abstract boolean canSend();

	/**
	 * Adds an info tab in the gui
	 */
	@SideOnly (Side.CLIENT)
	public String getInfo() {

		return null;
	}

	@Override
	public boolean isNode() {

		return true;
	}

	@Nonnull
	@Override
	public BlockDuct.ConnectionType getNeighborType() {

		return BlockDuct.ConnectionType.DUCT;
	}

	@Override
	public Cuboid6 getCuboid() {

		return TileGrid.subSelection[side].copy();
	}

	@Override
	public void checkSignal() {

		boolean wasPowered = isPowered;
		isPowered = rsMode.isDisabled() || rsMode.getState() == getPowerState();
		if (wasPowered != isPowered) {
			BlockHelper.callBlockUpdate(baseTile.getWorld(), baseTile.getPos());
		}
	}

	@Override
	public void onNeighborChange() {

		super.onNeighborChange();

		TileEntity adjacentTileEntity = BlockHelper.getAdjacentTileEntity(baseTile, side);

		clearCache();
		boolean wasValidInput = isValidInput;
		isValidInput = isValidTile(adjacentTileEntity);

		if (isValidInput) {
			cacheTile(adjacentTileEntity);
		}
		boolean wasPowered = isPowered;
		isPowered = rsMode.isDisabled() || rsMode.getState() == getPowerState();
		if (wasPowered != isPowered || isValidInput != wasValidInput) {
			BlockHelper.callBlockUpdate(baseTile.getWorld(), baseTile.getPos());
		}
	}

	public boolean canAlterRS() {

		return false;
	}

	public boolean getPowerState() {

		return baseTile.isPowered();
	}

	@Override
	public boolean respondsToSignalum() {

		return true;
	}

	@Override
	public List<ItemStack> getDrops() {

		LinkedList<ItemStack> drops = new LinkedList<>();
		drops.add(getPickBlock());
		return drops;
	}

	public FilterLogic getFilter() {

		if (filter == null) {
			filter = createFilterLogic();
		}
		return filter;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound tag) {

		type = tag.getByte("type") % 5;
		filter = createFilterLogic();
		filter.readFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {

		tag.setByte("type", (byte) type);

		if (filter != null) {
			filter.writeToNBT(tag);
		}
	}

	/* NETWORK METHODS */
	@Override
	public void addDescriptionToPacket(PacketBase packet) {

		packet.addBool(stuffed);
		packet.addByte(type);
	}

	@Override
	public void getDescriptionFromPacket(PacketBase packet) {

		stuffed = packet.getBool();
		type = packet.getByte();
	}

	@Override
	public void handleInfoPacket(PacketBase payload, boolean isServer, EntityPlayer player) {

		super.handleInfoPacket(payload, isServer, player);

		byte a = payload.getByte();
		handleInfoPacketType(a, payload, isServer, player);
	}

	public PacketTileInfo getNewPacket(byte type) {

		PacketTileInfo packet = getNewPacket();
		packet.addByte(type);
		return packet;
	}

	public void handleInfoPacketType(byte a, PacketBase payload, boolean isServer, EntityPlayer player) {

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

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiDuctConnection(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerDuctConnection(inventory, this);
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (ServerHelper.isServerWorld(baseTile.world())) {
			player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ATTACHMENT_ID + side, baseTile.getWorld(), baseTile.x(), baseTile.y(), baseTile.z());
		}
		return true;
	}

	@Override
	public void receiveGuiNetworkData(int i, int j) {

		super.receiveGuiNetworkData(i, j);
		if (i == 0) {
			filter.handleFlagByte(j);
		} else {
			filter.setLevel(i - 1, j, false);
		}
	}

	@Override
	public void sendGuiNetworkData(Container container, List<IContainerListener> players, boolean newListener) {

		super.sendGuiNetworkData(container, players, newListener);
		int flagByte = filter.getFlagByte();
		if (flagByte != prevFlag || newListener) {
			for (IContainerListener player : players) {
				player.sendWindowProperty(container, 0, flagByte);
			}
		}
		prevFlag = flagByte;

		if (filter.levelsChanged || newListener) {
			for (int i = 0; i < FilterLogic.defaultLevels.length; i++) {
				for (IContainerListener player : players) {
					player.sendWindowProperty(container, 1 + i, filter.getLevel(i));
				}
			}
			filter.levelsChanged = false;
		}
	}

	/* RENDER */
	@Override
	@SideOnly (Side.CLIENT)
	public CoverTransformer getHollowMask() {

		return CoverHoleRender.hollowDuctTile;
	}

	/* IFilterAttachment */
	@Override
	public IFilterItems getItemFilter() {

		return filter;
	}

	@Override
	public IFilterFluid getFluidFilter() {

		return filter;
	}

	/* IPortableData */
	@Override
	public String getDataType() {

		return "ConnectionBase";
	}

	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {

		if (canAlterRS() && tag.hasKey("RSControl")) {
			setControl(RedstoneControlHelper.getControlFromNBT(tag));
		}
		filter.readFromNBT(tag);
		onNeighborChange();
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		if (canAlterRS()) {
			RedstoneControlHelper.setItemStackTagRS(tag, this);
		}
		filter.writeToNBT(tag);
	}

	/* IStuffable */
	@Override
	public void stuffItem(ItemStack item) {

	}

	@Override
	public boolean canStuff() {

		return false;
	}

	@Override
	public boolean isStuffed() {

		return false;
	}

	/* IRedstoneControl */
	@Override
	public void setPowered(boolean isPowered) {

		this.isPowered = isPowered;
	}

	@Override
	public boolean isPowered() {

		return isPowered;
	}

	@Override
	public boolean setControl(ControlMode control) {

		if (!canAlterRS()) {
			return false;
		}
		rsMode = control;
		if (ServerHelper.isClientWorld(baseTile.world())) {
			PacketTileInfo packet = getNewPacket(NETWORK_ID.RSCONTROL);
			packet.addByte(rsMode.ordinal());
			PacketHandler.sendToServer(packet);
		} else {
			onNeighborChange();
		}
		return true;
	}

	@Override
	public ControlMode getControl() {

		return rsMode;
	}

	/* NETWORK ID CLASS */
	public static class NETWORK_ID {

		public final static byte GUI = 0;
		public final static byte RSCONTROL = 1;
		public final static byte FILTERFLAG = 2;
		public final static byte FILTERLEVEL = 3;

	}

}
