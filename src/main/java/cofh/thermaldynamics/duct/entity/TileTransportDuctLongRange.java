package cofh.thermaldynamics.duct.entity;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileTransportDuctLongRange extends TileTransportDuctBase {

	@Override
	public MultiBlockGrid createGrid() {

		return null;
	}

	byte d1, d2;
	byte connections;

	@Override
	public void onNeighborBlockChange() {

		d1 = 7;
		d2 = 7;
		super.onNeighborBlockChange();

		checkConnections();
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {

		d1 = 7;
		d2 = 7;

		super.onNeighborTileChange(pos);

		checkConnections();
	}

	public void checkConnections() {

		connections = 0;
		for (byte j = 5; j > 0; j--) {
			if (neighborTypes[j] != NeighborTypes.NONE) {
				if (connections == 0) {
					d1 = j;
					connections = 1;
				} else if (connections == 1) {
					d2 = j;
					connections = 2;
				} else {
					d1 = 7;
					d2 = 7;
					connections = 3;
					break;
				}
			}
		}
	}

	@Override
	public void formGrid() {

	}

	@Override
	public boolean isValidForForming() {

		return false;
	}

	public byte nextDirection(byte k) {

		if (connections != 2) {
			return -1;
		}
		if ((k ^ 1) == d1) {
			return d2;
		} else if ((k ^ 1) == d2) {
			return d1;
		} else {
			return -1;
		}
	}

	@Override
	public void handleTileSideUpdate(int i) {

		super.handleTileSideUpdate(i);
	}

	@Override
	public boolean advanceEntity(EntityTransport t) {

		int v = t.progress;
		v += t.step * 2;
		t.progress = (byte) (v % EntityTransport.PIPE_LENGTH);
		if (v >= EntityTransport.PIPE_LENGTH) {
			if (neighborTypes[t.direction] == NeighborTypes.MULTIBLOCK && connectionTypes[t.direction] == ConnectionTypes.NORMAL) {
				TileTransportDuctBase newHome = (TileTransportDuctBase) getConnectedSide(t.direction);
				newHome.onNeighborBlockChange();
				if (newHome.neighborTypes[t.direction ^ 1] == NeighborTypes.MULTIBLOCK) {
					t.pos = new BlockPos(newHome.getPos());

					t.oldDirection = t.direction;

					if (newHome instanceof TileTransportDuctLongRange) {
						TileTransportDuctLongRange lr = (TileTransportDuctLongRange) newHome;
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
			if (neighborTypes[t.direction] == NeighborTypes.NONE) {
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
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileTransportDuctLongRange || theTile instanceof TileTransportDuctCrossover;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		nbt.setByte("SimpleConnect", connections);
		nbt.setByte("SimpleConnect1", d1);
		nbt.setByte("SimpleConnect2", d2);

		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		connections = nbt.getByte("SimpleConnect");
		d1 = nbt.getByte("SimpleConnect1");
		d2 = nbt.getByte("SimpleConnect2");
	}

	@Override
	public BlockDuct.ConnectionTypes getRenderConnectionType(int side) {

		BlockDuct.ConnectionTypes connectionType = super.getRenderConnectionType(side);
		if (connectionType == BlockDuct.ConnectionTypes.NONE || connections == 0) {
			return connectionType;
		}

		if (side != d1 && side != d2) {
			return BlockDuct.ConnectionTypes.NONE;
		}

		// TODO: Optimize this - find someplace in the tile update dance to precalculate this
		TileEntity tile = BlockHelper.getAdjacentTileEntity(this, side);
		if (tile != null && tile.getClass() == TileTransportDuctLongRange.class) {
			TileTransportDuctLongRange t = (TileTransportDuctLongRange) tile;

			if ((t.d1 ^ 1) == side || (t.d2 ^ 1) == side) {
				return connectionType;
			}
			return BlockDuct.ConnectionTypes.NONE;
		}

		return connectionType;
	}

	@Override
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase packet = super.getTilePacket();
		packet.addShort(connections << 6 | d1 << 3 | d2);
		return packet;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);
		int b = payload.getShort();
		connections = (byte) ((b >> 6) & 7);
		d1 = (byte) ((b >> 3) & 7);
		d2 = (byte) (b & 7);
	}
}
