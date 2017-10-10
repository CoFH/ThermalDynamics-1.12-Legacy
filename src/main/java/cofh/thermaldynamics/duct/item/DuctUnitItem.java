package cofh.thermaldynamics.duct.item;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.core.util.CrashHelper;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.attachments.IStuffable;
import cofh.thermaldynamics.duct.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.duct.attachments.filter.IFilterItems;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.DuctUnit;
import cofh.thermaldynamics.duct.tiles.IDuctHolder;
import cofh.thermaldynamics.duct.tiles.TileGrid;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DuctUnitItem extends DuctUnit<DuctUnitItem, GridItem, DuctUnitItem.Cache> implements IGridTileRoute<DuctUnitItem, GridItem> {

	public final static byte MAX_TICKS_EXISTED_BEFORE_FIND_ALT = 2;
	public final static byte MAX_TICKS_EXISTED_BEFORE_STUFF = 6;
	public final static byte MAX_TICKS_EXISTED_BEFORE_DUMP = 10;
	public static final int MAX_CENTER_LINE = 10;
	// Type Helper Arrays
	static int[] _DUCT_LEN = { 40, 10, 40, 10 };
	static int[] _DUCT_HALF_LEN = { _DUCT_LEN[0] / 2, _DUCT_LEN[1] / 2, _DUCT_LEN[2] / 2, _DUCT_LEN[3] / 2 };
	static float[] _DUCT_TICK_LEN = { 1F / _DUCT_LEN[0], 1F / _DUCT_LEN[1], 1F / _DUCT_LEN[2], 1F / _DUCT_LEN[3] };
	static float[][][] _SIDE_MODS = new float[4][6][3];
	static int INSERT_SIZE = 16;
	static boolean searching = false;

	static {
		for (int i = 0; i < 4; i++) {
			float j = _DUCT_TICK_LEN[i];
			_SIDE_MODS[i][0] = new float[] { 0, -j, 0 };
			_SIDE_MODS[i][1] = new float[] { 0, j, 0 };
			_SIDE_MODS[i][2] = new float[] { 0, 0, -j };
			_SIDE_MODS[i][3] = new float[] { 0, 0, j };
			_SIDE_MODS[i][4] = new float[] { -j, 0, 0 };
			_SIDE_MODS[i][5] = new float[] { j, 0, 0 };
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
				storedNo += stackInSlot.getCount();
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
		if (stack.getCount() < toInsert) {
			return InventoryHelper.insertStackIntoInventory(inventory, stack, false);
		} else {
			ItemStack remaining = InventoryHelper.insertStackIntoInventory(inventory, stack.splitStack(toInsert), false);
			if (!remaining.isEmpty()) {
				stack.grow(remaining.getCount());
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
		if (stack.getCount() <= toInsert) {
			return InventoryHelper.insertStackIntoInventory(inventory, stack, true);
		} else {
			ItemStack remaining = InventoryHelper.insertStackIntoInventory(inventory, stack.splitStack(toInsert), true);
			if (!remaining.isEmpty()) {
				stack.grow(remaining.getCount());
			}
			return stack;
		}
	}

	@Override
	protected Cache[] createTileCache() {

		return new Cache[6];
	}

	@Override
	protected DuctUnitItem[] createDuctCache() {

		return new DuctUnitItem[6];
	}

	@Override
	public boolean isOutput(int side) {

		Cache cache = tileCache[side];
		return cache != null;
	}

	@Override
	public boolean isInputTile(TileEntity tile, byte side) {

		return !(tile instanceof IDuctHolder) && parent.getAttachment(side) instanceof ServoItem;
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

	@Nonnull
	@Override
	public DuctToken<DuctUnitItem, GridItem, Cache> getToken() {

		return DuctToken.ITEMS;
	}

	@Override
	public GridItem createGrid() {

		return new GridItem(parent.getWorld());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitItem, GridItem, Cache> adjDuct, byte side, byte oppositeSide) {

		return true;
	}

	@Nullable
	@Override
	public Cache cacheTile(@Nonnull TileEntity tile, byte side) {

		Attachment attachment = parent.getAttachment(side);
		if (attachment != null && !attachment.allowDuctConnection()) {
			return null;
		}
		if (InventoryHelper.hasItemHandlerCap(tile, EnumFacing.values()[side ^ 1])) {
			return new Cache(tile, attachment);
		}
		return null;
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}

		if (parent.attachmentData != null) {
			for (Attachment attachment : parent.attachmentData.attachments) {
				if (attachment != null && attachment.tickUnit() == DuctToken.ITEMS) {
					attachment.tick(pass);
				}
			}
		}

		if (pass == 0) {
			if (ticksExisted < MAX_TICKS_EXISTED_BEFORE_DUMP) {
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

		TileGrid.AttachmentData attachmentData = parent.attachmentData;
		if (attachmentData != null) {
			for (Attachment attachment : attachmentData.attachments) {
				if (attachment instanceof IStuffable && ((IStuffable) attachment).canStuff()) {
					return true;
				}
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

		return ductCache[side];
	}

	@Override
	public boolean shouldRenderInPass(int pass) {

		return pass == 0 && (!myItems.isEmpty() || !itemsToAdd.isEmpty() || centerLine > 0);
	}

	public RouteCache<DuctUnitItem, GridItem> getCache() {

		return getCache(true);
	}

	public RouteCache<DuctUnitItem, GridItem> getCache(boolean urgent) {

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

	public int getDuctLength() {

		return _DUCT_LEN[getDuctType().type];
	}

	public int getPipeHalfLength() {

		return _DUCT_HALF_LEN[getDuctType().type];
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

	public ItemStack insertItem(EnumFacing from, ItemStack item, boolean simulate) {

		int side = from.ordinal();
		if (!((isInput(side)) || (isOutput(side) && parent.getConnectionType(side).allowTransfer))) {
			return item;
		}
		if (grid == null) {
			return item;
		}
		RouteCache<DuctUnitItem, GridItem> routeCache = getCache(false);
		TravelingItem routeForItem = ServoItem.findRouteForItem(ItemHelper.cloneStack(item, Math.min(INSERT_SIZE, item.getCount())), ServoItem.getRoutesWithDestinations(routeCache.outputRoutes).filter(t -> t.endPoint != this || t.getLastSide() != side).iterator(), this, side, ServoItem.range[0], (byte) 1);
		if (routeForItem == null) {
			return item;
		}
		if (!simulate) {
			insertNewItem(routeForItem);
		}
		return ItemHandlerHelper.copyStackWithSize(item, item.getCount() - routeForItem.stack.getCount());
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
				if (!travelingItem.stack.isEmpty()) {
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
					centerLineSub[i] = MAX_CENTER_LINE;
				}
			}
			centerLine = MAX_CENTER_LINE;
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
	@SideOnly (Side.CLIENT)
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
		ticksExisted = MAX_TICKS_EXISTED_BEFORE_DUMP;
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

	public int canRouteItem(ItemStack anItem, byte i) {

		if (grid == null) {
			return -1;
		}
		int stackSizeLeft;
		ItemStack curItem;

		if (isOutput(i) && parent.getConnectionType(i).allowTransfer && itemPassesFiltering(i, anItem) && tileCache[i] != null) {
			curItem = anItem.copy();
			curItem.setCount(Math.min(getMoveStackSize(i), curItem.getCount()));

			if (curItem.getCount() > 0) {
				stackSizeLeft = simTransferI(i, curItem.copy());
				stackSizeLeft = (anItem.getCount() - curItem.getCount()) + stackSizeLeft;

				if (stackSizeLeft < anItem.getCount()) {
					internalSideCounter = tickInternalSideCounter(i + 1);
					return stackSizeLeft;
				}
			}
		}

		return -1;
	}

	public int simTransferI(int side, ItemStack insertingItem) {

		if (grid == null) {
			return 0;
		}
		Cache cache = tileCache[side];

		if (cache == null) {
			return 0;
		}
		try {
			ItemStack itemStack = simTransfer(side, insertingItem);
			return itemStack.isEmpty() ? 0 : itemStack.getCount();
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

		if (insertingItem.isEmpty()) {
			return ItemStack.EMPTY;
		}
		Cache cache = tileCache[side];

		if (grid == null || cache == null) {
			return insertingItem;
		}
		boolean routeItems = cache.filter.shouldIncRouteItems();
		int maxStock = cache.filter.getMaxStock();

		if (cache.dsuCache != null) { // IDeepStorage
			ItemStack cacheStack = cache.dsuCache.getStoredItemType();

			if (!cacheStack.isEmpty() && !ItemHelper.itemsIdentical(cacheStack, insertingItem)) {
				return insertingItem;
			}
			int s = !cacheStack.isEmpty() ? cacheStack.getCount() : 0;
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

						if (cacheStack.isEmpty() && !equalsItem) {
							return insertingItem;
						}
						if (equalsItem) {
							s += travelingItem.getCount();
						}
					}
					if (s >= m) {
						return insertingItem;
					}
				}
			}
			insertingItem.shrink((m - s));

			if (insertingItem.getCount() <= 0) {
				return ItemStack.EMPTY;
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
					insertingItem.grow(travelingItems.getItems().next().getCount());
					return simulateInsertItemStackIntoInventory(itemHandler, insertingItem, side ^ 1, maxStock);
				}
			} else {
				int s = 0;
				for (ItemStack travelingItem : travelingItems.getItems()) {
					if (!ItemHelper.itemsIdentical(insertingItem, travelingItem)) {
						s = -1;
						break;
					} else {
						s += travelingItem.getCount();
					}
				}
				if (s >= 0) {
					insertingItem.grow(s);
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
				if (!InventoryHelper.insertStackIntoInventory(simulatedInv, itemEntry.toItemStack(iterator.value()), false).isEmpty() && ItemHelper.itemsIdentical(insertingItem, itemEntry.toItemStack(iterator.value()))) {
					return insertingItem;
				}
			}
			return simulateInsertItemStackIntoInventory(simulatedInv, insertingItem, side ^ 1, maxStock);
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

		Cache cache = tileCache[i];
		return cache == null || cache.filter == null || cache.filter.matchesFilter(anItem);
	}

	public int getMoveStackSize(byte side) {

		return 64;
	}

	public int insertIntoInventory(ItemStack stack, int direction) {

		Cache cache = tileCache[direction];
		if (cache == null) {
			return stack.getCount();
		}
		if (!cache.filter.matchesFilter(stack)) {
			return stack.getCount();
		}
		return insertIntoInventory_do(stack, direction);
	}

	public void signalRepoll() {

		if (grid != null) {
			grid.shouldRepoll = true;
		}
	}

	public int insertIntoInventory_do(ItemStack stack, int direction) {

		Cache cache = tileCache[direction];
		IItemHandler itemHandler = cache.getItemHandler(direction ^ 1);

		if (itemHandler == null) {
			return stack.getCount();
		}
		signalRepoll();
		stack = insertItemStackIntoInventory(itemHandler, stack, direction ^ 1, cache.filter.getMaxStock());
		return stack.isEmpty() ? 0 : stack.getCount();
	}

	/* CAPABILITIES */
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@Override
	public <CAP> CAP getCapability(Capability<CAP> capability, EnumFacing facing) {

		Attachment attachment = parent.getAttachment(facing.ordinal());
		if (attachment instanceof ServoItem) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast((ServoItem) attachment);
		}

		return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new IItemHandler() {

			@Override
			public int getSlots() {

				return 1;
			}

			@Nonnull
			@Override
			public ItemStack getStackInSlot(int slot) {

				return ItemStack.EMPTY;
			}

			@Nonnull
			@Override
			public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

				if (searching || stack.isEmpty()) {
					return stack;
				}
				try {
					searching = true;
					return DuctUnitItem.this.insertItem(facing, stack, simulate);
				} finally {
					searching = false;
				}
			}

			@Nonnull
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {

				return ItemStack.EMPTY;
			}

			@Override
			public int getSlotLimit(int slot) {

				return 64;
			}
		});
	}

	/* CACHE CLASS */
	public static class Cache {

		@Nonnull
		public final TileEntity tile;
		@Nonnull
		public final IFilterItems filter;
		@Nullable
		public final IDeepStorageUnit dsuCache;

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
		}

		public IItemHandler getItemHandler(int face) {

			return getItemHandler(EnumFacing.values()[face]);
		}

		public IItemHandler getItemHandler(EnumFacing face) {

			if (InventoryHelper.hasItemHandlerCap(tile, face)) {
				IItemHandler capability = InventoryHelper.getItemHandlerCap(tile, face);
				if (capability != null) {
					return capability;
				}
			}
			return EmptyHandler.INSTANCE;
		}

		public boolean areEquivalentHandlers(@Nonnull IItemHandler itemHandler, int side) {

			EnumFacing facing = EnumFacing.values()[side];
			return InventoryHelper.hasItemHandlerCap(tile, facing) && itemHandler.equals(InventoryHelper.getItemHandlerCap(tile, facing));
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
