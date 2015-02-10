package cofh.thermaldynamics.ducts.attachments.servo;

import cofh.lib.util.helpers.ItemHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.core.TDProps;
import cofh.thermaldynamics.ducts.Duct;
import cofh.thermaldynamics.ducts.attachments.filter.FilterLogic;
import cofh.thermaldynamics.ducts.item.ItemGrid;
import cofh.thermaldynamics.ducts.item.TileItemDuct;
import cofh.thermaldynamics.ducts.item.TravelingItem;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;
import cofh.thermaldynamics.multiblock.listtypes.ListWrapper;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class ServoItem extends ServoBase {

	public static int[] range = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
	public static int[] maxSize = { 4, 16, 64, 64, 64 };
	public static boolean[] multiStack = { false, false, false, true, true };

	public LinkedList<ItemStack> stuffedItems = new LinkedList<ItemStack>();

    public TileItemDuct itemDuct;

	public ServoItem(TileMultiBlock tile, byte side, int type) {

		super(tile, side, type);
		itemDuct = ((TileItemDuct) tile);
	}

	public ServoItem(TileMultiBlock tile, byte side) {

		super(tile, side);
		itemDuct = ((TileItemDuct) tile);
	}

	@Override
	public int getId() {

		return AttachmentRegistry.SERVO_ITEM;
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
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		stuffedItems.clear();
		if (tag.hasKey("StuffedInv", 9)) {
			NBTTagList tlist = tag.getTagList("StuffedInv", 10);
			for (int j = 0; j < tlist.tagCount(); j++) {
				ItemStack item = ItemHelper.readItemStackFromNBT(tlist.getCompoundTagAt(j));
				if (item != null && item.getItem() != null)
					stuffedItems.add(item);
			}
		}
		stuffed = isStuffed();
	}

	@Override
	public boolean canStuff() {

		return true;
	}

	@Override
	public void stuffItem(ItemStack item) {

		for (ItemStack stuffed : stuffedItems) {
			if (ItemHelper.itemsEqualWithMetadata(item, stuffed, true)) {
				stuffed.stackSize += item.stackSize;
				if (stuffed.stackSize < 0)
					stuffed.stackSize = Integer.MAX_VALUE;
				return;
			}
		}

		stuffedItems.add(item.copy());
		onNeighborChange();
	}

	public RouteCache cache = null;
    public ListWrapper<Route> routeList = new ListWrapper<Route>();

	@Override
	public List<ItemStack> getDrops() {

		List<ItemStack> drops = super.getDrops();

		if (isStuffed())
			for (ItemStack stuffedItem : stuffedItems) {
				ItemStack stack = stuffedItem.copy();
				int m = stuffedItem.getMaxStackSize();
				for (int i = 0; stack.stackSize > 0 && i < TDProps.MAX_STUFFED_ITEMSTACKS_DROP; i++) {
					if (m < stack.stackSize)
						m = stack.stackSize;
					drops.add(ItemHelper.cloneStack(stack, m));
					stack.stackSize -= m;
				}
			}

		return drops;
	}

	public static int[] tickDelays = { 60, 40, 20, 10, 10 };
	public static byte[] speedBoost = { 1, 1, 1, 2, 3 };

	public int tickDelay() {

		return tickDelays[type];
	}

	@Override
	public boolean onWrenched() {

		Cuboid6 c = getCuboid();
		if (isStuffed()) {
			for (ItemStack stack : stuffedItems) {
				while (stack.stackSize > 0)
					dropItemStack(stack.splitStack(Math.min(stack.stackSize, stack.getMaxStackSize())));
			}
			stuffedItems.clear();
			onNeighborChange();
			return true;
		} else
			return super.onWrenched();
	}

	@Override
	public void tick(int pass) {

		if (pass == 0) {
			if (isPowered && (isValidInput || isStuffed()) && itemDuct.world().getTotalWorldTime() % tickDelay() == 0) {
				ItemGrid.toTick.add(this);
			}
			return;
		} else if (!isPowered || itemDuct.world().getTotalWorldTime() % tickDelay() != 0) {
            return;
        }

        if (!verifyCache()) return;

        if (cache.outputRoutes.isEmpty()) return;


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
        RouteCache cache1 = itemDuct.getCache(false);
        if (!cache1.isFinishedGenerating())
            return false;

        if (cache1 != cache) {
            cache = cache1;
            routeList.setList(cache.outputRoutes, getSortType());
        }
        return true;
    }

    public void handleItemSending() {
        if (cacheType == TileItemDuct.CacheType.ISIDEDINV) {
            int[] accessibleSlotsFromSide = cachedSidedInv.getAccessibleSlotsFromSide(side ^ 1);
            for (int i = 0; i < accessibleSlotsFromSide.length; i++) {
                int slot = accessibleSlotsFromSide[i];
                ItemStack itemStack = cachedSidedInv.getStackInSlot(slot);

                if (itemStack == null) {
                    continue;
                }
                itemStack = limitOutput(itemStack.copy(), cachedInv, slot, side);

                if (itemStack == null || itemStack.stackSize == 0 || !cachedSidedInv.canExtractItem(slot, itemStack, side ^ 1)) {
                    continue;
                }
                if (!filter.matchesFilter(itemStack)) {
                    continue;
                }
                TravelingItem travelingItem = getRouteForItem(itemStack);

                if (travelingItem == null) {
                    continue;
                }
                travelingItem.stack = cachedSidedInv.decrStackSize(slot, travelingItem.stack.stackSize);

                if (travelingItem.stack == null || travelingItem.stack.stackSize <= 0) {
                    cachedSidedInv.markDirty();
                    continue;
                }
                if (multiStack[type]) {
                    int totalSize = Math.min(travelingItem.stack.getMaxStackSize(), filter.getLevel(FilterLogic.levelStacksize));
                    if (travelingItem.stack.stackSize < totalSize) {
                        for (i++; i < accessibleSlotsFromSide.length && travelingItem.stack.stackSize < totalSize; i++) {
                            slot = accessibleSlotsFromSide[i];
                            itemStack = cachedSidedInv.getStackInSlot(slot);
                            if (ItemHelper.itemsEqualWithMetadata(travelingItem.stack, itemStack, true)
                                    && cachedSidedInv.canExtractItem(slot, itemStack, side ^ 1)) {
                                itemStack = cachedSidedInv.decrStackSize(slot, totalSize - travelingItem.stack.stackSize);
                                if (itemStack != null)
                                    travelingItem.stack.stackSize += itemStack.stackSize;
                            }
                        }
                    }
                }
                cachedSidedInv.markDirty();
                itemDuct.insertNewItem(travelingItem);
                return;
            }
        } else if (cacheType == TileItemDuct.CacheType.IINV) {
            for (int slot = 0; slot < cachedInv.getSizeInventory(); slot++) {
                ItemStack itemStack = cachedInv.getStackInSlot(slot);
                if (itemStack == null)
                    continue;

                itemStack = limitOutput(itemStack.copy(), cachedInv, slot, side);

                if (itemStack == null || itemStack.stackSize == 0) {
                    continue;
                }
                if (!filter.matchesFilter(itemStack)) {
                    continue;
                }
                TravelingItem travelingItem = getRouteForItem(itemStack);

                if (travelingItem == null) {
                    continue;
                }
                travelingItem.stack = cachedInv.decrStackSize(slot, travelingItem.stack.stackSize);

                if (travelingItem.stack == null || travelingItem.stack.stackSize <= 0) {
                    cachedInv.markDirty();
                    continue;
                }
                if (multiStack[type]) {
                    int totalSize = Math.min(travelingItem.stack.getMaxStackSize(), filter.getLevel(FilterLogic.levelStacksize));
                    if (multiStack[type] && travelingItem.stack.stackSize < totalSize) {
                        for (slot++; slot < cachedInv.getSizeInventory() && travelingItem.stack.stackSize < totalSize; slot++) {
                            itemStack = cachedInv.getStackInSlot(slot);
                            if (ItemHelper.itemsEqualWithMetadata(travelingItem.stack, itemStack, true)) {
                                itemStack = cachedInv.decrStackSize(slot, totalSize - travelingItem.stack.stackSize);
                                if (itemStack != null)
                                    travelingItem.stack.stackSize += itemStack.stackSize;
                            }
                        }
                    }
                }
                cachedInv.markDirty();
                itemDuct.insertNewItem(travelingItem);
                return;
            }
        }
    }

    public void handleStuffedItems() {
        for (Iterator<ItemStack> iterator = stuffedItems.iterator(); iterator.hasNext();) {
            ItemStack stuffedItem = iterator.next();
            ItemStack send = stuffedItem.copy();
            send.stackSize = Math.min(send.stackSize, send.getMaxStackSize());
            TravelingItem travelingItem = getRouteForItem(send);

            if (travelingItem == null) {
                continue;
            }
            stuffedItem.stackSize -= travelingItem.stack.stackSize;
            if (stuffedItem.stackSize <= 0)
                iterator.remove();

            itemDuct.insertNewItem(travelingItem);
            return;
        }
    }

    public byte getSpeed() {

		return speedBoost[type];
	}

	public static TravelingItem findRouteForItem(ItemStack item, Iterable<Route> routes, TileItemDuct duct, int side, int maxRange, byte speed) {

		if (item == null || item.stackSize == 0)
			return null;

		item = item.copy();

		if (item.stackSize == 0)
			return null;

		for (Route outputRoute : routes) {
			if (outputRoute.pathDirections.size() <= maxRange) {
				TileItemDuct.RouteInfo routeInfo = outputRoute.endPoint.canRouteItem(item);
				if (routeInfo.canRoute) {
					int stackSize = item.stackSize - routeInfo.stackSize;
					if (stackSize <= 0) {
						continue;
					}
					Route itemRoute = outputRoute.copy();
					itemRoute.pathDirections.add(routeInfo.side);
					item.stackSize -= routeInfo.stackSize;
					return new TravelingItem(item, duct, itemRoute, (byte) (side ^ 1), speed);
				}
			}
		}
		return null;
	}

	public int getMaxRange() {

		return range[type];
	}

	public ItemStack limitOutput(ItemStack itemStack, IInventory cachedInv, int slot, byte side) {

		itemStack.stackSize = Math.min(itemStack.stackSize, filter.getLevel(FilterLogic.levelStacksize));
		return itemStack;
	}

	@Override
	public void onNeighborChange() {

		if (stuffed != !stuffedItems.isEmpty()) {
			stuffed = isStuffed();
			tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
		}
		super.onNeighborChange();
	}

	@Override
	public boolean isStuffed() {

		return !stuffedItems.isEmpty();
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile instanceof IInventory;
	}

	@Override
	public void clearCache() {

		cacheType = TileItemDuct.CacheType.NONE;
		cachedInv = null;
		cachedSidedInv = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		cachedInv = (IInventory) tile;
		if (tile instanceof ISidedInventory) {
			cacheType = TileItemDuct.CacheType.ISIDEDINV;
			cachedSidedInv = (ISidedInventory) tile;
		} else
			cacheType = TileItemDuct.CacheType.IINV;
	}

	public IInventory cachedInv;
    public ISidedInventory cachedSidedInv;
    public TileItemDuct.CacheType cacheType;

	public ItemStack insertItem(ItemStack item) {

		if (!filter.matchesFilter(item))
			return item;

		ItemStack sending = limitOutput(item.copy(), null, -1, (byte) 0);
		TravelingItem routeForItem = getRouteForItem(sending);
		if (routeForItem == null) {
			return item;
		}
		itemDuct.insertNewItem(routeForItem);
		item.stackSize -= routeForItem.stack.stackSize;
		return item.stackSize > 0 ? item : null;
	}

	public TravelingItem getRouteForItem(ItemStack item) {

        if (!verifyCache()) return null;
		return ServoItem.findRouteForItem(item, routeList, itemDuct, side, getMaxRange(), getSpeed());
	}

	public ListWrapper.SortType getSortType() {

		int level = filter.getLevel(FilterLogic.levelRouteMode);
		return ListWrapper.SortType.values()[level];
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.ITEM, this);
	}

}
