package thermaldynamics.ducts.item;

import cofh.core.network.ITileInfoPacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.ServerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.core.TickHandlerClient;
import thermaldynamics.multiblock.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TileItemDuct extends TileMultiBlock implements IMultiBlockRoute, ITileInfoPacketHandler {
    final ItemDuct internalDuct;
    ItemGrid internalGrid;

    public List<TravelingItem> myItems = new LinkedList<TravelingItem>();
    public List<TravelingItem> itemsToRemove = new LinkedList<TravelingItem>();
    public List<TravelingItem> itemsToAdd = new LinkedList<TravelingItem>();

    // Type Helper Arrays
    static int[] _INPUT_TICK = {30, 30, 8, 8};
    static int[] _PIPE_LEN = {40, 40, 10, 10};
    static int[] _PIPE_HALF_LEN = {_PIPE_LEN[0] / 2, _PIPE_LEN[1] / 2, _PIPE_LEN[2] / 2, _PIPE_LEN[3] / 2};
    static float[] _PIPE_TICK_LEN = {0.025F, 0.025F, 0.10F, 0.10F};

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

    static boolean[] _RENDERS_ITEMS = {true, false, true, false};
    private int conduitType = 0;
    public CacheTypes[] cacheType = new CacheTypes[]{CacheTypes.IMPORTANT, CacheTypes.IMPORTANT, CacheTypes.IMPORTANT,
            CacheTypes.IMPORTANT, CacheTypes.IMPORTANT, CacheTypes.IMPORTANT};


    public TileItemDuct() {
        internalDuct = new ItemDuct(this);
    }

    /*
     * Should return true if theTile is significant to this multiblock
     *
     * IE: Inventory's to ItemDuct's
     */
    @Override
    public boolean isSignificantTile(TileEntity theTile, int side) {
        return internalDuct.isSignificantTile(theTile, side);
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

    public RouteCache cache;

    @Override
    public void tickPass(int pass) {
        tickItems();
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public boolean canStuffItem() {
        return false;
    }

    boolean wasVisited = false;


    @Override
    public boolean isOutput() {
        return isNode();
    }

    @Override
    public int getMaxRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public NeighborTypes getCachedSideType(byte side) {
        return neighborTypes[side];
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
    public byte getColor() {
        return 0;
    }


    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 ? !myItems.isEmpty() : centerLine > 0;
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        if (ServerHelper.isClientWorld(worldObj) || !isOutput())
            return true;

        LinkedList<Route> routes = internalGrid.getRoutesFromOutput(this);
//        if (routes.size() <= 1)
//            return true;

        Collections.shuffle(routes);
        for (Route route : routes) {

            Route itemRoute = route.copy();
            itemRoute.pathDirections.add((byte) 0);
            final TravelingItem travelingItem = new TravelingItem(new ItemStack(Blocks.glowstone), x(), y(), z(), itemRoute, (byte) 1);
            travelingItem.goingToStuff = true;
            insertItem(travelingItem);

            route = route.copy();
            route.pathDirections.add((byte) 0);

            TileItemDuct duct = this;
            byte direction = route.getNextDirection();
            byte oldDirection = 1;

            while (true) {
                duct.pulseLine(direction, (byte) (oldDirection ^ 1));
                if (duct.neighborTypes[direction] == NeighborTypes.MULTIBLOCK) {
                    TileItemDuct newHome = (TileItemDuct) duct.getConnectedSide(direction);
                    if (newHome != null) {
                        if (newHome.neighborTypes[direction ^ 1] == NeighborTypes.MULTIBLOCK) {
                            duct = newHome;
                            if (route.hasNextDirection()) {
                                oldDirection = direction;
                                direction = route.getNextDirection();
                            } else break;
                        } else break;
                    } else
                        break;
                } else
                    break;
            }

            break;
        }
        player.addChatComponentMessage(new ChatComponentText("Routes: " + routes.size()));

        return true;
    }

    public void pulseLineDo(int dir) {
        if (_RENDERS_ITEMS[conduitType]) {
            PacketTileInfo myPayload = PacketTileInfo.newPacket(this);

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
        return _PIPE_LEN[conduitType];
    }

    public int getPipeHalfLength() {

        return _PIPE_HALF_LEN[conduitType];
    }

    public float[][] getSideCoordsModifier() {

        return _SIDE_MODS[conduitType];
    }

    public void stuffItem(TravelingItem travelingItem) {

    }

    public void insertItem(TravelingItem travelingItem) {
        itemsToAdd.add(travelingItem);
    }

    public TileEntity getCachedTileEntity(byte direction) {
        return null;
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

    public void sendTravelingItemsPacket() {
        if (_RENDERS_ITEMS[conduitType]) {
            PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
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
    public void handleTileInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {
        myItems.clear();
        int b = payload.getByte();
        if (b == TileInfoPackets.PULSE_LINE) {
            if (centerLine == 0) centerLineMask = 0;
            centerLineMask = centerLineMask | payload.getByte();
            centerLine = maxCenterLine;
            if (!TickHandlerClient.tickBlocks.contains(this) && !TickHandlerClient.tickBlocksToAdd.contains(this)) {
                TickHandlerClient.tickBlocksToAdd.add(this);
            }
        } else if (b == TileInfoPackets.TRAVELING_ITEMS) {
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

    public class TileInfoPackets {

        public static final byte GUI_BUTTON = 0;
        public static final byte STUFFED_UPDATE = 1;
        public static final byte TRAVELING_ITEMS = 2;
        public static final byte STUFFED_ITEMS = 3;
        public static final byte REQUEST_STUFFED_ITEMS = 4;
        public static final byte PULSE_LINE = 5;
    }


    @Override
    public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {
        super.handleTilePacket(payload, isServer);
    }

    public void tickItemsClient() {
        if (centerLine > 0) {
            centerLine--;
        }

        if (itemsToAdd.size() > 0) {
            myItems.clear();
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

    public static final int maxCenterLine = 10;
    public int centerLine = 0;
    public int centerLineMask = 0;
}
