package thermaldynamics.ducts.energy.subgrid;

import net.minecraft.world.World;

import thermaldynamics.core.TDProps;

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
