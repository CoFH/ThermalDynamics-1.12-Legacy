package cofh.thermaldynamics.core;

public class TDProps {

	private TDProps() {

	}

	/* Graphics */
	public static final String PATH_GFX = "thermaldynamics:textures/";

	// Energy that each node can store.
	public static final int ENERGY_PER_NODE = 80 * 7;

	public static final int MAX_ITEMS_TRANSMITTED = 6;
	public static final int FLUID_EMPTY_UPDATE_DELAY = 96;
	public static final byte FLUID_UPDATE_DELAY = 4;
	public static final int ENDER_TRANSMIT_COST = 50;
	public static final int MAX_STUFFED_ITEMSTACKS_DROP = 30;

	/* Render Ids */
	public static int renderDuctId = -1;

}
