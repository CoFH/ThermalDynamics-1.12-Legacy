package cofh.thermaldynamics.duct.entity;

import cofh.api.block.IBlockConfigGui;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.item.RouteInfo;
import cofh.thermaldynamics.duct.tiles.DuctUnit;
import cofh.thermaldynamics.duct.tiles.IDuctHolder;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.gui.client.DirectoryEntry;
import cofh.thermaldynamics.gui.client.GuiTransport;
import cofh.thermaldynamics.gui.client.GuiTransportConfig;
import cofh.thermaldynamics.gui.container.ContainerTransport;
import cofh.thermaldynamics.gui.container.ContainerTransportConfig;
import cofh.thermaldynamics.multiblock.IGridTileRoute;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;

public class DuctUnitTransport extends DuctUnitTransportBase implements IBlockConfigGui {

	public DuctUnitTransport(TileGrid parent, Duct duct) {

		super(parent, duct);
	}

	@Override
	protected void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull ConnectionType type, byte oppositeSide) {

		super.handleTileSideUpdate(tile, holder, side, type, oppositeSide);

		if (type == ConnectionType.FORCED) {
			ductCache[side] = null;
			nodeMask |= (1 << side);
		}
	}

	@Override
	public boolean onWrench(EntityPlayer player, int side, RayTraceResult rayTrace) {

		if (ductCache[side] == null) {
			if (parent.getConnectionType(side) == ConnectionType.FORCED) {
				parent.setConnectionType(side, ConnectionType.NORMAL);
			} else {
				parent.setConnectionType(side, ConnectionType.FORCED);

				for (int j = 0; j < 6; j++) {
					if (side != j && parent.getConnectionType(j) == ConnectionType.FORCED) {
						parent.setConnectionType(j, ConnectionType.NORMAL);
					}
				}
			}
			onNeighborBlockChange();
			return true;
		} else {
			return false;
		}
	}

	@Nonnull
	@Override
	public BlockDuct.ConnectionType getRenderConnectionType(int side) {

		if (parent.getConnectionType(side) == cofh.thermaldynamics.duct.ConnectionType.FORCED) {
			return BlockDuct.ConnectionType.TILE_CONNECTION;
		}
		return super.getRenderConnectionType(side);
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (grid == null) {
			return false;
		}
		if (ServerHelper.isClientWorld(world())) {
			return true;
		}
		onNeighborBlockChange();

		for (int i = 0; i < 6; i++) {
			if (parent.getConnectionType(i) == ConnectionType.FORCED) {
				if (ductCache[i] != null) {
					continue;
				}
				PacketHandler.sendTo(parent.getTilePacket(), player);
				player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ID, world(), pos().getX(), pos().getY(), pos().getZ());
				return true;
			}
		}
		return false;
	}

	private static final OutputData BLANK_NAME = new OutputData();
	public OutputData data = BLANK_NAME;

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		if (data != BLANK_NAME) {
			data.write(nbt, this);
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		data = OutputData.read(nbt);
	}

	@Override
	public void writeToTilePacket(PacketCoFHBase packet) {

		if (data != BLANK_NAME) {
			packet.addBool(true);
			data.addToPacket(packet);
		} else {
			packet.addBool(false);
		}
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload) {

		super.handleTilePacket(payload);

		if (payload.getBool()) {
			if (data == BLANK_NAME) {
				data = new OutputData();
			}
			data.readPacket(payload);
		} else {
			data = BLANK_NAME;
		}
	}

	public final static int NETWORK_REQUEST = 0;
	public final static int NETWORK_SETOUTPUTDATA = 1;
	public final static int NETWORK_LIST = 2;
	public final static int NETWORK_CONFIG = 3;

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		byte type = payload.getByte();

		if (type == NETWORK_REQUEST && isServer) {
			sendPlayerToDest(thePlayer, payload.getInt(), payload.getInt(), payload.getInt());
		} else if (type == NETWORK_SETOUTPUTDATA && isServer) {
			if (data == BLANK_NAME) {
				data = new OutputData();
			}
			data.loadConfigData(payload);
			if (grid != null) {
				grid.onMajorGridChange();
			}
		} else if (type == NETWORK_LIST && !isServer) {
			Container openContainer = thePlayer.openContainer;

			if (!(openContainer instanceof ContainerTransport)) {
				return;
			}
			ContainerTransport transport = (ContainerTransport) openContainer;
			transport.setEntry(new DirectoryEntry(payload));
			ArrayList<DirectoryEntry> entries = new ArrayList<>();

			int size = payload.getShort();

			for (int i = 0; i < size; i++) {
				entries.add(new DirectoryEntry(payload));
			}
			transport.setDirectory(entries);
		} else if (type == NETWORK_CONFIG && isServer) {
			PacketHandler.sendTo(parent.getTilePacket(), thePlayer);
			thePlayer.openGui(ThermalDynamics.instance, GuiHandler.TILE_CONFIG, world(), pos().getX(), pos().getY(), pos().getZ());
		}
	}

	public void setName(String name) {

		if (!name.equals(this.data.name)) {
			if (data == BLANK_NAME) {
				data = new OutputData();
			}
			this.data.name = name;
			sendOutputDataConfigPacket();
		}
	}

	public void setIcon(ItemStack stack) {

		if (data == BLANK_NAME) {
			this.data = new OutputData();
		}
		this.data.item = stack;
		sendOutputDataConfigPacket();
	}

	public void sendOutputDataConfigPacket() {

		if (this.world().isRemote) {
			PacketTileInfo myPayload = newPacketTileInfo();
			myPayload.addByte(NETWORK_SETOUTPUTDATA);
			data.saveConfigData(myPayload);
			PacketHandler.sendToServer(myPayload);
		}
	}

	public void sendRequest(int x, int y, int z) {

		PacketTileInfo myPayload = newPacketTileInfo();
		myPayload.addByte(NETWORK_REQUEST);
		myPayload.addInt(x);
		myPayload.addInt(y);
		myPayload.addInt(z);
		PacketHandler.sendToServer(myPayload);
	}

	public PacketCoFHBase getDirectoryPacket() {

		PacketTileInfo myPayload = newPacketTileInfo();
		myPayload.addByte(NETWORK_LIST);

		LinkedList<Route<DuctUnitTransportBase, GridTransport>> outputRoutes = getCache().outputRoutes;
		ArrayList<DuctUnitTransport> ducts = new ArrayList<>(outputRoutes.size());

		for (Route<DuctUnitTransportBase, GridTransport> outputRoute : outputRoutes) {
			if (outputRoute.endPoint.isOutput() && outputRoute.endPoint != this && outputRoute.endPoint instanceof DuctUnitTransport) {
				ducts.add((DuctUnitTransport) outputRoute.endPoint);
			}
		}
		DirectoryEntry.addDirectoryEntry(myPayload, this);
		myPayload.addShort(ducts.size());

		for (DuctUnitTransport endPoint : ducts) {
			DirectoryEntry.addDirectoryEntry(myPayload, endPoint);
		}
		return myPayload;
	}

	public boolean sendPlayerToDest(EntityPlayer player, int x, int y, int z) {

		for (Route outputRoute : getCache().outputRoutes) {
			IGridTileRoute endPoint = outputRoute.endPoint;

			if (endPoint.x() == x && endPoint.y() == y && endPoint.z() == z) {
				Route route = outputRoute.copy();
				route.pathDirections.add(endPoint.getStuffedSide());

				EntityTransport entityTransport = new EntityTransport(this, route, (byte) (getStuffedSide() ^ 1), (byte) 50);
				entityTransport.start(player);

				if (player.openContainer instanceof ContainerTransport) {
					player.closeScreen();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerTransport(this);
	}

	@Override
	@SideOnly (Side.CLIENT)
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiTransport(this);
	}

	@Override
	public Object getConfigGuiServer(InventoryPlayer inventory) {

		return new ContainerTransportConfig(inventory, this);
	}

	@Override
	public Object getConfigGuiClient(InventoryPlayer inventory) {

		return new GuiTransportConfig(inventory, this);
	}

	@Override
	public boolean openConfigGui(IBlockAccess world, BlockPos pos, EnumFacing side, EntityPlayer player) {

		if (ServerHelper.isClientWorld(world())) {
			return true;
		}
		PacketHandler.sendTo(parent.getTilePacket(), player);
		player.openGui(ThermalDynamics.instance, GuiHandler.TILE_CONFIG, world(), pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public boolean isRoutable() {

		return true;
	}

	@Override
	public boolean isCrossover() {

		return false;
	}

	@Override
	public GridTransport createGrid() {

		return new GridTransport(world());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitTransportBase, GridTransport, TransportDestination> adjDuct, byte side, byte oppositeSide) {

		return parent.getConnectionType(side) != ConnectionType.FORCED && adjDuct.cast().isRoutable();
	}

	@Nullable
	@Override
	public TransportDestination cacheTile(@Nonnull TileEntity tile, byte side) {

		return null;
	}

	public RouteCache<DuctUnitTransportBase, GridTransport> getCache() {

		return getCache(true);
	}

	public RouteCache<DuctUnitTransportBase, GridTransport> getCache(boolean urgent) {

		assert grid != null;
		return urgent ? grid.getRoutesFromOutput(this) : grid.getRoutesFromOutputNonUrgent(this);
	}

	@Override
	public Route getRoute(Entity entity, int side, byte speed) {

		if (entity == null || entity.isDead) {
			return null;
		}
		for (Route outputRoute : getCache().outputRoutes) {
			if (outputRoute.endPoint == this || !outputRoute.endPoint.isOutput()) {
				continue;
			}

			Route route = outputRoute.copy();
			byte outSide = outputRoute.endPoint.getStuffedSide();
			route.pathDirections.add(outSide);
			return route;
		}
		return null;
	}

	public EntityTransport findRoute(Entity entity, int side, byte speed) {

		Route route = getRoute(entity, side, speed);
		return route != null ? new EntityTransport(this, route, (byte) side, speed) : null;
	}

	@Override
	public int getWeight() {

		return 1;
	}

	@Override
	public boolean canStuffItem() {

		return false;
	}

	@Override
	public boolean isOutput() {

		return nodeMask != 0;
	}

	@Override
	public int getMaxRange() {

		return Integer.MAX_VALUE;
	}

	@Override
	public RouteInfo canRouteItem(ItemStack stack) {

		return RouteInfo.noRoute;
	}

	@Override
	public byte getStuffedSide() {

		for (byte i = 0; i < 6; i++) {
			if (parent.getConnectionType(i) == ConnectionType.FORCED) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public boolean acceptingStuff() {

		return false;
	}

	public void advanceToNextTile(EntityTransport t) {

		t.advanceTile(this);
	}

	public static class OutputData {

		public String name = "";
		public ItemStack item = ItemStack.EMPTY;

		public void write(NBTTagCompound nbt, DuctUnitTransport transportDuct) {

			if (!"".equals(name)) {
				nbt.setString("DestinationName", name);
			}
			if (!item.isEmpty()) {
				nbt.setTag("DestinationIcon", item.writeToNBT(new NBTTagCompound()));
			}
		}

		public static OutputData read(NBTTagCompound nbt) {

			if (!nbt.hasKey("DestinationName") && !nbt.hasKey("DestinationIcon")) {
				return BLANK_NAME;
			}
			OutputData outputData = new OutputData();
			outputData.name = nbt.getString("DestinationName");
			outputData.item = new ItemStack(nbt.getCompoundTag("DestinationIcon"));
			return outputData;
		}

		public void addToPacket(PacketCoFHBase packet) {

			packet.addString(name);
			packet.addItemStack(item);
		}

		public void readPacket(PacketCoFHBase payload) {

			name = payload.getString();
			item = payload.getItemStack();
		}

		public void loadConfigData(PacketCoFHBase payload) {

			String prevName = name;
			ItemStack prevItem = item.copy();
			try {
				name = payload.getString();
				item = payload.getItemStack();
			} catch (RuntimeException error) {
				name = prevName;
				item = prevItem;
			}
		}

		public void saveConfigData(PacketTileInfo payload) {

			payload.addString(name);
			payload.addItemStack(item);
		}
	}

}
