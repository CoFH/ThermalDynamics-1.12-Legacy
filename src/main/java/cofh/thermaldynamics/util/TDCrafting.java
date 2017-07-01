package cofh.thermaldynamics.util;

import cofh.api.util.ThermalExpansionHelper;
import cofh.core.util.crafting.FluidIngredient;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermalfoundation.init.TFFluids;
import cofh.thermalfoundation.item.ItemMaterial;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.registries.GameData;

import static cofh.api.util.ThermalExpansionHelper.addPulverizerRecipe;
import static cofh.lib.util.helpers.RecipeHelper.*;

public class TDCrafting {

	public static boolean enableCoverRecipes = true;
	public static boolean useHardenedGlass = true;
	public static boolean useTransposerRecipes = true;

	public static void loadRecipes() {

		enableCoverRecipes = ThermalDynamics.CONFIG.get("Attachment.Cover", "Recipe", true);
		useTransposerRecipes = ThermalDynamics.CONFIG.get("Duct.Recipes", "UseFluidTransposer", true);
		String glassHardened = useHardenedGlass ? "blockGlassHardened" : "blockGlass";

		/* ENERGY */
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.energyBasic.itemStack, 6), "RRR", "IGI", "RRR", 'I', "ingotLead", 'G', "blockGlass", 'R', "dustRedstone");
		addShapelessRecipe(TDDucts.energyHardened.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "nuggetInvar", "nuggetInvar", "nuggetInvar");
		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.energyHardened.itemStack, 3), TDDucts.energyBasic.itemStack, TDDucts.energyBasic.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotInvar");

		addShapelessRecipe(TDDucts.energySignalum.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "nuggetSignalum", "nuggetSignalum", "nuggetSignalum");
		addShapelessRecipe(TDDucts.energyResonant.itemStack, TDDucts.energySignalum.itemStack, "dustRedstone", "nuggetEnderium", "nuggetEnderium", "nuggetEnderium");

		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.energySignalum.itemStack, 3), TDDucts.energyReinforced.itemStack, TDDucts.energyReinforced.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotSignalum");
		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.energyResonant.itemStack, 3), TDDucts.energySignalum.itemStack, TDDucts.energySignalum.itemStack, TDDucts.energySignalum.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotEnderium");

		addShapedRecipe(ItemHelper.cloneStack(TDDucts.energyReinforcedEmpty.itemStack, 6), "IGI", 'I', "ingotElectrum", 'G', glassHardened);
		// addShapedRecipe(ItemHelper.cloneStack(TDDucts.energySignalumEmpty.itemStack, 6), "IGI", 'I', "ingotSignalum", 'G', glassHardened);
		// addShapedRecipe(ItemHelper.cloneStack(TDDucts.energyResonantEmpty.itemStack, 6), "IGI", 'I', "ingotEnderium", 'G', glassHardened);
		addShapelessRecipe(TDDucts.energySignalumEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone", "nuggetSignalum", "nuggetSignalum", "nuggetSignalum");
		addShapelessRecipe(TDDucts.energyResonantEmpty.itemStack, TDDucts.energySignalumEmpty.itemStack, "dustRedstone", "nuggetEnderium", "nuggetEnderium", "nuggetEnderium");

		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.energySignalumEmpty.itemStack, 3), TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotSignalum");
		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.energyResonantEmpty.itemStack, 3), TDDucts.energySignalumEmpty.itemStack, TDDucts.energySignalumEmpty.itemStack, TDDucts.energySignalumEmpty.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotEnderium");

		addShapedRecipe(TDDucts.energySuperCondEmpty.itemStack, "IGI", "GEG", "IGI", 'I', "ingotElectrum", 'G', glassHardened, 'E', TDDucts.energyResonant.itemStack);

		/* ENERGY - TE Integration */
		addPulverizerRecipe(1600, TDDucts.energyBasic.itemStack, new ItemStack(Items.REDSTONE), ItemHelper.cloneStack(ItemMaterial.nuggetLead, 3));
		addPulverizerRecipe(1600, TDDucts.energyHardened.itemStack, new ItemStack(Items.REDSTONE, 2), ItemHelper.cloneStack(ItemMaterial.nuggetInvar, 3));

		addTransposerFill(800, TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforced.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(800, TDDucts.energySignalumEmpty.itemStack, TDDucts.energySignalum.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(800, TDDucts.energyResonantEmpty.itemStack, TDDucts.energyResonant.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(4000, TDDucts.energySuperCondEmpty.itemStack, TDDucts.energySuperCond.itemStack, new FluidStack(TFFluids.fluidCryotheum, 500), false);

		/* FLUID */
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidBasic.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "blockGlass");
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidBasicOpaque.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "ingotLead");

		addShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidHardened.itemStack, 6), "IGI", 'I', "ingotInvar", 'G', glassHardened);
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidHardenedOpaque.itemStack, 6), "IGI", 'I', "ingotInvar", 'G', "ingotLead");

		addShapelessRecipe(TDDucts.fluidEnergy.itemStack, TDDucts.fluidHardened.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum");
		addShapelessRecipe(TDDucts.fluidEnergyOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum");

		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.fluidEnergy.itemStack, 3), TDDucts.fluidHardened.itemStack, TDDucts.fluidHardened.itemStack, TDDucts.fluidHardened.itemStack, "ingotSignalum", "ingotElectrum");
		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.fluidEnergyOpaque.itemStack, 3), TDDucts.fluidHardenedOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "ingotSignalum", "ingotElectrum");

		addShapedRecipe(TDDucts.fluidSuper.itemStack, "IGI", "GEG", "IGI", 'I', "ingotBronze", 'G', glassHardened, 'E', TDDucts.fluidHardened.itemStack);
		addShapedRecipe(TDDucts.fluidSuperOpaque.itemStack, "IGI", "GEG", "IGI", 'I', "ingotBronze", 'G', glassHardened, 'E', TDDucts.fluidHardenedOpaque.itemStack);

		/* FLUID - TE Integration */
		addPulverizerRecipe(1600, TDDucts.fluidBasic.itemStack, ItemHelper.cloneStack(ItemMaterial.nuggetCopper, 3));
		addPulverizerRecipe(1600, TDDucts.fluidBasicOpaque.itemStack, ItemHelper.cloneStack(ItemMaterial.nuggetCopper, 3), ItemHelper.cloneStack(ItemMaterial.nuggetLead));

		/* ITEMS */
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.itemBasic.itemStack, 6), "IGI", 'I', "ingotTin", 'G', glassHardened);
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.itemBasicOpaque.itemStack, 6), "IGI", 'I', "ingotTin", 'G', "ingotLead");

		addShapelessRecipe(TDDucts.itemEnergy.itemStack, TDDucts.itemBasic.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum");
		addShapelessRecipe(TDDucts.itemEnergyOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum");

		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergy.itemStack, 3), TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, "ingotSignalum", "ingotElectrum");
		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergyOpaque.itemStack, 3), TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "ingotSignalum", "ingotElectrum");

		addShapelessRecipe(TDDucts.itemEnergyFast.itemStack, TDDucts.itemFast.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum");
		addShapelessRecipe(TDDucts.itemEnergyFastOpaque.itemStack, TDDucts.itemFastOpaque.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum");

		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergy.itemStack, 3), TDDucts.itemBasic.itemStack, TDDucts.fluidHardened.itemStack, TDDucts.fluidHardened.itemStack, "ingotSignalum", "ingotElectrum");
		addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergyOpaque.itemStack, 3), TDDucts.itemBasicOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "ingotSignalum", "ingotElectrum");

		//		TODO: Readd Omni/Warp Ducts
		//		GameRegistry.addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmni.itemStack, 2), TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		//		GameRegistry.addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmniOpaque.itemStack, 2), TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		//		GameRegistry.addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmni.itemStack, 6), TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, "ingotEnderium"));
		//		GameRegistry.addShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmniOpaque.itemStack, 6), TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "ingotEnderium"));

		/* ITEMS - TE Integration */
		addTransposerFill(800, TDDucts.itemBasic.itemStack, TDDucts.itemFast.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemFastOpaque.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemEnergy.itemStack, TDDucts.itemEnergyFast.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemEnergyOpaque.itemStack, TDDucts.itemEnergyFastOpaque.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);

		/* STRUCTURE */
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.structure.itemStack, 6), "iIi", 'i', "nuggetIron", 'I', "ingotLead");

		// TODO: Readd.
		// addShapedRecipe(ItemHelper.cloneStack(TDDucts.lightDuct.itemStack, 6), "LIL", 'L', "ingotLumium", 'I', "ingotLead");

		/* TRANSPORT */
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.transportFrame.itemStack, 4), "IGI", "G G", "IGI", 'I', "ingotBronze", 'G', glassHardened);
		addShapedRecipe(ItemHelper.cloneStack(TDDucts.transportLongRange.itemStack, 8), "IGI", "G G", "IGI", 'I', "ingotLead", 'G', glassHardened);

		addTransposerFill(800, TDDucts.transportFrame.itemStack, TDDucts.transportBasic.itemStack, new FluidStack(TFFluids.fluidAerotheum, 100), false);
		addTransposerFill(8000, TDDucts.transportBasic.itemStack, TDDucts.transportLinking.itemStack, new FluidStack(TFFluids.fluidEnder, 1000), false);

		/* COVERS */
		if (enableCoverRecipes) {
			RecipeCover.INSTANCE.setRegistryName("thermaldynamics:cover");
			GameData.register_impl(RecipeCover.INSTANCE);
		}

		/* SIGNALLER */
		// TODO: Readd.
		// addShapedRecipe(new ItemStack(TDItems.itemRelay, 2), "iGi", "IRI", 'R', "dustRedstone", 'G', "gemQuartz", 'I', "ingotLead", 'i', "nuggetSignalum"));

		/* CONVERSIONS */
		for (Duct[] duct : new Duct[][] { { TDDucts.itemBasic, TDDucts.itemBasicOpaque }, { TDDucts.itemFast, TDDucts.itemFastOpaque }, { TDDucts.itemEnergy, TDDucts.itemEnergyOpaque },
				//				TODO: Readd Omni/Warp Ducts
				//				{ TDDucts.itemOmni, TDDucts.itemOmniOpaque},
				{ TDDucts.fluidHardened, TDDucts.fluidHardenedOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			addShapelessRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, glassHardened);
			addShapelessRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead");
		}
		for (Duct[] duct : new Duct[][] { { TDDucts.fluidBasic, TDDucts.fluidBasicOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			addShapelessRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, "blockGlass");
			addShapelessRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead");
		}

		/* DENSE / VACUUM - TE Integration */
		//		for (DuctItem duct : new DuctItem[] { TDDucts.itemBasic, TDDucts.itemBasicOpaque,
		//				//				TODO: Readd Omni/Warp Ducts
		//				//				TDDucts.itemOmni, TDDucts.itemOmniOpaque,
		//				TDDucts.itemFast, TDDucts.itemFastOpaque, TDDucts.itemEnergy, TDDucts.itemEnergyOpaque }) {
		//			addShapelessRecipe(duct.getDenseItemStack(), duct.itemStack, "dustLead"));
		//			addShapelessRecipe(duct.getVacuumItemStack(), duct.itemStack, "dustSilver"));
		//			addShapelessRecipe(duct.itemStack, duct.getDenseItemStack()));// , "dustCharcoal"));
		//			addShapelessRecipe(duct.itemStack, duct.getVacuumItemStack()));// , "dustCharcoal"));
		//		}
	}

	/* HELPERS */
	public static void addTransposerFill(int energy, ItemStack input, ItemStack output, FluidStack fluid, boolean reversible) {

		if (Loader.isModLoaded("thermalexpansion")) {
			int numDucts = MathHelper.clamp(Fluid.BUCKET_VOLUME / fluid.amount, 1, 8);
			NonNullList<Object> ingredients = NonNullList.create();

			for (int i = 0; i < numDucts; i++) {
				ingredients.add(ItemHelper.cloneStack(input, 1));
			}
			ingredients.add(new FluidIngredient(fluid.getFluid().getName()));
			addShapelessFluidRecipe(ItemHelper.cloneStack(output, numDucts), ingredients.toArray());
		} else {
			ThermalExpansionHelper.addTransposerFill(energy, input, output, fluid, reversible);
		}
	}

}
