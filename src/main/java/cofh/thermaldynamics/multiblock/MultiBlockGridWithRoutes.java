package cofh.thermaldynamics.multiblock;

import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class MultiBlockGridWithRoutes extends MultiBlockGrid {
    public MultiBlockGridWithRoutes(World world) {
        super(world);
    }

    @Override
    public void doTickProcessing(long deadline) {
        synchronized (calculatingRoutes) {
            Iterator<RouteCache> iterator = calculatingRoutes.iterator();
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
    }


    @Override
    public boolean isTickProcessing() {
        return !calculatingRoutes.isEmpty();
    }

    public HashMap<IMultiBlockRoute, RouteCache> routeCacheMap = new HashMap<IMultiBlockRoute, RouteCache>();
    public final LinkedList<RouteCache> calculatingRoutes = new LinkedList<RouteCache>();

    @Override
    public void onMinorGridChange() {
        onMajorGridChange();
    }

    @Override
    public void onMajorGridChange() {
        if (!routeCacheMap.isEmpty()) {
            for (RouteCache routeCache : routeCacheMap.values())
                routeCache.invalidate();
            routeCacheMap.clear();
        }

        synchronized (calculatingRoutes) {
            if (!calculatingRoutes.isEmpty())
                calculatingRoutes.clear();
        }
    }

    public RouteCache getRoutesFromOutputNonUrgent(IMultiBlockRoute start) {
        RouteCache cache;
        cache = routeCacheMap.get(start);
        if (cache != null) {
            return cache;
        }

        cache = new RouteCache(start);
        synchronized (calculatingRoutes) {
            calculatingRoutes.add(cache);
        }
        routeCacheMap.put(start, cache);
        return cache;
    }


    public RouteCache getRoutesFromOutputRange(IMultiBlockRoute start, int maxRange) {
        RouteCache cache = routeCacheMap.get(start);
        if (cache == null) {
            cache = new RouteCache(start, maxRange);
            cache.generateCache();
            routeCacheMap.put(start, cache);
        } else if (cache.maxPathLength < maxRange) {
            cache.maxPathLength = maxRange;
            cache.generateCache();
        }

        return cache;
    }

    public RouteCache getRoutesFromOutput(IMultiBlockRoute start) {
        RouteCache cache = routeCacheMap.get(start);
        if (cache == null) {
            cache = new RouteCache(start);
            cache.generateCache();
            routeCacheMap.put(start, cache);
        } else if (!cache.isFinishedGenerating() || cache.maxPathLength < Integer.MAX_VALUE) {
            cache.maxPathLength = Integer.MAX_VALUE;
            cache.generateCache();
        }

        return cache;
    }

}
