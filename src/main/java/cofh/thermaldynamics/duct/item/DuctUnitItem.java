package cofh.thermaldynamics.duct.item;

import cofh.api.tileentity.IItemDuct;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.core.util.CrashHelper;
import cofh.lib.util.helpers.InventoryHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.*;
import cofh.thermaldynamics.duct.attachments.IStuffable;
import cofh.thermaldynamics.duct.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.duct.attachments.filter.IFilterItems;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.IDuctHolder;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.multiblock.IGridTileRoute;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;
import cofh.thermaldynamics.util.TickHandlerClient;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DuctUnitItem extends DuctUnit<DuctUnitItem, ItemGrid, DuctUnitItem.Cache> implements IGridTileRoute<DuctUnitItem, ItemGrid>, IItemDuct {
	public final static byte maxTicksExistedBeforeFindAlt = 2;
	public final static byte maxTicksExistedBeforeStuff = 6;
	public final static byte maxTicksExistedBeforeDump = 10;
	public static final int maxCenterLine = 10;
	// Type Helper Arrays
	static int[] _PIPE_LEN = {40, 10, 60, 40};
	static int[] _PIPE_HALF_LEN = {_PIPE_LEN[0] / 2, _PIPE_LEN[1] / 2, _PIPE_LEN[2] / 2, _PIPE_LEN[3] / 2};
	static float[] _PIPE_TICK_LEN = {1F / _PIPE_LEN[0], 1F / _PIPE_LEN[1], 1F / _PIPE_LEN[2], 1F / _PIPE_LEN[3]};
	static float[][][] _SIDE_MODS = new float[4][6][3];
	static int INSERT_SIZE = 8;

	static {
		for (int i = 0; i < 4; i++) {
			float j = _PIPE_TICK_LEN[i];
			_SIDE_MODS[i][0] = new float[]{0, -j, 0};
			_SIDE_MODS[i][1] = new float[]{0, j, 0};
			_SIDE_MODS[i][2] = new float[]{0, 0, -j};
			_SIDE_MODS[i][3] = new float[]{0, 0, j};
			_SIDE_MODS[i][4] = new float[]{-j, 0, 0};
			_SIDE_MODS[i][5] = new float[]{j, 0, 0};
		}
	}


	public byte internalSideCounter;
	public List<TravelingItem> myItems = new LinkedList<>();
	public List<TravelingItem> itemsToRemove = new LinkedList<>();
	public List<TravelingItem> itemsToAdd = new LinkedList<>();
	public byte pathWeightType = 0;
	public byte ticksExisted = 0;
	public boolean hasChanged = false;
	public int centerLine = 0;
	public int[] centerLineSub = new int[6];

	public DuctUnitItem(TileGrid parent, Duct duct) {
		super(parent, duct);
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

	@Override
	protected Cache[] createTileCaches() {
		return new Cache[6];
	}

	@Override
	protected DuctUnitItem[] createPipeCache() {
		return new DuctUnitItem[6];
	}

	@Override
	public boolean isOutput(int side) {
		Cache cache = tileCaches[side];
		return cache != null;
	}

	@Override
	public ItemStack insertItem(EnumFacing from, ItemStack item) {

		if (item == null) {
			return null;
		}
		int side = from.ordinal();
		if (!((isInput(side)) || (isOutput(side) && parent.getConnectionType(side).allowTransfer))) {
			return item;
		}
		if (grid == null) {
			return item;
		}
		Attachment attachment = parent.getAttachment(side);
		if (attachment != null && attachment.getId() == AttachmentRegistry.SERVO_ITEM) {
			return ((ServoItem) attachment).insertItem(item, false);
		} else {
			ItemStack itemCopy = ItemHelper.cloneStack(item);

			Cache cache = tileCaches[side];

			if (cache != null && cache.filter != null && !cache.filter.matchesFilter(item)) {
				return item;
			}

			RouteCache<DuctUnitItem, ItemGrid> routeCache = getCache(false);
			TravelingItem routeForItem = ServoItem.findRouteForItem(
					ItemHelper.cloneStack(item, Math.min(INSERT_SIZE, item.stackSize)),
					routeCache.outputRoutes, this, side, ServoItem.range[0], (byte) 1);
			if (routeForItem == null) {
				return item;
			}

			itemCopy.stackSize -= routeForItem.stack.stackSize;
			insertNewItem(routeForItem);
			return itemCopy.stackSize > 0 ? itemCopy : null;
		}
	}
//
//	/*
//	 * Should return true if theTile is significant to this multiblock
//	 *
//	 * IE: Inventory's to ItemDuct's
//	 */
//	@Override
//	public boolean isSignificantTile(TileEntity theTile, int side) {
//
//		if ((theTile instanceof IInventoryConnection)) {
//			IInventoryConnection.ConnectionType connectionType = ((IInventoryConnection) theTile).canConnectInventory(EnumFacing.VALUES[side ^ 1]);
//			if (connectionType == IInventoryConnection.ConnectionType.DENY) {
//				return false;
//			}
//			if (connectionType == IInventoryConnection.ConnectionType.FORCE) {
//				return true;
//			}
//		}
//		return theTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
//	}

	public Route getRoute(IGridTileRoute itemDuct) {

		for (Route outputRoute : getCache().outputRoutes) {
			if (outputRoute.endPoint == itemDuct || (outputRoute.endPoint.x() == itemDuct.x() && outputRoute.endPoint.y() == itemDuct.y() && outputRoute.endPoint.z() == itemDuct.z())) {
				return outputRoute;
			}
		}
		return null;
	}

	@Override
	public DuctToken<DuctUnitItem, ItemGrid, Cache> getToken() {
		return DuctToken.ITEMS;
	}

	@Override
	public ItemGrid createGrid() {

		return new ItemGrid(parent.getWorld());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitItem, ItemGrid, Cache> adjDuct, byte side) {
		return true;
	}

	@Nullable
	@Override
	public Cache cacheTile(@Nonnull TileEntity tile, byte side) {
		if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.values()[side ^ 1])) {
			return new Cache(tile, parent.getAttachment(side));
		}
		return null;
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}

		for (Attachment attachment : parent.getTickingAttachments(DuctToken.ITEMS)) {
			attachment.tick(pass);
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

		return isNode();
	}

	@Override
	public boolean canStuffItem() {
		for (Cache tileCach : tileCaches) {
			if (tileCach != null && tileCach.stuffableAttachment != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getMaxRange() {

		return Integer.MAX_VALUE;
	}

	@Override
	public ConnectionType getConnectionType(byte side) {
		return parent.getConnectionType(side);
	}

	@Override
	public DuctUnitItem getCachedTile(byte side) {
		return pipeCache[side];
	}

	@Override
	public boolean shouldRenderInPass(int pass) {

		return pass == 0 && (!myItems.isEmpty() || !itemsToAdd.isEmpty() || centerLine > 0);
	}

	public RouteCache<DuctUnitItem, ItemGrid> getCache() {

		return getCache(true);
	}

	public RouteCache<DuctUnitItem, ItemGrid> getCache(boolean urgent) {

		if (grid == null) {
			throw new IllegalStateException();
		}


		return urgent ? grid.getRoutesFromOutput(this) : grid.getRoutesFromOutputNonUrgent(this);
	}

	public void pulseLineDo(int dir) {

		if (!isOpaque()) {

			PacketTileInfo myPayload = newPacketTileInfo();
			myPayload.addByte(TileInfoPackets.PULSE_LINE);
			myPayload.addByte(dir);

			PacketHandler.sendToAllAround(myPayload, parent);
		}
	}

	public boolean isOpaque() {
		return false;
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

	public void stuffItem(TravelingItem travelingItem) {

		Attachment attachment = parent.getAttachment(travelingItem.direction);
		if (attachment instanceof IStuffable) {
			signalRepoll();
			((IStuffable) attachment).stuffItem(travelingItem.stack);
		}
	}

	public boolean acceptingItems() {

		return true;
	}

	public void insertNewItem(TravelingItem travelingItem) {

		grid.poll(travelingItem);
		transferItem(travelingItem);
	}

	public void transferItem(TravelingItem travelingItem) {

		itemsToAdd.add(travelingItem);
	}

	public void tickItems() {

		if (itemsToAdd.size() > 0) {
			myItems.addAll(itemsToAdd);
			itemsToAdd.clear();
			hasChanged = true;
		}
		if (myItems.size() > 0) {
			for (TravelingItem item : myItems) {
				item.tickForward(this);
				if (grid.repoll) {
					grid.poll(item);
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
			PacketTileInfo myPayload = newPacketTileInfo();
			myPayload.addByte(TileInfoPackets.TRAVELING_ITEMS);

			int loopStop = myItems.size();
			loopStop = Math.min(loopStop, TDProps.MAX_ITEMS_TRANSMITTED);
			myPayload.addByte(loopStop);
			for (int i = 0; i < loopStop; i++) {
				myItems.get(i).writePacket(myPayload);
			}

			PacketHandler.sendToAllAround(myPayload, parent);
		}
	}

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		int b = payload.getByte();
		handlePacketType(payload, b);
	}

//
//	@Override
//	public void createCaches() {
//
//		cache = new Cache();
//		cache.filterCache = new IFilterItems[]{IFilterItems.nullFilter, IFilterItems.nullFilter, IFilterItems.nullFilter, IFilterItems.nullFilter, IFilterItems.nullFilter, IFilterItems.nullFilter};
//		cache.handlerCache = new IItemHandler[6];
//		cache.cache3 = new IDeepStorageUnit[6];
//	}

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

//	@Override
//	public void cacheImportant(TileEntity tile, int side) {
//
//		Validate.notNull(cache);
//
//		if (tile instanceof IDeepStorageUnit) {
//			cache.cache3[side] = (IDeepStorageUnit) tile;
//		}
//		EnumFacing oppositeSide = EnumFacing.VALUES[side ^ 1];
//		IItemHandler capability = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, oppositeSide);
//		cache.handlerCache[side] = capability;
//
//		if (capability != null) {
//			for (EnumFacing facing : EnumFacing.values()) {
//				if (facing == oppositeSide) {
//					continue;
//				}
//				int bitMask = getSideEquivalencyMask(side, facing.ordinal());
//				if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) && tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) == capability) {
//					cache.handlerCacheEquivalencyBitSet |= bitMask;
//				} else {
//					cache.handlerCacheEquivalencyBitSet &= ~bitMask;
//				}
//			}
//		}
//
//		if (parent.getAttachment(side) instanceof IFilterAttachment) {
//			cache.filterCache[side] = ((IFilterAttachment) parent.getAttachment(side)).getItemFilter();
//		}
//	}

	public void removeItem(TravelingItem travelingItem, boolean disappearing) {

		if (disappearing) {
			signalRepoll();
		}
		itemsToRemove.add(travelingItem);
	}

	@Override
	public void writeToTilePacket(PacketCoFHBase payload) {
		payload.addByte(pathWeightType);
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload) {
		pathWeightType = payload.getByte();
	}

	@Override
	public void onPlaced(EntityLivingBase living, ItemStack stack) {
		super.onPlaced(living, stack);
//		if (stack.hasTagCompound()) {
//			byte b = stack.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT);
//			if (b == DuctItem.PATHWEIGHT_DENSE || b == DuctItem.PATHWEIGHT_VACUUM) {
//				pathWeightType = b;
//			}
//		}
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
	public ItemStack addNBTToItemStackDrop(ItemStack drop) {
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
			for (TravelingItem myItem : myItems) {
				myItem.tickClientForward(this);
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
	public RouteInfo canRouteItem(ItemStack anItem) {

		if (grid == null) {
			return RouteInfo.noRoute;
		}
		int stackSizeLeft;
		ItemStack curItem;

		for (byte i = internalSideCounter; i < EnumFacing.VALUES.length; i++) {
			if (isOutput(i) && parent.getConnectionType(i).allowTransfer && itemPassesFiltering(i, anItem) && tileCaches[i] != null) {
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
			if (isOutput(i) && parent.getConnectionType(i).allowTransfer && itemPassesFiltering(i, anItem) && tileCaches[i] != null) {
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
		return RouteInfo.noRoute;
	}

	public int simTransferI(int side, ItemStack insertingItem) {

		if (grid == null) {
			return 0;
		}
		Cache cache = tileCaches[side];
		if (cache == null) {
			return 0;
		}
		try {
			ItemStack itemStack = simTransfer(side, insertingItem);
			return itemStack == null ? 0 : itemStack.stackSize;
		} catch (Exception err) {
			IItemHandler handler = cache.getItemHandler(side ^ 1);

			CrashReport crashReport = CrashHelper.makeDetailedCrashReport(err, "Inserting", this, "Inserting Item", insertingItem, "Side", side, "Cache", handler, "Grid", grid);
			CrashHelper.addSurroundingDetails(crashReport, "ItemDuct", parent);
			CrashHelper.addInventoryContents(crashReport, "Destination Inventory", handler);
			throw new ReportedException(crashReport);
		}
	}

	public ItemStack simTransfer(int side, ItemStack insertingItem) {

		EnumFacing face = EnumFacing.VALUES[side];
		if (insertingItem == null) {
			return null;
		}

		Cache cache = tileCaches[side];

		if (grid == null || cache == null) {
			return insertingItem;
		}
		boolean routeItems = cache.filter.shouldIncRouteItems();

		int maxStock = cache.filter.getMaxStock();

		if (cache.dsuCache != null) { // IDeepStorage
			ItemStack cacheStack = cache.dsuCache.getStoredItemType();
			if (cacheStack != null && !ItemHelper.itemsIdentical(cacheStack, insertingItem)) {
				return insertingItem;
			}
			int s = cacheStack != null ? cacheStack.stackSize : 0;
			int m = Math.min(cache.dsuCache.getMaxStoredCount(), maxStock);

			if (s >= m) {
				return insertingItem;
			}
			if (routeItems) {
				StackMap travelingItems = grid.travelingItems.get(pos().offset(face));
				if (travelingItems != null && !travelingItems.isEmpty()) {
					for (Iterator<ItemStack> iterator = travelingItems.getItems(); s < m && iterator.hasNext(); ) {
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
			IItemHandler itemHandler = cache.getItemHandler(side ^ 1);
			if (!routeItems) {
				return simulateInsertItemStackIntoInventory(itemHandler, insertingItem, side ^ 1, maxStock);
			}

			StackMap travelingItems = grid.travelingItems.get(pos().offset(face));
			if (travelingItems == null || travelingItems.isEmpty()) {
				return simulateInsertItemStackIntoInventory(itemHandler, insertingItem, side ^ 1, maxStock);
			}
			if (travelingItems.size() == 1) {
				if (ItemHelper.itemsIdentical(insertingItem, travelingItems.getItems().next())) {
					insertingItem.stackSize += travelingItems.getItems().next().stackSize;
					return simulateInsertItemStackIntoInventory(itemHandler, insertingItem, side ^ 1, maxStock);
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
					return simulateInsertItemStackIntoInventory(itemHandler, insertingItem, side ^ 1, maxStock);
				}
			}

			SimulatedInv simulatedInv = SimulatedInv.wrapHandler(itemHandler);

			for (TObjectIntIterator<StackMap.ItemEntry> iterator = travelingItems.iterator(); iterator.hasNext(); ) {
				iterator.advance();

				StackMap.ItemEntry itemEntry = iterator.key();

				if (itemEntry.side != side && (cache.areEquivalentHandlers(itemHandler, itemEntry.side))) {
					continue;
				}

				if (InventoryHelper.insertStackIntoInventory(simulatedInv, itemEntry.toItemStack(iterator.value()), false) != null && ItemHelper.itemsIdentical(insertingItem, itemEntry.toItemStack(iterator.value()))) {
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
			Attachment attachment = parent.getAttachment(i);
			if (attachment instanceof IStuffable) {
				if (((IStuffable) attachment).canStuff()) {
					return i;
				}

			}
		}
		for (byte i = 0; i < 6; i++) {
			if (parent.getAttachment(i) instanceof IStuffable) {
				return i;
			}
		}
		throw new RuntimeException("IStuffable disappeared during calculation!");
	}

	@Override
	public boolean acceptingStuff() {

		for (byte i = 0; i < 6; i++) {
			if (parent.getAttachment(i) instanceof IStuffable) {
				return ((IStuffable) parent.getAttachment(i)).canStuff();
			}
		}
		return false;
	}

	private boolean stuffed() {

		return false;
	}

	private boolean itemPassesFiltering(byte i, ItemStack anItem) {
		Cache cache = tileCaches[i];
		return cache == null || cache.filter == null || cache.filter.matchesFilter(anItem);
	}

	public int getMoveStackSize(byte side) {

		return 64;
	}

	public int insertIntoInventory(ItemStack stack, int direction) {

		Cache cache = tileCaches[direction];
		if (cache == null) {
			return stack.stackSize;
		}
		if (!cache.filter.matchesFilter(stack)) {
			return stack.stackSize;
		}
		return insertIntoInventory_do(stack, direction);
	}

	public void signalRepoll() {

		if (grid != null) {
			grid.shouldRepoll = true;
		}
	}

	public int insertIntoInventory_do(ItemStack stack, int direction) {

		Cache cache = tileCaches[direction];
		IItemHandler itemHandler = cache.getItemHandler(direction ^ 1);
		if (itemHandler == null) {
			return stack.stackSize;
		}

		signalRepoll();
		stack = insertItemStackIntoInventory(itemHandler, stack, direction ^ 1, cache.filter.getMaxStock());
		return stack == null ? 0 : stack.stackSize;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public <CAP> CAP getCapability(Capability<CAP> capability, EnumFacing facing) {
		Attachment attachment = parent.getAttachment(facing.ordinal());
		if (attachment instanceof ServoItem) {
			ServoItem servo = (ServoItem) attachment;
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new IItemHandler() {

				@Override
				public int getSlots() {
					return 1;
				}

				@Override
				public ItemStack getStackInSlot(int slot) {
					return null;
				}

				@Override
				public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
					if (stack == null)
						return null;

					return servo.insertItem(stack, simulate);
				}

				@Override
				public ItemStack extractItem(int slot, int amount, boolean simulate) {
					return null;
				}
			});
		}

		return null;
	}

	public static class Cache {
		@Nonnull
		public final TileEntity tile;
		@Nonnull
		public final IFilterItems filter;
		@Nullable
		public final IDeepStorageUnit dsuCache;
		@Nullable
		public final IStuffable stuffableAttachment;

		public Cache(@Nonnull TileEntity tile, @Nullable Attachment attachment) {
			this.tile = tile;
			if (attachment instanceof IFilterAttachment) {
				filter = ((IFilterAttachment) attachment).getItemFilter();
			} else {
				filter = IFilterItems.nullFilter;
			}
			if (tile instanceof IDeepStorageUnit) {
				dsuCache = (IDeepStorageUnit) tile;
			} else {
				dsuCache = null;
			}
			if (attachment instanceof IStuffable) {
				this.stuffableAttachment = (IStuffable) attachment;
			} else {
				this.stuffableAttachment = null;
			}
		}

		public IItemHandler getItemHandler(int face) {
			return getItemHandler(EnumFacing.values()[face]);
		}

		public IItemHandler getItemHandler(EnumFacing face) {
			if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face)) {
				IItemHandler capability = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
				if (capability != null) {
					return capability;
				}
			}

			return EmptyHandler.INSTANCE;
		}

		public boolean areEquivalentHandlers(@Nonnull IItemHandler itemHandler, int side) {
			EnumFacing facing = EnumFacing.values()[side];
			return tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)
					&& itemHandler.equals(tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing));
		}
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
}
