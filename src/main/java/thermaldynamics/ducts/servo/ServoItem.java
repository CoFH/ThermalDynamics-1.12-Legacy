package thermaldynamics.ducts.servo;

import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ItemHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TravelingItem;
import thermaldynamics.multiblock.Route;
import thermaldynamics.multiblock.RouteCache;

import java.util.Iterator;
import java.util.LinkedList;

public class ServoItem extends ServoBase implements IStuffable {
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
        if (!stuffedItems.isEmpty()) {
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
        stuffed = !stuffedItems.isEmpty();
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

    public int tickDelay() {
        return 10;
    }

    @Override
    public void tick(int pass) {
        super.tick(pass);

        if (!isPowered())
            return;

        if (cache == null || cache.invalid)
            cache = itemDuct.getCache(false);

        if (itemDuct.world().getTotalWorldTime() % tickDelay() != 0)
            return;

        cache = itemDuct.getCache(false);

        if (cache.isFinishedGenerating() && cache.outputRoutes.isEmpty())
            return;

        if (!stuffedItems.isEmpty()) {
            for (Iterator<ItemStack> iterator = stuffedItems.iterator(); iterator.hasNext(); ) {
                ItemStack stuffedItem = iterator.next();
                ItemStack send = stuffedItem.copy();
                send.stackSize = Math.min(send.stackSize, send.getMaxStackSize());
                TravelingItem travelingItem = findRouteForItem(send, false);

                if (travelingItem == null) continue;

                stuffedItem.stackSize -= travelingItem.stack.stackSize;
                if (stuffedItem.stackSize <= 0)
                    iterator.remove();

                itemDuct.insertItem(travelingItem);
                return;
            }

            return;
        } else if (stuffed) {
            onNeighbourChange();
        }

        if (!isValidInput)
            return;

        TileEntity tileEntity = BlockHelper.getAdjacentTileEntity(tile, side);
        if (!(tileEntity instanceof IInventory))
            return;


        if (tileEntity instanceof ISidedInventory) {
            ISidedInventory cachedSidedInv = (ISidedInventory) tileEntity;
            for (int slot : cachedSidedInv.getAccessibleSlotsFromSide(side ^ 1)) {
                ItemStack itemStack = cachedSidedInv.getStackInSlot(slot);
                if (itemStack == null)
                    continue;

                itemStack = limitOutput(itemStack.copy(), cachedSidedInv, slot, side);

                if (itemStack == null || itemStack.stackSize == 0 || !cachedSidedInv.canExtractItem(slot, itemStack, side ^ 1))
                    continue;

                TravelingItem travelingItem = findRouteForItem(itemStack, false);

                if (travelingItem == null) continue;

                travelingItem.stack = cachedSidedInv.decrStackSize(slot, travelingItem.stack.stackSize);
                cachedSidedInv.markDirty();

                if (travelingItem.stack == null || travelingItem.stack.stackSize <= 0)
                    continue;

                itemDuct.insertItem(travelingItem);
                return;
            }
        } else {
            IInventory cachedInv = (IInventory) tileEntity;
            for (int slot = 0; slot < cachedInv.getSizeInventory(); slot++) {
                ItemStack itemStack = cachedInv.getStackInSlot(slot);
                if (itemStack == null) continue;

                itemStack = limitOutput(itemStack.copy(), cachedInv, slot, side);

                if (itemStack == null || itemStack.stackSize == 0) continue;

                TravelingItem travelingItem = findRouteForItem(itemStack, false);

                if (travelingItem == null) continue;

                travelingItem.stack = cachedInv.decrStackSize(slot, travelingItem.stack.stackSize);
                cachedInv.markDirty();

                if (travelingItem.stack == null || travelingItem.stack.stackSize <= 0)
                    continue;

                itemDuct.insertItem(travelingItem);
                return;
            }
        }
    }


    public TravelingItem findRouteForItem(ItemStack item, boolean infiniteRange) {
        if (!cache.isFinishedGenerating())
            cache.generateCache();

        for (Route outputRoute : cache.outputRoutes) {
            if (infiniteRange || outputRoute.pathDirections.size() <= getMaxRange()) {
                TileItemDuct.RouteInfo routeInfo = outputRoute.endPoint.canRouteItem(item);
                if (routeInfo.canRoute) {
                    Route itemRoute = outputRoute.copy();
                    itemRoute.pathDirections.add(routeInfo.side);
                    item = item.copy();
                    item.stackSize -= routeInfo.stackSize;
                    return new TravelingItem(item, itemDuct, itemRoute, (byte) (side ^ 1));
                }
            }
        }

        return null;
    }

    public int[] range = {4, 16, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

    public int getMaxRange() {
        return range[type];
    }

    public int[] maxSize = {1, 8, 64, 64, 64};

    public ItemStack limitOutput(ItemStack itemStack, IInventory cachedInv, int slot, byte side) {
        itemStack.stackSize = Math.min(itemStack.stackSize, maxSize[type]);
        return itemStack;
    }


    @Override
    public void onNeighbourChange() {
        if (stuffed != !stuffedItems.isEmpty()) {
            stuffed = !stuffedItems.isEmpty();
            tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
        }

        super.onNeighbourChange();
    }

    @Override
    public boolean isValidTile(TileEntity tile) {
        return tile instanceof IInventory;
    }

}
