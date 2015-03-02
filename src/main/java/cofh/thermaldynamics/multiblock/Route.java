package cofh.thermaldynamics.multiblock;

import cofh.repack.codechicken.lib.vec.BlockCoord;
import gnu.trove.list.linked.TByteLinkedList;

public class Route implements Comparable<Route> {
    public TByteLinkedList pathDirections = new TByteLinkedList();
    public int pathPos = 0;
    public IMultiBlockRoute endPoint;
    public int pathWeight = 0;
    public boolean routeFinished = false;
    public BlockCoord dest;

    public Route(IMultiBlockRoute myParent) {


        endPoint = myParent;
    }

    @SuppressWarnings("unchecked")
    public Route(Route prevRoute, IMultiBlockRoute newPlace, byte direction, boolean isFinished) {

        pathDirections = new TByteLinkedList( prevRoute.pathDirections);
        pathWeight = prevRoute.pathWeight + newPlace.getWeight();
        endPoint = newPlace;

        pathDirections.add(direction);
        routeFinished = isFinished;
    }

    // Used to set as a node
    @SuppressWarnings("unchecked")
    public Route(Route prevRoute, boolean endPath) {

        pathDirections = new TByteLinkedList( prevRoute.pathDirections);
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;

        routeFinished = true;
    }

    @SuppressWarnings("unchecked")
    public Route(Route prevRoute) {

        pathDirections = new TByteLinkedList( prevRoute.pathDirections);
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;

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

        if(this.pathDirections.size() < otherRoute.pathDirections.size())
            return  -1;

        if(this.pathDirections.size() > otherRoute.pathDirections.size())
            return  1;

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
