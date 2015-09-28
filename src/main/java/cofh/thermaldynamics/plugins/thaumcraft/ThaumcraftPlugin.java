package cofh.thermaldynamics.plugins.thaumcraft;

import static cofh.api.modhelpers.ThaumcraftHelper.parseAspects;
import cofh.asm.relauncher.Strippable;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermalexpansion.ThermalExpansion;
import net.minecraft.item.ItemStack;

public class ThaumcraftPlugin {

	public static void preInit() {

	}

	public static void initialize() {

	}

	public static void postInit() {

	}

	@Strippable("api:Thaumcraft|API")
	public static void loadComplete() throws Throwable {

		parseAspects(TDDucts.itemBasic.itemStack, "1 metallum, 1 iter, 1 machina, 1 vitreus");
		parseAspects(TDDucts.itemBasicOpaque.itemStack, "2 metallum, 1 iter, 1 machina");
		parseAspects(TDDucts.itemFast.itemStack, "1 metallum, 4 iter, 1 machina, 1 vitreus, 1 lux");
		parseAspects(TDDucts.itemFastOpaque.itemStack, "2 metallum, 4 iter, 1 machina, 1 lux");
		parseAspects(TDDucts.itemEnergy.itemStack, "1 metallum, 1 iter, 1 machina, 1 vitreus, 2 potentia");
		parseAspects(TDDucts.itemEnergyOpaque.itemStack, "2 metallum, 1 iter, 1 machina, 2 potentia");
		parseAspects(TDDucts.itemEnder.itemStack, "1 metallum, 2 iter, 2 machina, 1 vitreus, 4 alienis");
		parseAspects(TDDucts.itemEnderOpaque.itemStack, "2 metallum, 2 iter, 2 machina, 4 alienis");


		parseAspects(TDDucts.energyBasic.itemStack, "1 metallum, 1 vitreus, 1 potentia");
		parseAspects(TDDucts.energyHardened.itemStack, "1 metallum, 1 vitreus, 2 potentia");
		parseAspects(TDDucts.energyReinforced.itemStack, "1 metallum, 1 vitreus, 4 potentia, 2 machina");
		parseAspects(TDDucts.energyReinforcedEmpty.itemStack, "1 metallum, 1 vitreus");
		parseAspects(TDDucts.energyResonant.itemStack, "1 metallum, 1 vitreus, 8 potentia, 4 machina, 1 alienis");
		parseAspects(TDDucts.energyResonantEmpty.itemStack, "1 metallum, 1 vitreus, 1 alienis");
		parseAspects(TDDucts.energySuperCond.itemStack, "4 metallum, 2 vitreus, 16 potentia, 4 machina, 8 gelum");
		parseAspects(TDDucts.energySuperCondEmpty.itemStack, "4 metallum, 2 vitreus");


		parseAspects(TDDucts.structure.itemStack, "1 metallum, 1 ordo");
		parseAspects(TDDucts.lightDuct.itemStack, "2 metallum, 4 lux");

		parseAspects(TDDucts.fluidBasic.itemStack, "1 metallum, 1 vitreus, 1 aqua, 1 iter");
		parseAspects(TDDucts.fluidBasicOpaque.itemStack, "2 metallum, 1 aqua, 1 iter");

		parseAspects(TDDucts.fluidFlux.itemStack, "1 metallum, 1 vitreus, 1 aqua, 1 iter, 2 potentia");
		parseAspects(TDDucts.fluidFluxOpaque.itemStack, "2 metallum, 1 aqua, 1 iter, 2 potentia");

		parseAspects(TDDucts.fluidHardened.itemStack, "1 metallum, 1 vitreus, 1 aqua, 1 iter, 1 tutamen, 1 ignis");
		parseAspects(TDDucts.fluidHardened.itemStack, "2 metallum, 1 aqua, 1 iter, 1 tutamen, 1 ignis");

		parseAspects(TDDucts.fluidSuper.itemStack, "1 metallum, 1 vitreus, 4 aqua, 4 iter, 2 tutamen");
		parseAspects(TDDucts.fluidSuper.itemStack, "2 metallum, 4 aqua, 4 iter, 2 tutamen");


		parseAspects(TDDucts.transport.itemStack, "4 metallum, 1 vitreus, 4 iter");
		parseAspects(TDDucts.transport_longrange.itemStack, "4 metallum, 2 vitreus, 8 iter");
		parseAspects(TDDucts.transport_crossover.itemStack, "4 metallum, 2 vitreus, 4 iter, 4 alienis");
		parseAspects(TDDucts.transport_structure.itemStack, "4 metallum, 1 vitreus");

		parseAspects(ThermalDynamics.itemRelay, "1 metallum, 4 machina, 4 potentia");

		final String[] additionalAspects = {"", ", 1 tutamen", "1 metallum", "1 metallum, 1 potentia, 1 machina", "1 metallum, 2 alienis"};
		for (int i = 0; i < 5; i++) {
			parseAspects(new ItemStack(ThermalDynamics.itemServo, 1, i), "1 metallum, 1 potentia, 2 permutatio" + additionalAspects[i]);
			parseAspects(new ItemStack(ThermalDynamics.itemFilter, 1, i), "1 metallum, 1 machina, 2 permutatio" + additionalAspects[i]);
			parseAspects(new ItemStack(ThermalDynamics.itemRetriever, 1, i), "1 metallum, 1 potentia, 1 machina, 2 permutatio, 2 alienis" + additionalAspects[i]);
		}

		ThermalExpansion.log.info("Thermal Dynamics: Thaumcraft Plugin Enabled.");
	}


}