package thermaldynamics.ducts.item;


import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.core.TickHandlerClient;
import thermaldynamics.multiblock.Route;
import thermalexpansion.util.Utils;

import static thermaldynamics.block.TileMultiBlock.ConnectionTypes;
import static thermaldynamics.block.TileMultiBlock.NeighborTypes;

public class TravelingItem {

    public ItemStack stack;
    public float x;
    public float y;
    public float z;
    public byte progress;
    public byte direction;
    public byte oldDirection;
    public Route myPath;
    public boolean goingToStuff = false;
    public int startX;
    public int startY;
    public int startZ;
    public int destX;
    public int destY;
    public int destZ;
    public boolean mustGoToDest = false;
    public boolean hasDest = false;
    public boolean reRoute = false;

    public boolean shouldDie = false;

    public TravelingItem(ItemStack theItem, int xCoord, int yCoord, int zCoord, Route itemPath, byte oldDirection) {
        progress = 0;
        direction = itemPath.getNextDirection();
        myPath = itemPath;
        x = xCoord;
        y = yCoord;
        z = zCoord;
        startX = xCoord;
        startY = yCoord;
        startZ = zCoord;
        stack = theItem;
        this.oldDirection = oldDirection;
    }

    // Client Only
    public TravelingItem(byte progress, byte direction, byte oldDirection, ItemStack theItem, TileItemDuct homeTile) {
        this.progress = progress;
        this.direction = direction;
        this.oldDirection = oldDirection;
        stack = theItem;
        calcCoordsFromProgress(this, homeTile);
    }

    public void tickForward(TileItemDuct homeTile) {
        progress++;
        progress %= homeTile.getPipeLength();

        calcCoordsFromProgress(this, homeTile);

        if (myPath == null) {
            bounceItem(homeTile);
        } else if (progress == 0) {
            advanceTile(homeTile);
        } else if (progress % homeTile.getPipeHalfLength() == 0) {
            if (reRoute || homeTile.neighborTypes[direction] == NeighborTypes.NONE) {
                bounceItem(homeTile);
            }
        }
    }

    public void advanceTile(TileItemDuct homeTile) {
        if (homeTile.neighborTypes[direction] == NeighborTypes.MULTIBLOCK && homeTile.connectionTypes[direction] == ConnectionTypes.NORMAL) {
            TileItemDuct newHome = (TileItemDuct) homeTile.getConnectedSide(direction);
            if (newHome != null) {
                if (newHome.neighborTypes[direction ^ 1] == NeighborTypes.MULTIBLOCK) {
                    homeTile.removeItem(this);
                    if (myPath.pathPos == 0 && goingToStuff) {
                        if (newHome.canStuffItem()) {
                            goingToStuff = false;
                            newHome.stuffItem(this);
                        } else {
                            bounceItem(newHome);
                        }
                        return;
                    }
                    newHome.insertItem(this);
                    if (myPath.hasNextDirection()) {
                        oldDirection = direction;
                        direction = myPath.getNextDirection();
                    } else {
                        reRoute = true;
                    }
                }
            }
        } else if (homeTile.neighborTypes[direction] == NeighborTypes.TILE && homeTile.connectionTypes[direction] == ConnectionTypes.NORMAL) {
            if (myPath.pathPos >= myPath.pathWeight && goingToStuff) {
                if (homeTile.canStuffItem()) {
                    goingToStuff = false;
                    homeTile.stuffItem(this);
                    homeTile.removeItem(this);
                } else {
                    goingToStuff = false;
                    bounceItem(homeTile);
                }
                return;
            }
            if (homeTile.cacheType[direction] != TileMultiBlock.CacheTypes.NOT_SET) {
                stack.stackSize = Utils.addToInventory(homeTile.getCachedTileEntity(direction), direction, stack);

                if (stack.stackSize > 0) {
                    bounceItem(homeTile);
                    return;
                }
                homeTile.removeItem(this);
            } else {
                bounceItem(homeTile);
            }
        } else {
            bounceItem(homeTile);
        }
    }

