package thermaldynamics.multiblock;

import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class MultiBlockGridWithRoutes extends MultiBlockGrid {
    public MultiBlockGridWithRoutes(World world) {
        super(world);
    }

    @Override
    public void doTickProcessing(long deadline) {
        Iterator<RouteCache> iterator = calculatingRoutes.values().iterator();
        RouteCache routeCache = iterator.next();
        while (System.nanoTime() < deadline) {
            if (!routeCache.processStep()) {
                iterator.remove();
                routeCacheMap.put(routeCache.origin, routeCache);
                if (iterator.hasNext())
                    routeCache = iterator.next();
                else
                    return;
            }
        }
    }


    @Override
    public boolean isTickProcessing() {
        return !calculatingRoutes.isEmpty();
    }

    public HashMap<IMultiBlockRoute, RouteCache> routeCacheMap = new HashMap<IMultiBlockRoute, RouteCache>();
    public HashMap<IMultiBlockRoute, RouteCache> calculatingRoutes = new HashMap<IMultiBlockRoute, RouteCache>();

    @Override
    public void onMinorGridChange() {
        if (!calculatingRoutes.isEmpty()) {
            for (RouteCache routeCache : calculatingRoutes.values()) {
                routeCache.reset();
            }
        }
    }

    @Override
    public void onMajorGridChange() {
        if (!routeCacheMap.isEmpty())
            routeCacheMap.clear();

        if (!calculatingRoutes.isEmpty())
            calculatingRoutes.clear();
    }

    public RouteCache getRoutesFromOutputNonUrgent(IMultiBlockRoute start) {
        RouteCache cache;
        cache = routeCacheMap.get(start);
        if (cache != null && cache.maxPathLength == Integer.MAX_VALUE) {
            return cache;
        }

        cache = calculatingRoutes.get(start);

        if (cache != null)
            return cache;

        cache = new RouteCache(start);
        calculatingRoutes.put(start, cache);
        return cache;
    }


    public RouteCache getRoutesFromOutputRange(IMultiBlockRoute start, int maxRange) {
        RouteCache cache = routeCacheMap.get(start);
        if (cache == null || cache.maxPathLength < maxRange) {
            cache = new RouteCache(start);
            cache.generateCache();
            routeCacheMap.put(start, cache);
        }

        return cache;
    }

    public LinkedList<Route> getRoutesFromOutput(IMultiBlockRoute start) {
        RouteCache cache = routeCacheMap.get(start);
        if (cache == null) {
            cache = calculatingRoutes.get(start);
            if (cache == null)
                cache = new RouteCache(start);
            else
                calculatingRoutes.remove(start);
            cache.generateCache();
            routeCacheMap.put(start, cache);

        }

        return cache.outputRoutes;
    }

}
