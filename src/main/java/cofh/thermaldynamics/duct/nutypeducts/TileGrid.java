package cofh.thermaldynamics.duct.nutypeducts;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.util.BlockUtils;
import codechicken.lib.vec.Cuboid6;
import cofh.api.core.IPortableData;
import cofh.api.tileentity.ITileInfo;
import cofh.core.block.TileCore;
import cofh.core.network.ITileInfoPacketHandler;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.core.render.hitbox.CustomHitBox;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.lib.util.RayTracer;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.WrenchHelper;
import cofh.thermaldynamics.duct.*;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.TickHandler;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static cofh.thermaldynamics.duct.ConnectionType.BLOCKED;
import static cofh.thermaldynamics.duct.ConnectionType.NORMAL;

public abstract class TileGrid extends TileCore implements IDuctHolder, IPortableData, ITileInfoPacketHandler, ITilePacketHandler, ICustomHitBox, ITileInfo {
	public final static boolean isDebug = true;
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
	@Nullable
	public BlockDuct.ConnectionType[] clientConnections;
	@Nullable
	private ConnectionType connectionTypes[];
	private int lastUpdateTime;

	public static void genSelectionBoxes(Cuboid6[] subSelection, int i, double min, double min2, double max2) {

		subSelection[i] = new Cuboid6(min2, 0.0, min2, max2, min, max2);
		subSelection[i + 1] = new Cuboid6(min2, 1.0 - min, min2, max2, 1.0, max2);
		subSelection[i + 2] = new Cuboid6(min2, min2, 0.0, max2, max2, min);
		subSelection[i + 3] = new Cuboid6(min2, min2, 1.0 - min, max2, max2, 1.0);
		subSelection[i + 4] = new Cuboid6(0.0, min2, min2, min, max2, max2);
		subSelection[i + 5] = new Cuboid6(1.0 - min, min2, min2, 1.0, max2, max2);
	}

	@Override
	public String getDataType() {

		return "tile.thermaldynamics.duct";
	}

	public abstract Iterable<DuctUnit> getDuctUnits();

	@Override
	public boolean isSideBlocked(int side) {
		Attachment attachment = getAttachment(side);
		return attachment != null && !attachment.allowPipeConnection() || (connectionTypes != null && !connectionTypes[side].allowTransfer);

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

	}

	@Override
	public void onNeighborBlockChange() {
		if (ServerHelper.isClientWorld(worldObj)) {
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

		int renderHash = getRenderHash();
		int tileHash = getTileHash();

		if (attachmentData != null) {
			for (Attachment attachment : attachmentData.attachments) {
				if (attachment != null) {
					attachment.onNeighborChange();
				}
			}
		}

		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.updateAllSides(tiles, holders);
		}

		if (renderHash != getRenderHash()) {
			callBlockUpdate();
		}

		if(tileHash != getTileHash()) {
			rebuildChunkCache();
		}
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		byte side = (byte) getNeighbourDirection(getPos(), pos).ordinal();
		BlockDuct.ConnectionType visualConnectionType = getVisualConnectionType(side);

		if (attachmentData != null) {

			Attachment attachment = attachmentData.attachments[side];
			if (attachment != null)
				attachment.onNeighborChange();
		}

		TileEntity adjacentTileEntity = BlockHelper.getAdjacentTileEntity(this, side);
		IDuctHolder holder = adjacentTileEntity instanceof IDuctHolder ? (IDuctHolder) adjacentTileEntity : null;

		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.updateSide(adjacentTileEntity, holder, side);
		}

