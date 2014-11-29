package thermaldynamics.multiblock;

import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.TileMultiBlock;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

public class RouteCache {
    public IMultiBlockRoute origin;
    public LinkedList<Route> outputRoutes;
    public LinkedList<Route> stuffableRoutes;
    public HashSet<IMultiBlockRoute> visited;
    public HashSet<IMultiBlockRoute> outputvisited;
    private LinkedList<Route> validRoutes;
    public int maxPathLength;
    private boolean isFinishedGenerating;
    public boolean invalid = false;

    public RouteCache(IMultiBlockRoute origin) {
        this(origin, origin.getMaxRange());
    }

    public RouteCache(IMultiBlockRoute origin, int maxPathLength) {
        this.origin = origin;
        this.maxPathLength = maxPathLength;
        init();
    }

    public void init() {
        outputRoutes = new LinkedList<Route>();
        if (origin.isOutput()) {
            Route singleOutput = new Route(origin);

            singleOutput.routeFinished = true;
            outputRoutes.add(singleOutput);
        }
        stuffableRoutes = new LinkedList<Route>();
        validRoutes = new LinkedList<Route>();
        validRoutes.add(new Route(origin));
        visited = new HashSet<IMultiBlockRoute>();
        visited.add(origin);
        outputvisited = new HashSet<IMultiBlockRoute>();
        if (origin.isOutput()) outputvisited.add(origin);
    }

    public synchronized void generateCache() {

        while (processStep()) ;

    }

    public boolean processStep() {
        if (isFinishedGenerating)
            return false;

        boolean continueLoop = false;

        LinkedList<Route> newRoutes = new LinkedList<Route>();
        for (Route curRoute : validRoutes) {
            moveForwards(curRoute, newRoutes);
            if (!curRoute.routeFinished) {
                continueLoop = true;
            }
        }
        validRoutes.addAll(newRoutes);

        if (!continueLoop)
            finished();

        return continueLoop;
    }


    private void finished() {
        visited.clear();
        outputvisited.clear();
        validRoutes.clear();
        isFinishedGenerating = true;
        Collections.sort(outputRoutes);
    }

    public void moveForwards(Route route, LinkedList<Route> newRoutes) {
        boolean foundRoute = false;
        IMultiBlockRoute foundPath = null;

        if (route.routeFinished)
            return;


        if (route.pathDirections.size() > maxPathLength) {
            route.routeFinished = true;
            return;
        }


        byte foundDir = -1;
        for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
            if (route.endPoint.getCachedSideType(i) == TileMultiBlock.NeighborTypes.MULTIBLOCK && route.endPoint.getConnectionType(i) == TileMultiBlock.ConnectionTypes.NORMAL) {
                IMultiBlockRoute validTile = (IMultiBlockRoute) route.endPoint.getCachedTile(i);

                if (validTile != null) {
                    if (!visited.contains(validTile)) {
                        visited.add(validTile);

                        if (validTile.canStuffItem())
                            stuffableRoutes.add(new Route(route, validTile, i, true));

                        if (!foundRoute) {
                            foundPath = validTile;
                            foundDir = i;
                            foundRoute = true;
                        } else {
                            newRoutes.add(new Route(route, validTile, i, false));
                        }
                    }

                    if (validTile.isOutput() && !outputvisited.contains(validTile)) {
                        outputRoutes.add(new Route(route, validTile, i, true));
                        outputvisited.add(validTile);
                    }
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


    public synchronized boolean isFinishedGenerating() {
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
