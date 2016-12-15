package cofh.thermaldynamics.duct.item;

import codechicken.lib.vec.Vector3;
import cofh.core.network.PacketCoFHBase;
import cofh.core.util.CoreUtils;
import cofh.lib.util.helpers.BlockHelper;
import codechicken.lib.vec.BlockCoord;
import cofh.thermaldynamics.block.TileTDBase.ConnectionTypes;
import cofh.thermaldynamics.block.TileTDBase.NeighborTypes;
import cofh.thermaldynamics.core.TickHandlerClient;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TravelingItem {

	public ItemStack stack;

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
	public StackMap.ItemEntry stackItemEntry;

	public boolean shouldDie = false;
	public int step = 1;

	public TravelingItem(ItemStack theItem, IMultiBlock start, Route itemPath, byte oldDirection, byte speed) {

		this(theItem, start.x(), start.y(), start.z(), itemPath, oldDirection, speed);
	}

	public TravelingItem(ItemStack theItem, int xCoord, int yCoord, int zCoord, Route itemPath, byte oldDirection, byte speed) {

		progress = 0;
		direction = itemPath.getNextDirection();
		myPath = itemPath;

		startX = xCoord;
		startY = yCoord;
		startZ = zCoord;
		stack = theItem;
		this.oldDirection = oldDirection;
		this.step = speed;

		if (myPath != null && myPath.endPoint != null) {
			destX = myPath.endPoint.x();
			destY = myPath.endPoint.y();
			destZ = myPath.endPoint.z();
			hasDest = true;
		}
	}

	// Client Only
	public TravelingItem(byte progress, byte direction, byte oldDirection, ItemStack theItem, TileItemDuct homeTile, byte step) {

		this.progress = progress;
		this.direction = direction;
		this.oldDirection = oldDirection;
		stack = theItem;
		this.step = step;
	}

	public void tickForward(TileItemDuct homeTile) {

		progress += step;

		if (myPath == null) {
			bounceItem(homeTile);
		} else if (progress >= homeTile.getPipeLength()) {
			progress %= homeTile.getPipeLength();
			advanceTile(homeTile);
		} else if (progress >= homeTile.getPipeHalfLength() && progress - step < homeTile.getPipeHalfLength()) {
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
					homeTile.removeItem(this, false);
					newHome.transferItem(this);
					if (myPath.hasNextDirection()) {
						oldDirection = direction;
						direction = myPath.getNextDirection();
					} else {
						reRoute = true;
					}
				}
			}
		} else if (homeTile.neighborTypes[direction] == NeighborTypes.OUTPUT && homeTile.connectionTypes[direction] == ConnectionTypes.NORMAL) {
			stack.stackSize = homeTile.insertIntoInventory(stack.copy(), direction);

			if (stack.stackSize > 0) {
				bounceItem(homeTile);
				return;
			}
			homeTile.removeItem(this, true);
		} else if (homeTile.neighborTypes[direction] == NeighborTypes.INPUT && goingToStuff) {
			if (homeTile.canStuffItem()) {
				goingToStuff = false;
				homeTile.stuffItem(this);
				homeTile.removeItem(this, true);
			} else {
				goingToStuff = false;
				bounceItem(homeTile);
			}
		} else {
			bounceItem(homeTile);
		}
	}

	public void bounceItem(TileItemDuct homeTile) {

		RouteCache routes = homeTile.getCache();

		TileItemDuct.RouteInfo curInfo;

		reRoute = false;

		if (hasDest) {
			for (Route aRoute : routes.outputRoutes) {
				if (aRoute.endPoint.isNode() && aRoute.endPoint.x() == destX && aRoute.endPoint.y() == destY && aRoute.endPoint.z() == destZ) {
					curInfo = aRoute.endPoint.canRouteItem(stack);

					if (curInfo.canRoute) {
						myPath = aRoute.copy();
						myPath.pathDirections.add(curInfo.side);
						oldDirection = (byte) (direction ^ 1);
						direction = myPath.getNextDirection();
						homeTile.hasChanged = true;
						return;
					}
				}
			}

			if (homeTile.ticksExisted < TileItemDuct.maxTicksExistedBeforeFindAlt) {
				return;
			}
		}

		if (!hasDest || (!mustGoToDest && hasDest)) {
			for (Route aRoute : routes.outputRoutes) {
				if (aRoute.endPoint.isNode()) {
					curInfo = aRoute.endPoint.canRouteItem(stack);
					if (curInfo.canRoute) {
						myPath = aRoute.copy();
						myPath.pathDirections.add(curInfo.side);
						oldDirection = (byte) (direction ^ 1);
						direction = myPath.getNextDirection();
						homeTile.hasChanged = true;
						hasDest = true;
						destX = myPath.endPoint.x();
						destY = myPath.endPoint.y();
						destZ = myPath.endPoint.z();
						return;
					}
				}
			}
		}

		if (homeTile.ticksExisted <= TileItemDuct.maxTicksExistedBeforeStuff) {
			return;
		}

		// Failed to find an exit
		if (homeTile.acceptingStuff()) {
			byte d = homeTile.getStuffedSide();
			if (d == direction) {
				homeTile.stuffItem(this);
				homeTile.removeItem(this, true);
			} else {
				myPath = new Route(homeTile);
				myPath.pathDirections.add(myPath.endPoint.getStuffedSide());
				oldDirection = (byte) (direction ^ 1);
				direction = myPath.getNextDirection();
				homeTile.hasChanged = true;
			}
		} else {
			Route stuffedRoute = getStuffedRoute(routes);
			if (stuffedRoute != null) {
				goingToStuff = true;
				myPath = stuffedRoute;
				myPath.pathDirections.add(myPath.endPoint.getStuffedSide());
				oldDirection = (byte) (direction ^ 1);
				direction = myPath.getNextDirection();
				homeTile.hasChanged = true;
			} else if (homeTile.ticksExisted == TileItemDuct.maxTicksExistedBeforeDump) {
				CoreUtils.dropItemStackIntoWorld(stack, homeTile.getWorld(), new Vector3(homeTile.getPos()));
				homeTile.removeItem(this, true);
			}
		}
	}

	public Route getStuffedRoute(RouteCache homeTile) {

		if (homeTile.stuffableRoutes.isEmpty()) {
			return null;
		}

		Route backup = null;
		for (Route aRoute : homeTile.stuffableRoutes) {
			if (aRoute.endPoint.acceptingStuff()) {
				if (backup == null) {
					backup = aRoute.copy();
				}

				if (aRoute.endPoint.x() == startX && aRoute.endPoint.y() == startY && aRoute.endPoint.z() == startZ) {
					return aRoute.copy();
				}
			}
		}

		return backup;
	}

	public void tickClientForward(TileItemDuct homeTile) {

		progress += step;

		if (progress >= homeTile.getPipeLength()) {
			progress %= homeTile.getPipeLength();

			if (shouldDie) {
				homeTile.removeItem(this, true);
			} else {
				homeTile.removeItem(this, false);
				shouldDie = true;
				TileEntity newTile = BlockHelper.getAdjacentTileEntity(homeTile, direction);
				if (newTile instanceof TileItemDuct) {
					TileItemDuct itemDuct = (TileItemDuct) newTile;
					oldDirection = direction;
					itemDuct.myItems.add(this);
					if (!TickHandlerClient.tickBlocks.contains(itemDuct) && !TickHandlerClient.tickBlocksToAdd.contains(itemDuct)) {
						TickHandlerClient.tickBlocksToAdd.add(itemDuct);
					}
				}
			}
		}
	}

	public void writePacket(PacketCoFHBase myPayload) {

		myPayload.addByte(progress);
		myPayload.addByte(direction);
		myPayload.addByte(oldDirection);
		myPayload.addItemStack(stack);
		myPayload.addByte(step);
	}

	public static TravelingItem fromPacket(PacketCoFHBase payload, TileItemDuct homeTile) {

		return new TravelingItem(payload.getByte(), payload.getByte(), payload.getByte(), payload.getItemStack(), homeTile, payload.getByte());
	}

	public void toNBT(NBTTagCompound theNBT) {

		theNBT.setTag("stack", new NBTTagCompound());
		stack.writeToNBT(theNBT.getCompoundTag("stack"));

		theNBT.setByte("progress", progress);
		theNBT.setByte("direction", direction);
		theNBT.setByte("oldDir", oldDirection);
		theNBT.setBoolean("gts", goingToStuff);

		theNBT.setInteger("step", step);

		if (hasDest) {
			theNBT.setInteger("destX", destX);
			theNBT.setInteger("destY", destY);
			theNBT.setInteger("destZ", destZ);
			theNBT.setBoolean("mustGo", mustGoToDest);
		}
		theNBT.setInteger("startX", startX);
		theNBT.setInteger("startY", startY);
		theNBT.setInteger("startZ", startZ);

		if (myPath != null) {
			theNBT.setByteArray("route", myPath.toByteArray());
		}
	}

	public TravelingItem(NBTTagCompound theNBT) {

		stack = ItemStack.loadItemStackFromNBT(theNBT.getCompoundTag("stack"));
		if (stack.getItem() == null) {
			stack = null;
		}

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

		step = theNBT.getByte("step");

		startX = theNBT.getInteger("startX");
		startY = theNBT.getInteger("startY");
		startZ = theNBT.getInteger("startZ");

		if (theNBT.hasKey("route", 7)) {
			myPath = new Route(theNBT.getByteArray("route"));
		}
	}

	// DOWN, UP, NORTH, SOUTH, WEST, EAST

	public BlockPos getDest() {

		if (myPath == null) {
			return null;
		}
		if (myPath.dest == null) {
			if (myPath.endPoint == null) {
				if (!hasDest) {
					return null;
				}
				myPath.dest = (new BlockPos(destX, destY, destZ).offset(EnumFacing.VALUES[getLastSide()]));
			} else {
				myPath.dest = (new BlockPos(myPath.endPoint.x(), myPath.endPoint.y(), myPath.endPoint.z())).offset(EnumFacing.VALUES[getLastSide()]);
			}

		}
		return myPath.dest;
	}

	public StackMap.ItemEntry getStackEntry() {

		if (stack == null) {
			return null;
		}
		if (stackItemEntry == null || stackItemEntry.side != getLastSide()) {
			stackItemEntry = new StackMap.ItemEntry(stack, getLastSide());
		}
		return stackItemEntry;
	}

	public int getLastSide() {

		return myPath.pathDirections.isEmpty() ? direction : myPath.getLastSide();
	}

}
