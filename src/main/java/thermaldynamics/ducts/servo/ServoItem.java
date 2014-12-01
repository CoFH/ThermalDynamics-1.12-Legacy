package thermaldynamics.ducts.servo;

import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.item.PropsConduit;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TravelingItem;
import thermaldynamics.gui.containers.ContainerServo;
import thermaldynamics.gui.gui.GuiServo;
import thermaldynamics.multiblock.Route;
import thermaldynamics.multiblock.RouteCache;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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

    @Override
    public List<ItemStack> getDrops() {
        List<ItemStack> drops = super.getDrops();

        if (!stuffedItems.isEmpty())
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

    public int[] tickDelays = {60, 40, 20, 20, 20};
    public byte[] speedBoost = {1, 1, 1, 1, 2};

    public int tickDelay() {
        return tickDelays[type];
    }

    @Override
    public void tick(int pass) {
        super.tick(pass);

        if (!isPowered)
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
                TravelingItem travelingItem = getRouteForItem(send);

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

        if (cacheType == TileItemDuct.CacheType.ISIDEDINV) {
            for (int slot : cachedSidedInv.getAccessibleSlotsFromSide(side ^ 1)) {
                ItemStack itemStack = cachedSidedInv.getStackInSlot(slot);
                if (itemStack == null)
                    continue;

                itemStack = limitOutput(itemStack.copy(), cachedInv, slot, side);

                if (itemStack == null || itemStack.stackSize == 0 || !cachedSidedInv.canExtractItem(slot, itemStack, side ^ 1))
                    continue;

                TravelingItem travelingItem = getRouteForItem(itemStack);

                if (travelingItem == null) continue;

                travelingItem.stack = cachedSidedInv.decrStackSize(slot, travelingItem.stack.stackSize);
                cachedSidedInv.markDirty();

                if (travelingItem.stack == null || travelingItem.stack.stackSize <= 0)
                    continue;

                itemDuct.insertItem(travelingItem);
                return;
            }
        } else if (cacheType == TileItemDuct.CacheType.IINV) {
            for (int slot = 0; slot < cachedInv.getSizeInventory(); slot++) {
                ItemStack itemStack = cachedInv.getStackInSlot(slot);
                if (itemStack == null) continue;

                itemStack = limitOutput(itemStack.copy(), cachedInv, slot, side);

                if (itemStack == null || itemStack.stackSize == 0) continue;

                TravelingItem travelingItem = getRouteForItem(itemStack);

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

    public byte getSpeed() {
        return speedBoost[type];
    }


    public static TravelingItem findRouteForItem(ItemStack item, TileItemDuct duct, int side, int maxRange, byte speed) {
        RouteCache routeCache = duct.getCache(true);

        for (Route outputRoute : routeCache.outputRoutes) {
            if (outputRoute.pathDirections.size() <= maxRange) {
                TileItemDuct.RouteInfo routeInfo = outputRoute.endPoint.canRouteItem(item);
                if (routeInfo.canRoute) {
                    Route itemRoute = outputRoute.copy();
                    itemRoute.pathDirections.add(routeInfo.side);
                    item = item.copy();
                    item.stackSize -= routeInfo.stackSize;
                    return new TravelingItem(item, duct, itemRoute, (byte) (side ^ 1), speed);
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


    @Override
    public void sendGuiNetworkData(Container container, ICrafting player) {
        super.sendGuiNetworkData(container, player);
    }

    @Override
    public void receiveGuiNetworkData(int i, int j) {
        super.receiveGuiNetworkData(i, j);
    }


    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerServo(inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiServo(inventory, this);
    }

    public ItemStack insertItem(ItemStack item) {
        TravelingItem routeForItem = getRouteForItem(item);
        if (routeForItem == null)
            return item;

        itemDuct.insertItem(routeForItem);
        item.stackSize -= routeForItem.stack.stackSize;
        return item.stackSize > 0 ? item : null;
    }

    public TravelingItem getRouteForItem(ItemStack item) {
        return ServoItem.findRouteForItem(item, itemDuct, side, getMaxRange(), getSpeed());
    }
}
