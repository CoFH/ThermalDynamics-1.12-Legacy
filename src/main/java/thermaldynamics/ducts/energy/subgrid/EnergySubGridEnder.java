package thermaldynamics.ducts.energy.subgrid;

import net.minecraft.world.World;
import thermaldynamics.ducts.item.PropsConduit;

public class EnergySubGridEnder extends EnergySubGrid {

    public EnergySubGridEnder(World world) {
        super(world, 16 * PropsConduit.ENDER_TRANSMIT_COST, 16 * PropsConduit.ENDER_TRANSMIT_COST);
        lastPowered = world.getTotalWorldTime();
    }

    long lastPowered;
    boolean powered = false;

    public boolean isPowered() {
        return powered || myStorage.getEnergyStored() >= PropsConduit.ENDER_TRANSMIT_COST;
    }

    @Override
    public void tickGrid() {
        if (myStorage.getEnergyStored() >= PropsConduit.ENDER_TRANSMIT_COST) {
            powered = true;
            lastPowered = worldGrid.worldObj.getTotalWorldTime();
        } else if (powered && lastPowered + PropsConduit.ENDER_UPDATE_DELAY < worldGrid.worldObj.getTotalWorldTime()) {
            powered = false;
        }
    }

}
