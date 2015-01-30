package thermaldynamics.ducts.attachments.servo;

import cofh.lib.util.helpers.ItemHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.ducts.attachments.filter.FilterLogic;
import thermaldynamics.ducts.item.ItemGrid;
import thermaldynamics.ducts.item.PropsConduit;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TravelingItem;
import thermaldynamics.multiblock.Route;
import thermaldynamics.multiblock.RouteCache;
import thermaldynamics.multiblock.listtypes.ListWrapper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ServoItem extends ServoBase {
    LinkedList<ItemStack> stuffedItems = new LinkedList<ItemStack>();

    TileItemDuct itemDuct;

    public ServoItem(TileMultiBlock tile, byte side, int type) {
        super(tile, side, type);
        itemDuct = ((TileItemDuct) tile);

    }

    public ServoItem(TileMultiBlock tile, byte side) {
        super(tile, side);
        itemDuct = ((TileItemDuct) tile);
    }

    @Override
    public int getID() {
        return AttachmentRegistry.SERVO_INV;
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
            tag.setTag("stuffed", list);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        stuffedItems.clear();
        if (tag.hasKey("stuffed", 9)) {
            NBTTagList tlist = tag.getTagList("stuffed", 10);
            for (int j = 0; j < tlist.tagCount(); j++) {
                ItemStack item = ItemHelper.readItemStackFromNBT(tlist.getCompoundTagAt(j));
                if (item != null && item.getItem() != null)
                    stuffedItems.add(item);
            }
        }
        stuffed = isStuffed();


    }

    public boolean canStuff() {
        return true;
    }

    public void stuffItem(ItemStack item) {
        for (ItemStack stuffed : stuffedItems) {
            if (ItemHelper.itemsEqualWithMetadata(item, stuffed, true)) {
                stuffed.stackSize += item.stackSize;
                if (stuffed.stackSize < 0) stuffed.stackSize = Integer.MAX_VALUE;
                return;
            }
        }

        stuffedItems.add(item.copy());
        onNeighbourChange();
    }

    RouteCache cache = null;
    ListWrapper<Route> routeList = new ListWrapper<Route>();

    @Override
    public List<ItemStack> getDrops() {
        List<ItemStack> drops = super.getDrops();

        if (isStuffed())
            for (ItemStack stuffedItem : stuffedItems) {
                ItemStack stack = stuffedItem.copy();
                int m = stuffedItem.getMaxStackSize();
                for (int i = 0; stack.stackSize > 0 && i < PropsConduit.MAX_STUFFED_ITEMSTACKS_DROP; i++) {
                    if (m < stack.stackSize) m = stack.stackSize;
                    drops.add(ItemHelper.cloneStack(stack, m));
                    stack.stackSize -= m;
                }
            }

        return drops;
    }

    public static int[] tickDelays = {60, 40, 20, 10, 10};
    public static byte[] speedBoost = {1, 1, 1, 1, 2};

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
            onNeighbourChange();
            return true;
        } else
            return super.onWrenched();
    }

    @Override
    public void tick(int pass) {
        if (pass == 0) {
            if (isPowered && (isValidInput || isStuffed()) && itemDuct.world().getTotalWorldTime() % tickDelay() == 0)
                ItemGrid.toTick.add(this);
            return;
        } else if (!isPowered || itemDuct.world().getTotalWorldTime() % tickDelay() != 0) return;

        RouteCache cache1 = itemDuct.getCache(false);
        if (cache1 != cache) {
            cache = cache1;
            routeList.setList(cache.outputRoutes, getSortType());
        }


        if (cache.isFinishedGenerating() && cache.outputRoutes.isEmpty())
            return;

        if (pass == 1) {
            if (isStuffed()) {
                for (Iterator<ItemStack> iterator = stuffedItems.iterator(); iterator.hasNext(); ) {
                    ItemStack stuffedItem = iterator.next();
                    ItemStack send = stuffedItem.copy();
                    send.stackSize = Math.min(send.stackSize, send.getMaxStackSize());
                    TravelingItem travelingItem = getRouteForItem(send);

                    if (travelingItem == null) continue;

                    stuffedItem.stackSize -= travelingItem.stack.stackSize;
                    if (stuffedItem.stackSize <= 0)
                        iterator.remove();


                    itemDuct.insertNewItem(travelingItem);
                    return;
                }

            } else if (stuffed) {
                onNeighbourChange();
            }
        } else if (pass == 2 && !stuffed) {

            if (!isValidInput)
                return;

            if (cacheType == TileItemDuct.CacheType.ISIDEDINV) {
                int[] accessibleSlotsFromSide = cachedSidedInv.getAccessibleSlotsFromSide(side ^ 1);
                for (int i = 0; i < accessibleSlotsFromSide.length; i++) {
                    int slot = accessibleSlotsFromSide[i];
                    ItemStack itemStack = cachedSidedInv.getStackInSlot(slot);
                    if (itemStack == null)
                        continue;

                    itemStack = limitOutput(itemStack.copy(), cachedInv, slot, side);

                    if (itemStack == null || itemStack.stackSize == 0 || !cachedSidedInv.canExtractItem(slot, itemStack, side ^ 1))
                        continue;

                    if (!filter.matchesFilter(itemStack)) continue;

                    if (multiStack[type])
                        itemStack.stackSize = itemStack.getMaxStackSize();

                    TravelingItem travelingItem = getRouteForItem(itemStack);

                    if (travelingItem == null) continue;

                    travelingItem.stack = cachedSidedInv.decrStackSize(slot, travelingItem.stack.stackSize);


                    if (travelingItem.stack == null || travelingItem.stack.stackSize <= 0) {
                        cachedSidedInv.markDirty();
                        continue;
                    }

                    if (multiStack[type] && travelingItem.stack.stackSize < travelingItem.stack.getMaxStackSize()) {
                        int maxStackSize = travelingItem.stack.getMaxStackSize();
                        for (i++; i < accessibleSlotsFromSide.length && travelingItem.stack.stackSize < maxStackSize; i++) {
                            slot = accessibleSlotsFromSide[i];
                            itemStack = cachedSidedInv.getStackInSlot(slot);
                            if (ItemHelper.itemsEqualWithMetadata(travelingItem.stack, itemStack, true) && cachedSidedInv.canExtractItem(slot, itemStack, side ^ 1)) {
                                itemStack = cachedSidedInv.decrStackSize(slot, maxStackSize - travelingItem.stack.stackSize);
                                if (itemStack != null) travelingItem.stack.stackSize += itemStack.stackSize;
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
                    if (itemStack == null) continue;

                    itemStack = limitOutput(itemStack.copy(), cachedInv, slot, side);

                    if (itemStack == null || itemStack.stackSize == 0) continue;
                    if (!filter.matchesFilter(itemStack)) continue;

                    if (multiStack[type]) itemStack.stackSize = itemStack.getMaxStackSize();

                    TravelingItem travelingItem = getRouteForItem(itemStack);

                    if (travelingItem == null) continue;

                    travelingItem.stack = cachedInv.decrStackSize(slot, travelingItem.stack.stackSize);

                    if (travelingItem.stack == null || travelingItem.stack.stackSize <= 0) {
                        cachedInv.markDirty();
                        continue;
                    }

                    if (multiStack[type] && travelingItem.stack.stackSize < travelingItem.stack.getMaxStackSize()) {
                        for (slot++; slot < cachedInv.getSizeInventory()&& travelingItem.stack.stackSize < travelingItem.stack.getMaxStackSize(); slot++) {
                            itemStack = cachedInv.getStackInSlot(slot);
                            if (ItemHelper.itemsEqualWithMetadata(travelingItem.stack, itemStack, true)) {
                                itemStack = cachedInv.decrStackSize(slot, travelingItem.stack.getMaxStackSize() - travelingItem.stack.stackSize);
                                if (itemStack != null) travelingItem.stack.stackSize += itemStack.stackSize;
                            }
                        }
                    }

                    cachedInv.markDirty();


                    itemDuct.insertNewItem(travelingItem);
                    return;
                }
            }
        }
    }

    public byte getSpeed() {
        return speedBoost[type];
    }


    public static TravelingItem findRouteForItem(ItemStack item, Iterable<Route> routes, TileItemDuct duct, int side, int maxRange, byte speed) {
        if (item == null || item.stackSize == 0) return null;

        item = item.copy();

        if (item.stackSize == 0)
            return null;

        for (Route outputRoute : routes) {
            if (outputRoute.pathDirections.size() <= maxRange) {
                TileItemDuct.RouteInfo routeInfo = outputRoute.endPoint.canRouteItem(item);
                if (routeInfo.canRoute) {
                    int stackSize = item.stackSize - routeInfo.stackSize;


                    if (stackSize <= 0)
                        continue;

                    Route itemRoute = outputRoute.copy();
                    itemRoute.pathDirections.add(routeInfo.side);

                    item.stackSize -= routeInfo.stackSize;
                    return new TravelingItem(item, duct, itemRoute, (byte) (side ^ 1), speed);
                }
            }
        }

        return null;
    }

    public static int[] range = {4, 16, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
    public static boolean[] multiStack = {false, false, false, true, true};

    public int getMaxRange() {
        return range[type];
    }

    public static int[] maxSize = {1, 8, 64, 64, 64};

    public ItemStack limitOutput(ItemStack itemStack, IInventory cachedInv, int slot, byte side) {
        itemStack.stackSize = Math.min(itemStack.stackSize, maxSize[type]);
        return itemStack;
    }

    @Override
    public void onNeighbourChange() {
        if (stuffed != !stuffedItems.isEmpty()) {
            stuffed = isStuffed();
            tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
        }

        super.onNeighbourChange();
    }

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

    IInventory cachedInv;
    ISidedInventory cachedSidedInv;
    TileItemDuct.CacheType cacheType;


    public ItemStack insertItem(ItemStack item) {
        if (!filter.matchesFilter(item)) return item;

        ItemStack sending = limitOutput(item.copy(), null, -1, (byte) 0);
        TravelingItem routeForItem = getRouteForItem(sending);
        if (routeForItem == null)
            return item;

        itemDuct.insertNewItem(routeForItem);
        item.stackSize -= routeForItem.stack.stackSize;
        return item.stackSize > 0 ? item : null;
    }

    public TravelingItem getRouteForItem(ItemStack item) {
        RouteCache cache1 = itemDuct.getCache(false);
        if (cache1 != cache) {
            cache = cache1;
            routeList.setList(cache.outputRoutes, getSortType());
        }

        return ServoItem.findRouteForItem(item, routeList, itemDuct, side, getMaxRange(), getSpeed());
    }

    public ListWrapper.SortType getSortType() {
        int level = filter.getLevel(FilterLogic.levelRouteMode);
        return ListWrapper.SortType.values()[level];
    }


    @Override
    public FilterLogic createFilterLogic() {
        return new FilterLogic(type, Ducts.Type.Item, this);
    }

}
