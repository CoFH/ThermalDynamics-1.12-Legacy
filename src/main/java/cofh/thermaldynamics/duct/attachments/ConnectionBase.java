package cofh.thermaldynamics.duct.attachments;

import codechicken.lib.util.BlockUtils;
import codechicken.lib.vec.Cuboid6;
import cofh.api.tileentity.IPortableData;
import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.RedstoneControlHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.duct.attachments.filter.IFilterFluid;
import cofh.thermaldynamics.duct.attachments.filter.IFilterItems;
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

	public ConnectionBase(TileDuctBase tile, byte side) {

		super(tile, side);
	}

	public ConnectionBase(TileDuctBase tile, byte side, int type) {

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
	public TileDuctBase.NeighborTypes getNeighborType() {

		return isValidInput ? TileDuctBase.NeighborTypes.INPUT : TileDuctBase.NeighborTypes.DUCT_ATTACHMENT;
	}

	@Override
	public List<ItemStack> getDrops() {

		LinkedList<ItemStack> drops = new LinkedList<>();
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
		isPowered = rsMode.isDisabled() || rsMode.getState() == getPowerState();
		if (wasPowered != isPowered || isValidInput != wasValidInput) {
			BlockUtils.fireBlockUpdate(tile.getWorld(), tile.getPos());
		}
	}

	@Override
	public void checkSignal() {

		boolean wasPowered = isPowered;
		isPowered = rsMode.isDisabled() || rsMode.getState() == getPowerState();
		if (wasPowered != isPowered) {
			BlockUtils.fireBlockUpdate(tile.getWorld(), tile.getPos());
		}
	}

	@Override
	public boolean respondsToSignallum() {

		return true;
	}

	public boolean getPowerState() {

		if (tile.myGrid != null && tile.myGrid.rs != null) {
			if (tile.myGrid.rs.redstoneLevel > 0) {
				return true;
			}
		}

		return tile.getWorld().isBlockPowered(tile.getPos());
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

		return TileDuctBase.subSelection[side].copy();
	}

	@Override
	public boolean isNode() {

		return true;
	}

	public PacketTileInfo getNewPacket(byte type) {

		PacketTileInfo packet = getNewPacket();
		packet.addByte(type);
		return packet;
	}

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		super.handleInfoPacket(payload, isServer, thePlayer);
		byte a = payload.getByte();

		handleInfoPacketType(a, payload, isServer, thePlayer);
	}

	@Override
	public boolean setControl(ControlMode control) {

		if (!canAlterRS()) {
			return false;
		}
		rsMode = control;
		if (ServerHelper.isClientWorld(tile.world())) {
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

		if (filter == null) {
			filter = createFilterLogic();
		}
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

	@Override
	public void sendGuiNetworkData(Container container, List<IContainerListener> players, boolean newGuy) {

		super.sendGuiNetworkData(container, players, newGuy);
		int flagByte = filter.getFlagByte();
		if (flagByte != prevFlag || newGuy) {
			for (IContainerListener player : players) {
				player.sendProgressBarUpdate(container, 0, flagByte);
			}
		}
		prevFlag = flagByte;

		if (filter.levelsChanged || newGuy) {
			for (int i = 0; i < FilterLogic.defaultLevels.length; i++) {
				for (IContainerListener player : players) {
					player.sendProgressBarUpdate(container, 1 + i, filter.getLevel(i));
				}
			}
			filter.levelsChanged = false;
		}
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
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerDuctConnection(inventory, this);
	}

	@Override
	@SideOnly (Side.CLIENT)
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

		if (ServerHelper.isServerWorld(tile.world())) {
			player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ATTACHMENT_ID + side, tile.getWorld(), tile.x(), tile.y(), tile.z());
		}
		return true;
	}

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

	@Override
	@SideOnly (Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask() {

		return CoverHoleRender.hollowDuctTile;
	}
}
