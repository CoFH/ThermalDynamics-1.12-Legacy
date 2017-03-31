package cofh.thermaldynamics.duct.item;

public class RouteInfo {

	public static final RouteInfo noRoute = new RouteInfo();
	public boolean canRoute = false;
	public int stackSize = -1;
	public byte side = -1;

	public RouteInfo(int stackSizeLeft, byte i) {

		canRoute = true;
		stackSize = stackSizeLeft;
		side = i;
	}

	public RouteInfo() {

	}
}
