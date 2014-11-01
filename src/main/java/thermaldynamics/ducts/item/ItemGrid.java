package thermaldynamics.ducts.item;

import net.minecraft.world.World;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGridWithRoutes;

public class ItemGrid extends MultiBlockGridWithRoutes {
    public ItemGrid(World world) {
        super(world);
    }

    @Override
    public void tickGrid() {
        for (IMultiBlock m : nodeSet) {
            m.tickPass(0);
        }
        for (IMultiBlock m : idleSet) {
            m.tickPass(0);
        }

        super.tickGrid();
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {

        return aBlock instanceof TileItemDuct;
    }
}
