package cofh.thermaldynamics.ducts.energy.subgrid;

import cofh.thermaldynamics.core.TDProps;

import net.minecraft.world.World;

public class EnergySubGridEnder extends EnergySubGrid {

	public EnergySubGridEnder(World world) {

		super(world, 16 * TDProps.ENDER_TRANSMIT_COST, 16 * TDProps.ENDER_TRANSMIT_COST);
	}

	public boolean isPowered() {

		return myStorage.getEnergyStored() >= TDProps.ENDER_TRANSMIT_COST;
	}

	@Override
	public void tickGrid() {

	}

}
