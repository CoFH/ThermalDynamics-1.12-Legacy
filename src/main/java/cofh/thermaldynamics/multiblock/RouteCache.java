package cofh.thermaldynamics.multiblock;

import net.minecraft.util.EnumFacing;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

public class RouteCache<T extends IGridTileRoute<T, G>, G extends MultiBlockGrid<T>> {

	public T origin;
	public LinkedList<Route<T, G>> outputRoutes;
	public LinkedList<Route<T, G>> stuffableRoutes;
	public HashSet<T> visited;
	public HashSet<T> outputvisited;
	public int maxPathLength;
	public boolean invalid = false;
	private LinkedList<Route<T, G>> validRoutes;
	private boolean isFinishedGenerating;

	public RouteCache(T origin) {

		this(origin, origin.getMaxRange());
	}

	public RouteCache(T origin, int maxPathLength) {

		this.origin = origin;
		this.maxPathLength = maxPathLength;
		init();
	}

	public void init() {

		outputRoutes = new LinkedList<>();
		if (origin.isOutput()) {
			Route<T, G> singleOutput = new Route<>(origin);

			singleOutput.routeFinished = true;
			outputRoutes.add(singleOutput);
		}
		stuffableRoutes = new LinkedList<>();
		validRoutes = new LinkedList<>();
		validRoutes.add(new Route<>(origin));
		visited = new HashSet<>();
		visited.add(origin);
		outputvisited = new HashSet<>();
		if (origin.isOutput()) {
			outputvisited.add(origin);
		}
	}

	public synchronized void generateCache() {

		//noinspection StatementWithEmptyBody
		while (processStep()) {
		}

	}

	public boolean processStep() {

		if (isFinishedGenerating || invalid) {
			return false;
		}

		boolean continueLoop = false;

		LinkedList<Route<T, G>> newRoutes = new LinkedList<>();
		for (Route<T, G> curRoute : validRoutes) {
			moveForwards(curRoute, newRoutes);
			if (!curRoute.routeFinished) {
				continueLoop = true;
			}
		}
		validRoutes.addAll(newRoutes);

		if (!continueLoop) {
			finished();
		}

		return continueLoop;
	}

	private void finished() {

		visited.clear();
		outputvisited.clear();
		validRoutes.clear();
		isFinishedGenerating = true;
		Collections.sort(outputRoutes);
	}

	public void moveForwards(Route<T, G> route, LinkedList<Route<T, G>> newRoutes) {

		boolean foundRoute = false;
		T foundPath = null;

		if (route.routeFinished) {
			return;
		}

		if (route.pathDirections.size() > maxPathLength) {
			route.routeFinished = true;
			return;
		}

		byte foundDir = -1;
		for (byte i = 0; i < 6; i++) {
			T validTile = route.endPoint.getCachedTile(i);

			if (validTile != null) {
				if (!visited.contains(validTile)) {
					visited.add(validTile);

					validTile.onNeighborBlockChange();

					if (validTile.canStuffItem()) {
						stuffableRoutes.add(new Route<>(route, validTile, i, true));
					}

					if (!foundRoute) {
						foundPath = validTile;
						foundDir = i;
						foundRoute = true;
					} else {
						newRoutes.add(new Route<>(route, validTile, i, false));
					}
				}

				if (validTile.isOutput() && !outputvisited.contains(validTile)) {
					outputRoutes.add(new Route<>(route, validTile, i, true));
					outputvisited.add(validTile);
				}
			}
		}

		if (!foundRoute) {
			route.routeFinished = true;
		} else {
			route.pathDirections.add(foundDir);
			route.pathWeight += foundPath.getWeight();
			route.endPoint = foundPath;
		}
	}

	public boolean isFinishedGenerating() {

		return isFinishedGenerating;
	}

	public void reset() {

		isFinishedGenerating = false;
		init();
	}

	public void invalidate() {

		invalid = true;
		outputRoutes.clear();
		stuffableRoutes.clear();
		origin = null;
	}
}
