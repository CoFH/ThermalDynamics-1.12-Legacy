package cofh.thermaldynamics.multiblock;

import net.minecraft.world.World;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class MultiBlockGridWithRoutes<T extends IGridTileRoute<T, G>, G extends MultiBlockGridWithRoutes<T, G>> extends MultiBlockGrid<T> {

	public final LinkedList<RouteCache<T, G>> calculatingRoutes = new LinkedList<>();
	public HashMap<IGridTileRoute, RouteCache<T, G>> routeCacheMap = new HashMap<>();

	public MultiBlockGridWithRoutes(World world) {

		super(world);
	}

	@Override
	public void doTickProcessing(long deadline) {

		for (int i = 0; !calculatingRoutes.isEmpty(); ++i) {
			RouteCache routeCache = calculatingRoutes.peek();
			if (routeCache != null && !routeCache.processStep()) {
				calculatingRoutes.remove(routeCache);
			}
			if (i == 15) {
				if (System.nanoTime() > deadline) {
					return;
				}
				i = 0;
			}
		}
	}

	@Override
	public boolean isTickProcessing() {

		return !calculatingRoutes.isEmpty();
	}

	@Override
	public void onMinorGridChange() {

		super.onMinorGridChange();
		onMajorGridChange();
	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		if (!routeCacheMap.isEmpty()) {
			for (RouteCache<T, G> routeCache : routeCacheMap.values()) {
				routeCache.invalidate();
			}
			routeCacheMap.clear();
		}

		if (!calculatingRoutes.isEmpty()) {
			calculatingRoutes.clear();
		}
	}

	public RouteCache getRoutesFromOutputNonUrgent(T start) {

		RouteCache cache;
		cache = routeCacheMap.get(start);
		if (cache != null) {
			return cache;
		}

		cache = new RouteCache<T, G>(start);
		calculatingRoutes.add(cache);

		routeCacheMap.put(start, cache);
		return cache;
	}

	public RouteCache<T, G> getRoutesFromOutputRange(T start, int maxRange) {

		RouteCache<T, G> cache = routeCacheMap.get(start);
		if (cache == null) {
			cache = new RouteCache<T, G>(start, maxRange);
			cache.generateCache();
			routeCacheMap.put(start, cache);
		} else if (cache.maxPathLength < maxRange) {
			cache.maxPathLength = maxRange;
			cache.generateCache();
		}

		return cache;
	}

	public RouteCache<T, G> getRoutesFromOutput(T start) {

		RouteCache<T, G> cache = routeCacheMap.get(start);
		if (cache == null) {
			cache = new RouteCache<T, G>(start);
			cache.generateCache();
			routeCacheMap.put(start, cache);
		} else if (!cache.isFinishedGenerating() || cache.maxPathLength < Integer.MAX_VALUE) {
			cache.maxPathLength = Integer.MAX_VALUE;
			cache.generateCache();
		}

		return cache;
	}

}
