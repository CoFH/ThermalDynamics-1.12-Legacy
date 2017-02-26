package cofh.thermaldynamics.init;

import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.gui.CreativeTabTD;
import cofh.thermaldynamics.gui.CreativeTabTDCovers;

public class TDProps {

	private TDProps() {

	}

	public static void preInit() {

		configCommon();
		configClient();
	}

	public static void loadComplete() {

	}

	/* HELPERS */
	private static void configCommon() {

	}

	private static void configClient() {

		String category;
		String comment;

		/* GRAPHICS */
		category = "Render";

		comment = "This value affects the size of the inner duct model, such as fluids. Lower it if you experience texture z-fighting.";
		smallInnerModelScaling = MathHelper.clamp((float) ThermalDynamics.CONFIG_CLIENT.get(category, "InnerModelScaling", 0.99, comment), 0.50F, 0.99F);

		comment = "This value affects the size of the inner duct model, such as fluids, on the large (octagonal) ducts. Lower it if you experience texture z-fighting.";
		largeInnerModelScaling = MathHelper.clamp((float) ThermalDynamics.CONFIG_CLIENT.get(category, "LargeInnerModelScaling", 0.99, comment), 0.50F, 0.99F);

		category = "Interface";

		comment = "If TRUE, Thermal Dynamics Covers will have a Creative Tab.";
		enableCoverCreativeTab = ThermalDynamics.CONFIG_CLIENT.getConfiguration().getBoolean("ItemsInCommonTab", category, enableCoverCreativeTab, comment);

		comment = "If TRUE, Thermal Dynamics Covers will be shown in JEI.";
		showCoversInJEI = ThermalDynamics.CONFIG_CLIENT.getConfiguration().getBoolean("CoversInJEI", category, showCoversInJEI, comment);

		/* CREATIVE TABS */
		ThermalDynamics.tabCommon = new CreativeTabTD();

		if (enableCoverCreativeTab) {
			ThermalDynamics.tabCovers = new CreativeTabTDCovers();
		}
	}

	/* GENERAL */
	public static final int MAX_ITEMS_TRANSMITTED = 6;
	public static final int FLUID_EMPTY_UPDATE_DELAY = 96;
	public static final byte FLUID_UPDATE_DELAY = 4;
	public static final int ENDER_TRANSMIT_COST = 50;
	public static final int MAX_STUFFED_ITEMSTACKS_DROP = 30;

	/* TEXTURES */
	public static final String PATH_GFX = "thermaldynamics:textures/";

	/* RENDER */
	public static float smallInnerModelScaling = 0.99F;
	public static float largeInnerModelScaling = 0.99F;

	/* MISC */
	public static boolean enableCoverCreativeTab = true;
	public static boolean showCoversInJEI = false;

}
