package cofh.thermaldynamics.util.crafting;

import cofh.api.modhelpers.ThermalExpansionHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermalfoundation.fluid.TFFluids;
import cofh.thermalfoundation.item.TFItems;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class TDCrafting {

	public static boolean enableCoverRecipes = true;
	public static boolean useHardenedGlass = true;
	public static boolean useTransposerRecipes = true;

	public static void loadRecipes() {

		enableCoverRecipes = ThermalDynamics.config.get("Attachment.Cover", "Recipe", true);
		useHardenedGlass = ThermalDynamics.config.get("Duct.Recipes", "UseHardenedGlass", true);
		useTransposerRecipes = ThermalDynamics.config.get("Duct.Recipes", "UseFluidTransposer", true);
		String glassHardened = useHardenedGlass ? "blockGlassHardened" : "blockGlass";

		/* ENERGY */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.energyBasic.itemStack, 6), "RRR", "IGI", "RRR", 'I', "ingotLead", 'G',
				"blockGlass", 'R', "dustRedstone"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.energyHardened.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "nuggetInvar",
				"nuggetInvar", "nuggetInvar"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energyHardened.itemStack, 3), TDDucts.energyBasic.itemStack,
				TDDucts.energyBasic.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotInvar"));

		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.energyReinforcedEmpty.itemStack, 6), "IGI", 'I', "ingotElectrum", 'G',
				glassHardened));

		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.energyResonant.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "nuggetEnderium",
				"nuggetEnderium", "nuggetEnderium"));

		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energyResonant.itemStack, 3), TDDucts.energyReinforced.itemStack,
				TDDucts.energyReinforced.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotEnderium"));

		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.energyResonantEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone",
				"nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energyResonantEmpty.itemStack, 3), TDDucts.energyReinforcedEmpty.itemStack,
				TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone", "dustRedstone", "dustRedstone",
				"ingotEnderium"));

		GameRegistry.addRecipe(new ShapedOreRecipe(TDDucts.energySuperCondEmpty.itemStack, "IGI", "GEG", "IGI", 'I', "ingotElectrum", 'G', glassHardened, 'E',
				TDDucts.energyReinforced.itemStack));

		/* ENERGY - TE Integration */
		ThermalExpansionHelper.addPulverizerRecipe(1600, TDDucts.energyBasic.itemStack, new ItemStack(Items.redstone),
				ItemHelper.cloneStack(TFItems.nuggetLead, 3));
		ThermalExpansionHelper.addPulverizerRecipe(1600, TDDucts.energyHardened.itemStack, new ItemStack(Items.redstone, 2),
				ItemHelper.cloneStack(TFItems.nuggetInvar, 3));
		addTransposerFill(800, TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforced.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(800, TDDucts.energyResonantEmpty.itemStack, TDDucts.energyResonant.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false);
		addTransposerFill(4000, TDDucts.energySuperCondEmpty.itemStack, TDDucts.energySuperCond.itemStack, new FluidStack(TFFluids.fluidCryotheum, 500), false);

		/* FLUID */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidBasic.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "blockGlass"));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidBasicOpaque.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "ingotLead"));

		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidHardened.itemStack, 6), "IGI", 'I', "ingotInvar", 'G', glassHardened));
		GameRegistry
				.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidHardenedOpaque.itemStack, 6), "IGI", 'I', "ingotInvar", 'G', "ingotLead"));

		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.fluidFlux.itemStack, TDDucts.fluidHardened.itemStack, "nuggetSignalum", "nuggetSignalum",
				"nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.fluidFluxOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "nuggetSignalum",
				"nuggetSignalum", "nuggetSignalum", "nuggetElectrum", "nuggetElectrum", "nuggetElectrum"));

		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.fluidFlux.itemStack, 3), TDDucts.fluidHardened.itemStack,
				TDDucts.fluidHardened.itemStack, TDDucts.fluidHardened.itemStack, "ingotSignalum", "ingotElectrum"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.fluidFluxOpaque.itemStack, 3), TDDucts.fluidHardenedOpaque.itemStack,
				TDDucts.fluidHardenedOpaque.itemStack, TDDucts.fluidHardenedOpaque.itemStack, "ingotSignalum", "ingotElectrum"));

		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidSuper.itemStack, 6), "PlP", "IGI", "PlP", 'I', "ingotLumium", 'G',
				glassHardened, 'P', "ingotPlatinum", 'l', "nuggetLead"));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidSuperOpaque.itemStack, 6), "PlP", "IGI", "PlP", 'I', "ingotLumium", 'G',
				"ingotLead", 'P', "ingotPlatinum", 'l', "nuggetLead"));

		/* FLUID - TE Integration */
		ThermalExpansionHelper.addPulverizerRecipe(1600, TDDucts.fluidBasic.itemStack, ItemHelper.cloneStack(TFItems.nuggetCopper, 3));
		ThermalExpansionHelper.addPulverizerRecipe(1600, TDDucts.fluidBasicOpaque.itemStack, ItemHelper.cloneStack(TFItems.nuggetCopper, 3),
				ItemHelper.cloneStack(TFItems.nuggetLead));

		/* ITEMS */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.itemBasic.itemStack, 6), "IGI", 'I', "ingotTin", 'G', glassHardened));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.itemBasicOpaque.itemStack, 6), "IGI", 'I', "ingotTin", 'G', "ingotLead"));

		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.itemEnder.itemStack, 2), TDDucts.itemBasic.itemStack,
				TDDucts.itemBasic.itemStack, "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.itemEnderOpaque.itemStack, 2), TDDucts.itemBasicOpaque.itemStack,
				TDDucts.itemBasicOpaque.itemStack, "nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.itemEnder.itemStack, 6), TDDucts.itemBasic.itemStack,
				TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack,
				TDDucts.itemBasic.itemStack, "ingotEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.itemEnderOpaque.itemStack, 6), TDDucts.itemBasicOpaque.itemStack,
				TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack,
				TDDucts.itemBasicOpaque.itemStack, "ingotEnderium"));

		/* ITEMS - TE Integration */
		addTransposerFill(800, TDDucts.itemBasic.itemStack, TDDucts.itemFast.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemFastOpaque.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false);
		addTransposerFill(800, TDDucts.itemBasic.itemStack, TDDucts.itemEnergy.itemStack, new FluidStack(TFFluids.fluidRedstone, 50), false);
		addTransposerFill(800, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemEnergyOpaque.itemStack, new FluidStack(TFFluids.fluidRedstone, 50), false);

		/* STRUCTURE */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.structure.itemStack, 6), "iIi", 'i', "nuggetIron", 'I', "ingotLead"));

		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.lightDuct.itemStack, 6), "LIL", 'L', "ingotLumium", 'I', "ingotLead", 'R',
				"dustRedstone"));

		if (enableCoverRecipes) {
			GameRegistry.addRecipe(RecipeCover.instance);
		}

		/* SIGNALLER */

		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ThermalDynamics.itemSignaller, 2), "iGi", "IRI", 'R', "dustRedstone", 'G', "gearSignalum",
				'I', "ingotLead", 'i', "nuggetSignalum"));

		/* ATTACHMENTS */
		String[] materials = { "Iron", "Invar", "Electrum", "Signalum", "Enderium" };

		int hardGlassLevel = useHardenedGlass ? 2 : 5; // level to start using hardened glass

		for (int i = 0; i < materials.length; i++) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ThermalDynamics.itemServo, 2, i), "iGi", "IRI", 'R', "dustRedstone", 'G',
					i < hardGlassLevel ? "blockGlass" : "blockGlassHardened", 'I', "ingot" + materials[i], 'i', "nuggetIron"));

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ThermalDynamics.itemFilter, 2, i), "iGi", "IRI", 'R', Items.paper, 'G',
					i < hardGlassLevel ? "blockGlass" : "blockGlassHardened", 'I', "ingot" + materials[i], 'i', "nuggetIron"));

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ThermalDynamics.itemRetriever, 2, i), "iGi", "IRI", 'R', Items.ender_eye, 'G',
					i < hardGlassLevel ? "blockGlass" : "blockGlassHardened", 'I', "ingot" + materials[i], 'i', "nuggetEnderium"));

			if (i > 0) {
				for (Item item : new Item[] { ThermalDynamics.itemFilter, ThermalDynamics.itemServo, ThermalDynamics.itemRetriever }) {
					if (i < hardGlassLevel) {
						GameRegistry.addRecipe(addInputMetaRange(new ShapelessOreRecipe(new ItemStack(item, 1, i), "ingot" + materials[i]), new ItemStack(item,
								1), 0, i - 1));
					} else {
						if (i > hardGlassLevel) {
							GameRegistry.addRecipe(addInputMetaRange(new ShapelessOreRecipe(new ItemStack(item, 1, i), "ingot" + materials[i]), new ItemStack(
									item, 1), hardGlassLevel, i - 1));
						}
						GameRegistry.addRecipe(addInputMetaRange(
								addInputMetaRange(new ShapelessOreRecipe(new ItemStack(item, 2, i), "blockGlassHardened", "ingot" + materials[i], "ingot"
										+ materials[i]), new ItemStack(item, 1), 0, hardGlassLevel - 1), new ItemStack(item, 1), 0, hardGlassLevel - 1));
					}
				}
			}
		}

		/* CONVERSIONS */
		for (Duct[] duct : new Duct[][] { { TDDucts.itemBasic, TDDucts.itemBasicOpaque }, { TDDucts.itemFast, TDDucts.itemFastOpaque },
				// { TDDucts.itemEnergy, TDDucts.itemEnergyOpaque },
				{ TDDucts.itemEnder, TDDucts.itemEnderOpaque }, { TDDucts.fluidHardened, TDDucts.fluidHardenedOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, glassHardened));
			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead"));
		}
		for (Duct[] duct : new Duct[][] { { TDDucts.fluidBasic, TDDucts.fluidBasicOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, "blockGlass"));
			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead"));
		}

		/* DENSE / VACUUM - TE Integration */
		for (DuctItem duct : new DuctItem[] { TDDucts.itemBasic, TDDucts.itemBasicOpaque, TDDucts.itemEnder, TDDucts.itemEnderOpaque, TDDucts.itemFast,
				TDDucts.itemFastOpaque }) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(duct.getDenseItemStack(), duct.itemStack, "dustLead"));
			GameRegistry.addRecipe(new ShapelessOreRecipe(duct.getVacuumItemStack(), duct.itemStack, "dustSilver"));
		}
	}

	public static void addTransposerFill(int energy, ItemStack input, ItemStack output, FluidStack fluid, boolean reversible) {

		if (useTransposerRecipes && Loader.isModLoaded("ThermalExpansion")) {
			ThermalExpansionHelper.addTransposerFill(energy, input, output, fluid, reversible);
		} else {
			int i = MathHelper.clampI(FluidContainerRegistry.BUCKET_VOLUME / fluid.amount, 1, 8);
			ItemStack fluidBucket = getFluidBucket(fluid);
			if (fluidBucket != null) {
				ShapelessOreRecipe recipe = new ShapelessOreRecipe(ItemHelper.cloneStack(output, i), fluidBucket);

				for (int j = 0; j < i; j++) {
					recipe.getInput().add(input.copy());
				}
				GameRegistry.addRecipe(recipe);
			}
		}
	}

	public static ItemStack getFluidBucket(FluidStack fluidStack) {

		fluidStack = fluidStack.copy();
		fluidStack.amount = FluidContainerRegistry.BUCKET_VOLUME;
		return FluidContainerRegistry.fillFluidContainer(fluidStack, FluidContainerRegistry.EMPTY_BUCKET);
	}

	public static ShapelessOreRecipe addInputMetaRange(ShapelessOreRecipe recipe, ItemStack input, int minMeta, int maxMeta) {

		ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>(maxMeta - minMeta + 1);
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
			ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>(input.size());
			itemStacks.addAll(input);
			recipe.getInput().add(itemStacks);
		}
		return recipe;
	}

	public static ShapelessOreRecipe addInput(ShapelessOreRecipe recipe, ItemStack[]... inputs) {

		for (ItemStack[] input : inputs) {
			ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>(input.length);
			Collections.addAll(itemStacks, input);
			recipe.getInput().add(itemStacks);
		}
		return recipe;
	}
}
