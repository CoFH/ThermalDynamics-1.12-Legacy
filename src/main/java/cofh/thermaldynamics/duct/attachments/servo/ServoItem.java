package cofh.thermaldynamics.duct.attachments.servo;

import cofh.core.util.helpers.BlockHelper;
import cofh.core.util.helpers.InventoryHelper;
import cofh.core.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.GridItem;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;
import cofh.thermaldynamics.util.ListWrapper;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ServoItem extends ServoBase implements IItemHandler {

	public static int[] range = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
	public static int[] maxSize = { 8, 16, 32, 64, 64 };
	public static boolean[] multiStack = { false, false, false, true, true };

	public static int[] tickDelays = { 60, 40, 20, 10, 10 };
	public static byte[] speedBoost = { 1, 1, 1, 2, 3 };

	public RouteCache<DuctUnitItem, GridItem> cache = null;
	public ListWrapper<Route<DuctUnitItem, GridItem>> routesWithInsertSideList = new ListWrapper<>();

	public LinkedList<ItemStack> stuffedItems = new LinkedList<>();

	public DuctUnitItem itemDuct;

	public ServoItem(TileGrid tile, byte side, int type) {

		super(tile, side, type);
		itemDuct = tile.getDuct(DuctToken.ITEMS);
	}

	public ServoItem(TileGrid tile, byte side) {

		super(tile, side);
		itemDuct = tile.getDuct(DuctToken.ITEMS);
	}

	@Override
	public String getInfo() {

		return "tab.thermaldynamics.servoItem";
	}

	public static Stream<Route<DuctUnitItem, GridItem>> getRoutesWithDestinations(Collection<Route<DuctUnitItem, GridItem>> outputRoutes) {

		return outputRoutes.stream().flatMap(route -> IntStream.range(0, 6).filter(i -> route.endPoint.isOutput(i) && route.endPoint.getConnectionType((byte) i).allowTransfer && route.endPoint.tileCache[i] != null).mapToObj(i -> {
			Route<DuctUnitItem, GridItem> ductUnitItemGridItemRoute = new Route<>(route);
			ductUnitItemGridItemRoute.pathDirections.add((byte) i);
			return ductUnitItemGridItemRoute;
		}));
	}

	@Override
	public ResourceLocation getId() {

		return AttachmentRegistry.SERVO_ITEM;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		stuffedItems.clear();

		if (tag.hasKey("StuffedInv", 9)) {
			NBTTagList tlist = tag.getTagList("StuffedInv", 10);
			for (int j = 0; j < tlist.tagCount(); j++) {
				ItemStack item = ItemHelper.readItemStackFromNBT(tlist.getCompoundTagAt(j));
				if (!item.isEmpty()) {
					stuffedItems.add(item);
				}
			}
		}
		stuffed = isStuffed();
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {

		super.writeToNBT(tag);

		if (isStuffed()) {
			NBTTagList list = new NBTTagList();
			for (ItemStack item : stuffedItems) {
				NBTTagCompound newTag = new NBTTagCompound();
				ItemHelper.writeItemStackToNBT(item, newTag);
				list.appendTag(newTag);
			}
			tag.setTag("StuffedInv", list);
		}
	}

	@Override
	public boolean canStuff() {

		return true;
	}

	@Override
	public void stuffItem(ItemStack item) {

		for (ItemStack stuffed : stuffedItems) {
			if (ItemHelper.itemsEqualWithMetadata(item, stuffed, true)) {
				stuffed.grow(item.getCount());

				if (stuffed.getCount() < 0) {
					stuffed.setCount(Integer.MAX_VALUE);
				}
				return;
			}
		}
		stuffedItems.add(item.copy());
		onNeighborChange();
	}

	@Override
	public List<ItemStack> getDrops() {

		List<ItemStack> drops = super.getDrops();

		if (isStuffed()) {
			for (ItemStack stuffedItem : stuffedItems) {
				ItemStack stack = stuffedItem.copy();
				while (stack.getCount() > 0 && drops.size() <= TDProps.MAX_STUFFED_ITEMSTACKS_DROP) {
					if (stack.getCount() <= stuffedItem.getMaxStackSize()) {
						drops.add(ItemHelper.cloneStack(stack));
						break;
					} else {
						drops.add(stack.splitStack(stuffedItem.getMaxStackSize()));
					}
				}
			}
		}

		return drops;
	}

	public int tickDelay() {

		return tickDelays[type];
	}

	@Override
	public boolean onWrenched() {

		if (isStuffed()) {
			for (ItemStack stack : stuffedItems) {
				while (stack.getCount() > 0) {
					dropItemStack(stack.splitStack(Math.min(stack.getCount(), stack.getMaxStackSize())));
				}
			}
			stuffedItems.clear();
			onNeighborChange();
			return true;
		} else {
			return super.onWrenched();
		}
	}

	@Override
	public void tick(int pass) {

		if (pass == 0) {
			if (isPowered && (isValidInput || isStuffed()) && itemDuct.world().getTotalWorldTime() % tickDelay() == 0) {
				GridItem.toTick.add(this);
			}
			return;
		} else if (!isPowered || itemDuct.world().getTotalWorldTime() % tickDelay() != 0) {
			return;
		}
		if (!verifyCache()) {
			return;
		}
		if (cache.outputRoutes.isEmpty()) {
			return;
		}
		if (pass == 1) {
			if (isStuffed()) {
				handleStuffedItems();
			} else if (stuffed) {
				onNeighborChange();
			}
		} else if (pass == 2 && !stuffed) {
			if (!isValidInput) {
				return;
			}
			handleItemSending();
		}
	}

	public boolean verifyCache() {

		if (itemDuct.getGrid() == null) {
			return false;
		}
		RouteCache<DuctUnitItem, GridItem> cache1 = itemDuct.getCache(false);
		if (!cache1.isFinishedGenerating()) {
			return false;
		}
		if (cache1 != cache || routesWithInsertSideList.type != getSortType()) {
			cache = cache1;
			Stream<Route<DuctUnitItem, GridItem>> routesWithDestinations = getRoutesWithDestinations(cache.outputRoutes);
			LinkedList<Route<DuctUnitItem, GridItem>> objects = Lists.newLinkedList();
			routesWithDestinations.forEach(objects::add);
			routesWithInsertSideList.setList(objects, getSortType());
		}
		return true;
	}

	public void handleItemSending() {

		if (getCachedInv() != EmptyHandler.INSTANCE) {
			for (int slot = 0; slot < getCachedInv().getSlots(); slot++) {
				ItemStack item = getCachedInv().getStackInSlot(slot);
				if (item.isEmpty()) {
					continue;
				}
				item = limitOutput(ItemHelper.cloneStack(item, multiStack[type] ? item.getMaxStackSize() : item.getCount()));
				if (item.isEmpty()) {
					continue;
				}
				if (!filter.matchesFilter(item)) {
					continue;
				}
				TravelingItem travelingItem = getRouteForItem(item);
				if (travelingItem == null) {
					continue;
				}
				int totalSendSize = travelingItem.stack.getCount();
				travelingItem.stack = getCachedInv().extractItem(slot, travelingItem.stack.getCount(), false);

				if (travelingItem.stack.isEmpty()) {
					continue;
				}
				if (multiStack[type]) {
					if (travelingItem.stack.getCount() < totalSendSize) {
						for (slot++; slot < getCachedInv().getSlots() && travelingItem.stack.getCount() < totalSendSize; slot++) {
							item = getCachedInv().getStackInSlot(slot);
							if (ItemHelper.itemsEqualWithMetadata(travelingItem.stack, item, true)) {
								item = getCachedInv().extractItem(slot, totalSendSize - travelingItem.stack.getCount(), false);
								if (!item.isEmpty()) {
									travelingItem.stack.grow(item.getCount());
								}
							}
						}
					}
				}
				itemDuct.insertNewItem(travelingItem);
				routesWithInsertSideList.advanceCursor();
				return;
			}
		}
	}

	public void handleStuffedItems() {

		for (Iterator<ItemStack> iterator = stuffedItems.iterator(); iterator.hasNext(); ) {
			ItemStack stuffedItem = iterator.next();
			ItemStack send = stuffedItem.copy();
			send.setCount(Math.min(send.getCount(), send.getMaxStackSize()));
			TravelingItem travelingItem = getRouteForItem(send);

			if (travelingItem == null) {
				continue;
			}
			stuffedItem.shrink(travelingItem.stack.getCount());

			if (stuffedItem.getCount() <= 0) {
				iterator.remove();
			}
			itemDuct.insertNewItem(travelingItem);
			routesWithInsertSideList.advanceCursor();
			return;
		}
	}

	public byte getSpeed() {

		return speedBoost[type];
	}

	public static TravelingItem findRouteForItem(ItemStack item, Iterator<Route<DuctUnitItem, GridItem>> routes, DuctUnitItem duct, int side, int maxRange, byte speed) {

		if (item.isEmpty() || item.getCount() == 0) {
			return null;
		}
		item = item.copy();

		if (item.getCount() == 0) {
			return null;
		}
		while (routes.hasNext()) {
			Route<DuctUnitItem, GridItem> outputRoute = routes.next();
			if (outputRoute.pathDirections.size() <= maxRange) {
				int amountRemaining = outputRoute.endPoint.canRouteItem(item, outputRoute.getLastSide());

				if (amountRemaining != -1) {
					int stackSize = item.getCount() - amountRemaining;

					if (stackSize <= 0) {
						continue;
					}
					Route itemRoute = outputRoute.copy();
					item.setCount(stackSize);
					return new TravelingItem(item, duct, itemRoute, (byte) (side ^ 1), speed);
				}
			}
		}
		return null;
	}

	public int getMaxRange() {

		return range[type];
	}

	public int getMaxSend() {

		return filter.getLevel(FilterLogic.levelStackSize);
	}

	public ItemStack limitOutput(ItemStack itemStack) {

		itemStack.setCount(Math.min(itemStack.getCount(), getMaxSend()));
		return itemStack;
	}

	@Override
	public void onNeighborChange() {

		if (stuffed != isStuffed()) {
			stuffed = isStuffed();
			BlockHelper.callBlockUpdate(baseTile.getWorld(), baseTile.getPos());
		}
		super.onNeighborChange();
	}

	@Override
	public DuctToken tickUnit() {

		return DuctToken.ITEMS;
	}

	@Override
	public boolean isStuffed() {

		return !stuffedItems.isEmpty();
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public void clearCache() {

		this.myTile = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		this.myTile = tile;
	}

	public ItemStack insertItem(ItemStack item, boolean simulate) {

		if (!filter.matchesFilter(item)) {
			return item;
		}
		ItemStack sending = limitOutput(ItemHelper.cloneStack(item));
		TravelingItem routeForItem = getRouteForItem(sending);

		if (routeForItem == null) {
			return item;
		}
		if (!simulate) {
			itemDuct.insertNewItem(routeForItem);
			routesWithInsertSideList.advanceCursor();
		}
		return ItemHelper.cloneStack(item, item.getCount() - routeForItem.stack.getCount());
	}

	public TravelingItem getRouteForItem(ItemStack item) {

		if (!verifyCache()) {
			return null;
		}
		return ServoItem.findRouteForItem(item, routesWithInsertSideList.iterator(), itemDuct, side, getMaxRange(), getSpeed());
	}

	public ListWrapper.SortType getSortType() {

		int level = filter.getLevel(FilterLogic.levelRouteMode);
		return ListWrapper.SortType.values()[level];
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.ITEM, this);
	}

	public IItemHandler getCachedInv() {

		if (myTile == null) {
			return EmptyHandler.INSTANCE;
		}
		IItemHandler handler = InventoryHelper.getItemHandlerCap(myTile, EnumFacing.VALUES[side ^ 1]);
		return handler == null ? EmptyHandler.INSTANCE : handler;
	}

	@Override
	public int getSlots() {

		return 1;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {

		return ItemStack.EMPTY;
	}

	boolean searching = false;

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

		if (searching) {
			return stack;
		}
		try {
			searching = true;
			return insertItem(stack, simulate);
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
}
