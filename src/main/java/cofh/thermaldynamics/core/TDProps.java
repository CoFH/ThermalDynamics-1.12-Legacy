package cofh.thermaldynamics.core;

public class TDProps {

	private TDProps() {

	}

	/* General */
	public static final int MAX_ITEMS_TRANSMITTED = 6;
	public static final int FLUID_EMPTY_UPDATE_DELAY = 96;
	public static final byte FLUID_UPDATE_DELAY = 4;
	public static final int ENDER_TRANSMIT_COST = 50;
	public static final int MAX_STUFFED_ITEMSTACKS_DROP = 30;

	/* Graphics */
	public static final String PATH_GFX = "thermaldynamics:textures/";

	/* Duct Render Constants */
	public static float smallInnerModelScaling = 0.99F;
	public static float largeInnerModelScaling = 0.99F;

	/* Render Ids */
	public static int renderDuctId = -1;

}
