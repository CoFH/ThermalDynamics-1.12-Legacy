package thermaldynamics.multiblock;

import cofh.repack.codechicken.lib.vec.BlockCoord;

import java.util.LinkedList;

public class Route implements Comparable<Route> {
    public LinkedList<Byte> pathDirections = new LinkedList<Byte>();
    public int pathPos = 0;
    public IMultiBlockRoute endPoint;
    public IMultiBlockRoute startPoint;
    public int pathWeight = 0;
    public boolean routeFinished = false;
    public BlockCoord dest;

    public Route(IMultiBlockRoute myParent) {

        startPoint = myParent;
        endPoint = myParent;
    }

    @SuppressWarnings("unchecked")
    public Route(Route prevRoute, IMultiBlockRoute newPlace, byte direction, boolean isFinished) {

        pathDirections = (LinkedList<Byte>) prevRoute.pathDirections.clone();
        pathWeight = prevRoute.pathWeight + newPlace.getWeight();
        endPoint = newPlace;
        startPoint = prevRoute.startPoint;
        pathDirections.add(direction);
        routeFinished = isFinished;
    }

    // Used to set as a node
    @SuppressWarnings("unchecked")
    public Route(Route prevRoute, boolean endPath) {

        pathDirections = (LinkedList<Byte>) prevRoute.pathDirections.clone();
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;
        startPoint = prevRoute.startPoint;
        routeFinished = true;
    }

    @SuppressWarnings("unchecked")
    public Route(Route prevRoute) {

        pathDirections = (LinkedList<Byte>) prevRoute.pathDirections.clone();
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;
        startPoint = prevRoute.startPoint;
        routeFinished = prevRoute.routeFinished;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Route otherRoute) {

        if (this.pathWeight < otherRoute.pathWeight) {
            return -1;
        }
        if (this.pathWeight > otherRoute.pathWeight) {
            return 1;
        }
        return 0;
    }

    public Route copy() {

        return new Route(this);
    }

    public byte getNextDirection() {

        return pathDirections.get(pathPos++);
    }

    public boolean hasNextDirection() {

        return pathDirections.size() > pathPos;
    }

    public int getCurrentDirection() {

        return pathDirections.get(pathPos);
    }

    public int checkNextDirection() {

        return pathDirections.get(pathPos + 1);
    }

    public int getLastSide() {

        return pathDirections.size() > 0 ? pathDirections.get(pathDirections.size() - 1) : 0;
    }


}
