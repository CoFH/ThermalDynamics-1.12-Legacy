package cofh.thermaldynamics.util;

import cofh.api.util.ThermalExpansionHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermalfoundation.init.TFFluids;
import cofh.thermalfoundation.item.ItemMaterial;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static cofh.api.util.ThermalExpansionHelper.addPulverizerRecipe;
import static cofh.lib.util.helpers.ItemHelper.*;

//TODO, 1.11, Move StructuralDuct, Servo's and Filters back to IronNuggets.
public class TDCrafting {

	public static boolean enableCoverRecipes = true;
	public static boolean useHardenedGlass = true;
	public static boolean useTransposerRecipes = true;

	public static void loadRecipes() {

		enableCoverRecipes = ThermalDynamics.CONFIG.get("Attachment.Cover", "Recipe", true);
		useHardenedGlass = ThermalDynamics.CONFIG.get("Duct.Recipes", "UseHardenedGlass", true);
		useTransposerRecipes = ThermalDynamics.CONFIG.get("Duct.Recipes", "UseFluidTransposer", true);
		String glassHardened = useHardenedGlass ? "blockGlassHardened" : "blockGlass";

		/* ENERGY */
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.energyBasic.itemStack, 6), "RRR", "IGI", "RRR", 'I', "ingotLead", 'G', "blockGlass", 'R', "dustRedstone"));
		addRecipe(ShapelessRecipe(TDDucts.energyHardened.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "nuggetInvar", "nuggetInvar", "nuggetInvar"));
		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.energyHardened.itemStack, 3), TDDucts.energyBasic.itemStack, TDDucts.energyBasic.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotInvar"));

		addRecipe(ShapelessRecipe(TDDucts.energySignalum.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "nuggetSignalum", "nuggetSignalum", "nuggetSignalum"));
		addRecipe(ShapelessRecipe(TDDucts.energyResonant.itemStack, TDDucts.energySignalum.itemStack, "dustRedstone", "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));

		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.energySignalum.itemStack, 3), TDDucts.energyReinforced.itemStack, TDDucts.energyReinforced.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotSignalum"));
		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.energyResonant.itemStack, 3), TDDucts.energySignalum.itemStack, TDDucts.energySignalum.itemStack, TDDucts.energySignalum.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotEnderium"));

		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.energyReinforcedEmpty.itemStack, 6), "IGI", 'I', "ingotElectrum", 'G', glassHardened));
		// addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.energySignalumEmpty.itemStack, 6), "IGI", 'I', "ingotSignalum", 'G', glassHardened));
		// addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.energyResonantEmpty.itemStack, 6), "IGI", 'I', "ingotEnderium", 'G', glassHardened));
		addRecipe(ShapelessRecipe(TDDucts.energySignalumEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone", "nuggetSignalum", "nuggetSignalum", "nuggetSignalum"));
		addRecipe(ShapelessRecipe(TDDucts.energyResonantEmpty.itemStack, TDDucts.energySignalumEmpty.itemStack, "dustRedstone", "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));

		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.energySignalumEmpty.itemStack, 3), TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotSignalum"));
		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.energyResonantEmpty.itemStack, 3), TDDucts.energySignalumEmpty.itemStack, TDDucts.energySignalumEmpty.itemStack, TDDucts.energySignalumEmpty.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotEnderium"));

		addRecipe(ShapedRecipe(TDDucts.energySuperCondEmpty.itemStack, "IGI", "GEG", "IGI", 'I', "ingotElectrum", 'G', glassHardened, 'E', TDDucts.energyResonant.itemStack));

		/* ENERGY - TE Integration */
		addPulverizerRecipe(1600, TDDucts.energyBasic.itemStack, new ItemStack(Items.REDSTONE), ItemHelper.cloneStack(ItemMaterial.nuggetLead, 3));
		addPulverizerRecipe(1600, TDDucts.energyHardened.itemStack, new ItemStack(Items.REDSTONE, 2), ItemHelper.cloneStack(ItemMaterial.nuggetInvar, 3));

		addTransposerFill(800, TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforced.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(800, TDDucts.energySignalumEmpty.itemStack, TDDucts.energySignalum.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(800, TDDucts.energyResonantEmpty.itemStack, TDDucts.energyResonant.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(4000, TDDucts.energySuperCondEmpty.itemStack, TDDucts.energySuperCond.itemStack, new FluidStack(TFFluids.fluidCryotheum, 500), false);

		/* FLUID */
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidBasic.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "blockGlass"));
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidBasicOpaque.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "ingotLead"));

		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidHardened.itemStack, 6), "IGI", 'I', "ingotInvar", 'G', glassHardened));
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidHardenedOpaque.itemStack, 6), "IGI", 'I', "ingotInvar", 'G', "ingotLead"));

		addRecipe(ShapelessRecipe(TDDucts.fluidEnergy.itemStack, TDDucts.fluidHardened.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));
		addRecipe(ShapelessRecipe(TDDucts.fluidEnergyOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));

		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.fluidEnergy.itemStack, 3), TDDucts.fluidHardened.itemStack, TDDucts.fluidHardened.itemStack, TDDucts.fluidHardened.itemStack, "ingotSignalum", "ingotElectrum"));
		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.fluidEnergyOpaque.itemStack, 3), TDDucts.fluidHardenedOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "ingotSignalum", "ingotElectrum"));

		addRecipe(ShapedRecipe(TDDucts.fluidSuper.itemStack, "IGI", "GEG", "IGI", 'I', "ingotBronze", 'G', glassHardened, 'E', TDDucts.fluidHardened.itemStack));
		addRecipe(ShapedRecipe(TDDucts.fluidSuperOpaque.itemStack, "IGI", "GEG", "IGI", 'I', "ingotBronze", 'G', glassHardened, 'E', TDDucts.fluidHardenedOpaque.itemStack));

		//		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidSuper.itemStack, 6), "PlP", "IGI", "PlP", 'I', "ingotLumium", 'G', glassHardened, 'P', "ingotPlatinum", 'l', "nuggetLead"));
		//		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.fluidSuperOpaque.itemStack, 6), "PlP", "IGI", "PlP", 'I', "ingotLumium", 'G', "ingotLead", 'P', "ingotPlatinum", 'l', "nuggetLead"));

		/* FLUID - TE Integration */
		addPulverizerRecipe(1600, TDDucts.fluidBasic.itemStack, ItemHelper.cloneStack(ItemMaterial.nuggetCopper, 3));
		addPulverizerRecipe(1600, TDDucts.fluidBasicOpaque.itemStack, ItemHelper.cloneStack(ItemMaterial.nuggetCopper, 3), ItemHelper.cloneStack(ItemMaterial.nuggetLead));

		/* ITEMS */
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.itemBasic.itemStack, 6), "IGI", 'I', "ingotTin", 'G', glassHardened));
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.itemBasicOpaque.itemStack, 6), "IGI", 'I', "ingotTin", 'G', "ingotLead"));

		addRecipe(ShapelessRecipe(TDDucts.itemEnergy.itemStack, TDDucts.itemBasic.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));
		addRecipe(ShapelessRecipe(TDDucts.itemEnergyOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));

		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergy.itemStack, 3), TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, "ingotSignalum", "ingotElectrum"));
		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergyOpaque.itemStack, 3), TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "ingotSignalum", "ingotElectrum"));

		addRecipe(ShapelessRecipe(TDDucts.itemEnergyFast.itemStack, TDDucts.itemFast.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));
		addRecipe(ShapelessRecipe(TDDucts.itemEnergyFastOpaque.itemStack, TDDucts.itemFastOpaque.itemStack, "nuggetSignalum", "nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));

		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergy.itemStack, 3), TDDucts.itemBasic.itemStack, TDDucts.fluidHardened.itemStack, TDDucts.fluidHardened.itemStack, "ingotSignalum", "ingotElectrum"));
		addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemEnergyOpaque.itemStack, 3), TDDucts.itemBasicOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "ingotSignalum", "ingotElectrum"));

		//		TODO: Readd Omni/Warp Ducts
		//		GameRegistry.addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmni.itemStack, 2), TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		//		GameRegistry.addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmniOpaque.itemStack, 2), TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		//		GameRegistry.addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmni.itemStack, 6), TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, "ingotEnderium"));
		//		GameRegistry.addRecipe(ShapelessRecipe(ItemHelper.cloneStack(TDDucts.itemOmniOpaque.itemStack, 6), TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "ingotEnderium"));

		/* ITEMS - TE Integration */
		addTransposerFill(800, TDDucts.itemBasic.itemStack, TDDucts.itemFast.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemFastOpaque.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemEnergy.itemStack, TDDucts.itemEnergyFast.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemEnergyOpaque.itemStack, TDDucts.itemEnergyFastOpaque.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);

		/* STRUCTURE */
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.structure.itemStack, 6), "iIi", 'i', "nuggetTin", 'I', "ingotLead"));

		// TODO: Readd.
		// addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.lightDuct.itemStack, 6), "LIL", 'L', "ingotLumium", 'I', "ingotLead"));

		/* TRANSPORT */
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.transportFrame.itemStack, 4), "IGI", "G G", "IGI", 'I', "ingotBronze", 'G', glassHardened));
		addRecipe(ShapedRecipe(ItemHelper.cloneStack(TDDucts.transportLongRange.itemStack, 8), "IGI", "G G", "IGI", 'I', "ingotLead", 'G', glassHardened));

		addTransposerFill(800, TDDucts.transportFrame.itemStack, TDDucts.transportBasic.itemStack, new FluidStack(TFFluids.fluidAerotheum, 100), false);
		addTransposerFill(8000, TDDucts.transportBasic.itemStack, TDDucts.transportLinking.itemStack, new FluidStack(TFFluids.fluidEnder, 1000), false);

		/* COVERS */
		if (enableCoverRecipes) {
			addRecipe(RecipeCover.INSTANCE);
		}

		/* SIGNALLER */
		// TODO: Readd.
		// addRecipe(ShapedRecipe(new ItemStack(TDItems.itemRelay, 2), "iGi", "IRI", 'R', "dustRedstone", 'G', "gemQuartz", 'I', "ingotLead", 'i', "nuggetSignalum"));

		/* ATTACHMENTS */
		String[] materials = { "Iron", "Invar", "Electrum", "Signalum", "Enderium" };

		int hardGlassLevel = useHardenedGlass ? 2 : 5; // level to start using hardened glass

		for (int i = 0; i < materials.length; i++) {
			addRecipe(ShapedRecipe(new ItemStack(TDItems.itemServo, 2, i), "iGi", "IRI", 'R', "dustRedstone", 'G', i < hardGlassLevel ? "blockGlass" : "blockGlassHardened", 'I', "ingot" + materials[i], 'i', "nuggetIron"));
			addRecipe(ShapedRecipe(new ItemStack(TDItems.itemFilter, 2, i), "iGi", "IRI", 'R', Items.PAPER, 'G', i < hardGlassLevel ? "blockGlass" : "blockGlassHardened", 'I', "ingot" + materials[i], 'i', "nuggetIron"));
			addRecipe(ShapedRecipe(new ItemStack(TDItems.itemRetriever, 2, i), "iGi", "IRI", 'R', Items.ENDER_EYE, 'G', i < hardGlassLevel ? "blockGlass" : "blockGlassHardened", 'I', "ingot" + materials[i], 'i', "nuggetGold"));

			if (i > 0) {
				for (Item item : new Item[] { TDItems.itemFilter, TDItems.itemServo, TDItems.itemRetriever }) {
					if (i < hardGlassLevel) {
						addRecipe(addInputMetaRange(new ShapelessOreRecipe(new ItemStack(item, 1, i), "ingot" + materials[i]), new ItemStack(item, 1), 0, i - 1));
					} else {
						if (i > hardGlassLevel) {
							addRecipe(addInputMetaRange(new ShapelessOreRecipe(new ItemStack(item, 1, i), "ingot" + materials[i]), new ItemStack(item, 1), hardGlassLevel, i - 1));
						}
						addRecipe(addInputMetaRange(addInputMetaRange(new ShapelessOreRecipe(new ItemStack(item, 2, i), "blockGlassHardened", "ingot" + materials[i], "ingot" + materials[i]), new ItemStack(item, 1), 0, hardGlassLevel - 1), new ItemStack(item, 1), 0, hardGlassLevel - 1));
					}
				}
			}
		}

		/* CONVERSIONS */
		for (Duct[] duct : new Duct[][] { { TDDucts.itemBasic, TDDucts.itemBasicOpaque }, { TDDucts.itemFast, TDDucts.itemFastOpaque }, { TDDucts.itemEnergy, TDDucts.itemEnergyOpaque },
				//				TODO: Readd Omni/Warp Ducts
				//				{ TDDucts.itemOmni, TDDucts.itemOmniOpaque},
				{ TDDucts.fluidHardened, TDDucts.fluidHardenedOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			addRecipe(ShapelessRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, glassHardened));
			addRecipe(ShapelessRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead"));
		}
		for (Duct[] duct : new Duct[][] { { TDDucts.fluidBasic, TDDucts.fluidBasicOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			addRecipe(ShapelessRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, "blockGlass"));
			addRecipe(ShapelessRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead"));
		}

		/* DENSE / VACUUM - TE Integration */
		//		for (DuctItem duct : new DuctItem[] { TDDucts.itemBasic, TDDucts.itemBasicOpaque,
		//				//				TODO: Readd Omni/Warp Ducts
		//				//				TDDucts.itemOmni, TDDucts.itemOmniOpaque,
		//				TDDucts.itemFast, TDDucts.itemFastOpaque, TDDucts.itemEnergy, TDDucts.itemEnergyOpaque }) {
		//			addRecipe(ShapelessRecipe(duct.getDenseItemStack(), duct.itemStack, "dustLead"));
		//			addRecipe(ShapelessRecipe(duct.getVacuumItemStack(), duct.itemStack, "dustSilver"));
		//			addRecipe(ShapelessRecipe(duct.itemStack, duct.getDenseItemStack()));// , "dustCharcoal"));
		//			addRecipe(ShapelessRecipe(duct.itemStack, duct.getVacuumItemStack()));// , "dustCharcoal"));
		//		}
	}

	/* HELPERS */
	public static void addTransposerFill(int energy, ItemStack input, ItemStack output, FluidStack fluid, boolean reversible) {

		if (useTransposerRecipes && Loader.isModLoaded("thermalexpansion")) {
			ThermalExpansionHelper.addTransposerFill(energy, input, output, fluid, reversible);
		} else {
			int i = MathHelper.clamp(Fluid.BUCKET_VOLUME / fluid.amount, 1, 8);
			ItemStack fluidBucket = getFluidBucket(fluid);
			if (fluidBucket != null) {
				ShapelessOreRecipe recipe = new ShapelessOreRecipe(ItemHelper.cloneStack(output, i), fluidBucket);

				for (int j = 0; j < i; j++) {
					recipe.getInput().add(input.copy());
				}
				addRecipe(recipe);
			}
		}
	}

	public static ItemStack getFluidBucket(FluidStack fluidStack) {

		return UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, fluidStack.getFluid());
	}

	public static ShapelessOreRecipe addInputMetaRange(ShapelessOreRecipe recipe, ItemStack input, int minMeta, int maxMeta) {

		ArrayList<ItemStack> itemStacks = new ArrayList<>(maxMeta - minMeta + 1);
		for (int i = minMeta; i <= maxMeta; i++) {
			input = input.copy();
			input.setItemDamage(i);
			itemStacks.add(input);
		}
		recipe.getInput().add(itemStacks);
		return recipe;
	}

	public static ShapelessOreRecipe addInput(ShapelessOreRecipe recipe, Collection<ItemStack>... inputs) {

		for (Collection<ItemStack> input : inputs) {
			ArrayList<ItemStack> itemStacks = new ArrayList<>(input.size());
			itemStacks.addAll(input);
			recipe.getInput().add(itemStacks);
		}
		return recipe;
	}

	public static ShapelessOreRecipe addInput(ShapelessOreRecipe recipe, ItemStack[]... inputs) {

		for (ItemStack[] input : inputs) {
			ArrayList<ItemStack> itemStacks = new ArrayList<>(input.length);
			Collections.addAll(itemStacks, input);
			recipe.getInput().add(itemStacks);
		}
		return recipe;
	}

}
