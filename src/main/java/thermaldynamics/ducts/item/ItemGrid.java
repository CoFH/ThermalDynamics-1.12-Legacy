package thermaldynamics.ducts.item;

import cofh.repack.codechicken.lib.vec.BlockCoord;
import net.minecraft.world.World;
import thermaldynamics.block.Attachment;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGridWithRoutes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class ItemGrid extends MultiBlockGridWithRoutes {
    public ItemGrid(World world) {
        super(world);
    }

    public int travelingItemsCount = 0;
    public static ArrayList<Attachment> toTick = new ArrayList<Attachment>();
    public HashMap<BlockCoord, LinkedList<TravelingItem>> travelingItems = new HashMap<BlockCoord, LinkedList<TravelingItem>>();
    public boolean shouldRepoll = true;
    public boolean repoll = false;

    @Override
    public void tickGrid() {
        if (shouldRepoll) {
            repoll = true;
            if (!travelingItems.isEmpty())
                travelingItems.clear();
            travelingItemsCount = 0;
        } else
            repoll = false;

        shouldRepoll = false;

        for (IMultiBlock m : nodeSet) {
            if (!m.tickPass(0))
                break;
        }
        for (IMultiBlock m : idleSet) {
            if (!m.tickPass(0))
                break;
        }

        if (!toTick.isEmpty()) {
            for (Attachment attachment : toTick) {
                attachment.tick(1);
            }

            for (Attachment attachment : toTick) {
                attachment.tick(2);
            }

            toTick.clear();
        }

        super.tickGrid();
    }

    @Override
    public boolean canAddBlock(IMultiBlock aBlock) {

        return aBlock instanceof TileItemDuct;
    }

    public void poll(TravelingItem item) {
        BlockCoord dest = item.getDest();
        LinkedList<TravelingItem> list = travelingItems.get(dest);
        if (list == null) {
            list = new LinkedList<TravelingItem>();
            travelingItems.put(dest, list);
        }

        if (list.add(item))
            travelingItemsCount++;
    }

    @Override
    public void onMinorGridChange() {
        super.onMinorGridChange();
        shouldRepoll = true;
    }

    @Override
    public void onMajorGridChange() {
        shouldRepoll = true;
    }
}
