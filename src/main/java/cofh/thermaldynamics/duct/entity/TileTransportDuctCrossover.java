package cofh.thermaldynamics.duct.entity;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.BlockPosition;
import cofh.lib.util.helpers.BlockHelper;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.Route;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class TileTransportDuctCrossover extends TileTransportDuctBaseRoute {

	final BlockPosition[] rangePos = new BlockPosition[6];
	final static BlockPosition clientValue = new BlockPosition(0, 0, 0, EnumFacing.DOWN);

	public static byte CHARGE_TIME = 120;

	@Override
	public void handleTileSideUpdate(int i) {

		super.handleTileSideUpdate(i);

		if (rangePos[i] == null || rangePos[i].orientation == null) {
			rangePos[i] = null;
			return;
		}

		if (neighborTypes[i] != NeighborTypes.OUTPUT) {
			if (i < 2 || worldObj.isBlockLoaded(pos.offset(EnumFacing.VALUES[i]))) {
				rangePos[i] = null;
				// if (worldObj.blockExists(rangePos[i].x, rangePos[i].y, rangePos[i].z)) {
				// TileEntity theTile = worldObj.getTileEntity(rangePos[i].x, rangePos[i].y, rangePos[i].z);
				//
				// if (theTile instanceof TileTransportDuctCrossover){
				// theTile`
				// }
				// }
			}
			return;
		}

		if (rangePos[i] == clientValue) {
			return;
		}

		int j = rangePos[i].orientation.ordinal();
		TileEntity theTile;

		BlockPos position = new BlockPos(rangePos[i].x, rangePos[i].y, rangePos[i].z);
		if (worldObj.isBlockLoaded(position)) {
			theTile = worldObj.getTileEntity(position);

			if (theTile instanceof TileTransportDuctCrossover && !isBlockedSide(i) && !((TileTDBase) theTile).isBlockedSide(j ^ 1)) {
				neighborMultiBlocks[i] = (IMultiBlock) theTile;
				neighborTypes[i] = NeighborTypes.MULTIBLOCK;
			} else {
				rangePos[i] = null;
				super.handleTileSideUpdate(i);
			}
		} else {
			neighborMultiBlocks[i] = null;
			neighborTypes[i] = NeighborTypes.OUTPUT;
		}

	}

	@Override
	public boolean isOutput() {

		return false;
	}

	@Override
	public Route getRoute(Entity entity, int side, byte speed) {

		return null;
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (worldObj.isRemote) {
			return true;
		}
		@SuppressWarnings ("unused") int k;

		for (byte i = 0; i < 6; i++) {
			rangePos[i] = null;

			k = 1;

			TileEntity adjTileEntitySafe = getAdjTileEntitySafe(i);
			if (!(adjTileEntitySafe instanceof TileTransportDuctLongRange)) {
				continue;
			}

			player.addChatComponentMessage(new TextComponentString("Searching on side - " + EnumFacing.VALUES[i].toString()));

			TileTransportDuctLongRange travel = (TileTransportDuctLongRange) adjTileEntitySafe;

			TileTransportDuctCrossover finalDest = null;

			byte d = travel.nextDirection(i);

			BlockPos pos = new BlockPos(travel.getPos());

			while (d != -1) {
				k++;
				pos = pos.offset(EnumFacing.VALUES[d]);

				for (int j = 2; j < 6; j++) {
					worldObj.getChunkFromBlockCoords(pos.offset(EnumFacing.VALUES[j]));
				}
				TileEntity side = worldObj.getTileEntity(pos);

				if (side instanceof TileTransportDuctCrossover) {
					finalDest = ((TileTransportDuctCrossover) side);
					break;
				} else if (side instanceof TileTransportDuctLongRange) {
					travel = (TileTransportDuctLongRange) side;
				} else {
					break;
				}

				travel.onNeighborBlockChange();

				d = travel.nextDirection(d);
			}
			if (finalDest != null) {
				player.addChatComponentMessage(new TextComponentString("Linked to -  (" + finalDest.x() + ", " + finalDest.y() + ", " + finalDest.z() + ")"));
				finalDest.rangePos[d ^ 1] = new BlockPosition(this).setOrientation(EnumFacing.VALUES[i].getOpposite());
				rangePos[i] = new BlockPosition(finalDest).setOrientation(EnumFacing.VALUES[d]);

				if (internalGrid != null) {
					internalGrid.destroyAndRecreate();
				}

				if (finalDest.internalGrid != null) {
					finalDest.internalGrid.destroyAndRecreate();
				}
			} else {
				player.addChatComponentMessage(new TextComponentString("Failed at - (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
			}
		}
		return true;
	}

	@Override
	public IMultiBlock getPhysicalConnectedSide(byte direction) {

		if (rangePos[direction] != null) {
			TileEntity adjacentTileEntity = BlockHelper.getAdjacentTileEntity(this, direction);
			if (adjacentTileEntity instanceof TileTransportDuctLongRange) {
				return ((TileTransportDuctLongRange) adjacentTileEntity);
			}
			return null;
		}
		return super.getPhysicalConnectedSide(direction);
	}

	@Override
	public void advanceToNextTile(EntityTransport t) {

		if (rangePos[t.direction] == null) {
			super.advanceToNextTile(t);
		} else {
			if (this.neighborTypes[t.direction] == NeighborTypes.MULTIBLOCK && this.connectionTypes[t.direction].allowTransfer) {
				TileTransportDuctBase newHome = (TileTransportDuctBase) this.getPhysicalConnectedSide(t.direction);
				if (!(newHome instanceof TileTransportDuctLongRange)) {
					t.bouncePassenger(this);
					return;
				}

				if (newHome.neighborTypes[(t.direction ^ 1)] == NeighborTypes.MULTIBLOCK) {
					t.pos = new BlockPos(newHome.getPos());

					t.oldDirection = t.direction;
					t.direction = ((TileTransportDuctLongRange) newHome).nextDirection(t.direction);
					if (t.direction == -1) {
						t.dropPassenger();
					}
				} else {
					t.reRoute = true;
				}
			} else if (this.neighborTypes[t.direction] == NeighborTypes.OUTPUT && this.connectionTypes[t.direction].allowTransfer) {
				t.dropPassenger();
			} else {
				t.bouncePassenger(this);
			}
		}
	}

	@Override
	public boolean advanceEntity(EntityTransport t) {

		if (t.progress < EntityTransport.PIPE_LENGTH2 && (t.progress + t.step) >= EntityTransport.PIPE_LENGTH2) {
			if (neighborTypes[t.direction] == NeighborTypes.MULTIBLOCK && rangePos[t.direction] != null) {
				t.progress = EntityTransport.PIPE_LENGTH2;
				t.pause = CHARGE_TIME;
				return true;
			}
		}
		return super.advanceEntity(t);
	}

	@Override
	public boolean advanceEntityClient(EntityTransport t) {

		if (t.progress < EntityTransport.PIPE_LENGTH2 && (t.progress + t.step) >= EntityTransport.PIPE_LENGTH2) {
			if (neighborTypes[t.direction] == NeighborTypes.MULTIBLOCK && rangePos[t.direction] != null) {
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
				rangePos[i] = new BlockPosition(tag);
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
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileTransportDuctBaseRoute && !(theTile instanceof TileTransportDuctCrossover);
	}

	@Override
	public boolean isSignificantTile(TileEntity theTile, int side) {

		return theTile instanceof TileTransportDuctLongRange;
	}

	@Override
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase packet = super.getTilePacket();

		int rangeMask = 0;

		for (byte i = 0; i < 6; i++) {
			if (rangePos[i] != null) {
				rangeMask = rangeMask | (1 << i);
			}
		}

		packet.addInt(rangeMask);

		return packet;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);
		if (!isServer) {
			int rangeMask = payload.getInt();
			for (int i = 0; i < rangePos.length; i++) {
				if ((rangeMask & (1 << i)) != 0) {
					rangePos[i] = clientValue;
				}
			}
		}
	}

	@Override
	public int getWeight() {

		return super.getWeight() * 100;
	}
}
