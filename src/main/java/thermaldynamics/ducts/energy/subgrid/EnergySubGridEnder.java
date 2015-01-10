package thermaldynamics.ducts.energy.subgrid;

import net.minecraft.world.World;
import thermaldynamics.ducts.item.PropsConduit;

public class EnergySubGridEnder extends EnergySubGrid {

    public EnergySubGridEnder(World world) {
        super(world, 16 * PropsConduit.ENDER_TRANSMIT_COST, 16 * PropsConduit.ENDER_TRANSMIT_COST);
    }

    public boolean isPowered() {
        return myStorage.getEnergyStored() >= PropsConduit.ENDER_TRANSMIT_COST;
    }

    @Override
    public void tickGrid() {

    }

}
