package thermaldynamics.ducts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import thermaldynamics.ducts.Duct.Type;

public class TDDucts {

	private TDDucts() {

	}

	// public static Duct[] ductList = new Duct[96];
	public static ArrayList<Duct> ductList = new ArrayList<Duct>();
	public static ArrayList<Duct> ductListSorted = null;

	static Duct addDuct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
			String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

		Duct newDuct = new Duct(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency,
				frameTexture, frameFluidTexture, frameFluidTransparency);

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

		return ductList.get(id) != null ? ductList.get(id) : structure;
	}

	/* ENERGY */
	public static Duct energyBasic = addDuct(0, false, 1, 0, "energyBasic", Type.ENERGY, DuctFactory.energy, "lead", "lead", Duct.REDSTONE_BLOCK, 255, null,
			null, 255);

	public static Duct energyHardened = addDuct(1, false, 1, 1, "energyHardened", Type.ENERGY, DuctFactory.energy, "invar", "invar", Duct.REDSTONE_BLOCK, 255,
			null, null, 0);

	public static Duct energyReinforced = addDuct(2, false, 1, 2, "energyReinforced", Type.ENERGY, DuctFactory.energy, "electrum", "electrum",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 192, null, null, 0);

	public static Duct energyReinforcedEmpty = addDuct(3, false, 1, -1, "energyReinforcedEmpty", Type.STRUCTURAL, DuctFactory.structural, "electrum",
			"electrum", null, 0, null, null, 0);

	public static Duct energyResonant = addDuct(4, false, 1, 3, "energyResonant", Type.ENERGY, DuctFactory.energy, "enderium", "enderium",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 192, null, null, 0);

	public static Duct energySuperCond = addDuct(5, false, 1, 4, "energySuperconductor", Type.ENERGY, DuctFactory.energy_super, "electrum", "electrum",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 192, "electrum", "thermalfoundation:fluid/Fluid_Cryotheum_Still", 128);

	public static Duct energySuperCondEmpty = addDuct(6, false, 1, -1, "energySuperconductorEmpty", Type.STRUCTURAL, DuctFactory.structural, "electrum",
			"electrum", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, "electrum", null, 0);

	/* FLUID */
	public static Duct fluidBasic = addDuct(7, false, 1, 0, "fluidBasic", Type.FLUID, DuctFactory.fluid_fragile, "copper", "copper", null, 0, null, null, 0);

	public static Duct fluidBasicOpaque = addDuct(8, true, 1, 0, "fluidBasic", Type.FLUID, DuctFactory.fluid_fragile, "copper", "copper", null, 0, null, null,
			0);

	public static Duct fluidHardened = addDuct(9, false, 1, 1, "fluidHardened", Type.FLUID, DuctFactory.fluid, "invar", "invar", null, 0, null, null, 0);

	public static Duct fluidHardenedOpaque = addDuct(10, true, 1, 1, "fluidHardened", Type.FLUID, DuctFactory.fluid, "invar", "invar", null, 0, null, null, 0);

	public static Duct fluidEnergy = addDuct(11, false, 1, 1, "fluidFlux", Type.FLUID, DuctFactory.fluid_flux, "invar", "invar", null, 0, Duct.SIDE_DUCTS,
			"thermalfoundation:fluid/Fluid_Redstone_Still", 192);

	public static Duct fluidEnergyStruct = addDuct(12, false, 1, 1, "fluidFluxEmpty", Type.STRUCTURAL, DuctFactory.structural, "invar", "invar", null, 0,
			Duct.SIDE_DUCTS, null, 0);

	public static Duct fluidEnergyOpaque = addDuct(13, true, 1, 1, "fluidFlux", Type.FLUID, DuctFactory.fluid_flux, "invar", "invar", null, 0, Duct.SIDE_DUCTS,
			"thermalfoundation:fluid/Fluid_Redstone_Still", 192);

	public static Duct fluidEnergyOpaqueStruct = addDuct(14, true, 1, 1, "fluidFluxEmpty", Type.STRUCTURAL, DuctFactory.structural, "invar", "invar", null, 0,
			Duct.SIDE_DUCTS, null, 0);

	/* ITEM */
	public static Duct itemBasic = addDuct(15, false, 1, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);

	public static Duct itemBasicDense = addDuct(16, false, 1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);

	public static Duct itemBasicVacuum = addDuct(17, false, -1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);

	public static Duct itemBasicOpaque = addDuct(18, true, 1, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);

	public static Duct itemBasicOpaqueDense = addDuct(19, true, 1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);

	public static Duct itemBasicOpaqueVacuum = addDuct(20, true, -1000, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);

	public static Duct itemFast = addDuct(21, false, 1, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin", "tin",
			"thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0);

	public static Duct itemFastDense = addDuct(22, false, 1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin", "tin",
			"thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0);

	public static Duct itemFastVacuum = addDuct(23, false, -1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin", "tin",
			"thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0);

	public static Duct itemFastOpaque = addDuct(24, true, 1, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin_1", "tin", null, 0, null, null, 0);

	public static Duct itemFastOpaqueDense = addDuct(25, true, 1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin_1", "tin", null, 0, null, null, 0);

	public static Duct itemFastOpaqueVacuum = addDuct(26, true, -1000, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin_1", "tin", null, 0, null, null, 0);

	public static Duct itemEnder = addDuct(27, false, 0, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48, null, null, 0);

	public static Duct itemEnderDense = addDuct(28, false, 1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48, null,
			null, 0);

	public static Duct itemEnderVacuum = addDuct(29, false, -1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48, null,
			null, 0);

	public static Duct itemEnderOpaque = addDuct(30, true, 0, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48, null, null,
			0);

	public static Duct itemEnderOpaqueDense = addDuct(31, true, 1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48,
			null, null, 0);

	public static Duct itemEnderOpaqueVacuum = addDuct(32, true, -1000, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48,
			null, null, 0);

	public static Duct itemEnergy = addDuct(33, false, 1, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin", "tin",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0);

	public static Duct itemEnergyDense = addDuct(34, false, 1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin", "tin",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0);

	public static Duct itemEnergyVacuum = addDuct(35, false, -1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin", "tin",
			"thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0);

	public static Duct itemEnergyOpaque = addDuct(36, true, 1, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin_2", "tin", null, 0, null, null, 0);

	public static Duct itemEnergyOpaqueDense = addDuct(37, true, 1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin_2", "tin", null, 0, null, null, 0);

	public static Duct itemEnergyOpaqueVacuum = addDuct(38, true, -1000, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin_2", "tin", null, 0, null, null,
			0);

	/* STRUCTURE */
	public static Duct structure = addDuct(39, true, 1, -1, "structure", Type.STRUCTURAL, DuctFactory.structural, "support", null, null, 0, null, null, 0);

	/* HELPERS - NOT REAL */
	public static Duct structureInvis = new Duct(-1, false, 1, -1, "structure", Type.STRUCTURAL, DuctFactory.structural, "support", null, null, 0, null, null,
			0);
}
