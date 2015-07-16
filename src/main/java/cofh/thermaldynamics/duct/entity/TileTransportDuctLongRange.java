package cofh.thermaldynamics.duct.entity;

import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.position.BlockPosition;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileTransportDuctLongRange extends TileTransportDuctBase {
    @Override
    public MultiBlockGrid getNewGrid() {
        return null;
    }

    byte d1, d2;
    byte connections;

    @Override
    public void onNeighborBlockChange() {
        d1 = -1;
        d2 = -1;

        for (byte i = 0; i < 6; i++) {
            if (connectionTypes[i] == ConnectionTypes.REJECTED)
                connectionTypes[i] = ConnectionTypes.NORMAL;
        }

        super.onNeighborBlockChange();

        connections = 0;
        for (byte i = 5; i > 0; i--) {
            if (neighborTypes[i] != NeighborTypes.NONE) {
                if (connections == 0) {
                    d1 = i;
                    connections = 1;
                } else if (connections == 1) {
                    d2 = i;
                    connections = 2;
                } else {
                    neighborMultiBlocks[i] = null;
                    neighborTypes[i] = NeighborTypes.NONE;
                }
            }
        }

//        if (connections == 2) {
//            for (int i = 0; i < 6; i++) {
//                if (i != d1 && i != d2 && connectionTypes[i] == ConnectionTypes.NORMAL) {
//                    connectionTypes[i] = ConnectionTypes.REJECTED;
//                }
//            }
//        }
    }

    @Override
    public void onNeighborTileChange(int tileX, int tileY, int tileZ) {
        int i = BlockHelper.determineAdjacentSide(this, tileX, tileY, tileZ);
        d1 = -1;
        d2 = -1;

        if (connectionTypes[i] == ConnectionTypes.REJECTED)
            connectionTypes[i] = ConnectionTypes.NORMAL;

        super.onNeighborTileChange(tileX, tileY, tileZ);

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
                    connectionTypes[j] = ConnectionTypes.REJECTED;
                    if(neighborMultiBlocks[j] != null && ((TileTDBase)neighborMultiBlocks[j]).connectionTypes[j ^ 1] != ConnectionTypes.REJECTED ) {
                        neighborMultiBlocks[j].setNotConnected((byte)(j ^ 1));
                        ((TileTDBase)neighborMultiBlocks[j]).connectionTypes[j ^ 1] = ConnectionTypes.REJECTED;

                        neighborMultiBlocks[j] = null;
                    }
                    neighborTypes[j] = NeighborTypes.NONE;
                }
            }
        }

//        if (connections == 2) {
//            for (int j = 0; j < 6; j++) {
//                if (j != d1 && j != d2 && connectionTypes[j] == ConnectionTypes.NORMAL) {
//                    connectionTypes[j] = ConnectionTypes.REJECTED;
//                }
//            }
//        }
    }

    @Override
    public void formGrid() {

    }

    @Override
    public boolean isValidForForming() {
        return false;
    }

    public byte nextDirection(byte k) {
        if (connections != 2)
            return -1;
        if ((k ^ 1) == d1)
            return d2;
        else if ((k ^ 1) == d2)
            return d1;
        else
            return -1;
    }

    @Override
    public void handleTileSideUpdate(int i) {
        super.handleTileSideUpdate(i);
    }

    @Override
    public boolean advanceEntity(EntityTransport t) {
        int v = t.progress;
        v += t.step + (t.step);
        t.progress = (byte)(v % EntityTransport.PIPE_LENGTH);
        if (v >= EntityTransport.PIPE_LENGTH) {
            if (neighborTypes[t.direction] == TileTDBase.NeighborTypes.MULTIBLOCK
                    && connectionTypes[t.direction] == TileTDBase.ConnectionTypes.NORMAL) {
                TileTransportDuctBase newHome = (TileTransportDuctBase) getConnectedSide(t.direction);
                newHome.onNeighborBlockChange();
                if (newHome != null && newHome.neighborTypes[t.direction ^ 1] == TileTDBase.NeighborTypes.MULTIBLOCK) {
                    t.pos = new BlockPosition(newHome);

                    t.oldDirection = t.direction;

                    if (newHome instanceof TileTransportDuctLongRange) {
                        TileTransportDuctLongRange lr = (TileTransportDuctLongRange) newHome;
                        t.direction = lr.nextDirection(t.direction);
                        if(t.direction == -1){
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
            }
        } else if (t.progress >= EntityTransport.PIPE_LENGTH2 && t.progress - t.step < EntityTransport.PIPE_LENGTH2) {
            if (neighborTypes[t.direction] == TileTDBase.NeighborTypes.NONE) {
                t.dropPassenger();
            }
        }
        return false;
    }

    @Override
    public boolean advanceEntityClient(EntityTransport t) {
        int v = t.progress;
        v += t.step + (t.step);
        t.progress = (byte)(v % EntityTransport.PIPE_LENGTH);
        if (v >= EntityTransport.PIPE_LENGTH) {
            if (!t.trySimpleAdvance()) return true;
        }
        return false;
    }

    @Override
    public boolean isConnectable(TileEntity theTile, int side) {
        return theTile instanceof TileTransportDuctLongRange || theTile instanceof TileTransportDuctCrossover;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {

        super.writeToNBT(nbt);
        nbt.setByte("SimpleConnect", connections);
        nbt.setByte("SimpleConnect1", d1);
        nbt.setByte("SimpleConnect2", d2);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {

        super.readFromNBT(nbt);

        connections = nbt.getByte("SimpleConnect");
        d1 = nbt.getByte("SimpleConnect1");
        d2 = nbt.getByte("SimpleConnect2");
    }
}
