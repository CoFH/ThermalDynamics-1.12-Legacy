package thermaldynamics.ducts.item;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.debughelper.DebugHelper;
import thermaldynamics.multiblock.Route;

import java.util.LinkedList;

public class ItemDuct {
    private final TileItemDuct parentTile;

    public ItemDuct(TileItemDuct tileItemDuct) {
        this.parentTile = tileItemDuct;
    }

    public boolean isSignificantTile(TileEntity theTile, int side) {
        return theTile instanceof IInventory;
    }

    public void tickPass(int pass) {
        if (parentTile.isOutput()) {
            //final LinkedList<Route> routesFromOutput = parentTile.internalGrid.getRoutesFromOutput(parentTile);
//            DebugHelper.log(routesFromOutput.size());
        }
    }
}
