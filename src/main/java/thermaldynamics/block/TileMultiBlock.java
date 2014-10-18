package thermaldynamics.block;

import cofh.api.tileentity.IPlacedTile;
import cofh.core.block.TileCoFHBase;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.core.render.hitbox.CustomHitBox;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.repack.codechicken.lib.raytracer.IndexedCuboid6;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Vector3;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.StringUtils;
import thermaldynamics.core.TickHandler;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockFormer;
import thermaldynamics.multiblock.MultiBlockGrid;
import thermalexpansion.util.Utils;

import java.util.List;

public class TileMultiBlock extends TileCoFHBase implements IMultiBlock, IPlacedTile, ITilePacketHandler, ICustomHitBox {

    static {
        GameRegistry.registerTileEntity(TileMultiBlock.class, "thermalducts.multiblock");
    }

    public boolean isValid = true;
    public boolean isNode = false;
    public MultiBlockGrid myGrid;
    public IMultiBlock neighborMultiBlocks[] = new IMultiBlock[ForgeDirection.VALID_DIRECTIONS.length];
    public NeighborTypes neighborTypes[] = {NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE,
            NeighborTypes.NONE};
    public ConnectionTypes connectionTypes[] = {ConnectionTypes.NORMAL, ConnectionTypes.NORMAL, ConnectionTypes.NORMAL, ConnectionTypes.NORMAL,
            ConnectionTypes.NORMAL, ConnectionTypes.NORMAL};
    public int internalSideCounter = 0;

    @Override
    public void setInvalidForForming() {

        isValid = false;
    }

    @Override
    public void setValidForForming() {

        isValid = true;
    }

    @Override
    public boolean isValidForForming() {

        return isValid;
    }

    @Override
    public MultiBlockGrid getNewGrid() {

        return new MultiBlockGrid(worldObj);
    }

    @Override
    public void setGrid(MultiBlockGrid newGrid) {

        myGrid = newGrid;
    }

    @Override
    public MultiBlockGrid getGrid() {

        return myGrid;
    }

    @Override
    public IMultiBlock getConnectedSide(byte side) {

        return (IMultiBlock) BlockHelper.getAdjacentTileEntity(this, side);
    }

    @Override
    public boolean isSideConnected(byte side) {

        return connectionTypes[side] != ConnectionTypes.BLOCKED && BlockHelper.getAdjacentTileEntity(this, side) instanceof TileMultiBlock;
    }

    @Override
    public void setNotConnected(byte side) {

        connectionTypes[side] = ConnectionTypes.BLOCKED;
        neighborTypes[side] = NeighborTypes.NONE;
    }

    @Override
    public void tilePlaced() {

        onNeighborBlockChange();

        TickHandler.addMultiBlockToCalculate(this);

        System.out.println("tilePlaced");
    }

    @Override
    public void onNeighborBlockChange() {

        TileEntity theTile;
        boolean wasNode = isNode;
        isNode = false;
        for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
            theTile = BlockHelper.getAdjacentTileEntity(this, i);
            if (isConnectable(theTile, i)) {
                neighborMultiBlocks[i] = (IMultiBlock) theTile;
                neighborTypes[i] = NeighborTypes.MULTIBLOCK;
            } else if (isSignificantTile(theTile, i)) {
                neighborMultiBlocks[i] = null;
                neighborTypes[i] = NeighborTypes.TILE;
                isNode = true;
            } else {
                neighborMultiBlocks[i] = null;
                neighborTypes[i] = NeighborTypes.NONE;
            }
        }

        if (wasNode != isNode && myGrid != null) {
            myGrid.addBlock(this);
        }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void onNeighborTileChange(int tileX, int tileY, int tileZ) {

        int side = BlockHelper.determineAdjacentSide(this, tileX, tileY, tileZ);

        TileEntity theTile = worldObj.getTileEntity(tileX, tileY, tileZ);
        if (isConnectable(theTile, side)) {
            neighborMultiBlocks[side] = (IMultiBlock) theTile;
            neighborTypes[side] = NeighborTypes.MULTIBLOCK;
        } else if (isSignificantTile(theTile, side)) {
            neighborMultiBlocks[side] = null;
            neighborTypes[side] = NeighborTypes.TILE;
        } else {
            neighborMultiBlocks[side] = null;
            neighborTypes[side] = NeighborTypes.NONE;
        }
        boolean wasNode = isNode;
        checkIsNode();
        if (wasNode != isNode && myGrid != null) {
            myGrid.addBlock(this);
        }
    }

