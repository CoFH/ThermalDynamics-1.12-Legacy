package cofh.thermaldynamics.duct.nutypeducts;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.util.BlockUtils;
import codechicken.lib.vec.Cuboid6;
import cofh.api.core.IPortableData;
import cofh.core.block.TileCore;
import cofh.core.network.ITileInfoPacketHandler;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.RayTracer;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.WrenchHelper;
import cofh.thermaldynamics.duct.*;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.TickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import static cofh.thermaldynamics.duct.ConnectionType.BLOCKED;
import static cofh.thermaldynamics.duct.ConnectionType.NORMAL;

public abstract class TileGrid extends TileCore implements IDuctHolder, IPortableData, ITileInfoPacketHandler, ITilePacketHandler {
	static final int ATTACHMENT_SUB_HIT = 14;
	static final int COVER_SUB_HIT = 20;
	public static Cuboid6[] subSelection = new Cuboid6[12];
	public static Cuboid6 selection;
	public static Cuboid6[] subSelection_large = new Cuboid6[12];
	public static Cuboid6 selectionlarge;

	static {
		TileGrid.genSelectionBoxes(TileGrid.subSelection, 0, 0.25, 0.2, 0.8);
		TileGrid.genSelectionBoxes(TileGrid.subSelection, 6, 0.3, 0.3, 0.7);
		TileGrid.selection = new Cuboid6(0.3, 0.3, 0.3, 0.7, 0.7, 0.7);

		TileGrid.genSelectionBoxes(TileGrid.subSelection_large, 0, 0.1, 0.1, 0.9);
		TileGrid.genSelectionBoxes(TileGrid.subSelection_large, 6, 0.1, 0.1, 0.9);
		TileGrid.selectionlarge = new Cuboid6(0.1, 0.1, 0.1, 0.9, 0.9, 0.9);
	}

	static {
		GameRegistry.registerTileEntityWithAlternatives(TileGrid.class, "thermaldynamics.Duct", "thermaldynamics.multiblock");
	}

	public final LinkedList<WeakReference<Chunk>> neighbourChunks = new LinkedList<>();
	@Nullable
	public AttachmentData attachmentData;
	private ConnectionType connectionTypes[] = {NORMAL, NORMAL, NORMAL, NORMAL, NORMAL, NORMAL};
	private int lastUpdateTime;

	public static void genSelectionBoxes(Cuboid6[] subSelection, int i, double min, double min2, double max2) {

		subSelection[i] = new Cuboid6(min2, 0.0, min2, max2, min, max2);
		subSelection[i + 1] = new Cuboid6(min2, 1.0 - min, min2, max2, 1.0, max2);
		subSelection[i + 2] = new Cuboid6(min2, min2, 0.0, max2, max2, min);
		subSelection[i + 3] = new Cuboid6(min2, min2, 1.0 - min, max2, max2, 1.0);
		subSelection[i + 4] = new Cuboid6(0.0, min2, min2, min, max2, max2);
		subSelection[i + 5] = new Cuboid6(1.0 - min, min2, min2, 1.0, max2, max2);
	}

	public static BlockDuct.ConnectionType getDefaultConnectionType(NeighborType neighborType, ConnectionType connectionType) {

		if (neighborType == NeighborType.STRUCTURE) {
			return BlockDuct.ConnectionType.STRUCTURE;
		} else if (neighborType == NeighborType.INPUT) {
			return BlockDuct.ConnectionType.DUCT;
		} else if (neighborType == NeighborType.NONE) {
			if (connectionType == ConnectionType.FORCED) {
				return BlockDuct.ConnectionType.DUCT;
			}

			return BlockDuct.ConnectionType.NONE;
		} else if (connectionType == BLOCKED || connectionType == ConnectionType.REJECTED) {
			return BlockDuct.ConnectionType.NONE;
		} else if (neighborType == NeighborType.OUTPUT) {
			return BlockDuct.ConnectionType.TILECONNECTION;
		} else {
			return BlockDuct.ConnectionType.DUCT;
		}
	}

	@Override
	public String getDataType() {

		return "tile.thermaldynamics.duct";
	}

	public abstract Iterable<DuctUnit> getDuctUnits();

