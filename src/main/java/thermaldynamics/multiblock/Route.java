package thermaldynamics.multiblock;

import java.util.LinkedList;

public class Route implements Comparable<Route> {
    LinkedList<Byte> pathDirections = new LinkedList<Byte>();
    int pathPos = 0;
    IMultiBlockRoute endPoint;
    IMultiBlockRoute startPoint;
    int pathWeight = 0;
    boolean routeFinished = false;

    public Route(IMultiBlockRoute myParent) {

        startPoint = myParent;
        endPoint = myParent;
        myParent.setVisited(true);
    }

    public Route(Route prevRoute, IMultiBlockRoute newPlace, byte direction, boolean isFinished) {

        pathDirections = (LinkedList<Byte>) prevRoute.pathDirections.clone();
        pathWeight = prevRoute.pathWeight + newPlace.getWeight();
        endPoint = newPlace;
        startPoint = prevRoute.startPoint;
        pathDirections.add(direction);
        routeFinished = isFinished;
    }

    // Used to set as a node
    public Route(Route prevRoute, boolean endPath) {

        pathDirections = (LinkedList<Byte>) prevRoute.pathDirections.clone();
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;
        startPoint = prevRoute.startPoint;
        routeFinished = true;
    }

    public Route(Route prevRoute) {

        pathDirections = (LinkedList<Byte>) prevRoute.pathDirections.clone();
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;
        startPoint = prevRoute.startPoint;
        routeFinished = prevRoute.routeFinished;
    }

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

    public int getNextDirection() {

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
