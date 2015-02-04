package thermaldynamics.util.crafting;

import cofh.api.modhelpers.ThermalExpansionHelper;
import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Collections;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import thermaldynamics.ThermalDynamics;
import thermaldynamics.ducts.Duct;
import thermaldynamics.ducts.TDDucts;
import thermalfoundation.fluid.TFFluids;
import thermalfoundation.item.TFItems;

public class TDCrafting {

	public static void loadRecipes() {

		/* ENERGY */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.energyBasic.itemStack, 6), "RRR", "IGI", "RRR", 'I', "ingotLead", 'G',
				"blockGlass", 'R', "dustRedstone"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.energyHardened.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "nuggetInvar",
				"nuggetInvar", "nuggetInvar"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energyHardened.itemStack, 3), TDDucts.energyBasic.itemStack,
				TDDucts.energyBasic.itemStack, TDDucts.energyBasic.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotInvar"));

		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.energyReinforcedEmpty.itemStack, 6), "IGI", 'I', "ingotElectrum", 'G',
				"blockGlassHardened"));

		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.energyResonant.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "nuggetEnderium",
				"nuggetEnderium", "nuggetEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energyResonant.itemStack, 3), TDDucts.energyReinforced.itemStack,
				TDDucts.energyReinforced.itemStack, TDDucts.energyReinforced.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(TDDucts.energyResonantEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone",
				"nuggetEnderium", "nuggetEnderium", "nuggetEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energyResonantEmpty.itemStack, 3), TDDucts.energyReinforcedEmpty.itemStack,
				TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, "dustRedstone", "dustRedstone", "dustRedstone",
				"ingotEnderium"));

		GameRegistry.addRecipe(new ShapedOreRecipe(TDDucts.energySuperCondEmpty.itemStack, "IGI", "GEG", "IGI", 'I', "ingotElectrum", 'G',
				"blockGlassHardened", 'E', TDDucts.energyReinforced.itemStack));

		// TODO: Config option these
		if (!Loader.isModLoaded("ThermalExpansion")) {
			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energyReinforced.itemStack, 5),
					TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack,
					TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforcedEmpty.itemStack, TFItems.bucketRedstone));
			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.energySuperCond.itemStack, 2), TDDucts.energySuperCondEmpty.itemStack,
					TDDucts.energySuperCondEmpty.itemStack, TFItems.bucketCryotheum));
		}
		/* ENERGY - TE Integration */
		ThermalExpansionHelper.addPulverizerRecipe(1600, TDDucts.energyBasic.itemStack, new ItemStack(Items.redstone),
				ItemHelper.cloneStack(TFItems.nuggetLead, 3), 100);
		ThermalExpansionHelper.addPulverizerRecipe(1600, TDDucts.energyHardened.itemStack, new ItemStack(Items.redstone, 2),
				ItemHelper.cloneStack(TFItems.nuggetInvar, 3), 100);
		ThermalExpansionHelper.addTransposerFill(800, TDDucts.energyReinforcedEmpty.itemStack, TDDucts.energyReinforced.itemStack, new FluidStack(
				TFFluids.fluidRedstone, 200), false);
		ThermalExpansionHelper.addTransposerFill(4000, TDDucts.energySuperCondEmpty.itemStack, TDDucts.energySuperCond.itemStack, new FluidStack(
				TFFluids.fluidCryotheum, 500), false);

		/* FLUID */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidBasic.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "blockGlass"));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidBasicOpaque.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "ingotLead"));

		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidHardened.itemStack, 6), " I ", "IGI", " I ", 'I', "ingotInvar", 'G',
				"blockGlassHardened"));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidHardenedOpaque.itemStack, 6), " I ", "IGI", " I ", 'I', "ingotInvar",
				'G', "ingotLead"));
		// GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidEnergyEmpty.itemStack, 3), "LGL", "PPP", "LGL", 'L', "ingotLead", 'G',
		// "blockGlassHardened", 'P', TDDucts.fluidHardened.itemStack));
		// GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.fluidEnergyOpaqueEmpty.itemStack, 3), "LGL", "PPP", "LGL", 'L', "ingotLead",
		// 'G', "blockGlassHardened", 'P', TDDucts.fluidHardenedOpaque.itemStack));

		/* FLUID - TE Integration */
		// ThermalExpansionHelper.addTransposerFill(800, TDDucts.fluidEnergyEmpty.itemStack, TDDucts.fluidEnergy.itemStack, new FluidStack(
		// TFFluids.fluidRedstone, 100), false);
		// ThermalExpansionHelper.addTransposerFill(800, TDDucts.fluidEnergyOpaqueEmpty.itemStack, TDDucts.fluidEnergyOpaque.itemStack, new FluidStack(
		// TFFluids.fluidRedstone, 100), false);

		/* ITEMS */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.itemBasic.itemStack, 6), "IGI", 'I', "ingotTin", 'G', "blockGlassHardened"));
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.itemBasicOpaque.itemStack, 6), "IGI", 'I', "ingotTin", 'G', "ingotLead"));

		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.itemEnder.itemStack, 3), TDDucts.itemBasic.itemStack,
				TDDucts.itemBasic.itemStack, TDDucts.itemBasic.itemStack, "dustEnderium"));
		GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(TDDucts.itemEnderOpaque.itemStack, 3), TDDucts.itemBasicOpaque.itemStack,
				TDDucts.itemBasicOpaque.itemStack, TDDucts.itemBasicOpaque.itemStack, "dustEnderium"));

		/* ITEMS - TE Integration */
		ThermalExpansionHelper.addTransposerFill(800, TDDucts.itemBasic.itemStack, TDDucts.itemFast.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200),
				false);
		ThermalExpansionHelper.addTransposerFill(800, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemFastOpaque.itemStack, new FluidStack(
				TFFluids.fluidGlowstone, 200), false);
		// ThermalExpansionHelper.addTransposerFill(800, TDDucts.itemBasic.itemStack, TDDucts.itemEnergy.itemStack, new FluidStack(TFFluids.fluidRedstone, 50),
		// false);
		// ThermalExpansionHelper.addTransposerFill(800, TDDucts.itemBasicOpaque.itemStack, TDDucts.itemEnergyOpaque.itemStack, new FluidStack(
		// TFFluids.fluidRedstone, 50), false);

		/* STRUCTURE */
		GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(TDDucts.structure.itemStack, 6), "iIi", 'i', "nuggetLead", 'I', "ingotLead"));
		GameRegistry.addRecipe(RecipeCover.instance);

		/* ATTACHMENTS */
		String[] materials = { "Iron", "Invar", "Electrum", "Signalum", "Enderium" };

		for (int i = 0; i < materials.length; i++) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ThermalDynamics.itemServo, 2, i), "iGi", "IRI", 'R', "dustRedstone", 'G', "blockGlass",
					'I', "ingot" + materials[i], 'i', "nuggetIron"));

			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ThermalDynamics.itemFilter, 2, i), "iGi", "IRI", 'R', Items.paper, 'G', "blockGlass", 'I',
					"ingot" + materials[i], 'i', "nuggetIron"));
			if (i > 0) {
				GameRegistry.addRecipe(addInputMetaRange(new ShapelessOreRecipe(new ItemStack(ThermalDynamics.itemFilter, 1, i), "ingot" + materials[i]),
						new ItemStack(ThermalDynamics.itemFilter, 1, i - 1), 0, i - 1));
				GameRegistry.addRecipe(addInputMetaRange(new ShapelessOreRecipe(new ItemStack(ThermalDynamics.itemServo, 1, i), "ingot" + materials[i]),
						new ItemStack(ThermalDynamics.itemServo, 1, i - 1), 0, i - 1));
			}
		}

		/* CONVERSIONS */
		for (Duct[] duct : new Duct[][] { { TDDucts.itemBasic, TDDucts.itemBasicOpaque }, { TDDucts.itemFast, TDDucts.itemFastOpaque },
				// { TDDucts.itemEnergy, TDDucts.itemEnergyOpaque },
				{ TDDucts.itemEnder, TDDucts.itemEnderOpaque }, { TDDucts.fluidHardened, TDDucts.fluidHardenedOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, "blockGlassHardened"));
			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead"));
		}
		for (Duct[] duct : new Duct[][] { { TDDucts.fluidBasic, TDDucts.fluidBasicOpaque } }) {

			final ItemStack t = duct[0].itemStack;
			final ItemStack o = duct[1].itemStack;

			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, "blockGlass"));
			GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead"));
		}

		/* DENSE / VACUUM - TE Integration */
		for (Duct[] duct : new Duct[][] { { TDDucts.itemBasic, TDDucts.itemBasicDense, TDDucts.itemBasicVacuum },
				{ TDDucts.itemBasicOpaque, TDDucts.itemBasicOpaqueDense, TDDucts.itemBasicOpaqueVacuum },

				{ TDDucts.itemFast, TDDucts.itemFastDense, TDDucts.itemFastVacuum },
				{ TDDucts.itemFastOpaque, TDDucts.itemFastOpaqueDense, TDDucts.itemFastOpaqueVacuum },

				// { TDDucts.itemEnergy, TDDucts.itemEnergyDense, TDDucts.itemEnergyVacuum },
				// { TDDucts.itemEnergyOpaque, TDDucts.itemEnergyOpaqueDense, TDDucts.itemEnergyOpaqueVacuum },

				{ TDDucts.itemEnder, TDDucts.itemEnderDense, TDDucts.itemEnderVacuum },
				{ TDDucts.itemEnderOpaque, TDDucts.itemEnderOpaqueDense, TDDucts.itemEnderOpaqueVacuum }, }) {

			final ItemStack b = duct[0].itemStack;
			final ItemStack d = duct[1].itemStack;
			final ItemStack v = duct[2].itemStack;

			// TODO: Make this better
			GameRegistry.addRecipe(new ShapelessOreRecipe(d, b, "dustSilver"));
			GameRegistry.addRecipe(new ShapelessOreRecipe(v, b, "dustLead"));

			// ThermalExpansionHelper.addTransposerFill(800, duct[0].itemStack, duct[1].itemStack, new FluidStack(TFFluids.fluidCoal, 100), true);
			// ThermalExpansionHelper.addTransposerFill(800, duct[0].itemStack, duct[2].itemStack, new FluidStack(TFFluids.fluidSteam, 1000), true);
		}
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

	public static ShapelessOreRecipe addInput(ShapelessOreRecipe recipe, ItemStack[]... inputs) {

		for (ItemStack[] input : inputs) {
			ArrayList<ItemStack> itemStacks = new ArrayList<ItemStack>(input.length);
			Collections.addAll(itemStacks, input);
			recipe.getInput().add(itemStacks);
		}
		return recipe;
	}

}