    public void bounceItem(TileItemDuct homeTile) {
//
//        homeTile.cacheRoutes();
//        routeInfo curInfo;
//
//        if (hasDest) {
//            for (Route aRoute : homeTile.validOutputs) {
//                if (aRoute.endPoint.isNode && aRoute.endPoint.x() == destX && aRoute.endPoint.y() == destY && aRoute.endPoint.z() == destZ) {
//                    curInfo = aRoute.endPoint.canRouteItem(stack, aRoute.pathWeight == 0, aRoute.endPoint.moveStackSize[aRoute.getLastSide()]);
//
//                    if (curInfo.canRoute) {
//                        myPath = aRoute.copy();
//                        myPath.pathDirections.add(curInfo.side);
//                        oldDirection = direction ^ 1;
//                        direction = myPath.getNextDirection();
//                        homeTile.hasChanged = true;
//                        return;
//                    }
//                }
//            }
//        }
//        if (!hasDest || !mustGoToDest && hasDest) {
//            for (Route aRoute : homeTile.validOutputs) {
//                if (aRoute.endPoint.isNode) {
//                    curInfo = aRoute.endPoint.canRouteItem(stack, aRoute.pathWeight == 0, aRoute.endPoint.moveStackSize[aRoute.getLastSide()]);
//                    if (curInfo.canRoute) {
//                        myPath = aRoute.copy();
//                        myPath.pathDirections.add(curInfo.side);
//                        oldDirection = direction ^ 1;
//                        direction = myPath.getNextDirection();
//                        homeTile.hasChanged = true;
//                        return;
//                    }
//                }
//            }
//        }
//        if (homeTile.isInput) {
//            homeTile.removeItem(this);
//            homeTile.stuffItem(this);
//        } else if (!homeTile.validStuffable.isEmpty()) {
//            goingToStuff = true;
//            myPath = getStuffedRoute(homeTile);
//            myPath.pathDirections.add(myPath.endPoint.getStuffedSide());
//            oldDirection = direction ^ 1;
//            direction = myPath.getNextDirection();
//            homeTile.hasChanged = true;
//        } else {
        //CoreUtils.dropItemStackIntoWorld(stack, homeTile.getWorldObj(), homeTile.x(), homeTile.y(), homeTile.z());
        homeTile.removeItem(this);
//        }

    }

//    public Route getStuffedRoute(TileItemDuct homeTile) {
//        for (Route aRoute : homeTile.validStuffable) {
//            if (aRoute.endPoint.x() == startX && aRoute.endPoint.y() == startY && aRoute.endPoint.z() == startZ) {
//                return aRoute.copy();
//            }
//        }
//        return homeTile.validStuffable.getFirst().copy();
//    }

    public void tickClientForward(TileItemDuct homeTile) {
        progress++;
        progress %= homeTile.getPipeLength();

        if (!shouldDie || (progress <= homeTile.getPipeHalfLength())) {
            moveCoordsByProgress(progress, this, homeTile);
        }
        if (progress == 0) {
            if (shouldDie) {
                homeTile.removeItem(this);
            } else {
                homeTile.removeItem(this);
                shouldDie = true;
                TileEntity newTile = BlockHelper.getAdjacentTileEntity(homeTile, direction);
                if (newTile instanceof TileItemDuct) {
                    TileItemDuct itemDuct = (TileItemDuct) newTile;
                    oldDirection = direction;
                    itemDuct.myItems.add(this);
                    if (!TickHandlerClient.tickBlocks.contains(itemDuct)
                            && !TickHandlerClient.tickBlocksToAdd.contains(itemDuct)) {
                        TickHandlerClient.tickBlocksToAdd.add(itemDuct);
                    }
                    calcCoordsFromProgress(this, itemDuct);

                }
            }
        }
    }

