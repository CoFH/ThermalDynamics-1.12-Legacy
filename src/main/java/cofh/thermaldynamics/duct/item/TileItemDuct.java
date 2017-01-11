package cofh.thermaldynamics.duct.item;

import cofh.api.inventory.IInventoryConnection;
import cofh.api.transport.IItemDuct;
import cofh.core.crash.CrashHelper;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.InventoryHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.core.TDProps;
import cofh.thermaldynamics.core.TickHandlerClient;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.attachments.IStuffable;
import cofh.thermaldynamics.duct.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.duct.attachments.filter.IFilterItems;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.multiblock.*;
import com.google.common.collect.Iterables;
import gnu.trove.iterator.TObjectIntIterator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TileItemDuct extends TileTDBase implements IMultiBlockRoute, IItemDuct {

	public ItemGrid internalGrid;

	public List<TravelingItem> myItems = new LinkedList<TravelingItem>();
	public List<TravelingItem> itemsToRemove = new LinkedList<TravelingItem>();
	public List<TravelingItem> itemsToAdd = new LinkedList<TravelingItem>();

	public byte pathWeightType = 0;
	public byte ticksExisted = 0;
	public final static byte maxTicksExistedBeforeFindAlt = 2;
	public final static byte maxTicksExistedBeforeStuff = 6;
	public final static byte maxTicksExistedBeforeDump = 10;

	// Type Helper Arrays
	static int[] _PIPE_LEN = { 40, 10, 60, 40 };
	static int[] _PIPE_HALF_LEN = { _PIPE_LEN[0] / 2, _PIPE_LEN[1] / 2, _PIPE_LEN[2] / 2, _PIPE_LEN[3] / 2 };
	static float[] _PIPE_TICK_LEN = { 1F / _PIPE_LEN[0], 1F / _PIPE_LEN[1], 1F / _PIPE_LEN[2], 1F / _PIPE_LEN[3] };

	static float[][][] _SIDE_MODS = new float[4][6][3];
	static int INSERT_SIZE = 8;

	static {
		for (int i = 0; i < 4; i++) {
			float j = _PIPE_TICK_LEN[i];
			_SIDE_MODS[i][0] = new float[] { 0, -j, 0 };
			_SIDE_MODS[i][1] = new float[] { 0, j, 0 };
			_SIDE_MODS[i][2] = new float[] { 0, 0, -j };
			_SIDE_MODS[i][3] = new float[] { 0, 0, j };
			_SIDE_MODS[i][4] = new float[] { -j, 0, 0 };
			_SIDE_MODS[i][5] = new float[] { j, 0, 0 };
		}
	}

	public IFilterItems[] filterCache;
	public IItemHandler[] handlerCache;
	public IDeepStorageUnit[] cache3;

	@Override
	public ItemStack insertItem(EnumFacing from, ItemStack item) {

		if (item == null) {
			return null;
		}
		int side = from.ordinal();
		if (!((neighborTypes[side] == NeighborTypes.INPUT) || (neighborTypes[side] == NeighborTypes.OUTPUT && connectionTypes[side].allowTransfer))) {
			return item;
		}
		if (internalGrid == null) {
			return item;
		}
		Attachment attachment = attachments[side];
		if (attachment != null && attachment.getId() == AttachmentRegistry.SERVO_ITEM) {
			return ((ServoItem) attachment).insertItem(item);
		} else {
			ItemStack itemCopy = ItemHelper.cloneStack(item);

			if (filterCache != null && !filterCache[side].matchesFilter(item)) {
				return item;
			}

			TravelingItem routeForItem = ServoItem.findRouteForItem(ItemHelper.cloneStack(item, Math.min(INSERT_SIZE, item.stackSize)),
					getCache(false).outputRoutes, this, side, ServoItem.range[0], (byte) 1);
			if (routeForItem == null) {
				return item;
			}

			itemCopy.stackSize -= routeForItem.stack.stackSize;
			insertNewItem(routeForItem);
			return itemCopy.stackSize > 0 ? itemCopy : null;
		}
	}

	public Route getRoute(IMultiBlockRoute itemDuct) {

		for (Route outputRoute : getCache().outputRoutes) {
			if (outputRoute.endPoint == itemDuct
					|| (outputRoute.endPoint.x() == itemDuct.x() && outputRoute.endPoint.y() == itemDuct.y() && outputRoute.endPoint.z() == itemDuct.z())) {
				return outputRoute;
			}
		}
		return null;
	}

	public static class RouteInfo {

		public RouteInfo(int stackSizeLeft, byte i) {

			canRoute = true;
			stackSize = stackSizeLeft;
			side = i;
		}

		public RouteInfo() {

		}

		public boolean canRoute = false;
		public int stackSize = -1;
		public byte side = -1;
	}

	public static final RouteInfo noRoute = new RouteInfo();

	/*
	 * Should return true if theTile is significant to this multiblock
	 *
	 * IE: Inventory's to ItemDuct's
	 */
	@Override
	public boolean isSignificantTile(TileEntity theTile, int side) {
		if ((theTile instanceof IInventoryConnection)) {
			IInventoryConnection.ConnectionType connectionType = ((IInventoryConnection) theTile)
					.canConnectInventory(EnumFacing.VALUES[side ^ 1]);
			if (connectionType == IInventoryConnection.ConnectionType.DENY) {
				return false;
			}
			if (connectionType == IInventoryConnection.ConnectionType.FORCE) {
				return true;
			}
		}
        return theTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
    }

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (ItemGrid) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new ItemGrid(worldObj);
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}

		if (pass == 0) {
			if (ticksExisted < maxTicksExistedBeforeDump) {
				ticksExisted++;
			}
			tickItems();
		}
		return true;
	}

	@Override
	public int getWeight() {

		if (pathWeightType == DuctItem.PATHWEIGHT_DENSE) {
			return 1000;
		} else if (pathWeightType == DuctItem.PATHWEIGHT_VACUUM) {
			return -1000;
		} else {
			return getDuctType().pathWeight;
		}
	}

	@Override
	public TextureAtlasSprite getBaseIcon() {

		if (pathWeightType == DuctItem.PATHWEIGHT_DENSE) {
			return ((DuctItem) getDuctType()).iconBaseTextureDense;
		} else if (pathWeightType == DuctItem.PATHWEIGHT_VACUUM) {
			return ((DuctItem) getDuctType()).iconBaseTextureVacuum;
		} else {
			return super.getBaseIcon();
		}
	}

	@Override
	public boolean isOutput() {

		return isOutput;
	}

	@Override
	public boolean canStuffItem() {

		for (Attachment attachment : attachments) {
			if (attachment instanceof IStuffable) {
				return true;
			}
		}
		return false;
	}

	boolean wasVisited = false;

	@Override
	public int getMaxRange() {

		return Integer.MAX_VALUE;
	}

	@Override
	public NeighborTypes getCachedSideType(byte side) {

		return neighborTypes[side];
	}

	@Override
	public ConnectionTypes getConnectionType(byte side) {

		return connectionTypes[side];
	}

	@Override
	public IMultiBlock getCachedTile(byte side) {

		return neighborMultiBlocks[side];
	}

	@Override
	public int x() {

		return getPos().getX();
	}

	@Override
	public int y() {

		return getPos().getY();
	}

	@Override
	public int z() {

		return getPos().getZ();
	}

	@Override
	public boolean shouldRenderInPass(int pass) {

		return pass == 0 && (!myItems.isEmpty() || !itemsToAdd.isEmpty() || centerLine > 0);
	}

	public RouteCache getCache() {

		return getCache(true);
	}

	public RouteCache getCache(boolean urgent) {

		return urgent ? internalGrid.getRoutesFromOutput(this) : internalGrid.getRoutesFromOutputNonUrgent(this);
	}

	// @Override
	// public boolean openGui(EntityPlayer player) {
	// if (!isOutput())
	// return false;
	//
	// if (ServerHelper.isClientWorld(worldObj) || !isOutput())
	// return true;
	//
	// LinkedList<Route> routes = internalGrid.getRoutesFromOutput(this).outputRoutes;
	//
	// if (routes.size() <= 1)
	// return true;
	//
	//
	// for (Route route : routes) {
	// if (route.pathDirections.size() < 1)
	// continue;
	//
	// byte input;
	// for (input = 0; input < 6 && neighborTypes[input ^ 1] != NeighborTypes.OUTPUT; ) input++;
	// byte output;
	// for (output = 0; output < 6 && ((TileItemDuct) route.endPoint).neighborTypes[output] != NeighborTypes.OUTPUT; )
	// output++;
	//
	// Route itemRoute = route.copy();
	// itemRoute.pathDirections.add(output);
	// final TravelingItem travelingItem = new TravelingItem(new ItemStack(Blocks.glowstone), x(), y(), z(), itemRoute, input);
	// travelingItem.goingToStuff = true;
	// insertItem(travelingItem);
	//
	// break;
	// }
	// // player.addChatComponentMessage(new ChatComponentText("Routes: " + routes.size()));
	//
	// return true;
	// }

	public void pulseLineDo(int dir) {

		if (!getDuctType().opaque) {
			PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
			myPayload.addByte(0);
			myPayload.addByte(TileInfoPackets.PULSE_LINE);
			myPayload.addByte(dir);

			PacketHandler.sendToAllAround(myPayload, this);
		}
	}

	public void pulseLine(byte dir) {

		pulseLineDo(1 << dir);
	}

	public void pulseLine(byte dir1, byte dir2) {

		pulseLineDo((1 << dir1) | (1 << dir2));
	}

	public void pulseLine() {

		pulseLineDo(63);
	}

	public int getPipeLength() {

		return _PIPE_LEN[getDuctType().type];
	}

	public int getPipeHalfLength() {

		return _PIPE_HALF_LEN[getDuctType().type];
	}

	public float[][] getSideCoordsModifier() {

		return _SIDE_MODS[getDuctType().type];
	}

	public void stuffItem(TravelingItem travelingItem) {

		Attachment attachment = attachments[travelingItem.direction];
		if (attachment instanceof IStuffable) {
			signalRepoll();
			((IStuffable) attachment).stuffItem(travelingItem.stack);
		}
	}

	public boolean acceptingItems() {

		return true;
	}

	public void insertNewItem(TravelingItem travelingItem) {

		internalGrid.poll(travelingItem);
		transferItem(travelingItem);
	}

	public void transferItem(TravelingItem travelingItem) {

		itemsToAdd.add(travelingItem);
	}

	public boolean hasChanged = false;

	public void tickItems() {

		if (itemsToAdd.size() > 0) {
			myItems.addAll(itemsToAdd);
			itemsToAdd.clear();
			hasChanged = true;
		}
		if (myItems.size() > 0) {
			for (TravelingItem item : myItems) {
				item.tickForward(this);
				if (internalGrid.repoll) {
					internalGrid.poll(item);
				}
			}
			if (itemsToRemove.size() > 0) {
				myItems.removeAll(itemsToRemove);
				itemsToRemove.clear();
				hasChanged = true;
			}
		}
		if (hasChanged) {
			sendTravelingItemsPacket();
			hasChanged = false;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		itemsToAdd.clear();
		myItems.clear();

		if (nbt.hasKey("TravellingItems", 9)) {
			NBTTagList list = nbt.getTagList("TravellingItems", 10);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound compound = list.getCompoundTagAt(i);
				TravelingItem travelingItem = new TravelingItem(compound);
				if (travelingItem.stack != null) {
					myItems.add(travelingItem);
				}
			}
		}

		pathWeightType = nbt.getByte("Weight");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		NBTTagList items = new NBTTagList();
		for (TravelingItem travelingItem : Iterables.concat(itemsToAdd, myItems)) {
			NBTTagCompound tag = new NBTTagCompound();
			travelingItem.toNBT(tag);
			items.appendTag(tag);
		}
		if (items.tagCount() > 0) {
			nbt.setTag("TravellingItems", items);
		}
		if (pathWeightType != 0) {
			nbt.setByte("Weight", pathWeightType);
		}
		return nbt;
	}

	public void sendTravelingItemsPacket() {

		if (!getDuctType().opaque) {
			PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
			myPayload.addByte(0);
			myPayload.addByte(TileInfoPackets.TRAVELING_ITEMS);

			int loopStop = myItems.size();
			loopStop = Math.min(loopStop, TDProps.MAX_ITEMS_TRANSMITTED);
			myPayload.addByte(loopStop);
			for (int i = 0; i < loopStop; i++) {
				myItems.get(i).writePacket(myPayload);
			}

			PacketHandler.sendToAllAround(myPayload, this);
		}
	}

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		int b = payload.getByte();
		handlePacketType(payload, b);
	}

	@Override
	public boolean cachesExist() {

		return handlerCache != null;
	}

	@Override
	public void createCaches() {

		filterCache = new IFilterItems[] { IFilterItems.nullFilter, IFilterItems.nullFilter, IFilterItems.nullFilter, IFilterItems.nullFilter,
				IFilterItems.nullFilter, IFilterItems.nullFilter };
		handlerCache = new IItemHandler[6];
		cache3 = new IDeepStorageUnit[6];
	}

	public void handlePacketType(PacketCoFHBase payload, int b) {

		if (b == TileInfoPackets.PULSE_LINE) {
			int c = payload.getByte();
			for (int i = 0; i < 6; i++) {
				if ((c & (1 << i)) != 0) {
					centerLineSub[i] = maxCenterLine;
				}
			}
			centerLine = maxCenterLine;
			if (!TickHandlerClient.tickBlocks.contains(this) && !TickHandlerClient.tickBlocksToAdd.contains(this)) {
				TickHandlerClient.tickBlocksToAdd.add(this);
			}
		} else if (b == TileInfoPackets.TRAVELING_ITEMS) {
			myItems.clear();
			byte n = payload.getByte();
			if (n > 0) {
				for (byte i = 0; i < n; i++) {
					myItems.add(TravelingItem.fromPacket(payload, this));
				}
				if (!TickHandlerClient.tickBlocks.contains(this) && !TickHandlerClient.tickBlocksToAdd.contains(this)) {
					TickHandlerClient.tickBlocksToAdd.add(this);
				}
			}
		}
	}

	@Override
	public void cacheImportant(TileEntity tile, int side) {

		if (tile instanceof IDeepStorageUnit) {
			cache3[side] = (IDeepStorageUnit) tile;
		}
		handlerCache[side] = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
		if (attachments[side] instanceof IFilterAttachment) {
			filterCache[side] = ((IFilterAttachment) attachments[side]).getItemFilter();
		}
	}

	@Override
	public void clearCache(int side) {

		filterCache[side] = IFilterItems.nullFilter;
		handlerCache[side] = null;
		cache3[side] = null;
	}

	public void removeItem(TravelingItem travelingItem, boolean disappearing) {

		if (disappearing) {
			signalRepoll();
		}
		itemsToRemove.add(travelingItem);
	}

	public class TileInfoPackets {

		public static final byte GUI_BUTTON = 0;
		public static final byte STUFFED_UPDATE = 1;
		public static final byte TRAVELING_ITEMS = 2;
		public static final byte STUFFED_ITEMS = 3;
		public static final byte REQUEST_STUFFED_ITEMS = 4;
		public static final byte PULSE_LINE = 5;
		public static final byte ENDER_POWER = 6;
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase packet = super.getPacket();
		packet.addByte(pathWeightType);
		return packet;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);
		if (!isServer) {
			pathWeightType = payload.getByte();
		}
	}

	@Override
	public void onPlacedBy(EntityLivingBase living, ItemStack stack) {

		super.onPlacedBy(living, stack);
		if (stack.hasTagCompound()) {
			byte b = stack.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT);
			if (b == DuctItem.PATHWEIGHT_DENSE || b == DuctItem.PATHWEIGHT_VACUUM) {
				pathWeightType = b;
			}
		}
		ticksExisted = maxTicksExistedBeforeDump;
	}

	@Override
	public void dropAdditional(ArrayList<ItemStack> ret) {

		for (TravelingItem travelingItem : Iterables.concat(myItems, itemsToAdd)) {
			ret.add(travelingItem.stack);
		}
		super.dropAdditional(ret);
	}

	@Override
	public ItemStack getDrop() {

		ItemStack drop = super.getDrop();
		if (pathWeightType != 0) {
			if (!drop.hasTagCompound()) {
                drop.setTagCompound(new NBTTagCompound());
			}
			drop.getTagCompound().setByte(DuctItem.PATHWEIGHT_NBT, pathWeightType);
		}
		return drop;
	}

	public void tickItemsClient() {

		if (centerLine > 0) {
			centerLine--;
			for (int i = 0; i < 6; i++) {
				if (centerLineSub[i] > 0) {
					centerLineSub[i]--;
				}
			}
		}

		if (itemsToAdd.size() > 0) {
			myItems.addAll(itemsToAdd);
			itemsToAdd.clear();
		}
		if (myItems.size() > 0) {
			for (int i = 0; i < myItems.size(); i++) {
				myItems.get(i).tickClientForward(this);
			}
			if (itemsToRemove.size() > 0) {
				myItems.removeAll(itemsToRemove);
				itemsToRemove.clear();
			}
		} else if (centerLine == 0) {
			TickHandlerClient.tickBlocksToRemove.add(this);
		}
	}

	@Override
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileItemDuct;
	}

	public static final int maxCenterLine = 10;
	public int centerLine = 0;
	public int[] centerLineSub = new int[6];

	// public int getIncoming(ItemStack anItem, int side) {
	// int stackSize = 0;
	// HashSet<TravelingItem> travelingItems = internalGrid.travelingItems.get(new BlockCoord(this).offset(side));
	// if (travelingItems != null && !travelingItems.isEmpty()) {
	// for (TravelingItem travelingItem : travelingItems) {
	// if (ItemHelper.itemsEqualWithMetadata(anItem, travelingItem.stack, true)) {
	// stackSize += travelingItem.stack.stackSize;
	// }
	// }
	// }
	//
	// return stackSize;
	// }

	@Override
	public RouteInfo canRouteItem(ItemStack anItem) {

		if (internalGrid == null || !cachesExist()) {
			return noRoute;
		}
		int stackSizeLeft;
		ItemStack curItem;

		for (byte i = internalSideCounter; i < EnumFacing.VALUES.length; i++) {
			if (neighborTypes[i] == NeighborTypes.OUTPUT && connectionTypes[i].allowTransfer && itemPassesFiltering(i, anItem) && handlerCache[i] != null) {
				curItem = anItem.copy();
				curItem.stackSize = Math.min(getMoveStackSize(i), curItem.stackSize);

				if (curItem.stackSize > 0) {
					stackSizeLeft = simTransferI(i, curItem.copy());
					stackSizeLeft = (anItem.stackSize - curItem.stackSize) + stackSizeLeft;
					if (stackSizeLeft < anItem.stackSize) {
						tickInternalSideCounter(i + 1);
						return new RouteInfo(stackSizeLeft, i);
					}
				}
			}
		}
		for (byte i = 0; i < internalSideCounter; i++) {
			if (neighborTypes[i] == NeighborTypes.OUTPUT && connectionTypes[i].allowTransfer && itemPassesFiltering(i, anItem) && handlerCache[i] != null) {
				curItem = anItem.copy();
				curItem.stackSize = Math.min(getMoveStackSize(i), curItem.stackSize);
				if (curItem.stackSize > 0) {
					stackSizeLeft = simTransferI(i, curItem.copy());
					stackSizeLeft = (anItem.stackSize - curItem.stackSize) + stackSizeLeft;
					if (stackSizeLeft < anItem.stackSize) {
						tickInternalSideCounter(i + 1);
						return new RouteInfo(stackSizeLeft, i);
					}
				}
			}
		}
		return noRoute;
	}

	public int simTransferI(int side, ItemStack insertingItem) {

		try {
			ItemStack itemStack = simTransfer(side, insertingItem);
			return itemStack == null ? 0 : itemStack.stackSize;
		} catch (Exception err) {
			IItemHandler handler = handlerCache[side];

			CrashReport crashReport = CrashHelper.makeDetailedCrashReport(err, "Inserting", this, "Inserting Item", insertingItem, "Side", side, "Cache",
                    handler, "Grid", internalGrid);
			CrashHelper.addSurroundingDetails(crashReport, "ItemDuct", this);
			CrashHelper.addInventoryContents(crashReport, "Destination Inventory", handler);
			throw new ReportedException(crashReport);
		}
	}

	public ItemStack simTransfer(int side, ItemStack insertingItem) {
        EnumFacing face = EnumFacing.VALUES[side];
		if (insertingItem == null) {
			return null;
		}
		if (internalGrid == null || !cachesExist()) {
			return insertingItem;
		}
		boolean routeItems = filterCache[side].shouldIncRouteItems();

		int maxStock = filterCache[side].getMaxStock();

		if (cache3[side] != null) { // IDeepStorage
			ItemStack cacheStack = cache3[side].getStoredItemType();
			if (cacheStack != null && !ItemHelper.itemsIdentical(cacheStack, insertingItem)) {
				return insertingItem;
			}
			int s = cacheStack != null ? cacheStack.stackSize : 0;
			int m = Math.min(cache3[side].getMaxStoredCount(), maxStock);

			if (s >= m) {
				return insertingItem;
			}
			if (routeItems) {
				StackMap travelingItems = internalGrid.travelingItems.get(getPos().offset(face));
				if (travelingItems != null && !travelingItems.isEmpty()) {
					for (Iterator<ItemStack> iterator = travelingItems.getItems(); s < m && iterator.hasNext();) {
						ItemStack travelingItem = iterator.next();
						boolean equalsItem = ItemHelper.itemsIdentical(insertingItem, travelingItem);
						if (cacheStack == null && !equalsItem) {
							return insertingItem;
						}
						if (equalsItem) {
							s += travelingItem.stackSize;
						}
					}
					if (s >= m) {
						return insertingItem;
					}
				}
			}
			insertingItem.stackSize -= (m - s);
			if (insertingItem.stackSize <= 0) {
				return null;
			}
			return insertingItem;
		} else {
			if (!routeItems) {
				return simulateInsertItemStackIntoInventory(handlerCache[side], insertingItem, side ^ 1, maxStock);
			}

			StackMap travelingItems = internalGrid.travelingItems.get(getPos().offset(face));
			if (travelingItems == null || travelingItems.isEmpty()) {
				return simulateInsertItemStackIntoInventory(handlerCache[side], insertingItem, side ^ 1, maxStock);
			}
			if (travelingItems.size() == 1) {
				if (ItemHelper.itemsIdentical(insertingItem, travelingItems.getItems().next())) {
					insertingItem.stackSize += travelingItems.getItems().next().stackSize;
					return simulateInsertItemStackIntoInventory(handlerCache[side], insertingItem, side ^ 1, maxStock);
				}
			} else {
				int s = 0;
				for (ItemStack travelingItem : travelingItems.getItems()) {
					if (!ItemHelper.itemsIdentical(insertingItem, travelingItem)) {
						s = -1;
						break;
					} else {
						s += travelingItem.stackSize;
					}
				}
				if (s >= 0) {
					insertingItem.stackSize += s;
					return simulateInsertItemStackIntoInventory(handlerCache[side], insertingItem, side ^ 1, maxStock);
				}
			}

            SimulatedInv simulatedInv = SimulatedInv.wrapHandler(handlerCache[side]);


			for (TObjectIntIterator<StackMap.ItemEntry> iterator = travelingItems.iterator(); iterator.hasNext();) {
				iterator.advance();

				if (InventoryHelper.insertStackIntoInventory(simulatedInv, iterator.key().toItemStack(iterator.value()), false) != null
						&& ItemHelper.itemsIdentical(insertingItem, iterator.key().toItemStack(iterator.value()))) {
					return insertingItem;
				}
			}
			insertingItem = simulateInsertItemStackIntoInventory(simulatedInv, insertingItem, side ^ 1, maxStock);
			simulatedInv.clear();
			return insertingItem;
		}
	}

	@Override
	public byte getStuffedSide() {

		for (byte i = 0; i < 6; i++) {
			if (attachments[i] instanceof IStuffable) {
				if (((IStuffable) attachments[i]).canStuff()) {
					return i;
				}

			}
		}
		for (byte i = 0; i < 6; i++) {
			if (attachments[i] instanceof IStuffable) {
				return i;
			}
		}
		throw new RuntimeException("IStuffable disappeared during calculation!");
	}

	@Override
	public boolean acceptingStuff() {

		for (byte i = 0; i < 6; i++) {
			if (attachments[i] instanceof IStuffable) {
				return ((IStuffable) attachments[i]).canStuff();
			}
		}
		return false;
	}

	private boolean stuffed() {

		return false;
	}

	private boolean itemPassesFiltering(byte i, ItemStack anItem) {

		return filterCache[i].matchesFilter(anItem);
	}

	public int getMoveStackSize(byte side) {

		return 64;
	}

	public int insertIntoInventory(ItemStack stack, int direction) {

		if (!cachesExist() || handlerCache[direction] == null) {
			return stack.stackSize;
		}
		if (!filterCache[direction].matchesFilter(stack)) {
			return stack.stackSize;
		}
		return insertIntoInventory_do(stack, direction);
	}

	public void signalRepoll() {

		if (internalGrid != null) {
			internalGrid.shouldRepoll = true;
		}
	}

	public int insertIntoInventory_do(ItemStack stack, int direction) {

		signalRepoll();
		stack = insertItemStackIntoInventory(handlerCache[direction], stack, direction ^ 1, filterCache[direction].getMaxStock());
		return stack == null ? 0 : stack.stackSize;
	}

    public static int getNumItems(IItemHandler inv, int side, ItemStack insertingItem, int cap) {

        //if (inv instanceof IDeepStorageUnit) {
        //	ItemStack storedItemType = ((IDeepStorageUnit) inv).getStoredItemType();
        //	if (ItemHelper.itemsIdentical(storedItemType, insertingItem)) {
        //		return storedItemType.stackSize;
        //	} else {
        //		return 0;
        //	}
        //}

        int storedNo = 0;

        for (int slot = 0; slot < inv.getSlots(); slot++) {
            ItemStack stackInSlot = inv.getStackInSlot(slot);
            if (ItemHelper.itemsIdentical(stackInSlot, insertingItem)) {
                storedNo += stackInSlot.stackSize;
                if (storedNo >= cap) {
                    return storedNo;
                }
            }
        }
        return storedNo;

    }

	public static ItemStack insertItemStackIntoInventory(IItemHandler inventory, ItemStack stack, int side, int cap) {

		if (cap < 0 || cap == Integer.MAX_VALUE) {
			return InventoryHelper.insertStackIntoInventory(inventory, stack, false);
		}
		int toInsert = cap - getNumItems(inventory, side, stack, cap);

		if (toInsert <= 0) {
			return stack;
		}
		if (stack.stackSize < toInsert) {
			return InventoryHelper.insertStackIntoInventory(inventory, stack, false);
		} else {
			ItemStack remaining = InventoryHelper.insertStackIntoInventory(inventory, stack.splitStack(toInsert), false);
			if (remaining != null) {
				stack.stackSize += remaining.stackSize;
			}
			return stack;
		}
	}

	public static ItemStack simulateInsertItemStackIntoInventory(IItemHandler inventory, ItemStack stack, int side, int cap) {

		if (cap < 0 || cap == Integer.MAX_VALUE) {
			return InventoryHelper.insertStackIntoInventory(inventory, stack, true);
		}

		int toInsert = cap - getNumItems(inventory, side, stack, cap);

		if (toInsert <= 0) {
			return stack;
		}
		if (stack.stackSize <= toInsert) {
			return InventoryHelper.insertStackIntoInventory(inventory, stack, true);
		} else {
			ItemStack remaining = InventoryHelper.insertStackIntoInventory(inventory, stack.splitStack(toInsert), true);
			if (remaining != null) {
				stack.stackSize += remaining.stackSize;
			}
			return stack;
		}
	}

}