	@Override
	public boolean isSideBlocked(int side) {
		Attachment attachment = getAttachment(side);
		return attachment != null && !attachment.allowPipeConnection() || !connectionTypes[side].allowTransfer;

	}

	@Override
	public void onChunkUnload() {

		if (ServerHelper.isServerWorld(worldObj)) {
			for (DuctUnit unit : getDuctUnits()) {
				unit.onChunkUnload();
			}
		}

		super.onChunkUnload();
	}


	@Override
	public void invalidate() {

		if (ServerHelper.isServerWorld(worldObj)) {
			for (DuctUnit unit : getDuctUnits()) {
				unit.invalidate();
			}
		}
		super.invalidate();
	}

	protected void onAttachmentsChanged() {

		for (DuctUnit ductUnit : getDuctUnits()) {
			MultiBlockGrid grid = ductUnit.getGrid();
			if (grid != null) {
				grid.destroyAndRecreate();
			}
		}
	}

	@Override
	public void blockPlaced() {

		if (ServerHelper.isServerWorld(worldObj)) {

			for (DuctUnit ductUnit : getDuctUnits()) {
				ductUnit.onPlaced();
				TickHandler.addMultiBlockToCalculate(ductUnit);
			}
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

		TileEntity[] tiles = new TileEntity[6];
		IDuctHolder[] holders = new IDuctHolder[6];
		for (int i = 0; i < 6; i++) {
			TileEntity adjacentTileEntity = BlockHelper.getAdjacentTileEntity(this, i);
			tiles[i] = adjacentTileEntity;
			if (adjacentTileEntity instanceof IDuctHolder) {
				holders[i] = (IDuctHolder) adjacentTileEntity;
			}
		}

		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.updateAllSides(tiles, holders);
		}
	}

	public void setConnectionType(byte i, ConnectionType type) {

		if (connectionTypes == null) {
			if (type == NORMAL) {
				return;
			}
			connectionTypes = new ConnectionType[]{NORMAL, NORMAL, NORMAL, NORMAL, NORMAL, NORMAL};
			connectionTypes[i] = type;
		} else {
			connectionTypes[i] = type;
			if (type == NORMAL) {
				for (ConnectionType connectionType : connectionTypes) {
					if (connectionType != NORMAL) {
						return;
					}
				}
				connectionTypes = null;
			}
		}
	}

	public ConnectionType getConnectionType(byte i) {

		if (attachmentData != null && attachmentData.attachments[i] != null) {
			return BLOCKED;
		}
		if (connectionTypes == null) {
			return NORMAL;
		}
		return connectionTypes[i];
	}

	public boolean checkForChunkUnload() {

		if (neighbourChunks.isEmpty()) {
			return false;
		}
		for (WeakReference<Chunk> neighbourChunk : neighbourChunks) {
			Object chunk = neighbourChunk.get();
			if (chunk != null && !((Chunk) chunk).isChunkLoaded) {
				neighbourChunks.clear();
				onNeighborBlockChange();
				return true;
			}
		}
		return false;
	}