    public static void calcCoordsFromProgress(TravelingItem theItem, TileItemDuct homeTile) {

        theItem.x = START_COORD[theItem.oldDirection][0];
        theItem.y = START_COORD[theItem.oldDirection][1];
        theItem.z = START_COORD[theItem.oldDirection][2];

        for (int i = 0; i < theItem.progress; i++) {
            moveCoordsByProgress(i, theItem, homeTile);
        }
    }

    public static void moveCoordsByProgress(int Progress, TravelingItem theItem, TileItemDuct homeTile) {

        if (Progress <= homeTile.getPipeHalfLength()) {
            theItem.x += homeTile.getSideCoordsModifier()[theItem.oldDirection][0];
            theItem.y += homeTile.getSideCoordsModifier()[theItem.oldDirection][1];
            theItem.z += homeTile.getSideCoordsModifier()[theItem.oldDirection][2];
        } else {
            if (theItem.direction >= 0) {
                theItem.x += homeTile.getSideCoordsModifier()[theItem.direction][0];
                theItem.y += homeTile.getSideCoordsModifier()[theItem.direction][1];
                theItem.z += homeTile.getSideCoordsModifier()[theItem.direction][2];
            }
        }
    }

    public void writePacket(PacketCoFHBase myPayload) {
        myPayload.addByte(progress);
        myPayload.addByte(direction);
        myPayload.addByte(oldDirection);
        myPayload.addItemStack(stack);
    }

    public static TravelingItem fromPacket(PacketCoFHBase payload, TileItemDuct homeTile) {

        return new TravelingItem(payload.getByte(), payload.getByte(), payload.getByte(), payload.getItemStack(), homeTile);
    }

    public void toNBT(NBTTagCompound theNBT) {

        theNBT.setTag("stack", new NBTTagCompound());
        stack.writeToNBT(theNBT.getCompoundTag("stack"));

        theNBT.setFloat("x", x);
        theNBT.setFloat("y", y);
        theNBT.setFloat("z", z);
        theNBT.setByte("progress", progress);
        theNBT.setByte("direction", direction);
        theNBT.setByte("oldDir", oldDirection);
        theNBT.setBoolean("gts", goingToStuff);

        if (hasDest) {
            theNBT.setInteger("destX", myPath.endPoint.x());
            theNBT.setInteger("destY", myPath.endPoint.y());
            theNBT.setInteger("destZ", myPath.endPoint.z());
            theNBT.setBoolean("mustGo", mustGoToDest);
        }
        theNBT.setInteger("startX", startX);
        theNBT.setInteger("startY", startY);
        theNBT.setInteger("startZ", startZ);
    }

    public TravelingItem(NBTTagCompound theNBT) {

        stack = ItemStack.loadItemStackFromNBT(theNBT.getCompoundTag("stack"));
        x = theNBT.getFloat("x");
        y = theNBT.getFloat("y");
        z = theNBT.getFloat("z");
        progress = theNBT.getByte("progress");
        direction = theNBT.getByte("direction");
        oldDirection = theNBT.getByte("oldDir");
        goingToStuff = theNBT.getBoolean("goingToStuff");

        if (theNBT.hasKey("destX")) {
            hasDest = true;
            destX = theNBT.getInteger("destX");
            destY = theNBT.getInteger("destY");
            destZ = theNBT.getInteger("destZ");
            mustGoToDest = theNBT.getBoolean("mustGo");
        }

        startX = theNBT.getInteger("startX");
        startY = theNBT.getInteger("startY");
        startZ = theNBT.getInteger("startZ");

    }

    public static final float[][] START_COORD = {{0.5F, 1, 0.5F}, {0.5F, 0, 0.5F}, {0.5F, 0.5F, 1}, {0.5F, 0.5F, 0}, {1, 0.5F, 0.5F},
            {0, 0.5F, 0.5F}};
    // DOWN, UP, NORTH, SOUTH, WEST, EAST

}