    private void checkIsNode() {

        isNode = false;
        for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
            if (neighborTypes[i] == NeighborTypes.TILE) {
                isNode = true;
                return;
            }
        }
    }

    public void tickInternalSideCounter(int start) {

        for (int a = start; a < neighborTypes.length; a++) {
            if (neighborTypes[a] == NeighborTypes.MULTIBLOCK && connectionTypes[a] == ConnectionTypes.NORMAL) {
                internalSideCounter = a;
                return;
            }
        }
        for (int a = 0; a < start; a++) {
            if (neighborTypes[a] == NeighborTypes.MULTIBLOCK && connectionTypes[a] == ConnectionTypes.NORMAL) {
                internalSideCounter = a;
                return;
            }
        }
    }

    /*
     * Should return true if theTile is an instance of this multiblock.
     *
     * This must also be an instance of IMultiBlock
     */
    public boolean isConnectable(TileEntity theTile, int side) {

        return theTile instanceof TileMultiBlock;
    }

    /*
     * Should return true if theTile is significant to this multiblock
     *
     * IE: Inventory's to ItemDuct's
     */
    public boolean isSignificantTile(TileEntity theTile, int side) {

        return false;
    }

    @Override
    public String getName() {

        return "tile.thermalducts.multiblock.name";
    }

    @Override
    public int getType() {

        return 0;
    }

    @Override
    public void tickMultiBlock() {

        System.out.println("Tick Multiblock");
        onNeighborBlockChange();
        formGrid();
    }

    public void formGrid() {

        if (myGrid == null && ServerHelper.isServerWorld(worldObj)) {
            new MultiBlockFormer().formGrid(this);
            // DEBUG CODE
            System.out.println("Grid Formed: " + (myGrid != null ? myGrid.nodeSet.size() + myGrid.idleSet.size() : "Failed"));
        }
    }

    @Override
    public void tickPass(int pass) {

    }

    @Override
    public boolean isNode() {

        return isNode;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

        super.readFromNBT(nbt);
        // for (int i = 0; i < neighborTypes.length; i++) {
        // neighborTypes[i] = NeighborTypes.values()[nbt.getByte("neTypes" + i)];
        // }
        for (int i = 0; i < connectionTypes.length; i++) {
            connectionTypes[i] = ConnectionTypes.values()[nbt.getByte("conTypes" + i)];
        }

        TickHandler.addMultiBlockToCalculate(this);

    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {

        super.writeToNBT(nbt);
        // for (int i = 0; i < neighborTypes.length; i++) {
        // nbt.setByte("neTypes" + i, (byte) neighborTypes[i].ordinal());
        // }
        for (int i = 0; i < connectionTypes.length; i++) {
            nbt.setByte("conTypes" + i, (byte) connectionTypes[i].ordinal());
        }
    }

    public static enum NeighborTypes {
        NONE, MULTIBLOCK, TILE
    }

    public static enum ConnectionTypes {
        NORMAL, BLOCKED;

        public ConnectionTypes next() {

            if (this == NORMAL) {
                return BLOCKED;
            }
            return NORMAL;
        }
    }

    public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

        double minX, minY, minZ;
        double maxX, maxY, maxZ;
        minX = minY = minZ = 0.3;
        maxX = maxY = maxZ = 0.7;

        Vector3 pos = new Vector3(xCoord, yCoord, zCoord);

        // Add TILE sides
        for (int i = 0; i < 6; i++) {
            if (neighborTypes[i] == NeighborTypes.TILE) {
                cuboids.add(new IndexedCuboid6(i, subSelection[i].copy().add(pos)));
            }
        }
        // Add MULTIBLOCK sides
        for (int i = 6; i < 12; i++) {
            if (neighborTypes[i - 6] == NeighborTypes.MULTIBLOCK) {
                cuboids.add(new IndexedCuboid6(i, subSelection[i].copy().add(pos)));
            }
        }
        cuboids.add(new IndexedCuboid6(13, new Cuboid6(minX, minY, minZ, maxX, maxY, maxZ).add(pos)));
    }

    @Override
    public boolean shouldRenderCustomHitBox(int subHit, EntityPlayer thePlayer) {

        return subHit == 13 || (subHit > 5 && !Utils.isHoldingUsableWrench(thePlayer, xCoord, yCoord, zCoord));
    }

    @Override
    public CustomHitBox getCustomHitBox(int subHit, EntityPlayer thePlayer) {

        CustomHitBox hb = new CustomHitBox(.4, .4, .4, xCoord + .3, yCoord + .3, zCoord + .3);

        for (int i = 0; i < neighborTypes.length; i++) {
            if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
                hb.drawSide(i, true);
                hb.setSideLength(i, .3);
            } else if (neighborTypes[i] == NeighborTypes.TILE) {
                hb.drawSide(i, true);
                hb.setSideLength(i, .04);
            }
        }

        return hb;
    }

    public static Cuboid6[] subSelection = new Cuboid6[12];

    static {

        double min = 0.25;
        double min2 = 0.2;
        double max2 = 0.8;

        subSelection[0] = new Cuboid6(min2, 0.0, min2, max2, min, max2);
        subSelection[1] = new Cuboid6(min2, 1.0 - min, min2, max2, 1.0, max2);
        subSelection[2] = new Cuboid6(min2, min2, 0.0, max2, max2, min);
        subSelection[3] = new Cuboid6(min2, min2, 1.0 - min, max2, max2, 1.0);
        subSelection[4] = new Cuboid6(0.0, min2, min2, min, max2, max2);
        subSelection[5] = new Cuboid6(1.0 - min, min2, min2, 1.0, max2, max2);

        min = 0.3;
        min2 = 0.3;
        max2 = 0.7;

        subSelection[6] = new Cuboid6(min2, 0.0, min2, max2, min, max2);
        subSelection[7] = new Cuboid6(min2, 1.0 - min, min2, max2, 1.0, max2);
        subSelection[8] = new Cuboid6(min2, min2, 0.0, max2, max2, min);
        subSelection[9] = new Cuboid6(min2, min2, 1.0 - min, max2, max2, 1.0);
        subSelection[10] = new Cuboid6(0.0, min2, min2, min, max2, max2);
        subSelection[11] = new Cuboid6(1.0 - min, min2, min2, 1.0, max2, max2);
    }

    @Override
    public boolean onWrench(EntityPlayer player, int hitSide) {

        if (Utils.isHoldingUsableWrench(player, xCoord, yCoord, zCoord)) {
            int subHit = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord).subHit;
            if (subHit > 5 && subHit < 13) {
                connectionTypes[subHit - 6] = connectionTypes[subHit - 6].next();
                player.addChatMessage(new ChatComponentText("ConType " + (subHit - 6) + " : " + connectionTypes[subHit - 6] + ":"
                        + connectionTypes[subHit - 6].next()));
            }
        }
        return false;
    }

    public void doDebug(EntityPlayer thePlayer) {

        thePlayer.addChatMessage(new ChatComponentText("Neighbors: " + StringUtils.join(neighborTypes, ",")));
        thePlayer.addChatMessage(new ChatComponentText("isNode: " + isNode));
        thePlayer.addChatMessage(new ChatComponentText("Grid Nodes: " + myGrid.nodeSet.size()));
    }

    /* NETWORK METHODS */
    @Override
    public PacketCoFHBase getPacket() {

        PacketCoFHBase payload = super.getPacket();

        for (byte i = 0; i < neighborTypes.length; i++) {
            payload.addByte(neighborTypes[i].ordinal());
        }
        // payload.addBool(isNode);

        return payload;
    }

    /* ITilePacketHandler */
    @Override
    public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

        if (ServerHelper.isClientWorld(worldObj)) {
            for (byte i = 0; i < neighborTypes.length; i++) {
                neighborTypes[i] = NeighborTypes.values()[payload.getByte()];
            }
        }
    }

}
