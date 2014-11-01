package thermaldynamics.ducts.energy;

import cofh.api.energy.EnergyStorage;
import net.minecraft.world.World;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class EnergySubGrid extends MultiBlockGrid {
    public EnergyStorage myStorage = new EnergyStorage(1000, 80);

    private int perStorage = 10;
    private int type = 0;

    public EnergySubGrid(World world, int type, int perStorage) {
        super(world);
        this.perStorage = perStorage;
        this.type = type;
    }

    @Override
    public void balanceGrid() {
        myStorage.setCapacity(idleSet.size() * perStorage);
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {
        return aBlock instanceof TileEnergyDuct && ((TileEnergyDuct) aBlock).type == this.type;
    }

    @Override
    public void tickGrid() {

    }

    @Override
    public boolean canGridsMerge(MultiBlockGrid grid) {
        return super.canGridsMerge(grid) && ((EnergySubGrid) grid).type == this.type;
    }


}
