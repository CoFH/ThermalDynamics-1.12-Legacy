package thermaldynamics.ducts.item;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.ServerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.core.TickHandlerClient;
import thermaldynamics.debughelper.DebugHelper;
import thermaldynamics.multiblock.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TileItemDuct extends TileMultiBlock implements IMultiBlockRoute {
    final ItemDuct internalDuct;
    ItemGrid internalGrid;
    private boolean wasOutputFound;
    private int pipeHalfLength;
    private float[][] sideCoordsModifier;

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
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 && !myItems.isEmpty();
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        if (ServerHelper.isClientWorld(worldObj) || !isOutput())
            return true;

        LinkedList<Route> routes = internalGrid.getRoutesFromOutput(this);
        if (routes.size() <= 1)
            return true;

        Collections.shuffle(routes);
        for (Route route : routes) {
            if (route.pathDirections.size() == 0)
                continue;
            Route itemRoute = route.copy();
            itemRoute.pathDirections.add((byte) 0);
            final TravelingItem travelingItem = new TravelingItem(new ItemStack(Items.stick), x(), y(), z(), itemRoute, (byte) 1);
            travelingItem.goingToStuff = true;
            insertItem(travelingItem);
            break;
//            double r = player.worldObj.rand.nextDouble(),
//                    g = player.worldObj.rand.nextDouble(),
//                    b = player.worldObj.rand.nextDouble();
//
//            double m = r > g ? b > r ? b : r : b > g ? b : g;
//            r = r / m;
//            g = g / m;
//            b = b / m;
//
//            double dx = player.worldObj.rand.nextDouble() * 0.5 + 0.25,
//                    dy = player.worldObj.rand.nextDouble() * 0.5 + 0.25,
//                    dz = player.worldObj.rand.nextDouble() * 0.5 + 0.25;
//
//            route = route.copy();
//            BlockPosition pos = new BlockPosition(xCoord, yCoord, zCoord);
//
//            while (route.hasNextDirection()) {
//                ForgeDirection dir = ForgeDirection.getOrientation(route.getNextDirection());
//                for (float f = 0; f < 1; f += 0.2)
//                    Minecraft.getMinecraft().theWorld.spawnParticle("reddust", pos.x + dx + dir.offsetX * f, pos.y + dy + dir.offsetY * f, pos.z + dz + dir.offsetZ * f, r, g, b);
//                pos.step(dir);
//
//            }
        }
        player.addChatComponentMessage(new ChatComponentText("Routes: " + routes.size()));

        return true;
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

            for (TravelingItem item : myItems) {
                DebugHelper.showParticle(null, x() + item.x, y() + item.y, z() + item.z, item.hashCode());
            }
        }


        if (hasChanged) {
            //sendTravelingItemsPacket();
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

    public class TileInfoPackets {

        public static final byte GUI_BUTTON = 0;
        public static final byte STUFFED_UPDATE = 1;
        public static final byte TRAVELING_ITEMS = 2;
        public static final byte STUFFED_ITEMS = 3;
        public static final byte REQUEST_STUFFED_ITEMS = 4;
    }


    @Override
    public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {
        super.handleTilePacket(payload, isServer);
    }

    public void tickItemsClient() {

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
        } else {
            TickHandlerClient.tickBlocksToRemove.add(this);
        }
    }
}
