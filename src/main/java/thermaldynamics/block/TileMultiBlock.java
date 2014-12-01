package thermaldynamics.block;

import cofh.api.tileentity.IPlacedTile;
import cofh.core.block.TileCoFHBase;
import cofh.core.network.ITileInfoPacketHandler;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Facing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.commons.lang3.StringUtils;
import thermaldynamics.core.TickHandler;
import thermaldynamics.debughelper.DebugHelper;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockFormer;
import thermaldynamics.multiblock.MultiBlockGrid;
import thermalexpansion.util.Utils;

import java.util.LinkedList;
import java.util.List;

public abstract class TileMultiBlock extends TileCoFHBase implements IMultiBlock, IPlacedTile, ITilePacketHandler, ICustomHitBox, ITileInfoPacketHandler {

    static {
        GameRegistry.registerTileEntity(TileMultiBlock.class, "thermalducts.multiblock");
    }

    public static Cuboid6[] subSelection = new Cuboid6[12];
    public static Cuboid6 selection;

    static {
        genSelectionBoxes(subSelection, 0, 0.25, 0.2, 0.8);
        genSelectionBoxes(subSelection, 6, 0.3, 0.3, 0.7);
        selection = new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);
    }


    private static void genSelectionBoxes(Cuboid6[] subSelection, int i, double min, double min2, double max2) {
        subSelection[i] = new Cuboid6(min2, 0.0, min2, max2, min, max2);
        subSelection[i + 1] = new Cuboid6(min2, 1.0 - min, min2, max2, 1.0, max2);
        subSelection[i + 2] = new Cuboid6(min2, min2, 0.0, max2, max2, min);
        subSelection[i + 3] = new Cuboid6(min2, min2, 1.0 - min, max2, max2, 1.0);
        subSelection[i + 4] = new Cuboid6(0.0, min2, min2, min, max2, max2);
        subSelection[i + 5] = new Cuboid6(1.0 - min, min2, min2, 1.0, max2, max2);
    }

    public boolean isValid = true;
    public boolean isNode = false;
    public MultiBlockGrid myGrid;
    public IMultiBlock neighborMultiBlocks[] = new IMultiBlock[ForgeDirection.VALID_DIRECTIONS.length];
    public NeighborTypes neighborTypes[] = {NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE,
            NeighborTypes.NONE};
    public ConnectionTypes connectionTypes[] = {ConnectionTypes.NORMAL, ConnectionTypes.NORMAL, ConnectionTypes.NORMAL, ConnectionTypes.NORMAL,
            ConnectionTypes.NORMAL, ConnectionTypes.NORMAL};
    public byte internalSideCounter = 0;

    public Attachment attachments[] = new Attachment[]{
            null, null, null, null, null, null
    };

    LinkedList<Attachment> tickingAttachments = new LinkedList<Attachment>();

    public static final SubTileMultiBlock[] blankSubTiles = {};
    public SubTileMultiBlock[] subTiles = blankSubTiles;
    public long lastUpdateTime = -1;
    public int hashCode = 0;

    public LinkedList<Chunk> neighbourChunks = new LinkedList<Chunk>();

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (ServerHelper.isServerWorld(worldObj)) {
            for (SubTileMultiBlock subTile : subTiles) subTile.onChunkUnload();


            if (myGrid != null) {
                tileUnloading();
                myGrid.removeBlock(this);
            }
        }
    }

    public void tileUnloading() {

    }

    @Override
    public World world() {
        return getWorldObj();
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
    public void invalidate() {
        super.invalidate();

        if (ServerHelper.isServerWorld(worldObj)) {
            for (SubTileMultiBlock subTile : subTiles) subTile.invalidate();
            if (myGrid != null) myGrid.removeBlock(this);
        }
    }

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
    public abstract MultiBlockGrid getNewGrid();

    @Override
    public MultiBlockGrid getGrid() {
        return myGrid;
    }

    @Override
    public void setGrid(MultiBlockGrid newGrid) {
        myGrid = newGrid;
    }

    @Override
    public IMultiBlock getConnectedSide(byte side) {
        return (IMultiBlock) BlockHelper.getAdjacentTileEntity(this, side);

    }

    @Override
    public boolean isBlockedSide(int side) {
        return connectionTypes[side] == ConnectionTypes.BLOCKED;
    }

    @Override
    public boolean isSideConnected(byte side) {
        TileEntity tileEntity = BlockHelper.getAdjacentTileEntity(this, side);
        return tileEntity instanceof TileMultiBlock && !isBlockedSide(side) && !((TileMultiBlock) tileEntity).isBlockedSide(side ^ 1);
    }

    @Override
    public void setNotConnected(byte side) {
        TileEntity tileEntity = BlockHelper.getAdjacentTileEntity(this, side);

        if (isSignificantTile(tileEntity, side)) {
            neighborMultiBlocks[side] = null;
            neighborTypes[side] = NeighborTypes.OUTPUT;
            if (!isNode) {
                isNode = true;
                if (myGrid != null) myGrid.addBlock(this);
            }
        } else if (isStructureTile(tileEntity, side)) {
            neighborMultiBlocks[side] = null;
            neighborTypes[side] = NeighborTypes.STRUCTURE;
        } else {
            neighborTypes[side] = NeighborTypes.NONE;
            neighborMultiBlocks[side] = null;
        }

        for (SubTileMultiBlock subTile : subTiles) subTile.onNeighbourChange();
    }

    @Override
    public void tilePlaced() {
        onNeighborBlockChange();

        if (ServerHelper.isServerWorld(worldObj)) {
            TickHandler.addMultiBlockToCalculate(this);
        }
    }

    public boolean isStructureTile(TileEntity tile, byte side) {
        return false;
    }

    public boolean removeAttachment(Attachment attachment) {
        if (attachment == null) return false;

        attachments[attachment.side] = null;
        tickingAttachments.remove(attachment);
        connectionTypes[attachment.side] = ConnectionTypes.NORMAL;
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
        onNeighborBlockChange();
        if (myGrid != null) myGrid.destroyAndRecreate();
        for (SubTileMultiBlock subTile : subTiles) subTile.destroyAndRecreate();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        return true;
    }

    public boolean addAttachment(Attachment attachment) {
        if (attachments[attachment.side] != null || !attachment.canAddToTile(this))
            return false;

        if (ServerHelper.isClientWorld(worldObj))
            return true;

        attachments[attachment.side] = attachment;
        if (attachment.doesTick()) tickingAttachments.add(attachment);
        connectionTypes[attachment.side] = ConnectionTypes.BLOCKED;
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
        onNeighborBlockChange();
        if (myGrid != null) myGrid.destroyAndRecreate();
        for (SubTileMultiBlock subTile : subTiles) subTile.destroyAndRecreate();
        return true;
    }

    @Override
    public void onNeighborBlockChange() {
        if (ServerHelper.isClientWorld(worldObj) && lastUpdateTime == worldObj.getTotalWorldTime())
            return;

        TileEntity theTile;
        boolean wasNode = isNode;
        isNode = false;
        boolean wasInput = isInput;
        isInput = false;
        boolean wasOutput = isOutput;
        isOutput = false;


        for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
            clearCache(i);
            if (attachments[i] != null) {
                attachments[i].onNeighbourChange();

                neighborTypes[i] = attachments[i].getNeighbourType();
                if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
                    theTile = BlockHelper.getAdjacentTileEntity(this, i);
                    if (isConnectable(theTile, i) && isUnblocked(theTile, i)) {
                        neighborMultiBlocks[i] = (IMultiBlock) theTile;
                    } else {
                        neighborMultiBlocks[i] = null;
                        neighborTypes[i] = NeighborTypes.NONE;
                    }
                } else
                    neighborMultiBlocks[i] = null;
                connectionTypes[i] = ConnectionTypes.BLOCKED;
                isNode = attachments[i].isNode();
                isInput = isInput || neighborTypes[i] == NeighborTypes.INPUT;
                isOutput = isOutput || neighborTypes[i] == NeighborTypes.OUTPUT;

            } else {
                theTile = BlockHelper.getAdjacentTileEntity(this, i);
                if (theTile == null) {
                    neighborMultiBlocks[i] = null;
                    neighborTypes[i] = NeighborTypes.NONE;
                    connectionTypes[i] = ConnectionTypes.NORMAL;
                } else if (isConnectable(theTile, i) && isUnblocked(theTile, i)) {
                    neighborMultiBlocks[i] = (IMultiBlock) theTile;
                    neighborTypes[i] = NeighborTypes.MULTIBLOCK;
                } else if (isSignificantTile(theTile, i)) {
                    neighborMultiBlocks[i] = null;
                    neighborTypes[i] = NeighborTypes.OUTPUT;
                    cacheImportant(theTile, i);
                    isNode = true;
                    isOutput = true;
                } else if (isStructureTile(theTile, i)) {
                    neighborMultiBlocks[i] = null;
                    neighborTypes[i] = NeighborTypes.STRUCTURE;
                    cacheStructural(theTile, i);
                } else {
                    neighborMultiBlocks[i] = null;
                    neighborTypes[i] = NeighborTypes.NONE;
                }
            }


        }

        if (wasNode != isNode && myGrid != null) {
            myGrid.addBlock(this);
        } else if (myGrid != null && (isOutput != wasOutput || isInput != wasInput)) {
            myGrid.onMajorGridChange();
        }

        for (SubTileMultiBlock subTile : subTiles) subTile.onNeighbourChange();

        rebuildChunkCache();

        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean checkForChunkUnload() {
        if (neighbourChunks.isEmpty()) return false;
        for (Chunk neighbourChunk : neighbourChunks) {
            if (!neighbourChunk.isChunkLoaded) {
                onNeighborBlockChange();
                return true;
            }
        }
        return false;
    }

    public void rebuildChunkCache() {
        if (!neighbourChunks.isEmpty()) neighbourChunks.clear();
        Chunk base = worldObj.getChunkFromBlockCoords(x(), y());

        for (byte i = 0; i < 6; i++) {
            if (neighborTypes[i] == NeighborTypes.INPUT || neighborTypes[i] == NeighborTypes.OUTPUT) {
                Chunk chunk = worldObj.getChunkFromBlockCoords(x() + Facing.offsetsXForSide[i], z() + Facing.offsetsZForSide[i]);
                if (chunk != base) neighbourChunks.add(chunk);
            }
        }
    }

    public void cacheStructural(TileEntity theTile, int i) {

    }

    @Override
    public void onNeighborTileChange(int tileX, int tileY, int tileZ) {
        if (ServerHelper.isClientWorld(worldObj) && lastUpdateTime == worldObj.getTotalWorldTime())
            return;

        int i = BlockHelper.determineAdjacentSide(this, tileX, tileY, tileZ);

        clearCache(i);

        if (attachments[i] != null) {
            attachments[i].onNeighbourChange();
            neighborTypes[i] = attachments[i].getNeighbourType();
            if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
                TileEntity theTile = worldObj.getTileEntity(tileX, tileY, tileZ);
                neighborMultiBlocks[i] = isConnectable(theTile, i) && isUnblocked(theTile, i) ? (IMultiBlock) theTile : null;
            } else {
                neighborMultiBlocks[i] = null;
            }
            connectionTypes[i] = ConnectionTypes.BLOCKED;
        } else {
            TileEntity theTile = worldObj.getTileEntity(tileX, tileY, tileZ);
            if (isConnectable(theTile, i) && isUnblocked(theTile, i)) {
                neighborMultiBlocks[i] = (IMultiBlock) theTile;
                neighborTypes[i] = NeighborTypes.MULTIBLOCK;
            } else if (isSignificantTile(theTile, i)) {
                neighborMultiBlocks[i] = null;
                neighborTypes[i] = NeighborTypes.OUTPUT;
                cacheImportant(theTile, i);
            } else if (isStructureTile(theTile, (byte) i)) {
                neighborMultiBlocks[i] = null;
                neighborTypes[i] = NeighborTypes.STRUCTURE;
                cacheStructural(theTile, i);
            } else {
                neighborMultiBlocks[i] = null;
                neighborTypes[i] = NeighborTypes.NONE;
            }
        }

        for (SubTileMultiBlock subTile : subTiles) subTile.onNeighbourChange();

        boolean wasNode = isNode;
        boolean wasInput = isInput;
        boolean wasOutput = isOutput;
        checkIsNode();
        if (wasNode != isNode && myGrid != null) {
            myGrid.addBlock(this);
        } else if (myGrid != null && (isOutput != wasOutput || isInput != wasInput)) {
            myGrid.onMajorGridChange();
        }

        rebuildChunkCache();
    }

    private void checkIsNode() {

        isNode = false;
        for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
            if (neighborTypes[i] == NeighborTypes.OUTPUT || (attachments[i] != null && attachments[i].isNode())) {
                isNode = true;
                return;
            }
            if (neighborTypes[i] == NeighborTypes.OUTPUT) {
                isOutput = true;
            }

            if (neighborTypes[i] == NeighborTypes.INPUT) {
                isInput = true;
            }

        }
    }

    public void tickInternalSideCounter(int start) {
        for (int a = start; a < neighborTypes.length; a++) {
            if (neighborTypes[a] == NeighborTypes.OUTPUT && connectionTypes[a] == ConnectionTypes.NORMAL) {
                internalSideCounter = (byte) a;
                return;
            }
        }
        for (int a = 0; a < start; a++) {
            if (neighborTypes[a] == NeighborTypes.OUTPUT && connectionTypes[a] == ConnectionTypes.NORMAL) {
                internalSideCounter = (byte) a;
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

    public boolean isUnblocked(TileEntity tile, int side) {
        return !isBlockedSide(side) && !((TileMultiBlock) tile).isBlockedSide(side ^ 1);
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
        onNeighborBlockChange();
        formGrid();

        for (SubTileMultiBlock subTile : subTiles) {
            subTile.onNeighbourChange();
            subTile.formGrid();
        }
    }

    public void formGrid() {
        if (myGrid == null && ServerHelper.isServerWorld(worldObj)) {
            DebugHelper.startTimer();
            new MultiBlockFormer().formGrid(this);
            // DEBUG CODE
            DebugHelper.stopTimer("Grid");
            DebugHelper.info("Grid Formed: " + (myGrid != null ? myGrid.nodeSet.size() + myGrid.idleSet.size() : "Failed"));
        }
    }

    @Override
    public boolean tickPass(int pass) {
        if (checkForChunkUnload()) return false;

        if (!tickingAttachments.isEmpty())
            for (Attachment attachment : tickingAttachments) {
                attachment.tick(pass);
            }

        return true;
    }

    @Override
    public boolean isNode() {
        return isNode;
    }

    public boolean existsYet() {
        return worldObj != null && worldObj.blockExists(xCoord, yCoord, zCoord);
    }

    @Override
    public IMultiBlock[] getSubTiles() {
        return subTiles;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

        super.readFromNBT(nbt);
        for (byte i = 0; i < 6; i++) {
            if (nbt.hasKey("attachment" + i, 10)) {
                NBTTagCompound tag = nbt.getCompoundTag("attachment" + i);
                int id = tag.getShort("id");
                attachments[i] = AttachmentRegistry.createAttachment(this, i, id);
                if (attachments[i] != null) {
                    attachments[i].readFromNBT(tag);
                    if (attachments[i].doesTick()) tickingAttachments.add(attachments[i]);
                }
            } else
                attachments[i] = null;

            connectionTypes[i] = ConnectionTypes.values()[nbt.getByte("conTypes" + i)];
        }


        for (int i = 0; i < this.subTiles.length; i++) {
            this.subTiles[i].readFromNBT(nbt.getCompoundTag("subTile" + i));
        }

        TickHandler.addMultiBlockToCalculate(this);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {

        super.writeToNBT(nbt);
        for (int i = 0; i < 6; i++) {
            if (attachments[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setShort("id", (short) attachments[i].getID());
                attachments[i].writeToNBT(tag);
                nbt.setTag("attachment" + i, tag);
            }

            nbt.setByte("conTypes" + i, (byte) connectionTypes[i].ordinal());
        }

        for (int i = 0; i < this.subTiles.length; i++) {
            SubTileMultiBlock a = this.subTiles[i];
            NBTTagCompound tag = new NBTTagCompound();
            a.writeToNBT(tag);
            nbt.setTag("subTile" + i, tag);
        }
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        MovingObjectPosition movingObjectPosition = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
        if (movingObjectPosition == null)
            return false;

        int subHit = movingObjectPosition.subHit;

        if (subHit > 13 && subHit < 20) {
            return attachments[subHit - 14].openGui(player);
        }
        return super.openGui(player);
    }

    Ducts duct = null;

    public Ducts getDuctType() {
        if (duct == null) duct = Ducts.getDuct(((BlockDuct) getBlockType()).offset + getBlockMetadata());
        return duct;
    }

    public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {
        Vector3 pos = new Vector3(xCoord, yCoord, zCoord);


        for (int i = 0; i < 6; i++) {
            // Add ATTACHMENT sides
            if (attachments[i] != null) {
                cuboids.add(new IndexedCuboid6(i + 14, attachments[i].getCuboid().add(pos)));

                if (neighborTypes[i] != NeighborTypes.NONE)
                    cuboids.add(new IndexedCuboid6(i + 14, subSelection[i + 6].copy().add(pos)));
            } else {
                // Add TILE sides
                if (neighborTypes[i] == NeighborTypes.OUTPUT)
                    cuboids.add(new IndexedCuboid6(i, subSelection[i].copy().add(pos)));

                    // Add MULTIBLOCK sides
                else if (neighborTypes[i] == NeighborTypes.MULTIBLOCK)
                    cuboids.add(new IndexedCuboid6(i + 6, subSelection[i + 6].copy().add(pos)));

                    // Add STRUCTURE sides
                else if (neighborTypes[i] == NeighborTypes.STRUCTURE)
                    cuboids.add(new IndexedCuboid6(i, subSelection[i + 6].copy().add(pos)));

            }
        }

        cuboids.add(new IndexedCuboid6(13, selection.copy().add(pos)));
    }

    @Override
    public boolean shouldRenderCustomHitBox(int subHit, EntityPlayer thePlayer) {

        return subHit == 13 || (subHit > 5 && subHit < 13 && !Utils.isHoldingUsableWrench(thePlayer, xCoord, yCoord, zCoord));
    }

    @Override
    public CustomHitBox getCustomHitBox(int subHit, EntityPlayer thePlayer) {

        CustomHitBox hb = new CustomHitBox(.4, .4, .4, xCoord + .3, yCoord + .3, zCoord + .3);

        for (int i = 0; i < neighborTypes.length; i++) {
            if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
                hb.drawSide(i, true);
                hb.setSideLength(i, .3);
            } else if (neighborTypes[i] != NeighborTypes.NONE) {
                hb.drawSide(i, true);
                hb.setSideLength(i, .04);
            }
        }

        return hb;
    }

    @Override
    public boolean onWrench(EntityPlayer player, int hitSide) {

        if (Utils.isHoldingUsableWrench(player, xCoord, yCoord, zCoord)) {
            MovingObjectPosition rayTrace = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
            if (rayTrace == null)
                return false;

            int subHit = rayTrace.subHit;
            if (subHit > 0 && subHit <= 13) {
                int i = subHit == 13 ? hitSide : subHit < 6 ? subHit : subHit - 6;

                onNeighborBlockChange();

                connectionTypes[i] = connectionTypes[i].next();

                TileEntity tile = BlockHelper.getAdjacentTileEntity(this, i);
                if (isConnectable(tile, i)) {
                    ((TileMultiBlock) tile).connectionTypes[i ^ 1] = connectionTypes[i];
                }

                worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());

                if (myGrid != null) myGrid.destroyAndRecreate();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return true;
            }
            if (subHit > 13 && subHit < 20) {
                return attachments[subHit - 14].onWrenched();
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

        int attachmentMask = 0;
        for (byte i = 0; i < neighborTypes.length; i++) {
            payload.addByte(neighborTypes[i].ordinal());
            payload.addByte(connectionTypes[i].ordinal());
            if (attachments[i] != null)
                attachmentMask = attachmentMask | (1 << i);
        }

        payload.addBool(isNode);

        payload.addByte(attachmentMask);
        for (byte i = 0; i < 6; i++) {
            if (attachments[i] != null) {
                payload.addByte(attachments[i].getID());
                attachments[i].addDescriptionToPacket(payload);
            }
        }

        payload.addInt(myGrid == null ? 0 : myGrid.hashCode());

        return payload;
    }

    public void handleTileInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {
        byte b = payload.getByte();
        if (b == 0) {
            handleInfoPacket(payload, isServer, thePlayer);
        } else if (b >= 1 && b <= 6) {
            attachments[b - 1].handleInfoPacket(payload, isServer, thePlayer);
        }
    }

    public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

    }

    public abstract void cacheImportant(TileEntity tile, int side);

    public abstract void clearCache(int side);

    /* ITilePacketHandler */
    @Override
    public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {
        if (!isServer) {
            for (byte i = 0; i < neighborTypes.length; i++) {
                neighborTypes[i] = NeighborTypes.values()[payload.getByte()];
                connectionTypes[i] = ConnectionTypes.values()[payload.getByte()];
            }

            isNode = payload.getBool();

            int attachmentMask = payload.getByte();
            for (byte i = 0; i < 6; i++) {
                if ((attachmentMask & (1 << i)) != 0) {
                    attachments[i] = AttachmentRegistry.createAttachment(this, i, payload.getByte());
                    attachments[i].getDescriptionFromPacket(payload);
                } else
                    attachments[i] = null;
            }

            hashCode = payload.getInt();

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

            lastUpdateTime = worldObj.getTotalWorldTime();
        }
    }

    public boolean isOutput = false;
    public boolean isInput = false;

    public BlockDuct.ConnectionTypes getConnectionType(int side) {
        if (neighborTypes[side] == NeighborTypes.STRUCTURE)
            return BlockDuct.ConnectionTypes.STRUCTURE;

        if (neighborTypes[side] == NeighborTypes.INPUT)
            return BlockDuct.ConnectionTypes.DUCT;

        if (neighborTypes[side] == NeighborTypes.NONE || connectionTypes[side] == ConnectionTypes.BLOCKED || connectionTypes[side] == ConnectionTypes.REJECTED)
            return BlockDuct.ConnectionTypes.NONE;

        if (neighborTypes[side] == NeighborTypes.OUTPUT)
            return BlockDuct.ConnectionTypes.TILECONNECTION;
        else
            return BlockDuct.ConnectionTypes.DUCT;
    }


    public void randomDisplayTick() {

    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    public boolean renderAdditional(int renderType, int[] connections, int pass) {
        return false;
    }

    public boolean isSubNode() {
        return false;
    }

    public static enum NeighborTypes {
        NONE, MULTIBLOCK, OUTPUT(true), INPUT(true), STRUCTURE(true), DUCT_ATTACHMENT;

        NeighborTypes() {
            this(false);
        }

        // Are we attached to a non-multiblock tile
        public boolean attachedToNeightbour;

        NeighborTypes(boolean b) {
            this.attachedToNeightbour = b;
        }
    }

    public static enum ConnectionTypes {
        NORMAL(true), ONEWAY(true), REJECTED(false), BLOCKED(false);

        ConnectionTypes(boolean allowTransfer) {
            this.allowTransfer = allowTransfer;
        }

        public final boolean allowTransfer;

        public ConnectionTypes next() {
            if (this == NORMAL) {
                return BLOCKED;
            }
            return NORMAL;
        }
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
    }
}
