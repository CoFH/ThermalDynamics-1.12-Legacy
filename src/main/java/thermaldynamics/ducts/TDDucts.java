package thermaldynamics.ducts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import thermaldynamics.ducts.Duct.Type;

public class TDDucts {

	private TDDucts() {

	}

	public static final String PATH_DUCT = "thermaldynamics:duct";

	public static ArrayList<Duct> ductList = new ArrayList<Duct>();
	public static ArrayList<Duct> ductListSorted = null;

	static Duct addDuct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
			String connectionTexture, String fluidTexture, int fluidTransparency, String overDuct, String overDuct2, int overDuct2Trans) {

		Duct newDuct = new Duct(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency,
				overDuct, overDuct2, overDuct2Trans);
		ductList.add(newDuct);
		return newDuct;
	}

	public static Duct getDuct(int id) {

		if (isValid(id)) {
			return ductList.get(id);
		} else {
			return structure;
		}
	}

	public static List<Duct> getSortedDucts() {

		if (ductListSorted == null) {
			ductListSorted = new ArrayList<Duct>();
			for (Duct duct : ductList) {
				if (duct != null) {
					ductListSorted.add(duct);
				}
			}
			Collections.sort(ductListSorted, new Comparator<Duct>() {

				@Override
				public int compare(Duct o1, Duct o2) {

					int i = o1.ductType.compareTo(o2.ductType);
					if (i == 0) {
						i = o1.compareTo(o2);
					}
					return i;
				}
			});
		}
		return ductListSorted;
	}

	public static boolean isValid(int id) {

		return id < ductList.size() && ductList.get(id) != null;
	}

	public static Duct getType(int id) {

		return ductList.get(id);
	}

	/* ENERGY */
	public static Duct energyBasic = addDuct(0, false, 1, 0, "energyBasic", Type.ENERGY, DuctFactory.energy, "lead", PATH_DUCT + "energy/ConnectionEnergy00",
			Duct.REDSTONE_BLOCK, 255, null, null, 255);

	public static Duct energyHardened = addDuct(1, false, 1, 1, "energyHardened", Type.ENERGY, DuctFactory.energy, "invar", PATH_DUCT
			+ "energy/ConnectionEnergy10", Duct.REDSTONE_BLOCK, 255, null, null, 0);

	public static Duct energyReinforced = addDuct(2, false, 1, 2, "energyReinforced", Type.ENERGY, DuctFactory.energy, "electrum", PATH_DUCT
			+ "energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, null, null, 0);

	public static Duct energyReinforcedStruct = addDuct(9, false, 1, -1, "energyReinforcedEmpty", Type.STRUCTURAL, DuctFactory.structural, "electrum",
			PATH_DUCT + "energy/ConnectionEnergy20", null, 0, null, null, 0);

	public static Duct energySuperCond = addDuct(13, false, 1, 3, "energySuperconductor", Type.ENERGY, DuctFactory.energy_super, "electrum", PATH_DUCT
			+ "energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, PATH_DUCT + "overduct/OverDuctElectrum",
			"thermalfoundation:fluid/Fluid_Cryotheum_Still", 72);

	public static Duct energySuperCondStruct = addDuct(14, false, 1, -1, "energySuperconductorEmpty", Type.STRUCTURAL, DuctFactory.structural, "electrum",
			PATH_DUCT + "energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, PATH_DUCT + "overduct/OverDuctElectrum", null, 0);

	/* FLUID */
	public static Duct fluidBasic = addDuct(3, false, 1, 0, "fluidBasic", Type.FLUID, DuctFactory.fluid_fragile, "copper", PATH_DUCT
			+ "fluid/ConnectionFluid00", null, 0, null, null, 0);

	public static Duct fluidBasicOpaque = addDuct(4, true, 1, 0, "fluidBasic", Type.FLUID, DuctFactory.fluid_fragile, "copper", PATH_DUCT
			+ "fluid/ConnectionFluid00", null, 0, null, null, 0);

	public static Duct fluidHardened = addDuct(11, false, 1, 1, "fluidHardened", Type.FLUID, DuctFactory.fluid, "invar", PATH_DUCT + "fluid/ConnectionFluid10",
			null, 0, null, null, 0);

	public static Duct fluidHardenedOpaque = addDuct(12, true, 1, 1, "fluidHardened", Type.FLUID, DuctFactory.fluid, "invar", PATH_DUCT
			+ "fluid/ConnectionFluid11", null, 0, null, null, 0);

	public static Duct fluidEnergy = addDuct(35, false, 1, 1, "fluidFlux", Type.FLUID, DuctFactory.fluid_flux, "invar", PATH_DUCT + "fluid/ConnectionFluid10",
			null, 0, Duct.SIDE_DUCTS, "thermalfoundation:fluid/Fluid_Redstone_Still", 192);

	public static Duct fluidEnergyStruct = addDuct(37, false, 1, 1, "fluidFluxEmpty", Type.STRUCTURAL, DuctFactory.structural, "invar", PATH_DUCT
			+ "fluid/ConnectionFluid10", null, 0, Duct.SIDE_DUCTS, null, 0);

	public static Duct fluidEnergyOpaque = addDuct(36, true, 1, 1, "fluidFlux", Type.FLUID, DuctFactory.fluid_flux, "invar", PATH_DUCT
			+ "fluid/ConnectionFluid11", null, 0, Duct.SIDE_DUCTS, "thermalfoundation:fluid/Fluid_Redstone_Still", 192);

	public static Duct fluidEnergyOpaqueStruct = addDuct(38, true, 1, 1, "fluidFluxEmpty", Type.STRUCTURAL, DuctFactory.structural, "invar", PATH_DUCT
			+ "fluid/ConnectionFluid11", null, 0, Duct.SIDE_DUCTS, null, 0);

	/* ITEM */
	public static Duct itemBasic = addDuct(5, false, 1, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00", null, 0, null,
			null, 0);

	public static Duct itemBasicDense = addDuct(19, false, 1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00", null,
			0, null, null, 0);

	public static Duct itemBasicVacuum = addDuct(27, false, -1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00",
			null, 0, null, null, 0);

	public static Duct itemBasicOpaque = addDuct(6, true, 1, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00", null, 0,
			null, null, 0);

	public static Duct itemBasicOpaqueDense = addDuct(20, true, 1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00",
			null, 0, null, null, 0);

	public static Duct itemBasicOpaqueVacuum = addDuct(28, true, -1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin",
			PATH_DUCT + "item/ConnectionItem00", null, 0, null, null, 0);

	public static Duct itemFast = addDuct(7, false, 1, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00",
			"thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0);

	public static Duct itemFastDense = addDuct(21, false, 1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00",
			"thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0);

	public static Duct itemFastVacuum = addDuct(29, false, -1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin", PATH_DUCT + "item/ConnectionItem00",
			"thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0);

	public static Duct itemFastOpaque = addDuct(8, true, 1, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin_1", PATH_DUCT + "item/ConnectionItem00", null, 0,
			null, null, 0);

	public static Duct itemFastOpaqueDense = addDuct(22, true, 1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin_1", PATH_DUCT + "item/ConnectionItem00",
			null, 0, null, null, 0);

	public static Duct itemFastOpaqueVacuum = addDuct(30, true, -1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin_1",
			PATH_DUCT + "item/ConnectionItem00", null, 0, null, null, 0);

	public static Duct itemEnder = addDuct(15, false, 0, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", PATH_DUCT + "item/ConnectionItem20",
			null, 48, null, null, 0);

	public static Duct itemEnderDense = addDuct(23, false, 1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", PATH_DUCT
			+ "item/ConnectionItem20", null, 48, null, null, 0);

	public static Duct itemEnderVacuum = addDuct(31, false, -1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", PATH_DUCT
			+ "item/ConnectionItem20", null, 48, null, null, 0);

	public static Duct itemEnderOpaque = addDuct(16, true, 0, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", PATH_DUCT
			+ "item/ConnectionItem20", null, 48, null, null, 0);

	public static Duct itemEnderOpaqueDense = addDuct(24, true, 1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", PATH_DUCT
			+ "item/ConnectionItem20", null, 48, null, null, 0);

	public static Duct itemEnderOpaqueVacuum = addDuct(32, true, -1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", PATH_DUCT
			+ "item/ConnectionItem20", null, 48, null, null, 0);

	public static Duct itemEnergy = addDuct(17, false, 1, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin", PATH_DUCT + "item/ConnectionItem00",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0);

	public static Duct itemEnergyDense = addDuct(25, false, 1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin", PATH_DUCT + "item/ConnectionItem00",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0);

	public static Duct itemEnergyVacuum = addDuct(33, false, -1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin",
			PATH_DUCT + "item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0);

	public static Duct itemEnergyOpaque = addDuct(18, true, 1, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin_2", PATH_DUCT + "item/ConnectionItem00",
			null, 0, null, null, 0);

	public static Duct itemEnergyOpaqueDense = addDuct(26, true, 1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin_2", PATH_DUCT
			+ "item/ConnectionItem00", null, 0, null, null, 0);

	public static Duct itemEnergyOpaqueVacuum = addDuct(34, true, -1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin_2", PATH_DUCT
			+ "item/ConnectionItem00", null, 0, null, null, 0);

	/* STRUCTURE */
	public static Duct structure = addDuct(10, true, 1, -1, "structure", Type.STRUCTURAL, DuctFactory.structural, "support", null, null, 0, null, null, 0);

}
