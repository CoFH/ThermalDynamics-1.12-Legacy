package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitTransportLongRange extends DuctUnitTransportBase {

	public DuctUnitTransportLongRange(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public TransportGrid createGrid() {

		return null;
	}

	@Override
	public void formGrid() {

	}

	@Override
	public boolean isValidForForming() {
		return false;
	}

	public byte nextDirection(byte k) {

		for (byte i = 0; i < 6; i++) {
			if (k == (i ^ 1)) {
				continue;
			}
			if (pipeCache[i] != null) return i;
		}
		return -1;
	}


	@Override
	public boolean advanceEntity(EntityTransport t) {

		int v = t.progress;
		v += t.step * 2;
		t.progress = (byte) (v % EntityTransport.PIPE_LENGTH);
		if (v >= EntityTransport.PIPE_LENGTH) {
			if (pipeCache[t.direction] != null) {
				DuctUnitTransportBase newHome = getConnectedSide(t.direction);
				newHome.onNeighborBlockChange();
				if (newHome.pipeCache[t.direction ^ 1] != null) {
					t.pos = new BlockPos(newHome.pos());

					t.oldDirection = t.direction;

					if (newHome instanceof DuctUnitTransportLongRange) {
						DuctUnitTransportLongRange lr = (DuctUnitTransportLongRange) newHome;
						t.direction = lr.nextDirection(t.direction);
						if (t.direction == -1) {
							t.dropPassenger();
							return true;
						}
					} else if (t.myPath != null) {
						if (t.myPath.hasNextDirection()) {
							t.direction = t.myPath.getNextDirection();
						} else {
							t.reRoute = true;
						}
					}
				}
			} else {
				t.dropPassenger();
				return true;
			}
		} else if (t.progress >= EntityTransport.PIPE_LENGTH2 && t.progress - t.step < EntityTransport.PIPE_LENGTH2) {
			if (pipeCache[t.direction] == null) {
				t.dropPassenger();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean advanceEntityClient(EntityTransport t) {

		int v = t.progress;
		v += t.step + (t.step);
		t.progress = (byte) (v % EntityTransport.PIPE_LENGTH);
		if (v >= EntityTransport.PIPE_LENGTH) {
			if (!t.trySimpleAdvance()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isRoutable() {
		return false;
	}

	@Override
	public boolean isCrossover() {
		return false;
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitTransportBase, TransportGrid, TransportDestination> adjDuct, byte side) {
		return !adjDuct.cast().isRoutable() && !adjDuct.cast().isCrossover();
	}

	@Nullable
	@Override
	public TransportDestination cacheTile(@Nonnull TileEntity tile, byte side) {
		return null;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
	}

//	@Nonnull
//	@Override
//	public BlockDuct.ConnectionType getRenderConnectionType(int side) {
//
//		BlockDuct.ConnectionType connectionType = super.getRenderConnectionType(side);
//		if (connectionType == BlockDuct.ConnectionType.NONE || connections == 0) {
//			return connectionType;
//		}
//
//		if (side != d1 && side != d2) {
//			return BlockDuct.ConnectionType.NONE;
//		}
//
//		// TODO: Optimize this - find someplace in the tile update dance to precalculate this
//		TileEntity tile = BlockHelper.getAdjacentTileEntity(this, side);
//		if (tile != null && tile.getClass() == DuctUnitTransportLongRange.class) {
//			DuctUnitTransportLongRange t = (DuctUnitTransportLongRange) tile;
//
//			if ((t.d1 ^ 1) == side || (t.d2 ^ 1) == side) {
//				return connectionType;
//			}
//			return BlockDuct.ConnectionType.NONE;
//		}
//
//		return connectionType;
//	}
//
//	@Override
//	public PacketCoFHBase getTilePacket() {
//
//		PacketCoFHBase packet = super.getTilePacket();
//		packet.addShort(connections << 6 | d1 << 3 | d2);
//		return packet;
//	}
//
//	@Override
//	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {
//
//		super.handleTilePacket(payload, isServer);
//		int b = payload.getShort();
//		connections = (byte) ((b >> 6) & 7);
//		d1 = (byte) ((b >> 3) & 7);
//		d2 = (byte) (b & 7);
//	}
}