	public void rebuildChunkCache() {

		if (!neighbourChunks.isEmpty()) {
			neighbourChunks.clear();
		}

		BlockPos pos = getPos();

		int dx = pos.getX() & 15;
		int dz = pos.getZ() & 15;
		if (dx != 0 && dz != 0 && dx != 15 && dz != 15) {
			return;
		}

		Chunk base = worldObj.getChunkFromBlockCoords(pos);

		for (byte i = 0; i < 6; i++) {
			boolean important = false;
			for (DuctUnit ductUnit : getDuctUnits()) {
				if (ductUnit.tileCaches[i] != null) {
					important = true;
					break;
				}
			}

			if (!important) {
				continue;
			}

			EnumFacing facing = EnumFacing.VALUES[i];
			Chunk chunk = worldObj.getChunkFromBlockCoords(pos.offset(facing));
			if (chunk != base) {
				neighbourChunks.add(new WeakReference<>(chunk));
			}
		}
	}

	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return true;
	}

	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return true;
	}

	public boolean addAttachment(Attachment attachment) {

		if (!attachment.canAddToTile(this)) {
			return false;
		}

		if (attachmentData != null && attachmentData.attachments[attachment.side] != null) {
			return false;
		}

		if (ServerHelper.isClientWorld(worldObj)) {
			return true;
		}

		if (attachmentData == null) {
			attachmentData = new AttachmentData();
		}

		attachmentData.attachments[attachment.side] = attachment;

		if (attachment.doesTick()) {
			attachmentData.tickingAttachments.add(attachment);
		}

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();
		onAttachmentsChanged();

		return false;
	}

	public boolean removeAttachment(Attachment attachment) {

		if (attachment == null || attachmentData == null) {
			return false;
		}
		attachmentData.attachments[attachment.side] = null;
		attachmentData.tickingAttachments.remove(attachment);
		worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
		onNeighborBlockChange();

		onAttachmentsChanged();

		BlockUtils.fireBlockUpdate(getWorld(), getPos());
		return true;
	}

	public boolean addCover(Cover cover) {
		if (cover == null) {
			return false;
		}

		byte side = cover.side;
		if (attachmentData == null) {
			attachmentData = new AttachmentData();
		} else if (attachmentData.covers[side] != null) {
			return false;
		}
		attachmentData.covers[side] = cover;

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();
		onAttachmentsChanged();

		return true;
	}

	public boolean removeCover(byte side) {

		if (attachmentData == null || attachmentData.covers[side] == null) {
			return false;
		}
		attachmentData.covers[side] = null;

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();
		onAttachmentsChanged();

		return true;
	}

	Attachment getAttachmentSelected(EntityPlayer player) {

		if (attachmentData == null) {
			return null;
		}
		RayTraceResult rayTrace = RayTracer.retraceBlock(worldObj, player, getPos());
		if (rayTrace == null) {
			return null;
		}
		int subHit = rayTrace.subHit;
		if (subHit >= ATTACHMENT_SUB_HIT && subHit < ATTACHMENT_SUB_HIT + 6) {
			return attachmentData.attachments[subHit - ATTACHMENT_SUB_HIT];
		}
		if (subHit >= COVER_SUB_HIT && subHit < COVER_SUB_HIT + 6) {
			return attachmentData.covers[subHit - COVER_SUB_HIT];
		}
		return null;
	}

	@Override
	public int getLightValue() {
		int light = 0;
		for (DuctUnit ductUnit : getDuctUnits()) {
			light = Math.max(light, ductUnit.getLightValue());
		}
		return light;
	}

	@Override
	public void updateLighting() {
		super.updateLighting();
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		readAttachmentsFromNBT(nbt);
		readCoversFromNBT(nbt);

		for (DuctUnit ductUnit : getDuctUnits()) {
			String key = ductUnit.getToken().key;
			if (nbt.hasKey(key, 10)) {
				ductUnit.readFromNBT(nbt.getCompoundTag(key));
			}
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		writeAttachmentsToNBT(nbt);
		writeCoversToNBT(nbt);

		for (DuctUnit ductUnit : getDuctUnits()) {
			NBTTagCompound tag = ductUnit.saveToNBT();
			if (tag != null) {
				nbt.setTag(ductUnit.getToken().key, tag);
			}
		}

		return nbt;
	}

	public void readAttachmentsFromNBT(NBTTagCompound nbt) {

		NBTTagList list = nbt.getTagList("Attachments", 10);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			byte side = tag.getByte("side");
			if (attachmentData == null) {
				attachmentData = new AttachmentData();
			}
			int id = tag.getInteger("id");
			attachmentData.attachments[side] = AttachmentRegistry.createAttachment(this, side, id);
			attachmentData.attachments[side].readFromNBT(nbt);
		}
	}

	public void writeAttachmentsToNBT(NBTTagCompound nbt) {

		NBTTagList list = new NBTTagList();
		if (attachmentData != null) {
			for (byte i = 0; i < 6; i++) {
				Attachment attachment = attachmentData.attachments[i];
				if (attachment != null) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setInteger("side", i);
					tag.setInteger("id", (byte) attachment.getId());
					attachment.writeToNBT(tag);
					list.appendTag(tag);
				}
			}
		}
		nbt.setTag("Attachments", list);
	}

	public void readCoversFromNBT(NBTTagCompound nbt) {

		NBTTagList list = nbt.getTagList("Covers", 10);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			byte side = tag.getByte("Side");
			if (attachmentData == null) {
				attachmentData = new AttachmentData();
			}
			attachmentData.covers[side] = new Cover(this, side);
			attachmentData.covers[side].readFromNBT(nbt);
		}
	}

	public void writeCoversToNBT(NBTTagCompound nbt) {

		if (attachmentData == null) {
			return;
		}
		NBTTagList list = new NBTTagList();
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.covers[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("Side", i);
				attachmentData.covers[i].writeToNBT(tag);
				list.appendTag(tag);
			}
		}
		nbt.setTag("Covers", list);
	}

	/* SERVER -> CLIENT */
	@Override
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase payload = super.getTilePacket();

		if (attachmentData == null) {
			payload.addByte(0);
			payload.addByte(0);
			return payload;
		}

		int attachmentMask = 0;
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.attachments[i] != null) {
				attachmentMask |= (1 << i);
			}
		}

		payload.addByte(attachmentMask);
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.attachments[i] != null) {
				payload.addByte(attachmentData.attachments[i].getId());
				attachmentData.attachments[i].addDescriptionToPacket(payload);
			}
		}

		int coverMask = 0;
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.covers[i] != null) {
				coverMask |= (1 << i);
			}
		}

		payload.addByte(coverMask);
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.covers[i] != null) {
				attachmentData.covers[i].addDescriptionToPacket(payload);
			}
		}
		return payload;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		int attachmentMask = payload.getByte();
		for (byte i = 0; i < 6; i++) {
			if ((attachmentMask & (1 << i)) != 0) {
				if (attachmentData == null) {
					attachmentData = new AttachmentData();
				}
				int id = payload.getByte();
				attachmentData.attachments[i] = AttachmentRegistry.createAttachment(this, i, id);
				attachmentData.attachments[i].getDescriptionFromPacket(payload);
			} else if (attachmentData != null) {
				attachmentData.attachments[i] = null;
			}
		}

		int coverMask = payload.getByte();
		for (byte i = 0; i < 6; i++) {
			if ((coverMask & (1 << i)) != 0) {
				if (attachmentData == null) {
					attachmentData = new AttachmentData();
				}
				attachmentData.covers[i] = new Cover(this, i);
				attachmentData.covers[i].getDescriptionFromPacket(payload);
			} else if (attachmentData != null) {
				attachmentData.covers[i] = null;
			}
		}

		if (coverMask == 0 && attachmentMask == 0) {
			attachmentData = null;
		}

		callBlockUpdate();
	}

	/* IPortableData */
	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {

		Attachment attachment = getAttachmentSelected(player);

		if (attachment instanceof IPortableData) {
			((IPortableData) attachment).readPortableData(player, tag);
		}
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		Attachment attachment = getAttachmentSelected(player);

		if (attachment instanceof IPortableData) {
			((IPortableData) attachment).writePortableData(player, tag);
		}
	}

	public Attachment getAttachment(int side) {

		AttachmentData attachmentData = this.attachmentData;
		if (attachmentData == null) {
			return null;
		}
		return attachmentData.attachments[side];
	}

	@SideOnly(Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask(byte side) {

		BlockDuct.ConnectionType connectionType = getRenderConnectionType(side);
		if (connectionType == BlockDuct.ConnectionType.TILECONNECTION) {
			return CoverHoleRender.hollowDuctTile;
		} else if (connectionType == BlockDuct.ConnectionType.NONE) {
			return null;
		} else {
			return CoverHoleRender.hollowDuct;
		}
	}

	public BlockDuct.ConnectionType getRenderConnectionType(int side) {

		Attachment attachment = getAttachment(side);
		if (attachment != null) {
			return attachment.getRenderConnectionType();
		} else {
			BlockDuct.ConnectionType connectionType = BlockDuct.ConnectionType.NONE;

			for (DuctUnit ductUnit : getDuctUnits()) {
				BlockDuct.ConnectionType ductType = ductUnit.getConnectionType(side);
				connectionType = BlockDuct.ConnectionType.getPriority(connectionType, ductType);
			}

			return connectionType;
		}
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {

		return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
	}

	public abstract Duct getDuctType();

	@Nullable
	public Cover getCover(int side) {
		if (attachmentData == null) {
			return null;
		}
		return attachmentData.covers[side];
	}

	public int x() {
		return getPos().getX();
	}

	public int y() {
		return getPos().getY();
	}


	public int z() {
		return getPos().getZ();
	}

	public World world() {
		return getWorld();
	}

	public boolean isPowered() {
		return worldObj.isBlockPowered(pos);
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		RayTraceResult movingObjectPosition = codechicken.lib.raytracer.RayTracer.retrace(player);
		if (movingObjectPosition == null) {
			return false;
		}

		int subHit = movingObjectPosition.subHit;

		if (subHit > 13 && subHit < 20) {
			return getAttachment(subHit - 14).openGui(player);
		}
		return super.openGui(player);
	}

	public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

		if (!getDuctType().isLargeTube()) {
			addTraceableCuboids(cuboids, TileGrid.selection, TileGrid.subSelection);
		} else {
			addTraceableCuboids(cuboids, TileGrid.selectionlarge, TileGrid.subSelection_large);
		}
	}

	public void addTraceableCuboids(List<IndexedCuboid6> cuboids, Cuboid6 centerSelection, Cuboid6[] subSelection) {

		for (int i = 0; i < 6; i++) {
			BlockDuct.ConnectionType renderConnectionType = getRenderConnectionType(i);

			// Add ATTACHMENT sides
			Attachment attachment = getAttachment(i);
			if (attachment != null) {
				cuboids.add(new IndexedCuboid6(i + 14, attachment.getCuboid()));


				if (renderConnectionType != BlockDuct.ConnectionType.NONE) {
					cuboids.add(new IndexedCuboid6(i + 14, subSelection[i + 6].copy()));
				}
			}

			Cover cover = getCover(i);
			if (cover != null) {
				cuboids.add(new IndexedCuboid6(i + 20, cover.getCuboid()));
			}

			{
				// Add TILE sides
				switch (renderConnectionType) {
					case TILECONNECTION:
						cuboids.add(new IndexedCuboid6(i, subSelection[i].copy()));
						break;
					case DUCT:
					case CLEANDUCT:
						cuboids.add(new IndexedCuboid6(i + 6, subSelection[i + 6].copy()));
						break;
					case STRUCTURE:
						cuboids.add(new IndexedCuboid6(i, subSelection[i + 6].copy()));
						break;
				}

			}
		}

		cuboids.add(new IndexedCuboid6(13, centerSelection.copy()));
	}

	@Override
	public boolean onWrench(EntityPlayer player, EnumFacing side) {

		RayTraceResult rayTrace = codechicken.lib.raytracer.RayTracer.retraceBlock(worldObj, player, getPos());
		if (WrenchHelper.isHoldingUsableWrench(player, rayTrace)) {
			if (rayTrace == null) {
				return false;
			}

			int subHit = rayTrace.subHit;
			if (subHit >= 0 && subHit <= 13) {
				int i = subHit == 13 ? side.ordinal() : subHit < 6 ? subHit : subHit - 6;

				onNeighborBlockChange();

				connectionTypes[i] = connectionTypes[i].next();

				TileEntity tile = BlockHelper.getAdjacentTileEntity(this, i);
				if (isConnectable(tile, i)) {
					((TileDuctBase) tile).connectionTypes[i ^ 1] = connectionTypes[i];
				}

				worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());

				for (DuctUnit ductUnit : getDuctUnits()) {
					if (ductUnit.grid != null) {
						ductUnit.grid.destroyAndRecreate();
					}
				}

				BlockUtils.fireBlockUpdate(world(), getPos());
				return true;
			}
			if (subHit > 13 && subHit < 20) {
				return getAttachment(subHit - 14).onWrenched();
			}

			if (subHit >= 20 && subHit < 26) {
				return getCover(subHit - 20).onWrenched();
			}
		}
		return false;
	}

	@Override
	public void handleTileInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		byte b = payload.getByte();
		if (b == 0) {
			handleInfoPacket(payload, isServer, thePlayer);
		} else if (b >= 1 && b <= 6) {
			getAttachment(b - 1).handleInfoPacket(payload, isServer, thePlayer);
		}
	}

	public static class AttachmentData {
		public final Attachment attachments[] = new Attachment[6];
		public final Cover[] covers = new Cover[6];
		public final LinkedList<Attachment> tickingAttachments = new LinkedList<>();
	}
}
