package cofh.thermaldynamics.duct.tiles;

import codechicken.lib.raytracer.IndexedCuboid6;
import cofh.CoFHCore;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctTransport;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.entity.*;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileTransportDuct extends TileGrid {

	DuctUnitTransportBase transport;

	public TileTransportDuct() {

		this(TDDucts.transportBasic);
	}

	public TileTransportDuct(DuctTransport transportBasic) {

		transport = createDuctUnit(transportBasic);
	}

	@Nonnull
	public DuctUnitTransportBase createDuctUnit(DuctTransport transportBasic) {

		return new DuctUnitTransport(this, transportBasic);
	}

	@Nullable
	@Override
	public <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> token) {

		if (token == DuctToken.TRANSPORT) {
			return (T) transport;
		}
		return null;
	}

	@Override
	public Iterable<DuctUnit> getDuctUnits() {

		return ImmutableList.of(transport);
	}

	@Override
	public Duct getDuctType() {

		return transport.getDuctType();
	}

	@Override
	public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

		EntityPlayer player = CoFHCore.proxy.getClientPlayer();
		if (player != null && player.getRidingEntity() != null && player.getRidingEntity().getClass() == EntityTransport.class) {
			return;
		}
		super.addTraceableCuboids(cuboids);
	}

	public static class Linking extends TileTransportDuct {

		public Linking() {

			super(TDDucts.transportLinking);
		}

		@Nonnull
		@Override
		public DuctUnitTransportBase createDuctUnit(DuctTransport duct) {

			return new DuctUnitTransportLinking(this, duct);
		}
	}

	public static class LongRange extends TileTransportDuct {

		public LongRange() {

			super(TDDucts.transportLongRange);
		}

		@Nonnull
		@Override
		public DuctUnitTransportBase createDuctUnit(DuctTransport duct) {

			return new DuctUnitTransportLongRange(this, duct);
		}
	}

}
