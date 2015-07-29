package cofh.thermaldynamics.block;

import cofh.api.tileentity.IPortableData;
import cofh.api.tileentity.ITileInfo;
import cofh.core.block.TileCoFHBase;
import cofh.core.network.ITileInfoPacketHandler;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.core.render.hitbox.CustomHitBox;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.position.BlockPosition;
import cofh.repack.codechicken.lib.raytracer.IndexedCuboid6;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Vector3;
import cofh.thermaldynamics.core.TickHandler;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockFormer;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.Utils;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileTDBase extends TileCoFHBase implements IMultiBlock, ITilePacketHandler, ICustomHitBox, ITileInfoPacketHandler, IPortableData, ITileInfo {

	static {
		GameRegistry.registerTileEntityWithAlternatives(TileTDBase.class, "thermaldynamics.Duct", "thermaldynamics.multiblock");
	}

	public static Cuboid6[] subSelection = new Cuboid6[12];
	public static Cuboid6 selection;

	public static Cuboid6[] subSelection_large = new Cuboid6[12];
	public static Cuboid6 selectionlarge;

	static {
		genSelectionBoxes(subSelection, 0, 0.25, 0.2, 0.8);
		genSelectionBoxes(subSelection, 6, 0.3, 0.3, 0.7);
		selection = new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);

		genSelectionBoxes(subSelection_large, 0, 0.1, 0.1, 0.9);
		genSelectionBoxes(subSelection_large, 6, 0.1, 0.1, 0.9);
		selectionlarge = new Cuboid6(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);
	}

	public int facadeMask;

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
	public NeighborTypes neighborTypes[] = { NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE,
			NeighborTypes.NONE };
	public ConnectionTypes connectionTypes[] = { ConnectionTypes.NORMAL, ConnectionTypes.NORMAL, ConnectionTypes.NORMAL, ConnectionTypes.NORMAL,
			ConnectionTypes.NORMAL, ConnectionTypes.NORMAL, ConnectionTypes.BLOCKED };
	public byte internalSideCounter = 0;

	public Attachment attachments[] = new Attachment[] { null, null, null, null, null, null };

	public Cover[] covers = new Cover[6];

	LinkedList<Attachment> tickingAttachments = new LinkedList<Attachment>();

	public static final SubTileMultiBlock[] blankSubTiles = {};
	public SubTileMultiBlock[] subTiles = blankSubTiles;
	public long lastUpdateTime = -1;
	public int hashCode = 0;

	public LinkedList<WeakReference<Chunk>> neighbourChunks = new LinkedList<WeakReference<Chunk>>();

	@Override
	public void onChunkUnload() {

		if (ServerHelper.isServerWorld(worldObj)) {
			for (SubTileMultiBlock subTile : subTiles) {
				subTile.onChunkUnload();
			}

			if (myGrid != null) {
				tileUnloading();
				myGrid.removeBlock(this);
			}
		}

		super.invalidate();
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
			for (SubTileMultiBlock subTile : subTiles) {
				subTile.invalidate();
			}
			if (myGrid != null) {
				myGrid.removeBlock(this);
			}
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

		if (side >= neighborMultiBlocks.length) {
			return null;
		}
		return neighborMultiBlocks[side];

	}

	@Override
	public boolean isBlockedSide(int side) {

		return connectionTypes[side] == ConnectionTypes.BLOCKED || (attachments[side] != null && !attachments[side].allowPipeConnection());
	}

	@Override
	public boolean isSideConnected(byte side) {

		if (side >= neighborMultiBlocks.length) {
			return false;
		}
		IMultiBlock tileEntity = neighborMultiBlocks[side];
		return tileEntity != null && !isBlockedSide(side) && !tileEntity.isBlockedSide(side ^ 1);
	}

	@Override
	public void setNotConnected(byte side) {

		TileEntity tileEntity = BlockPosition.getAdjacentTileEntity(this, ForgeDirection.getOrientation(side));

		if (isSignificantTile(tileEntity, side)) {
			neighborMultiBlocks[side] = null;
			neighborTypes[side] = NeighborTypes.OUTPUT;
			if (!isNode) {
				isNode = true;
				if (myGrid != null) {
					myGrid.addBlock(this);
				}
			}
		} else if (isStructureTile(tileEntity, side)) {
			neighborMultiBlocks[side] = null;
			neighborTypes[side] = NeighborTypes.STRUCTURE;
		} else {
			neighborTypes[side] = NeighborTypes.NONE;
			neighborMultiBlocks[side] = null;
			connectionTypes[side] = ConnectionTypes.BLOCKED;
		}

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

		for (SubTileMultiBlock subTile : subTiles) {
			subTile.onNeighbourChange();
		}
	}

	public boolean isStructureTile(TileEntity tile, int side) {

		return false;
	}

	public boolean removeAttachment(Attachment attachment) {

		if (attachment == null) {
			return false;
		}

		attachments[attachment.side] = null;
		tickingAttachments.remove(attachment);
		connectionTypes[attachment.side] = ConnectionTypes.NORMAL;
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		onNeighborBlockChange();
		if (myGrid != null) {
			myGrid.destroyAndRecreate();
		}
		for (SubTileMultiBlock subTile : subTiles) {
			subTile.destroyAndRecreate();
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}

	public boolean addAttachment(Attachment attachment) {

		if (attachments[attachment.side] != null || !attachment.canAddToTile(this)) {
			return false;
		}

		if (ServerHelper.isClientWorld(worldObj)) {
			return true;
		}

		attachments[attachment.side] = attachment;
		if (attachment.doesTick()) {
			tickingAttachments.add(attachment);
		}
		connectionTypes[attachment.side] = ConnectionTypes.BLOCKED;
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		onNeighborBlockChange();
		if (myGrid != null) {
			myGrid.destroyAndRecreate();
		}
		for (SubTileMultiBlock subTile : subTiles) {
			subTile.destroyAndRecreate();
		}
		return true;
	}

	@Override
	public void blockPlaced() {

		if (ServerHelper.isServerWorld(worldObj)) {
			TickHandler.addMultiBlockToCalculate(this);
		}
	}

	@Override
	public void onNeighborBlockChange() {

		if (ServerHelper.isClientWorld(worldObj) && lastUpdateTime == worldObj.getTotalWorldTime()) {
			return;
		}

		if (isInvalid()) {
			return;
		}

		boolean wasNode = isNode;
		isNode = false;
		boolean wasInput = isInput;
		isInput = false;
		boolean wasOutput = isOutput;
		isOutput = false;

		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			handleSideUpdate(i);
		}

		if (wasNode != isNode && myGrid != null) {
			myGrid.addBlock(this);
		} else if (myGrid != null && (isOutput != wasOutput || isInput != wasInput)) {
			myGrid.onMajorGridChange();
		}

		for (SubTileMultiBlock subTile : subTiles) {
			subTile.onNeighbourChange();
		}

		for (Attachment tickingAttachment : tickingAttachments) {
			tickingAttachment.postNeighbourChange();
		}

		if (ServerHelper.isServerWorld(worldObj)) {
			rebuildChunkCache();
		}

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void handleSideUpdate(int i) {

		TileEntity theTile;
		if (cachesExist()) {
			clearCache(i);
		}

		handleAttachmentUpdate(i);
		handleTileSideUpdate(i);
	}

	public void handleAttachmentUpdate(int i) {

		TileEntity theTile;
		neighborTypes[i] = null;
		if (attachments[i] != null) {
			attachments[i].onNeighborChange();
			neighborMultiBlocks[i] = null;

			neighborTypes[i] = attachments[i].getNeighborType();
			if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
				theTile = getAdjTileEntitySafe(i);
				if (isConnectable(theTile, i) && isUnblocked(theTile, i)) {
					neighborMultiBlocks[i] = (IMultiBlock) theTile;
				} else {
					neighborTypes[i] = NeighborTypes.NONE;
				}
			} else if (neighborTypes[i] == NeighborTypes.OUTPUT) {
				theTile = getAdjTileEntitySafe(i);
				if (isSignificantTile(theTile, i)) {
					if (!cachesExist()) {
						createCaches();
					}
					cacheImportant(theTile, i);
				}
				isOutput = true;
			} else if (neighborTypes[i] == NeighborTypes.INPUT) {
				theTile = getAdjTileEntitySafe(i);
				if (theTile != null) {
					if (!cachesExist()) {
						createCaches();
					}
					cacheInputTile(theTile, i);
				}
				isInput = true;
			} else {
				neighborMultiBlocks[i] = null;
			}

			connectionTypes[i] = ConnectionTypes.NORMAL;
			isNode = attachments[i].isNode();
		}
	}

	public void handleTileSideUpdate(int i) {

		TileEntity theTile;
		if (neighborTypes[i] == null) {
			theTile = getAdjTileEntitySafe(i);
			if (theTile == null) {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.NONE;
				if (connectionTypes[i] != ConnectionTypes.FORCED) {
					connectionTypes[i] = ConnectionTypes.NORMAL;
				}
			} else if (isConnectable(theTile, i) && isUnblocked(theTile, i)) {
				neighborMultiBlocks[i] = (IMultiBlock) theTile;
				neighborTypes[i] = NeighborTypes.MULTIBLOCK;
			} else if (connectionTypes[i].allowTransfer && isSignificantTile(theTile, i)) {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.OUTPUT;
				if (!cachesExist()) {
					createCaches();
				}
				cacheImportant(theTile, i);
				isNode = true;
				isOutput = true;
			} else if (connectionTypes[i].allowTransfer && isStructureTile(theTile, i)) {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.STRUCTURE;
				if (!cachesExist()) {
					createCaches();
				}
				cacheStructural(theTile, i);
				isNode = true;
			} else {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.NONE;
			}
		}
	}

	public void cacheInputTile(TileEntity theTile, int side) {

	}

	public TileEntity getAdjTileEntitySafe(int i) {

		return (BlockHelper.getAdjacentTileEntity(this, i));
	}

	public boolean checkForChunkUnload() {

		if (neighbourChunks.isEmpty()) {
			return false;
		}
		for (WeakReference<Chunk> neighbourChunk : neighbourChunks) {
			Object chunk = neighbourChunk.get();
			if (chunk != null && !((Chunk) chunk).isChunkLoaded) {
				onNeighborBlockChange();
				neighbourChunks.clear();
				return true;
			}
		}
		return false;
	}

	public void rebuildChunkCache() {

		if (!neighbourChunks.isEmpty()) {
			neighbourChunks.clear();
		}
		if (!isNode) {
			return;
		}
		Chunk base = worldObj.getChunkFromBlockCoords(x(), y());

		for (byte i = 0; i < 6; i++) {
			if (neighborTypes[i] == NeighborTypes.INPUT || neighborTypes[i] == NeighborTypes.OUTPUT) {
				Chunk chunk = worldObj.getChunkFromBlockCoords(x() + Facing.offsetsXForSide[i], z() + Facing.offsetsZForSide[i]);
				if (chunk != base) {
					neighbourChunks.add(new WeakReference<Chunk>(chunk));
				}
			}
		}
	}

	public void cacheStructural(TileEntity theTile, int i) {

	}

	@Override
	public void onNeighborTileChange(int tileX, int tileY, int tileZ) {

		if (ServerHelper.isClientWorld(worldObj) && lastUpdateTime == worldObj.getTotalWorldTime()) {
			return;
		}

		if (isInvalid()) {
			return;
		}

		int i = BlockHelper.determineAdjacentSide(this, tileX, tileY, tileZ);

		boolean wasNode = isNode;
		boolean wasInput = isInput;
		boolean wasOutput = isOutput;

		handleSideUpdate(i);

		for (SubTileMultiBlock subTile : subTiles) {
			subTile.onNeighbourChange();
		}

		checkIsNode();
		if (wasNode != isNode && myGrid != null) {
			myGrid.addBlock(this);
		} else if (myGrid != null && (isOutput != wasOutput || isInput != wasInput)) {
			myGrid.onMajorGridChange();
		}

		for (Attachment tickingAttachment : tickingAttachments) {
			tickingAttachment.postNeighbourChange();
		}

		if (ServerHelper.isServerWorld(worldObj)) {
			rebuildChunkCache();
		}
	}

	public void checkIsNode() {

		isNode = false;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (neighborTypes[i] == NeighborTypes.OUTPUT || neighborTypes[i] == NeighborTypes.STRUCTURE || (attachments[i] != null && attachments[i].isNode())) {
				isNode = true;
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
	 * Should return true if theTile is an instance of this multiblock. This must also be an instance of IMultiBlock
	 */
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileTDBase;
	}

	public boolean isUnblocked(TileEntity tile, int side) {

		return !isBlockedSide(side) && !((TileTDBase) tile).isBlockedSide(side ^ 1);
	}

	/*
	 * Should return true if theTile is significant to this multiblock IE: Inventory's to ItemDuct's
	 */
	public boolean isSignificantTile(TileEntity theTile, int side) {

		return false;
	}

	@Override
	public String getName() {

		return "tile.thermaldynamics.multiblock.name";
	}

	@Override
	public int getType() {

		return 0;
	}

	@Override
	public void tickMultiBlock() {

		if (isInvalid()) {
			return;
		}

		onNeighborBlockChange();
		formGrid();

		for (SubTileMultiBlock subTile : subTiles) {
			subTile.onNeighbourChange();
			subTile.formGrid();
		}
	}

	public void formGrid() {

		if (myGrid == null && ServerHelper.isServerWorld(worldObj)) {
			// DebugHelper.startTimer();
			new MultiBlockFormer().formGrid(this);
			// DEBUG CODE
			// DebugHelper.stopTimer("Grid");
			// DebugHelper.info("Grid Formed: " + (myGrid != null ? myGrid.nodeSet.size() + myGrid.idleSet.size() : "Failed"));
		}
	}

	@Override
	public boolean tickPass(int pass) {

		if (checkForChunkUnload()) {
			return false;
		}

		if (!tickingAttachments.isEmpty()) {
			for (Attachment attachment : tickingAttachments) {
				attachment.tick(pass);
			}
		}
		return true;
	}

	@Override
	public boolean isNode() {

		return isNode;
	}

	@Override
	public boolean existsYet() {

		return worldObj != null && worldObj.blockExists(xCoord, yCoord, zCoord) && worldObj.getBlock(xCoord, yCoord, zCoord) instanceof BlockDuct;
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
					if (attachments[i].doesTick()) {
						tickingAttachments.add(attachments[i]);
					}
				}
			} else {
				attachments[i] = null;
			}

			if (nbt.hasKey("facade" + i, 10)) {
				NBTTagCompound tag = nbt.getCompoundTag("facade" + i);
				covers[i] = new Cover(this, i);
				covers[i].readFromNBT(tag);
			} else {
				covers[i] = null;
			}

			connectionTypes[i] = ConnectionTypes.values()[nbt.getByte("conTypes" + i)];
		}

		recalcFacadeMask();

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
				tag.setShort("id", (short) attachments[i].getId());
				attachments[i].writeToNBT(tag);
				nbt.setTag("attachment" + i, tag);
			}

			if (covers[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				covers[i].writeToNBT(tag);
				nbt.setTag("facade" + i, tag);
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
		if (movingObjectPosition == null) {
			return false;
		}

		int subHit = movingObjectPosition.subHit;

		if (subHit > 13 && subHit < 20) {
			return attachments[subHit - 14].openGui(player);
		}
		return super.openGui(player);
	}

	Duct duct = null;

	public Duct getDuctType() {

		if (duct == null) {
			duct = TDDucts.getDuct(((BlockDuct) getBlockType()).offset + getBlockMetadata());
		}
		return duct;
	}

	public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

		if (!getDuctType().isLargeTube()) {
			addTraceableCuboids(cuboids, selection, subSelection);
		} else {
			addTraceableCuboids(cuboids, selectionlarge, subSelection_large);
		}
	}

	public void addTraceableCuboids(List<IndexedCuboid6> cuboids, Cuboid6 centerSelection, Cuboid6[] subSelection) {

		Vector3 pos = new Vector3(xCoord, yCoord, zCoord);

		for (int i = 0; i < 6; i++) {
			// Add ATTACHMENT sides
			if (attachments[i] != null) {
				cuboids.add(new IndexedCuboid6(i + 14, attachments[i].getCuboid().add(pos)));

				if (neighborTypes[i] != NeighborTypes.NONE) {
					cuboids.add(new IndexedCuboid6(i + 14, subSelection[i + 6].copy().add(pos)));
				}
			}
			if (covers[i] != null) {
				cuboids.add(new IndexedCuboid6(i + 20, covers[i].getCuboid().add(pos)));
			}

			{
				// Add TILE sides
				if (neighborTypes[i] == NeighborTypes.OUTPUT) {
					cuboids.add(new IndexedCuboid6(i, subSelection[i].copy().add(pos)));
				} else if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
					cuboids.add(new IndexedCuboid6(i + 6, subSelection[i + 6].copy().add(pos)));
				} else if (neighborTypes[i] == NeighborTypes.STRUCTURE) {
					cuboids.add(new IndexedCuboid6(i, subSelection[i + 6].copy().add(pos)));
				}

			}
		}

		cuboids.add(new IndexedCuboid6(13, centerSelection.copy().add(pos)));
	}

	@Override
	public boolean onWrench(EntityPlayer player, int hitSide) {

		if (Utils.isHoldingUsableWrench(player, xCoord, yCoord, zCoord)) {
			MovingObjectPosition rayTrace = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
			if (rayTrace == null) {
				return false;
			}

			int subHit = rayTrace.subHit;
			if (subHit >= 0 && subHit <= 13) {
				int i = subHit == 13 ? hitSide : subHit < 6 ? subHit : subHit - 6;

				onNeighborBlockChange();

				connectionTypes[i] = connectionTypes[i].next();

				TileEntity tile = BlockHelper.getAdjacentTileEntity(this, i);
				if (isConnectable(tile, i)) {
					((TileTDBase) tile).connectionTypes[i ^ 1] = connectionTypes[i];
				}

				worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());

				if (myGrid != null) {
					myGrid.destroyAndRecreate();
				}

				for (SubTileMultiBlock subTile : subTiles) {
					subTile.destroyAndRecreate();
				}

				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				return true;
			}
			if (subHit > 13 && subHit < 20) {
				return attachments[subHit - 14].onWrenched();
			}

			if (subHit >= 20 && subHit < 26) {
				return covers[subHit - 20].onWrenched();
			}
		}
		return false;
	}

	public void doDebug(EntityPlayer thePlayer) {

//		thePlayer.addChatMessage(new ChatComponentText("Neighbors: " + StringUtils.join(neighborTypes, ",")));
//		thePlayer.addChatMessage(new ChatComponentText("Connections: " + StringUtils.join(connectionTypes, ",")));
//		thePlayer.addChatMessage(new ChatComponentText("isNode: " + isNode));
//
	}

	public boolean addFacade(Cover cover) {

		if (covers[cover.side] != null) {
			return false;
		}

		covers[cover.side] = cover;
		recalcFacadeMask();
		getWorldObj().notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		onNeighborBlockChange();
		getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
		return true;
	}

	public void removeFacade(Cover cover) {

		covers[cover.side] = null;
		recalcFacadeMask();
		getWorldObj().notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		onNeighborBlockChange();
		getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void recalcFacadeMask() {

		facadeMask = 0;
		for (byte i = 0; i < 6; i++) {
			if (covers[i] != null) {
				facadeMask = facadeMask | (1 << i);
			}
		}
	}

	/* NETWORK METHODS */
	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		int attachmentMask = 0;
		recalcFacadeMask();
		for (byte i = 0; i < neighborTypes.length; i++) {
			payload.addByte(neighborTypes[i].ordinal());
			payload.addByte(connectionTypes[i].ordinal());
			if (attachments[i] != null) {
				attachmentMask = attachmentMask | (1 << i);
			}
		}

		payload.addBool(isNode);

		payload.addByte(attachmentMask);
		for (byte i = 0; i < 6; i++) {
			if (attachments[i] != null) {
				payload.addByte(attachments[i].getId());
				attachments[i].addDescriptionToPacket(payload);
			}
		}

		payload.addByte(facadeMask);
		for (byte i = 0; i < 6; i++) {
			if (covers[i] != null) {
				covers[i].addDescriptionToPacket(payload);
			}
		}

		payload.addInt(myGrid == null ? 0 : myGrid.hashCode());

		return payload;
	}

	@Override
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

	public abstract boolean cachesExist();

	public abstract void createCaches();

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
				} else {
					attachments[i] = null;
				}
			}

			facadeMask = payload.getByte();
			for (byte i = 0; i < 6; i++) {
				if ((facadeMask & (1 << i)) != 0) {
					covers[i] = new Cover(this, i);
					covers[i].getDescriptionFromPacket(payload);
				} else {
					covers[i] = null;
				}
			}
			recalcFacadeMask();

			hashCode = payload.getInt();

			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

			lastUpdateTime = worldObj.getTotalWorldTime();
		}
	}

	public boolean isOutput = false;
	public boolean isInput = false;

	public BlockDuct.ConnectionTypes getConnectionType(int side) {

		if (attachments[side] != null) {
			return attachments[side].getRenderConnectionType();
		} else {
			return getDefaultConnectionType(neighborTypes[side], connectionTypes[side]);
		}
	}

	public static BlockDuct.ConnectionTypes getDefaultConnectionType(NeighborTypes neighborType, ConnectionTypes connectionType) {

		if (neighborType == NeighborTypes.STRUCTURE) {
			return BlockDuct.ConnectionTypes.STRUCTURE;
		} else if (neighborType == NeighborTypes.INPUT) {
			return BlockDuct.ConnectionTypes.DUCT;
		} else if (neighborType == NeighborTypes.NONE) {
			if (connectionType == ConnectionTypes.FORCED) {
				return BlockDuct.ConnectionTypes.DUCT;
			}

			return BlockDuct.ConnectionTypes.NONE;
		} else if (connectionType == ConnectionTypes.BLOCKED || connectionType == ConnectionTypes.REJECTED) {
			return BlockDuct.ConnectionTypes.NONE;
		} else if (neighborType == NeighborTypes.OUTPUT) {
			return BlockDuct.ConnectionTypes.TILECONNECTION;
		} else {
			return BlockDuct.ConnectionTypes.DUCT;
		}
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

	public IIcon getBaseIcon() {

		return getDuctType().iconBaseTexture;
	}

	public ItemStack getDrop() {

		return new ItemStack(getBlockType(), 1, getBlockMetadata());
	}

	public void onPlacedBy(EntityLivingBase living, ItemStack stack) {

	}

	public void dropAdditional(ArrayList<ItemStack> ret) {

	}

	public static enum NeighborTypes {
		NONE, MULTIBLOCK, OUTPUT(true), INPUT(true), STRUCTURE(true), DUCT_ATTACHMENT;

		NeighborTypes() {

			this(false);
		}

		// Are we attached to a non-multiblock tile
		public final boolean attachedToNeightbour;

		NeighborTypes(boolean b) {

			this.attachedToNeightbour = b;
		}
	}

	public static enum ConnectionTypes {
		NORMAL(true), ONEWAY(true), REJECTED(false), BLOCKED(false), FORCED(true);

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

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {

		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
	}

	@Override
	public String getDataType() {

		return "tile.thermaldynamics.duct";
	}

	public void cofh_invalidate() {

		markChunkDirty();
	}

	@Override
	public void addRelays() {

		for (Attachment attachment : attachments) {
			if (attachment != null) {
				if (attachment.getId() == AttachmentRegistry.RELAY) {
					Relay signaller = (Relay) attachment;
					if (signaller.isInput()) {
						myGrid.addSignalInput(signaller);
					} else {
						myGrid.addSignalOutput(attachment);
					}
				} else if (attachment.respondsToSignallum()) {
					myGrid.addSignalOutput(attachment);
				}
			}
		}
	}

	/* ICustomHitBox */
	@Override
	public boolean shouldRenderCustomHitBox(int subHit, EntityPlayer thePlayer) {

		return subHit == 13 || (subHit > 5 && subHit < 13 && !Utils.isHoldingUsableWrench(thePlayer, xCoord, yCoord, zCoord));
	}

	@Override
	public CustomHitBox getCustomHitBox(int subHit, EntityPlayer thePlayer) {

		double v1 = getDuctType().isLargeTube() ? 0.075 : .3;
		double v = (1 - v1 * 2);

		CustomHitBox hb = new CustomHitBox(v, v, v, xCoord + v1, yCoord + v1, zCoord + v1);

		for (int i = 0; i < neighborTypes.length; i++) {
			if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
				hb.drawSide(i, true);
				hb.setSideLength(i, v1);
			} else if (neighborTypes[i] != NeighborTypes.NONE) {
				hb.drawSide(i, true);
				hb.setSideLength(i, .04);
			}
		}
		return hb;
	}

	/* IPortableData */
	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {

		if (!tag.hasKey("AttachmentType", 8)) {
			return;
		}
		MovingObjectPosition rayTrace = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
		if (rayTrace == null) {
			return;
		}
		int subHit = rayTrace.subHit;
		if (subHit <= 13 || subHit >= 20) {
			return;
		}
		if (!(attachments[subHit - 14] instanceof IPortableData)) {
			return;
		}
		IPortableData iPortableData = (IPortableData) attachments[subHit - 14];

		if (tag.getString("AttachmentType").equals(iPortableData.getDataType())) {
			iPortableData.readPortableData(player, tag);
		}
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		MovingObjectPosition rayTrace = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
		if (rayTrace == null) {
			return;
		}

		int subHit = rayTrace.subHit;
		if (subHit <= 13 || subHit >= 20) {
			return;
		}
		if (!(attachments[subHit - 14] instanceof IPortableData)) {
			return;
		}
		IPortableData iPortableData = (IPortableData) attachments[subHit - 14];
		iPortableData.writePortableData(player, tag);
		if (!tag.hasNoTags()) {
			tag.setString("AttachmentType", iPortableData.getDataType());
		}
	}


    @Override
    public void getTileInfo(List<IChatComponent> info, ForgeDirection side, EntityPlayer player, boolean debug) {
        MultiBlockGrid grid = getGrid();
        if(grid != null) grid.addInfo(info, player, debug);

        Attachment attachment = getAttachmentSelected(player);
        if(attachment != null) attachment.addInfo(info, player, debug);
    }

    public Attachment getAttachmentSelected(EntityPlayer player){
        MovingObjectPosition rayTrace = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
        if (rayTrace == null) {
            return null;
        }

        int subHit = rayTrace.subHit;
        if (subHit > 13 && subHit < 20) {
            return attachments[subHit - 14];
        }

        if (subHit >= 20 && subHit < 26) {
            return covers[subHit - 20];
        }

        return null;
    }
}
