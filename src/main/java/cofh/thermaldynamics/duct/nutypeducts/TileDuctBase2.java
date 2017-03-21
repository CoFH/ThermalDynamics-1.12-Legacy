package cofh.thermaldynamics.duct.nutypeducts;

import codechicken.lib.util.BlockUtils;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.block.SubTileGridTile;
import cofh.thermaldynamics.block.TileModular;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.multiblock.ISingleTick;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.TickHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

import static cofh.thermaldynamics.duct.TileDuctBase.ConnectionTypes.NORMAL;

public abstract class TileDuctBase2 extends TileModular implements IDuctHolder, ISingleTick {

	static {
		GameRegistry.registerTileEntityWithAlternatives(TileDuctBase2.class, "thermaldynamics.Duct", "thermaldynamics.multiblock");
	}

	public final LinkedList<WeakReference<Chunk>> neighbourChunks = new LinkedList<>();
	public TileDuctBase.ConnectionTypes connectionTypes[] = {
			NORMAL, NORMAL, NORMAL,
			NORMAL, NORMAL, NORMAL};
	@Nullable
	public AttachmentData attachmentData;
	private int lastUpdateTime;

	public abstract Iterable<DuctUnit> getDuctUnits();

	@Override
	public boolean isSideBlocked(int side) {
		return connectionTypes[side].allowTransfer;
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

	public boolean removeAttachment(Attachment attachment) {
		if (attachment == null || attachmentData == null) {
			return false;
		}
		attachmentData.attachments[attachment.side] = null;
		attachmentData.tickingAttachments.remove(attachment);
		connectionTypes[attachment.side] = TileDuctBase.ConnectionTypes.NORMAL;
		worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
		onNeighborBlockChange();

		allGridsDestroyAndRecreate();

		BlockUtils.fireBlockUpdate(getWorld(), getPos());
		return true;
	}

	protected void allGridsDestroyAndRecreate() {
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

	public void setConnectionType(byte i, TileDuctBase.ConnectionTypes type) {
		if (connectionTypes == null) {
			if (type == NORMAL) return;
			connectionTypes = new TileDuctBase.ConnectionTypes[]{
					NORMAL, NORMAL, NORMAL,
					NORMAL, NORMAL, NORMAL
			};
			connectionTypes[i] = type;
		} else {
			connectionTypes[i] = type;
			if (type == NORMAL) {
				for (TileDuctBase.ConnectionTypes connectionType : connectionTypes) {
					if (connectionType != NORMAL) {
						return;
					}
				}
				connectionTypes = null;
			}
		}
	}

	public TileDuctBase.ConnectionTypes getConnectionType(byte i) {
		if (connectionTypes == null) return NORMAL;
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
		if (dx != 0 && dz != 0 && dx != 15 && dz != 15) return;

		Chunk base = worldObj.getChunkFromBlockCoords(pos);

		for (byte i = 0; i < 6; i++) {
			boolean important = false;
			for (DuctUnit ductUnit : getDuctUnits()) {
				if (ductUnit.tileCaches[i] != null) {
					important = true;
					break;
				}
			}

			if (!important) continue;

			EnumFacing facing = EnumFacing.VALUES[i];
			Chunk chunk = worldObj.getChunkFromBlockCoords(pos.offset(facing));
			if (chunk != base) {
				neighbourChunks.add(new WeakReference<>(chunk));
			}
		}
	}

	@Override
	public boolean existsYet() {

		return worldObj != null && worldObj.isBlockLoaded(getPos()) && worldObj.getBlockState(getPos()).getBlock() instanceof BlockDuct;
	}

	@Override
	public void singleTick() {
		if (isInvalid()) {
			return;
		}

		onNeighborBlockChange();

		for (DuctUnit ductUnit : getDuctUnits()) {
			ductUnit.formGrid();
		}
	}

	@Override
	public World world() {
		return worldObj;
	}
}
