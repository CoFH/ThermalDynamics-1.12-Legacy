package thermaldynamics.ducts.item;

import cofh.api.inventory.IInventoryConnection;
import cofh.api.transport.IItemDuct;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.BlockHelper;
import com.google.common.collect.Iterables;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.core.TickHandlerClient;
import thermaldynamics.ducts.servo.IStuffable;
import thermaldynamics.ducts.servo.ServoItem;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.IMultiBlockRoute;
import thermaldynamics.multiblock.MultiBlockGrid;
import thermaldynamics.multiblock.RouteCache;
import thermalexpansion.util.Utils;

import java.util.LinkedList;
import java.util.List;

public class TileItemDuct extends TileMultiBlock implements IMultiBlockRoute, IItemDuct {
    ItemGrid internalGrid;

    public List<TravelingItem> myItems = new LinkedList<TravelingItem>();
    public List<TravelingItem> itemsToRemove = new LinkedList<TravelingItem>();
    public List<TravelingItem> itemsToAdd = new LinkedList<TravelingItem>();

    // Type Helper Arrays
    static int[] _PIPE_LEN = {40, 10, 60, 1};
    static int[] _PIPE_HALF_LEN = {_PIPE_LEN[0] / 2, _PIPE_LEN[1] / 2, _PIPE_LEN[2] / 2, 1};
    static float[] _PIPE_TICK_LEN = {1F / _PIPE_LEN[0], 1F / _PIPE_LEN[1], 1F / _PIPE_LEN[2], 1F / _PIPE_LEN[3]};

    static float[][][] _SIDE_MODS = new float[4][6][3];

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


    public IInventory[] cache = new IInventory[6];
    public ISidedInventory[] cache2 = new ISidedInventory[6];
    public CacheType[] cacheType = {CacheType.NONE, CacheType.NONE, CacheType.NONE, CacheType.NONE, CacheType.NONE, CacheType.NONE,};

    @Override
    public ItemStack insertItem(ForgeDirection from, ItemStack item) {
        if (!((neighborTypes[from.ordinal()] == NeighborTypes.INPUT) || (neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT && connectionTypes[from.ordinal()].allowTransfer)))
            return item;


        Attachment attachment = attachments[from.ordinal()];
        if (attachment == null || attachment.getID() != AttachmentRegistry.SERVO_INV) {
            return item;
        }

        return ((ServoItem) attachment).insertItem(item);
    }

    public static enum CacheType {
        NONE, IINV, ISIDEDINV
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
        return theTile instanceof IInventory &&
                (!(theTile instanceof IInventoryConnection)
                        || ((IInventoryConnection) theTile).canConnectInventory(ForgeDirection.VALID_DIRECTIONS[side ^ 1]) != IInventoryConnection.ConnectionType.DENY
                );
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
        if (!super.tickPass(pass)) return false;

        if (pass == 0) {
            tickItems();
        }
        return true;
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public boolean isOutput() {
        return isOutput;
    }

    @Override
    public boolean canStuffItem() {
        return isInput;
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
        return xCoord;
    }

    @Override
    public int y() {
        return yCoord;
    }

    @Override
    public int z() {
        return zCoord;
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

//    @Override
//    public boolean openGui(EntityPlayer player) {
//        if (!isOutput())
//            return false;
//
//        if (ServerHelper.isClientWorld(worldObj) || !isOutput())
//            return true;
//
//        LinkedList<Route> routes = internalGrid.getRoutesFromOutput(this).outputRoutes;
//
//        if (routes.size() <= 1)
//            return true;
//
//
//        for (Route route : routes) {
//            if (route.pathDirections.size() < 1)
//                continue;
//
//            byte input;
//            for (input = 0; input < 6 && neighborTypes[input ^ 1] != NeighborTypes.OUTPUT; ) input++;
//            byte output;
//            for (output = 0; output < 6 && ((TileItemDuct) route.endPoint).neighborTypes[output] != NeighborTypes.OUTPUT; )
//                output++;
//
//            Route itemRoute = route.copy();
//            itemRoute.pathDirections.add(output);
//            final TravelingItem travelingItem = new TravelingItem(new ItemStack(Blocks.glowstone), x(), y(), z(), itemRoute, input);
//            travelingItem.goingToStuff = true;
//            insertItem(travelingItem);
//
//            break;
//        }
////        player.addChatComponentMessage(new ChatComponentText("Routes: " + routes.size()));
//
//        return true;
//    }

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
            ((IStuffable) attachment).stuffItem(travelingItem.stack);
        }
    }

