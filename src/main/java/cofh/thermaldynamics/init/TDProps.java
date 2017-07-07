package cofh.thermaldynamics.init;

import cofh.CoFHCore;
import cofh.core.gui.CreativeTabCore;
import cofh.core.util.CoreUtils;
import cofh.core.util.TimeTracker;
import cofh.core.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.ItemBlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.item.ItemCover;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TDProps {

	private TDProps() {

	}

	public static void preInit() {

		configCommon();
		configClient();
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
		ThermalDynamics.tabCommon = new CreativeTabCore("thermaldynamics") {

			int iconIndex = 0;
			TimeTracker iconTracker = new TimeTracker();

			void updateIcon() {

				World world = CoFHCore.proxy.getClientWorld();
				if (CoreUtils.isClient() && iconTracker.hasDelayPassed(world, 80)) {
					int next = MathHelper.RANDOM.nextInt(TDDucts.ductList.size() - 1);
					iconIndex = next >= iconIndex ? next + 1 : next;
					iconTracker.markTime(world);
				}
			}

			@Override
			@SideOnly (Side.CLIENT)
			public ItemStack getIconItemStack() {

				updateIcon();
				return TDDucts.getDuct(iconIndex).itemStack;
			}

			@Override
			@SideOnly (Side.CLIENT)
			public void displayAllRelevantItems(NonNullList<ItemStack> list) {

				NonNullList<ItemStack> stacks = NonNullList.create();

				// TODO: Revisit this.
				super.displayAllRelevantItems(stacks);

				for (Duct d : TDDucts.getSortedDucts()) {
					list.add(d.itemStack.copy());

					//			if (d instanceof DuctItem) {
					//				list.add(((DuctItem) d).getDenseItemStack());
					//				list.add(((DuctItem) d).getVacuumItemStack());
					//			}
				}
				for (ItemStack item : stacks) {
					if (!(item.getItem() instanceof ItemBlockDuct)) {
						list.add(item);
					}
				}
			}

		};

		if (enableCoverCreativeTab) {
			ThermalDynamics.tabCovers = new CreativeTabCore("thermaldynamics", "Covers") {

				int iconIndex = 0;
				TimeTracker iconTracker = new TimeTracker();

				void updateIcon() {

					World world = CoFHCore.proxy.getClientWorld();
					if (CoreUtils.isClient() && iconTracker.hasDelayPassed(world, 80)) {
						int next = MathHelper.RANDOM.nextInt(ItemCover.getCoverList().size() - 1);
						iconIndex = next >= iconIndex ? next + 1 : next;
						iconTracker.markTime(world);
					}
				}

				@Override
				@SideOnly (Side.CLIENT)
				public ItemStack getIconItemStack() {

					updateIcon();
					return ItemCover.getCoverList().get(iconIndex);
				}

			};

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
