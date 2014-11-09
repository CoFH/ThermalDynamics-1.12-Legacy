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
            if (!m.tickPass(0))
                break;
        }
        for (IMultiBlock m : idleSet) {
            if (!m.tickPass(0))
                break;
        }

        super.tickGrid();
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {

        return aBlock instanceof TileItemDuct;
    }
}