		if (visualConnectionType != getVisualConnectionType(side)) {
			callBlockUpdate();
		}
	}

	public EnumFacing getNeighbourDirection(BlockPos pos, BlockPos neighbour) {
		int dx = pos.getX() - neighbour.getX();
		if (dx == 0) {
			int dz = pos.getZ() - neighbour.getZ();
			if (dz == 0) {
				int dy = pos.getY() - neighbour.getY();
				if (dy == 1) {
					return EnumFacing.UP;
				} else if (dy == -1) {
					return EnumFacing.DOWN;
				}
			} else if (dz == 1) {
				return EnumFacing.SOUTH;
			} else if (dz == -1) {
				return EnumFacing.NORTH;
			}
		} else if (dx == 1) {
			return EnumFacing.EAST;
		} else if (dx == -1) {
			return EnumFacing.WEST;
		}
		throw new IllegalStateException("Positions " + pos + " and " + neighbour + " are not adjacent");
	}

	private int getTileHash(){
		int hash = 0;
		for (int i = 0; i < 6; i++) {
			for (DuctUnit unit : getDuctUnits()) {
				hash = hash * 31 + ((unit.isInput(i) || unit.isOutput(i)) ? 1 : 0);
			}
		}
		return hash;
	}

	private int getRenderHash() {
		int prev = 0;
		for (int i = 0; i < 6; i++) {
			BlockDuct.ConnectionType type = getVisualConnectionType(i);
			prev = prev * 8 + type.ordinal();
		}
		return prev;
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

	public ConnectionType getConnectionType(int i) {

		if(attachmentData != null){
			Attachment attachment = attachmentData.attachments[i];
			if(attachment != null ){
				return attachment.allowPipeConnection() ? NORMAL : BLOCKED;
			}
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
		BlockPos pos = getPos();

		int dx = pos.getX() & 15;
		int dz = pos.getZ() & 15;
		if (dx != 0 && dz != 0 && dx != 15 && dz != 15) {
			return;
		}

		if (!neighbourChunks.isEmpty()) {
			neighbourChunks.clear();
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

		DuctToken tickUnit = attachment.tickUnit();
		if (tickUnit != null) {
			attachmentData.tickingAttachments.computeIfAbsent(tickUnit, t -> new ArrayList<>()).add(attachment);
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
		DuctToken tickUnit = attachment.tickUnit();
		if (tickUnit != null) {
			List<Attachment> attachments = attachmentData.tickingAttachments.get(tickUnit);
			if (attachments != null) attachments.remove(attachment);
		}

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

		for (DuctUnit ductUnit : getDuctUnits()) {
			TickHandler.addMultiBlockToCalculate(ductUnit);
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
			Attachment attachment = attachmentData.attachments[side];
			attachment.readFromNBT(nbt);

			DuctToken tickUnit = attachment.tickUnit();
			if (tickUnit != null) {
				attachmentData.tickingAttachments.computeIfAbsent(tickUnit, t -> new ArrayList<>()).add(attachment);
			}
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

		for (int i = 0; i < 6; i++) {
			payload.addByte(getVisualConnectionType(i).ordinal());
		}

		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.writeToTilePacket(payload);
		}

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
		if (isServer) return;

		if (clientConnections == null)
			clientConnections = new BlockDuct.ConnectionType[6];
		for (int i = 0; i < 6; i++) {
			BlockDuct.ConnectionType connectionType = BlockDuct.ConnectionType.values()[payload.getByte()];
			clientConnections[i] = connectionType;
		}

		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.handleTilePacket(payload);
		}

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

	@Nullable
	public Attachment getAttachment(int side) {

		AttachmentData attachmentData = this.attachmentData;
		if (attachmentData == null) {
			return null;
		}
		return attachmentData.attachments[side];
	}

	@SideOnly(Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask(byte side) {

		BlockDuct.ConnectionType connectionType = getVisualConnectionType(side);
		if (connectionType == BlockDuct.ConnectionType.TILE_CONNECTION) {
			return CoverHoleRender.hollowDuctTile;
		} else if (connectionType == BlockDuct.ConnectionType.NONE) {
			return null;
		} else {
			return CoverHoleRender.hollowDuct;
		}
	}

	@Nonnull
	public BlockDuct.ConnectionType getVisualConnectionType(int side) {
		if (ServerHelper.isClientWorld(worldObj)) {
			if (clientConnections == null)
				return BlockDuct.ConnectionType.NONE;
			return clientConnections[side];
		}

		Attachment attachment = getAttachment(side);
		if (attachment != null) {

			return attachment.getNeighborType();
		} else {


			BlockDuct.ConnectionType connectionType = BlockDuct.ConnectionType.NONE;

			for (DuctUnit ductUnit : getDuctUnits()) {
				BlockDuct.ConnectionType ductType = ductUnit.getRenderConnectionType(side);
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
			BlockDuct.ConnectionType renderConnectionType = getVisualConnectionType(i);

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
					case TILE_CONNECTION:
						cuboids.add(new IndexedCuboid6(i, subSelection[i].copy()));
						break;
					case DUCT:
					case CLEAN_DUCT:
						cuboids.add(new IndexedCuboid6(i + 6, subSelection[i + 6].copy()));
						break;
					case STRUCTURE_CONNECTION:
					case STRUCTURE_CLEAN:
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

				setConnectionType((byte) i, getConnectionType(i).next());

				TileEntity tile = BlockHelper.getAdjacentTileEntity(this, i);
				if (tile instanceof TileGrid) {
					((TileGrid) tile).setConnectionType((byte) (i ^ 1), getConnectionType(i));
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
			byte t = payload.getByte();
			DuctToken token = DuctToken.TOKENS[t];
			DuctUnit duct = Validate.notNull(getDuct(token));
			duct.handleInfoPacket(payload, isServer, thePlayer);
		} else if (b >= 1 && b <= 6) {
			Validate.notNull(getAttachment(b - 1)).handleInfoPacket(payload, isServer, thePlayer);
		}
	}

	/* ICustomHitBox */
	@Override
	public boolean shouldRenderCustomHitBox(int subHit, EntityPlayer thePlayer) {

		return subHit == 13 || (subHit > 5 && subHit < 13 && !WrenchHelper.isHoldingUsableWrench(thePlayer, codechicken.lib.raytracer.RayTracer.retrace(thePlayer)));
	}

	@Override
	public CustomHitBox getCustomHitBox(int subHit, EntityPlayer thePlayer) {

		double v1 = getDuctType().isLargeTube() ? 0.075 : .3;
		double v = (1 - v1 * 2);

		CustomHitBox hb = new CustomHitBox(v, v, v, pos.getX() + v1, pos.getY() + v1, pos.getZ() + v1);

		for (int i = 0; i < 6; i++) {
			BlockDuct.ConnectionType renderConnectionType = getVisualConnectionType(i);

			if (renderConnectionType == BlockDuct.ConnectionType.DUCT) {
				hb.drawSide(i, true);
				hb.setSideLength(i, v1);
			} else if (renderConnectionType != BlockDuct.ConnectionType.NONE) {
				hb.drawSide(i, true);
				hb.setSideLength(i, .04);
			}
		}
		return hb;
	}

	public void onPlacedBy(EntityLivingBase living, ItemStack stack) {
		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.onPlaced(living, stack);
		}


		if (ServerHelper.isServerWorld(worldObj)) {
			for (DuctUnit ductUnit : getDuctUnits()) {
				TickHandler.addMultiBlockToCalculate(ductUnit);
			}
		}
	}

	public void randomDisplayTick() {
		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.randomDisplayTick();
		}
	}

	public ItemStack getDrop() {
		ItemStack stack = new ItemStack(getBlockType(), 1, getBlockMetadata());

		for (DuctUnit ductUnit : getDuctUnits()) {
			stack = ductUnit.addNBTToItemStackDrop(stack);
		}

		return stack;
	}

	public void dropAdditional(ArrayList<ItemStack> ret) {
		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.dropAdditional(ret);
		}
	}

	public TextureAtlasSprite getBaseIcon() {
		return getDuctType().iconBaseTexture;
	}

	@Override
	public void getTileInfo(List<ITextComponent> info, EnumFacing side, EntityPlayer player, boolean debug) {

		for (DuctUnit ductUnit : getDuctUnits()) {
			if (isDebug) {
				info.add(new TextComponentString(ductUnit.toString()));
			}
			MultiBlockGrid grid = ductUnit.getGrid();
			if (grid != null) {
				info.add(new TextComponentTranslation("info.thermaldynamics.info.duct"));
				grid.addInfo(info, player, debug || isDebug);
			} else if (isDebug) {
				info.add(new TextComponentString("No Grid"));
			}

			if (isDebug) {
				StringBuilder builder = new StringBuilder("  Tiles={");
				for (int i = 0; i < 6; i++) {
					if (ductUnit.tileCaches[i] != null) {
						builder.append(i).append("=").append(ductUnit.tileCaches[i]).append(",");
					}
				}
				builder.append("}");

				info.add(new TextComponentString(builder.toString()));
				builder = new StringBuilder("  Pipes={");
				for (int i = 0; i < ductUnit.pipeCache.length; i++) {
					if (ductUnit.pipeCache[i] != null) {
						builder.append(i).append("=").append(ductUnit.pipeCache[i]).append(",");
					}
				}
				builder.append("}");
				info.add(new TextComponentString(builder.toString()));

				builder = new StringBuilder("  Attach={");
				if(attachmentData != null){
					for (int i = 0; i < 6; i++) {
						Attachment attachment = attachmentData.attachments[i];
						if(attachment != null){
							builder.append(i).append("=").append(attachment.getId()).append(",");
						}
					}
				}
				info.add(new TextComponentString(builder.append("}").toString()));

				builder = new StringBuilder("  Tickers={");
				if(attachmentData != null){
					for (Map.Entry<DuctToken, List<Attachment>> entry : attachmentData.tickingAttachments.entrySet()) {
						builder.append(entry.getKey()).append("=[");
						for (Attachment attachment : entry.getValue()) {
							builder.append(attachment.getId()).append(",");
						}
						builder.append("],");
					}
				}
				info.add(new TextComponentString(builder.append("}").toString()));
			}
		}

		Attachment attachment = getAttachmentSelected(player);
		if (attachment != null) {

			info.add(new TextComponentTranslation("info.thermaldynamics.info.attachment"));
			int v = info.size();
			attachment.addInfo(info, player, debug);
			if (info.size() == v) {
				info.remove(v - 1);
			}
		}
	}

	public Object getConfigGuiServer(InventoryPlayer inventory) {

		return null;
	}

	public Object getConfigGuiClient(InventoryPlayer inventory) {

		return null;
	}

	@Override
	public int getType() {
		return getDuctType().type;
	}

	@Override
	public String getTileName() {
		return getDuctType().unlocalizedName;
	}

	public int getEnergyStored(EnumFacing from) {
		return Validate.notNull(getDuct(DuctToken.ENERGY)).getEnergyStored();
	}

	public int getMaxEnergyStored(EnumFacing from) {
		return Validate.notNull(getDuct(DuctToken.ENERGY)).getMaxEnergyStored();
	}

	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		return Validate.notNull(getDuct(DuctToken.ENERGY)).receiveEnergy(maxReceive, simulate);
	}

	public boolean canConnectEnergy(EnumFacing from) {
		return getVisualConnectionType(from.ordinal()).renderDuct();
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing facing) {
		if (getVisualConnectionType(facing.ordinal()).renderDuct()) {
			for (DuctUnit<?, ?, ?> ductUnit : getDuctUnits()) {
				if (ductUnit.hasCapability(capability, facing)) {
					return true;
				}
			}
		}
		return super.hasCapability(capability, facing);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
		if (getVisualConnectionType(facing.ordinal()).renderDuct()) {
			for (DuctUnit<?, ?, ?> ductUnit : getDuctUnits()) {
				if (ductUnit.hasCapability(capability, facing)) {
					T cap = ductUnit.getCapability(capability, facing);
					if (cap != null) {
						return capability.cast(cap);
					}
				}
			}
		}
		return super.getCapability(capability, facing);
	}

	public List<Attachment> getTickingAttachments(DuctToken unit) {
		if (attachmentData != null) {
			List<Attachment> attachments = attachmentData.tickingAttachments.get(unit);
			if (attachments != null) return attachments;
		}
		return ImmutableList.of();
	}

	public static class AttachmentData {
		public final Attachment attachments[] = new Attachment[6];
		public final Cover[] covers = new Cover[6];
		public final HashMap<DuctToken, List<Attachment>> tickingAttachments = new HashMap<>();


	}
}
