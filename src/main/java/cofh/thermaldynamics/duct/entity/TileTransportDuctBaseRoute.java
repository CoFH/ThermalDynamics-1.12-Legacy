package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.IMultiBlockRoute;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class TileTransportDuctBaseRoute extends TileTransportDuctBase implements IMultiBlockRoute {

	public TransportGrid internalGrid;

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (TransportGrid) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new TransportGrid(worldObj);
	}

	public RouteCache getCache() {

		return getCache(true);
	}

	public RouteCache getCache(boolean urgent) {

		return urgent ? internalGrid.getRoutesFromOutput(this) : internalGrid.getRoutesFromOutputNonUrgent(this);
	}

	public Route getRoute(Entity entity, int side, byte speed) {

		if (entity == null || entity.isDead) {
			return null;
		}

		for (Route outputRoute : getCache().outputRoutes) {
			if (outputRoute.endPoint == this || !outputRoute.endPoint.isOutput()) {
				continue;
			}

			Route route = outputRoute.copy();
			byte outSide = outputRoute.endPoint.getStuffedSide();
			route.pathDirections.add(outSide);
			return route;
		}
		return null;
	}

	public EntityTransport findRoute(Entity entity, int side, byte speed) {

		Route route = getRoute(entity, side, speed);
		return route != null ? new EntityTransport(this, route, (byte) side, speed) : null;
	}

	@Override
	public int getWeight() {

		return 1;
	}

	@Override
	public boolean canStuffItem() {

		return false;
	}

	@Override
	public boolean isOutput() {

		return false;
	}

	@Override
	public int getMaxRange() {

		return Integer.MAX_VALUE;
	}

	@Override
	public NeighborTypes getCachedSideType(byte side) {

		return neighborTypes[side];
	}

	@Override
	public ConnectionTypes getConnectionType(byte side) {

		return connectionTypes[side];
	}

	@Override
	public IMultiBlock getCachedTile(byte side) {

		return neighborMultiBlocks[side];
	}

	@Override
	public TileItemDuct.RouteInfo canRouteItem(ItemStack stack) {

		return TileItemDuct.noRoute;
	}

	@Override
	public byte getStuffedSide() {

		for (byte i = 0; i < 6; i++) {
			if (neighborTypes[i] == NeighborTypes.OUTPUT) {
				return i;
			}
		}

		return 0;
	}

	@Override
	public boolean acceptingStuff() {

		return false;
	}

	@Override
	public boolean advanceEntity(EntityTransport t) {

		t.progress += t.step;
		if (t.myPath == null) {
			t.bouncePassenger(this);
		} else if (t.progress >= EntityTransport.PIPE_LENGTH) {
			t.progress %= EntityTransport.PIPE_LENGTH;
			advanceToNextTile(t);
		} else if (t.progress >= EntityTransport.PIPE_LENGTH2 && t.progress - t.step < EntityTransport.PIPE_LENGTH2) {
			if (t.reRoute || neighborTypes[t.direction] == TileTDBase.NeighborTypes.NONE) {
				t.bouncePassenger(this);
			}
		}
		return false;
	}

	public void advanceToNextTile(EntityTransport t) {

		t.advanceTile(this);
	}

	@Override
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileTransportDuctBaseRoute;
	}

}
