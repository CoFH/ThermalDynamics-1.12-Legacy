package cofh.thermaldynamics.duct.entity;

import codechicken.lib.raytracer.IndexedCuboid6;
import cofh.CoFHCore;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.item.RouteInfo;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.IGridTileRoute;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.List;

public abstract class DuctUnitTransportBase extends DuctUnit<DuctUnitTransportBase, TransportGrid, DuctUnitTransportBase.TransportDestination> implements IGridTileRoute<DuctUnitTransportBase, TransportGrid> {

	public DuctUnitTransportBase(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public DuctToken<DuctUnitTransportBase, TransportGrid, DuctUnitTransportBase.TransportDestination> getToken() {
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

	public abstract boolean advanceEntity(EntityTransport transport);

	public IGridTile getPhysicalConnectedSide(byte direction) {

		return super.getConnectedSide(direction);
	}

	public boolean advanceEntityClient(EntityTransport t) {

		t.progress += t.step;
		if (t.progress >= EntityTransport.PIPE_LENGTH) {
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
		return false;
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
		return pipeCache[side];
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

	public static class TransportDestination {

	}
}
