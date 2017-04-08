package cofh.thermaldynamics.duct.entity;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.item.RouteInfo;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.IDuctHolder;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitTransportLinking extends DuctUnitTransportBase {

	final static SidedBlockPos clientValue = SidedBlockPos.ORIGIN;
	public static byte CHARGE_TIME = 20;
	final SidedBlockPos[] rangePos = new SidedBlockPos[6];

	public DuctUnitTransportLinking(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull ConnectionType type) {
		SidedBlockPos sidedBlockPos = rangePos[side];
		if (sidedBlockPos != null) {
			if (world().isBlockLoaded(sidedBlockPos.pos)) {
				TileEntity distantTile = world().getTileEntity(sidedBlockPos.pos);
				DuctUnitTransportBase transportBase = IDuctHolder.getTokenFromTile(distantTile, DuctToken.TRANSPORT);
				if (transportBase != null && transportBase.isCrossover()) {
					super.handleTileSideUpdate(distantTile, (IDuctHolder) distantTile, (byte) sidedBlockPos.side.ordinal(), type);
				}
			}
		} else {
			super.handleTileSideUpdate(tile, holder, side, type);
		}
	}


	//	@Override
//	public void handleTileSideUpdate(int i) {
//
//		super.handleTileSideUpdate(i);
//
//		if (rangePos[i] == null || rangePos[i].orientation == null) {
//			rangePos[i] = null;
//			return;
//		}
//
//		if (neighborTypes[i] != NeighborType.OUTPUT) {
//			if (i < 2 || worldObj.isBlockLoaded(pos.offset(EnumFacing.VALUES[i]))) {
//				rangePos[i] = null;
//			}
//			return;
//		}
//
//		if (rangePos[i] == clientValue) {
//			return;
//		}
//
//		int j = rangePos[i].orientation.ordinal();
//		TileEntity theTile;
//
//		BlockPos position = new BlockPos(rangePos[i].x, rangePos[i].y, rangePos[i].z);
//		if (worldObj.isBlockLoaded(position)) {
//			theTile = worldObj.getTileEntity(position);
//
//			if (theTile instanceof DuctUnitTransportCrossover && !isBlockedSide(i) && !((TileDuctBase) theTile).isBlockedSide(j ^ 1)) {
//				neighborMultiBlocks[i] = (IGridTile) theTile;
//				neighborTypes[i] = NeighborType.MULTIBLOCK;
//			} else {
//				rangePos[i] = null;
//				super.handleTileSideUpdate(i);
//			}
//		} else {
//			neighborMultiBlocks[i] = null;
//			neighborTypes[i] = NeighborType.OUTPUT;
//		}
//
//	}

	@Override
	public boolean isOutput() {

		return false;
	}

	@Override
	public boolean isRoutable() {
		return true;
	}

	@Override
	public boolean isCrossover() {
		return true;
	}

	@Override
	public Route getRoute(Entity entityTransport, int direction, byte step) {
		return null;
	}


	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitTransportBase, TransportGrid, TransportDestination> adjDuct, byte side) {
		return !adjDuct.cast().isCrossover();
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (ServerHelper.isClientWorld(world())) {
			return true;
		}

		for (byte i = 0; i < 6; i++) {
			rangePos[i] = null;

			TileEntity adjTileEntitySafe = BlockHelper.getAdjacentTileEntity(parent, i);
			DuctUnitTransportBase duct = IDuctHolder.getTokenFromTile(adjTileEntitySafe, DuctToken.TRANSPORT);
			if (duct == null || !duct.isLongRange()) {
				continue;
			}

			player.addChatComponentMessage(new TextComponentString("Searching on side - " + EnumFacing.VALUES[i].toString()));

			DuctUnitTransportLongRange travel = (DuctUnitTransportLongRange) duct;

			DuctUnitTransportLinking finalDest = null;

			byte d = travel.nextDirection(i);

			BlockPos pos = travel.pos();

			while (d != -1) {
				pos = pos.offset(EnumFacing.VALUES[d]);

				for (int j = 2; j < 6; j++) {
					world().getChunkFromBlockCoords(pos.offset(EnumFacing.VALUES[j]));
				}
				DuctUnitTransportBase side = IDuctHolder.getTokenFromTile(world().getTileEntity(pos), DuctToken.TRANSPORT);

				if (side == null) {
					break;
				} else if (side.isCrossover()) {
					finalDest = ((DuctUnitTransportLinking) side);
					break;
				} else if (side.isLongRange()) {
					travel = (DuctUnitTransportLongRange) side;
				} else {
					break;
				}

				travel.onNeighborBlockChange();

				d = travel.nextDirection(d);
			}
			if (finalDest != null) {
				player.addChatComponentMessage(new TextComponentString("Linked to -  (" + finalDest.x() + ", " + finalDest.y() + ", " + finalDest.z() + ")"));
				finalDest.rangePos[d ^ 1] = new SidedBlockPos(pos(), EnumFacing.VALUES[i].getOpposite());
				rangePos[i] = new SidedBlockPos(finalDest.pos(), EnumFacing.VALUES[d]);

				if (grid != null) {
					grid.destroyAndRecreate();
				}

				if (finalDest.grid != null && finalDest.grid != grid) {
					finalDest.grid.destroyAndRecreate();
				}
			} else {
				player.addChatComponentMessage(new TextComponentString("Failed at - (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
			}
		}
		return true;
	}

	@Override
	public IGridTile getPhysicalConnectedSide(byte direction) {

		if (rangePos[direction] != null) {
			TileEntity adjacentTileEntity = BlockHelper.getAdjacentTileEntity(parent, direction);
			DuctUnitTransportBase ductUnitTransportBase = IDuctHolder.getTokenFromTile(adjacentTileEntity, DuctToken.TRANSPORT);
			if (ductUnitTransportBase instanceof DuctUnitTransportLongRange) {
				return ductUnitTransportBase;
			}
			return null;
		}
		return super.getPhysicalConnectedSide(direction);
	}

	public void advanceToNextTile(EntityTransport t) {

		if (rangePos[t.direction] == null) {
			advanceToNextTile(t);
		} else {
			if (this.pipeCache[t.direction] != null) {
				DuctUnitTransportBase newHome = (DuctUnitTransportBase) this.getPhysicalConnectedSide(t.direction);
				if (!(newHome instanceof DuctUnitTransportLongRange)) {
					t.bouncePassenger(this);
					return;
				}

				if (((DuctUnitTransportLongRange) newHome).pipeCache[(t.direction ^ 1)] != null) {
					t.pos = new BlockPos(newHome.pos());

					t.oldDirection = t.direction;
					t.direction = ((DuctUnitTransportLongRange) newHome).nextDirection(t.direction);
					if (t.direction == -1) {
						t.dropPassenger();
					}
				} else {
					t.reRoute = true;
				}
			} else if (this.tileCaches[t.direction] != null) {
				t.dropPassenger();
			} else {
				t.bouncePassenger(this);
			}
		}
	}

	@Override
	public boolean advanceEntity(EntityTransport t) {

		if (t.progress < EntityTransport.PIPE_LENGTH2 && (t.progress + t.step) >= EntityTransport.PIPE_LENGTH2) {
			if (pipeCache[t.direction] != null && rangePos[t.direction] != null) {
				t.progress = EntityTransport.PIPE_LENGTH2;
				t.pause = CHARGE_TIME;
				return true;
			}
		}
		return advanceEntity(t);
	}

	@Override
	public boolean advanceEntityClient(EntityTransport t) {

		if (t.progress < EntityTransport.PIPE_LENGTH2 && (t.progress + t.step) >= EntityTransport.PIPE_LENGTH2) {
			if (pipeCache[t.direction] != null && rangePos[t.direction] != null) {
				t.progress = EntityTransport.PIPE_LENGTH2;
				t.pause = CHARGE_TIME;
				return true;
			}
		}
		return super.advanceEntityClient(t);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		for (byte i = 0; i < 6; i++) {
			if (nbt.hasKey("crossover" + i, 10)) {
				NBTTagCompound tag = nbt.getCompoundTag("crossover" + i);
				rangePos[i] = new SidedBlockPos(tag);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		for (int i = 0; i < 6; i++) {
			if (rangePos[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				rangePos[i].writeToNBT(tag);
				nbt.setTag("crossover" + i, tag);
			}
		}

		return nbt;
	}


	@Override
	public void writeToTilePacket(PacketCoFHBase packet) {
		int rangeMask = 0;

		for (byte i = 0; i < 6; i++) {
			if (rangePos[i] != null) {
				rangeMask = rangeMask | (1 << i);
			}
		}

		packet.addInt(rangeMask);
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload) {
		int rangeMask = payload.getInt();
		for (int i = 0; i < rangePos.length; i++) {
			if ((rangeMask & (1 << i)) != 0) {
				rangePos[i] = clientValue;
			}
		}
	}

	@Override
	public int getWeight() {

		return getWeight() * 100;
	}

	@Override
	public TransportGrid createGrid() {

		return new TransportGrid(world());
	}

	@Nullable
	@Override
	public TransportDestination cacheTile(@Nonnull TileEntity tile, byte side) {
		return null;
	}

	public RouteCache<DuctUnitTransportBase, TransportGrid> getCache() {

		return getCache(true);
	}

	public RouteCache<DuctUnitTransportBase, TransportGrid> getCache(boolean urgent) {

		assert grid != null;
		return urgent ? grid.getRoutesFromOutput(this) : grid.getRoutesFromOutputNonUrgent(this);
	}

	public EntityTransport findRoute(Entity entity, int side, byte speed) {

		Route route = getRoute(entity, side, speed);
		return route != null ? new EntityTransport(this, route, (byte) side, speed) : null;
	}

	@Override
	public boolean canStuffItem() {

		return false;
	}

	@Override
	public int getMaxRange() {

		return Integer.MAX_VALUE;
	}

	@Override
	public RouteInfo canRouteItem(ItemStack stack) {

		return RouteInfo.noRoute;
	}

	@Override
	public byte getStuffedSide() {

		for (byte i = 0; i < 6; i++) {
			if (isOutput(i)) {
				return i;
			}
		}

		return 0;
	}

	@Override
	public boolean acceptingStuff() {

		return false;
	}

	@Nonnull
	@Override
	protected BlockDuct.ConnectionType getConnectionTypeDuct(DuctUnitTransportBase duct, int side) {
		if (rangePos[side] == null && duct.isLongRange()) {
			return BlockDuct.ConnectionType.TILE_CONNECTION;
		}
		return super.getConnectionTypeDuct(duct, side);
	}

	public static class SidedBlockPos {
		public static SidedBlockPos ORIGIN = new SidedBlockPos(BlockPos.ORIGIN, EnumFacing.DOWN);
		@Nonnull
		final BlockPos pos;
		@Nonnull
		final EnumFacing side;

		public SidedBlockPos(@Nonnull BlockPos pos, @Nonnull EnumFacing side) {
			this.pos = pos;
			this.side = side;
		}

		public SidedBlockPos(NBTTagCompound tag) {
			this(new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z")), EnumFacing.values()[tag.getInteger("s")]);
		}

		public void writeToNBT(NBTTagCompound tag) {
			tag.setInteger("x", pos.getX());
			tag.setInteger("y", pos.getY());
			tag.setInteger("z", pos.getZ());
			tag.setInteger("s", side.ordinal());
		}
	}
}
