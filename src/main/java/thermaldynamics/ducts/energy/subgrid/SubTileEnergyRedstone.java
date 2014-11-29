package thermaldynamics.ducts.energy.subgrid;

import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class SubTileEnergyRedstone extends SubTileEnergy {
    public EnergySubGridDistribute internalGrid;

    public SubTileEnergyRedstone(TileMultiBlock parent) {
        super(parent);
    }

    @Override
    public MultiBlockGrid getNewGrid() {
        return new EnergySubGridDistribute(parent.world(), 2400, 400);
    }

    @Override
    public void setGrid(MultiBlockGrid newGrid) {
        super.setGrid(newGrid);
        internalGrid = (EnergySubGridDistribute) newGrid;
    }
}
