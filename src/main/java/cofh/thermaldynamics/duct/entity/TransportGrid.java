package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGridWithRoutes;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.world.World;

public class TransportGrid extends MultiBlockGridWithRoutes {

	public TransportGrid(World world) {

		super(world);
	}

	ArrayList<EntityTransport> travellers = new ArrayList<EntityTransport>();

	@Override
	public void tickGrid() {

		super.tickGrid();
		for (Iterator<EntityTransport> iterator = travellers.iterator(); iterator.hasNext();) {
			EntityTransport traveller = iterator.next();
			if (traveller.isDead || traveller.riddenByEntity == null) {
				iterator.remove();
			}
		}
	}

	@Override
	public boolean canAddBlock(IMultiBlock aBlock) {

		return aBlock instanceof TileTransportDuct;
	}
}
