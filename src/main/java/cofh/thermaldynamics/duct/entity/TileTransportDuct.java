package cofh.thermaldynamics.duct.entity;

import codechicken.lib.raytracer.RayTracer;
import cofh.api.block.IBlockConfigGui;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.WrenchHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.NeighborType;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.gui.client.DirectoryEntry;
import cofh.thermaldynamics.gui.client.GuiTransport;
import cofh.thermaldynamics.gui.client.GuiTransportConfig;
import cofh.thermaldynamics.gui.container.ContainerTransport;
import cofh.thermaldynamics.gui.container.ContainerTransportConfig;
import cofh.thermaldynamics.multiblock.IGridTileRoute;
import cofh.thermaldynamics.multiblock.Route;
import net.minecraft.block.state.IBlockState;
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

import java.util.ArrayList;
import java.util.LinkedList;

public class TileTransportDuct extends TileTransportDuctBaseRoute implements IBlockConfigGui {

	@Override
	public boolean isOutput() {

		return isOutput;
	}

	@Override
	public void handleTileSideUpdate(int i) {

		super.handleTileSideUpdate(i);

		if (connectionTypes[i] == ConnectionType.FORCED) {
			neighborMultiBlocks[i] = null;
			neighborTypes[i] = NeighborType.OUTPUT;
			isNode = true;
			isOutput = true;
		}
	}

	@Override
	public boolean isBlockedSide(int side) {

		return super.isBlockedSide(side) || connectionTypes[side] == ConnectionType.FORCED;
	}

	@Override
	public boolean onWrench(EntityPlayer player, EnumFacing side) {

		RayTraceResult rayTrace = RayTracer.retraceBlock(worldObj, player, pos);
		if (WrenchHelper.isHoldingUsableWrench(player, rayTrace)) {
			if (rayTrace == null) {
				return false;
			}

			int subHit = rayTrace.subHit;
			if (subHit >= 0 && subHit <= 13) {
				int i = subHit == 13 ? side.ordinal() : subHit < 6 ? subHit : subHit - 6;

				onNeighborBlockChange();

				TileEntity tile = BlockHelper.getAdjacentTileEntity(this, i);
				if (isConnectable(tile, i)) {
					connectionTypes[i] = connectionTypes[i].next();
					((TileDuctBase) tile).connectionTypes[i ^ 1] = connectionTypes[i];
				} else {
					if (connectionTypes[i] == ConnectionType.FORCED) {
						connectionTypes[i] = ConnectionType.NORMAL;
					} else {
						connectionTypes[i] = ConnectionType.FORCED;
						for (int j = 0; j < 6; j++) {
							if (i != j && connectionTypes[j] == ConnectionType.FORCED) {
								connectionTypes[j] = ConnectionType.NORMAL;
							}
						}
					}
				}

				onNeighborBlockChange();

				worldObj.notifyNeighborsOfStateChange(pos, getBlockType());

				if (myGrid != null) {
					myGrid.destroyAndRecreate();
				}

				for (SubTileGridTile subTile : subTiles) {
					subTile.destroyAndRecreate();
				}

				IBlockState state = worldObj.getBlockState(pos);
				worldObj.notifyBlockUpdate(pos, state, state, 1);
				return true;
			}
			if (subHit > 13 && subHit < 20) {
				return attachments[subHit - 14].onWrenched();
			}

			if (subHit >= 20 && subHit < 26) {
				return covers[subHit - 20].onWrenched();
			}
		}
		return false;
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (super.openGui(player) || ServerHelper.isClientWorld(worldObj)) {
			return true;
		}

		if (internalGrid == null) {
			return false;
		}

		onNeighborBlockChange();

		for (int i = 0; i < 6; i++) {
			if (connectionTypes[i] == ConnectionType.FORCED) {
				if (neighborMultiBlocks[i] != null) {
					continue;
				}

				PacketHandler.sendTo(getTilePacket(), player);
				player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ID, worldObj, pos.getX(), pos.getY(), pos.getZ());
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
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase packet = super.getTilePacket();
		if (data != BLANK_NAME) {
			packet.addBool(true);
			data.addToPacket(packet);
		} else {
			packet.addBool(false);
		}
		return packet;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);
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
			if (internalGrid != null) {
				internalGrid.onMajorGridChange();
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
			PacketHandler.sendTo(getTilePacket(), thePlayer);
			thePlayer.openGui(ThermalDynamics.instance, GuiHandler.TILE_CONFIG, worldObj, pos.getX(), pos.getY(), pos.getZ());
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

		if (this.worldObj.isRemote) {
			PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
			myPayload.addByte(0);
			myPayload.addByte(NETWORK_SETOUTPUTDATA);
			data.saveConfigData(myPayload);
			PacketHandler.sendToServer(myPayload);
		}
	}

	public void sendRequest(int x, int y, int z) {

		PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
		myPayload.addByte(0);
		myPayload.addByte(NETWORK_REQUEST);
		myPayload.addInt(x);
		myPayload.addInt(y);
		myPayload.addInt(z);
		PacketHandler.sendToServer(myPayload);
	}

	public PacketCoFHBase getDirectoryPacket() {

		PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
		myPayload.addByte(0);
		myPayload.addByte(NETWORK_LIST);

		LinkedList<Route> outputRoutes = getCache().outputRoutes;

		ArrayList<TileTransportDuct> ducts = new ArrayList<>(outputRoutes.size());

		for (Route outputRoute : outputRoutes) {
			if (outputRoute.endPoint.isOutput() && outputRoute.endPoint != this && outputRoute.endPoint instanceof TileTransportDuct) {
				ducts.add((TileTransportDuct) outputRoute.endPoint);
			}
		}

		DirectoryEntry.addDirectoryEntry(myPayload, this);

		myPayload.addShort(ducts.size());
		for (TileTransportDuct endPoint : ducts) {
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

		if (ServerHelper.isClientWorld(worldObj)) {
			return true;
		}

		PacketHandler.sendTo(getTilePacket(), player);
		player.openGui(ThermalDynamics.instance, GuiHandler.TILE_CONFIG, worldObj, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	public static class OutputData {

		public String name = "";
		public ItemStack item = null;

		public void write(NBTTagCompound nbt, TileTransportDuct transportDuct) {

			if (!"".equals(name)) {
				nbt.setString("DestinationName", name);
			}
			if (item != null) {
				nbt.setTag("DestinationIcon", item.writeToNBT(new NBTTagCompound()));
			}
		}

		public static OutputData read(NBTTagCompound nbt) {

			if (!nbt.hasKey("DestinationName") && !nbt.hasKey("DestinationIcon")) {
				return BLANK_NAME;
			}

			OutputData outputData = new OutputData();
			outputData.name = nbt.getString("DestinationName");
			outputData.item = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("DestinationIcon"));
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
			ItemStack prevItem = ItemStack.copyItemStack(item);
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

	@Override
	@SideOnly (Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask(byte side) {

		BlockDuct.ConnectionTypes connectionType = getRenderConnectionType(side);
		return connectionType == BlockDuct.ConnectionTypes.NONE ? null : CoverHoleRender.hollowDuctTransport;
	}

}
