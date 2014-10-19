package thermaldynamics.multiblock;

import net.minecraft.world.World;

import java.util.HashMap;
import java.util.LinkedList;

public class MultiBlockGridWithRoutes extends MultiBlockGrid {
    public MultiBlockGridWithRoutes(World world) {
        super(world);
    }

    public HashMap<IMultiBlockRoute, RouteCache> routeCacheMap = new HashMap<IMultiBlockRoute, RouteCache>();


    @Override
    public void onMajorGridChange() {
        if (!routeCacheMap.isEmpty())
            routeCacheMap.clear();
    }

    public LinkedList<Route> getRoutesFromOutputRange(IMultiBlockRoute start, int maxRange) {
        RouteCache cache = routeCacheMap.get(start);
        if (cache == null || cache.maxPathLength < maxRange) {
            cache = new RouteCache(start);
            cache.generateCache();
            routeCacheMap.put(start, cache);
        }

        return cache.outputRoutes;
    }

    public LinkedList<Route> getRoutesFromOutput(IMultiBlockRoute start) {
        RouteCache cache = routeCacheMap.get(start);
        if (cache == null) {
            cache = new RouteCache(start);
            cache.generateCache();
            routeCacheMap.put(start, cache);
        }

        return cache.outputRoutes;
    }

}
