package thermaldynamics.multiblock;

import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.LinkedList;

public class RouteCache {
    public final IMultiBlockRoute origin;
    public LinkedList<Route> outputRoutes;
    public LinkedList<Route> stuffableRoutes;
    public LinkedList<IMultiBlockRoute> toClear = new LinkedList<IMultiBlockRoute>();
    public final int maxPathLength;

    public RouteCache(IMultiBlockRoute origin) {
        this(origin, origin.getMaxRange());
    }

    public RouteCache(IMultiBlockRoute origin, int maxPathLength) {
        this.origin = origin;
        this.maxPathLength = maxPathLength;
    }

    public void generateCache() {
        outputRoutes = new LinkedList<Route>();
        stuffableRoutes = new LinkedList<Route>();
        LinkedList<Route> validRoutes = new LinkedList<Route>();
        validRoutes.add(new Route(origin));
        generateCacheDo(validRoutes);
    }

    public void refreshCache() {

    }

    private void generateCacheDo(LinkedList<Route> validRoutes) {
        toClear.clear();


        LinkedList<Route> newRoutes;
        boolean continueLoop = true;
        while (continueLoop) {
            newRoutes = new LinkedList<Route>();
            continueLoop = false;
            for (Route curRoute : validRoutes) {
                moveForwards(curRoute, newRoutes, outputRoutes, stuffableRoutes);
                if (!curRoute.routeFinished) {
                    continueLoop = true;
                }
            }
            validRoutes.addAll(newRoutes);
        }

        for (IMultiBlockRoute multiBlockRoute : toClear) {
            multiBlockRoute.setVisited(false);
        }

        if (origin.isOutput()) {
            Route singleOutput = new Route(origin);
            singleOutput.routeFinished = true;
            outputRoutes.add(singleOutput);
        }

        Collections.sort(outputRoutes);
    }

    public void moveForwards(Route route, LinkedList<Route> newRoutes, LinkedList<Route> outputRoutes, LinkedList<Route> stuffableRoutes) {
        boolean foundRoute = false;
        IMultiBlockRoute foundPath = null;

        if (route.pathDirections.size() > maxPathLength) {
            route.routeFinished = true;
            return;
        }

        byte foundDir = -1;
        for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
            if (route.endPoint.isSideConnected(i)) {
                IMultiBlockRoute validTile = (IMultiBlockRoute) route.endPoint.getConnectedSide(i);

                if (validTile != null && !validTile.wasVisited()) {
                    validTile.setVisited(true);
                    toClear.add(validTile);

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
            }
        }

        if (route.endPoint.isOutput()) {
            outputRoutes.add(new Route(route, true));
        }


        if (!foundRoute) {
            route.routeFinished = true;
        } else {
            route.pathDirections.add(foundDir);
            route.pathWeight += foundPath.getWeight();
            route.endPoint = foundPath;
        }
    }


}
