package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.item.RouteInfo;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitTransportBaseRoute extends DuctUnitTransportBase {

	public DuctUnitTransportBaseRoute(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public TransportGrid createGrid() {

		return new TransportGrid(world());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitTransportBase, TransportGrid, Void> adjDuct, byte side) {
		return true;
	}

	@Nullable
	@Override
	public Void cacheTile(@Nonnull TileEntity tile, byte side) {
		return null;
	}

	public RouteCache<DuctUnitTransportBase, TransportGrid> getCache() {

		return getCache(true);
	}

	public RouteCache<DuctUnitTransportBase, TransportGrid> getCache(boolean urgent) {

		assert grid != null;
		return urgent ? grid.getRoutesFromOutput(this) : grid.getRoutesFromOutputNonUrgent(this);
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

	@Override
	public boolean advanceEntity(EntityTransport t) {

		t.progress += t.step;
		if (t.myPath == null) {
			t.bouncePassenger(this);
		} else if (t.progress >= EntityTransport.PIPE_LENGTH) {
			t.progress %= EntityTransport.PIPE_LENGTH;
			advanceToNextTile(t);
		} else if (t.progress >= EntityTransport.PIPE_LENGTH2 && t.progress - t.step < EntityTransport.PIPE_LENGTH2) {
			if (t.reRoute || pipeCache[t.direction] != null) {
				t.bouncePassenger(this);
			}
		}
		return false;
	}

	public void advanceToNextTile(EntityTransport t) {

		t.advanceTile(this);
	}
}