    public void insertItem(TravelingItem travelingItem) {
        itemsToAdd.add(travelingItem);
    }

    public IInventory getCachedTileEntity(byte direction) {
        return cache[direction];
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
        NBTTagList list = nbt.getTagList("TravellingItems", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            myItems.add(new TravelingItem(compound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        NBTTagList items = new NBTTagList();
        for (TravelingItem travelingItem : Iterables.concat(itemsToAdd, myItems)) {
            NBTTagCompound tag = new NBTTagCompound();
            travelingItem.toNBT(tag);
            items.appendTag(tag);
        }

        nbt.setTag("TravellingItems", items);
    }


    public void sendTravelingItemsPacket() {
        if (!getDuctType().opaque) {
            PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
            myPayload.addByte(0);
            myPayload.addByte(TileInfoPackets.TRAVELING_ITEMS);

            int loopStop = myItems.size();
            loopStop = Math.min(loopStop, PropsConduit.MAX_ITEMS_TRANSMITTED);
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
        cache[side] = (IInventory) tile;
        if (tile instanceof ISidedInventory) {
            cache2[side] = ((ISidedInventory) tile);
            cacheType[side] = CacheType.ISIDEDINV;
        } else {
            cacheType[side] = CacheType.IINV;
        }
    }

    @Override
    public void clearCache(int side) {
        cache[side] = null;
        cacheType[side] = CacheType.NONE;
    }

    public void
    removeItem(TravelingItem travelingItem) {
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
    public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {
        super.handleTilePacket(payload, isServer);
    }

    public void tickItemsClient() {
        if (centerLine > 0) {
            centerLine--;
            for (int i = 0; i < 6; i++) {
                if (centerLineSub[i] > 0) centerLineSub[i]--;
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

    public boolean isConnectable(TileEntity theTile, int side) {
        return theTile instanceof TileItemDuct;
    }


    public static final int maxCenterLine = 10;
    public int centerLine = 0;
    public int[] centerLineSub = new int[6];


    public RouteInfo canRouteItem(ItemStack anItem) {
        int[] coords;
        int stackSizeLeft;
        ItemStack curItem;
        for (byte i = internalSideCounter; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
            if (neighborTypes[i] == NeighborTypes.OUTPUT && connectionTypes[i].allowTransfer && itemPassesFiltering(i, anItem)) {
                coords = BlockHelper.getAdjacentCoordinatesForSide(x(), y(), z(), i);
                curItem = anItem.copy();
                curItem.stackSize = Math.min(getMoveStackSize(i), curItem.stackSize);
                if (curItem.stackSize > 0) {
                    stackSizeLeft = Utils.canAddToInventory(coords[0], coords[1], coords[2], world(), i, curItem.copy());
                    stackSizeLeft = (anItem.stackSize - curItem.stackSize) + stackSizeLeft;
                    if (stackSizeLeft < anItem.stackSize) {
                        tickInternalSideCounter(i + 1);
                        return new RouteInfo(stackSizeLeft, i);
                    }
                }
            }
        }
        for (byte i = 0; i < internalSideCounter; i++) {
            if (neighborTypes[i] == NeighborTypes.OUTPUT && connectionTypes[i].allowTransfer && itemPassesFiltering(i, anItem)) {
                coords = BlockHelper.getAdjacentCoordinatesForSide(x(), y(), z(), i);
                curItem = anItem.copy();
                curItem.stackSize = Math.min(getMoveStackSize(i), curItem.stackSize);
                if (curItem.stackSize > 0) {
                    stackSizeLeft = Utils.canAddToInventory(coords[0], coords[1], coords[2], world(), i, curItem.copy());
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

    @Override
    public byte getStuffedSide() {

        for (byte i = 0; i < 6; i++) {
            if (attachments[i] instanceof IStuffable) {
                if (((IStuffable) attachments[i]).canStuff())
                    return i;

            }
        }

        for (byte i = 0; i < 6; i++) {
            if (attachments[i] instanceof IStuffable) {
                return i;
            }
        }

        throw new RuntimeException("IStuffable disapeared during calculation");
    }

    @Override
    public boolean acceptingStuff() {
        for (byte i = 0; i < 6; i++) {
            if (attachments[i] instanceof IStuffable)
                return ((IStuffable) attachments[i]).canStuff();
        }
        return false;
    }

    private boolean stuffed() {
        return false;
    }

    private boolean itemPassesFiltering(byte i, ItemStack anItem) {
        return true;
    }

    public int getMoveStackSize(byte side) {
        return 64;
    }
}
