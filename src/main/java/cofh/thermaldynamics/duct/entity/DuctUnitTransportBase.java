package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.item.RouteInfo;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.DuctUnit;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.IGridTileRoute;
import cofh.thermaldynamics.multiblock.Route;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public abstract class DuctUnitTransportBase extends DuctUnit<DuctUnitTransportBase, GridTransport, DuctUnitTransportBase.TransportDestination> implements IGridTileRoute<DuctUnitTransportBase, GridTransport> {

	public DuctUnitTransportBase(TileGrid parent, Duct duct) {

		super(parent, duct);
	}

	@Nonnull
	@Override
	public DuctToken<DuctUnitTransportBase, GridTransport, DuctUnitTransportBase.TransportDestination> getToken() {

		return DuctToken.TRANSPORT;
	}

	//	@Override
	//	public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {
	//
	//		EntityPlayer player = CoFHCore.proxy.getClientPlayer();
	//		if (player != null && player.getRidingEntity() != null && player.getRidingEntity().getClass() == EntityTransport.class) {
	//			return;
	//		}
	//		super.addTraceableCuboids(cuboids);
	//	}

	public void advanceEntity(EntityTransport t) {

		t.progress += t.step;
		if (t.myPath == null) {
			t.bouncePassenger(this);
		} else if (t.progress >= EntityTransport.DUCT_LENGTH) {
			t.progress %= EntityTransport.DUCT_LENGTH;
			advanceToNextTile(t);
		} else if (t.progress >= EntityTransport.DUCT_LENGTH2 && t.progress - t.step < EntityTransport.DUCT_LENGTH2) {
			if (t.reRoute || getRenderConnectionType(t.direction) == BlockDuct.ConnectionType.NONE) {
				t.bouncePassenger(this);
			}
		}
	}

	public void advanceToNextTile(EntityTransport t) {

	}

	public IGridTile getPhysicalConnectedSide(byte direction) {

		return super.getConnectedSide(direction);
	}

	public boolean advanceEntityClient(EntityTransport t) {

		t.progress += t.step;
		if (t.progress >= EntityTransport.DUCT_LENGTH) {
			if (!t.trySimpleAdvance()) {
				return true;
			}
		}
		return false;
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

		return nodeMask != 0;
	}

	@Override
	public int getMaxRange() {

		return 0;
	}

	@Override
	public ConnectionType getConnectionType(byte side) {

		return parent.getConnectionType(side);
	}

	@Override
	public DuctUnitTransportBase getCachedTile(byte side) {

		return ductCache[side];
	}

	@Override
	public RouteInfo canRouteItem(ItemStack stack) {

		return RouteInfo.noRoute;
	}

	@Override
	public byte getStuffedSide() {

		return 0;
	}

	@Override
	public boolean acceptingStuff() {

		return false;
	}

	public abstract boolean isRoutable();

	public abstract boolean isCrossover();

	public boolean isLongRange() {

		return !isRoutable() && !isCrossover();
	}

	public boolean hasTooManyConnections() {

		return false;
	}

	public abstract Route getRoute(Entity entityTransport, int direction, byte step);

	public static class TransportDestination {

	}

	@Override
	protected TransportDestination[] createTileCache() {

		return new TransportDestination[6];
	}

	@Override
	protected DuctUnitTransportBase[] createDuctCache() {

		return new DuctUnitTransportBase[6];
	}
}
