package cofh.thermaldynamics.duct.entity;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.position.BlockPosition;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.debughelper.DebugHelper;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.Route;
import java.util.LinkedList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Facing;
import net.minecraftforge.common.util.ForgeDirection;

public class TileTransportDuctCrossover extends TileTransportDuctBaseRoute {

    final BlockPosition[] rangePos = new BlockPosition[6];
    final static BlockPosition clientValue = new BlockPosition(0, 0, 0, ForgeDirection.DOWN);
    public static final int PAUSE_LEVEL = 60;

    public void handleTileSideUpdate(int i) {
        if (rangePos[i] == null || rangePos[i].orientation == ForgeDirection.UNKNOWN) {
            rangePos[i] = null;
            super.handleTileSideUpdate(i);
            return;
        }

        if(rangePos[i] == clientValue){
            super.handleTileSideUpdate(i);
            return;
        }

        int j = rangePos[i].orientation.ordinal();
        TileEntity theTile;

        if (worldObj.blockExists(rangePos[i].x, rangePos[i].y, rangePos[i].z)) {
            theTile = worldObj.getTileEntity(rangePos[i].x, rangePos[i].y, rangePos[i].z);

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
        if (worldObj.isRemote)
            return true;

        LinkedList<TileTransportDuctCrossover> toUpdate = new LinkedList<TileTransportDuctCrossover>();

        DebugHelper.startTimer();
        int k;

        for (byte i = 0; i < 6; i++) {
            rangePos[i] = null;

            k = 1;

            TileEntity adjTileEntitySafe = getAdjTileEntitySafe(i);
            if (!(adjTileEntitySafe instanceof TileTransportDuctLongRange)) {
                continue;
            }

            player.addChatComponentMessage(new ChatComponentText("Searching on side - " + ForgeDirection.getOrientation(i)));

            TileTransportDuctLongRange travel = (TileTransportDuctLongRange) adjTileEntitySafe;

            TileTransportDuctCrossover finalDest = null;

            byte d = travel.nextDirection(i);

            BlockPosition pos = new BlockPosition(travel);

            while (d != -1) {
                k++;
                pos.step(d);

                for (int j = 2; j < 6; j++) {
                    worldObj.getChunkFromBlockCoords(pos.x + Facing.offsetsXForSide[j], pos.z + Facing.offsetsZForSide[j]);
                }
                TileEntity side = worldObj.getTileEntity(pos.x, pos.y, pos.z);

                if (side instanceof TileTransportDuctCrossover) {
                    finalDest = ((TileTransportDuctCrossover) side);
                    break;
                } else if (side instanceof TileTransportDuctLongRange) {
                    travel = (TileTransportDuctLongRange) side;
                } else
                    break;

                travel.onNeighborBlockChange();

                d = travel.nextDirection(d);
            }

            if (finalDest != null) {
                player.addChatComponentMessage(new ChatComponentText("Linked to -  (" + finalDest.x() + ", " + finalDest.y() + ", " + finalDest.z() + ")"));
                finalDest.rangePos[d ^ 1] = new BlockPosition(this).setOrientation(ForgeDirection.getOrientation(i ^ 1));
                rangePos[i] = new BlockPosition(finalDest).setOrientation(ForgeDirection.getOrientation(d));

                if (internalGrid != null)
                    internalGrid.destroyAndRecreate();

                if (finalDest.internalGrid != null)
                    finalDest.internalGrid.destroyAndRecreate();
            } else
                player.addChatComponentMessage(new ChatComponentText("Failed at - (" + pos.x + ", " + pos.y + ", " + pos.z + ")"));
        }

        DebugHelper.stopTimer("Timer: ");
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
        if (rangePos[t.direction] == null)
            super.advanceToNextTile(t);
        else {
            if (this.neighborTypes[t.direction] == TileTDBase.NeighborTypes.MULTIBLOCK
                    && this.connectionTypes[t.direction].allowTransfer) {
                TileTransportDuctBase newHome = (TileTransportDuctBase) this.getPhysicalConnectedSide(t.direction);
                if (!(newHome instanceof TileTransportDuctLongRange)) {
                    t.bouncePassenger(this);
                    return;
                }

                if (newHome.neighborTypes[(t.direction ^ 1)] == NeighborTypes.MULTIBLOCK) {
                    t.pos = new BlockPosition(newHome);

                    t.oldDirection = t.direction;
                    t.direction = ((TileTransportDuctLongRange) newHome).nextDirection(t.direction);
                    if (t.direction == -1) {
                        t.dropPassenger();
                    }
                } else
                    t.reRoute = true;
            } else if (this.neighborTypes[t.direction] == TileTDBase.NeighborTypes.OUTPUT && this.connectionTypes[t.direction].allowTransfer) {
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
                t.pause = PAUSE_LEVEL;
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
                t.pause = PAUSE_LEVEL;
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
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        for (int i = 0; i < 6; i++) {
            if (rangePos[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                rangePos[i].writeToNBT(tag);
                nbt.setTag("crossover" + i, tag);
            }
        }
    }

    @Override
    public boolean isConnectable(TileEntity theTile, int side) {
        return theTile instanceof TileTransportDuctBaseRoute
                && !(theTile instanceof TileTransportDuctCrossover);
    }

    @Override
    public boolean isSignificantTile(TileEntity theTile, int side) {
        return theTile instanceof TileTransportDuctLongRange;
    }

    @Override
    public PacketCoFHBase getPacket() {
        PacketCoFHBase packet = super.getPacket();

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
