package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Duct.Type;
import cofh.thermaldynamics.duct.light.DuctLight;
import cofh.thermaldynamics.duct.tiles.*;

import java.util.ArrayList;
import java.util.List;

public class TDDucts {

	private static final String REDSTONE_STILL = "thermalfoundation:blocks/fluid/redstone_still";
	private static final String GLOWSTONE_STILL = "thermalfoundation:blocks/fluid/glowstone_still";
	private static final String CRYOTHEUM_STILL = "thermalfoundation:blocks/fluid/cryotheum_still";
	private static final String GREEN_GLASS = "thermaldynamics:blocks/duct/base/green_glass";
	private static final IDuctFactory STRUCTURAL = (duct, worldObj) -> new TileStructuralDuct();
	public static ArrayList<Duct> ductList = new ArrayList<>();
	public static ArrayList<Duct> ductListSorted = null;

	public static int OFFSET_ENERGY = 0 * 16;
	public static int OFFSET_FLUID = 1 * 16;
	public static int OFFSET_ITEM = 2 * 16;
	public static int OFFSET_STRUCTURE = 3 * 16;
	public static int OFFSET_TRANSPORT = 4 * 16;

	/* ENERGY */
	public static Duct energyBasic;
	public static Duct energyHardened;
	public static Duct energyReinforced;
	public static Duct energyResonant;
	public static Duct energySignalum;
	public static Duct energySuperCond;

	public static Duct energyReinforcedEmpty;
	public static Duct energyResonantEmpty;
	public static Duct energySignalumEmpty;
	public static Duct energySuperCondEmpty;

	/* FLUID */
	public static Duct fluidBasic;
	public static Duct fluidBasicOpaque;

	public static Duct fluidHardened;
	public static Duct fluidHardenedOpaque;

	public static Duct fluidSuper;
	public static Duct fluidSuperOpaque;

	public static Duct fluidEnergy;
	public static Duct fluidEnergyOpaque;

	/* ITEM */
	public static DuctItem itemBasic;
	public static DuctItem itemBasicOpaque;

	public static DuctItem itemFast;
	public static DuctItem itemFastOpaque;

	public static DuctItem itemEnergy;
	public static DuctItem itemEnergyOpaque;

	public static DuctItem itemEnergyFast;
	public static DuctItem itemEnergyFastOpaque;

	/* ENDER */
	public static DuctItem itemOmni;
	public static DuctItem itemOmniOpaque;

	public static DuctItem itemEnder;
	public static DuctItem itemEnderOpaque;

	/* TRANSPORT */
	public static DuctTransport transportBasic;
	public static DuctTransport transportLongRange;
	public static DuctTransport transportLinking;
	public static DuctTransport transportFrame;

	/* STRUCTURE */
	public static Duct structure;
	public static DuctLight lightDuct;

	/* HELPERS - NOT REAL */
	public static Duct structureInvis = new Duct(-1, false, 1, -1, "structure", Type.STRUCTURAL, STRUCTURAL, "structure", null, null, 0, null, null, 0);
	public static Duct placeholder = new Duct(-1, false, 1, -1, "structure", Type.STRUCTURAL, STRUCTURAL, "structure", null, null, 0, null, null, 0);

	private TDDucts() {

	}

	static Duct addDuct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, IDuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

		Duct newDuct = new Duct(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, frameTexture, frameFluidTexture, frameFluidTransparency);

		return registerDuct(newDuct);
	}

	static DuctItem addDuctItem(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, IDuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

		DuctItem newDuct = new DuctItem(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, frameTexture, frameFluidTexture, frameFluidTransparency);

		return registerDuct(newDuct);
	}

	static DuctTransport addDuctTransport(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, IDuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

		DuctTransport newDuct = new DuctTransport(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, frameTexture, frameFluidTexture, frameFluidTransparency);

		return registerDuct(newDuct);
	}

	static <T extends Duct> T registerDuct(T newDuct) {

		int id = newDuct.id;
		while (id >= ductList.size()) {
			ductList.add(null);
		}
		Duct oldDuct = ductList.set(id, newDuct);
		if (oldDuct != null) {
			ThermalDynamics.LOG.info("Replacing " + oldDuct.unlocalizedName + " with " + newDuct.unlocalizedName);
		}
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
			ductListSorted = new ArrayList<>();
			for (Duct duct : ductList) {
				if (duct != null) {
					ductListSorted.add(duct);
				}
			}
			ductListSorted.sort((o1, o2) -> {

				int i = o1.ductType.compareTo(o2.ductType);
				if (i == 0) {
					i = o1.compareTo(o2);
				}
				return i;
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

	public static boolean addDucts() {

		addEnergyDucts();
		addFluidDucts();
		addItemDucts();
		addSupportDucts();
		addTransportDucts();

		return true;
	}

	static void addEnergyDucts() {

		final int redstoneFluidTransparency = 192;

		energyBasic = addDuct(OFFSET_ENERGY, false, 1, 0, "energyBasic", Type.ENERGY, (duct, worldObj) -> new TileDuctEnergy.Basic(), "lead", "lead", Duct.REDSTONE_BLOCK, 255, null, null, 0);
		energyHardened = addDuct(OFFSET_ENERGY + 1, false, 1, 1, "energyHardened", Type.ENERGY, (duct, worldObj) -> new TileDuctEnergy.Hardened(), "invar", "invar", Duct.REDSTONE_BLOCK, 255, null, null, 0);
		energyReinforced = addDuct(OFFSET_ENERGY + 2, false, 1, 2, "energyReinforced", Type.ENERGY, (duct, worldObj) -> new TileDuctEnergy.Reinforced(), "electrum", "electrum", REDSTONE_STILL, redstoneFluidTransparency, null, null, 0);
		energySignalum = addDuct(OFFSET_ENERGY + 3, false, 1, 3, "energySignalum", Type.ENERGY, (duct, worldObj) -> new TileDuctEnergy.Signalum(), "signalum", "signalum", REDSTONE_STILL, redstoneFluidTransparency, null, null, 0);
		energyResonant = addDuct(OFFSET_ENERGY + 4, false, 1, 4, "energyResonant", Type.ENERGY, (duct, worldObj) -> new TileDuctEnergy.Resonant(), "enderium", "enderium", REDSTONE_STILL, redstoneFluidTransparency, null, null, 0);
		energySuperCond = addDuct(OFFSET_ENERGY + 5, false, 1, 5, "energySuper", Type.ENERGY, (duct, worldObj) -> new TileEnergyDuctSuper(), "enderium", "enderium", REDSTONE_STILL, 255, "electrum", CRYOTHEUM_STILL, 96);

		energyReinforcedEmpty = addDuct(OFFSET_ENERGY + 6, false, 1, -1, "energyReinforcedEmpty", Type.CRAFTING, STRUCTURAL, "electrum", "electrum", null, 0, null, null, 0);
		energySignalumEmpty = addDuct(OFFSET_ENERGY + 7, false, 1, -1, "energySignalumEmpty", Type.CRAFTING, STRUCTURAL, "signalum", "signalum", null, 0, null, null, 0);
		energyResonantEmpty = addDuct(OFFSET_ENERGY + 8, false, 1, -1, "energyResonantEmpty", Type.CRAFTING, STRUCTURAL, "enderium", "enderium", null, 0, null, null, 0);
		energySuperCondEmpty = addDuct(OFFSET_ENERGY + 9, false, 1, -1, "energySuperEmpty", Type.CRAFTING, STRUCTURAL, "enderium", "enderium", REDSTONE_STILL, 192, "electrum", null, 0);

		energyReinforced.setRarity(1);
		energySignalum.setRarity(1);
		energyResonant.setRarity(2);
		energySuperCond.setRarity(2);

		energyReinforcedEmpty.setRarity(1);
		energySignalumEmpty.setRarity(1);
		energyResonantEmpty.setRarity(2);
		energySuperCondEmpty.setRarity(2);
	}

	static void addFluidDucts() {

		fluidBasic = addDuct(OFFSET_FLUID, false, 1, 0, "fluidBasic", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Basic.Transparent(), "copper", "copper", null, 0, null, null, 0);
		fluidBasicOpaque = addDuct(OFFSET_FLUID + 1, true, 1, 0, "fluidBasic", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Basic.Opaque(), "copper", "copper", null, 0, null, null, 0);

		fluidHardened = addDuct(OFFSET_FLUID + 2, false, 1, 1, "fluidHardened", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Hardened.Transparent(), "invar", "invar", null, 0, null, null, 0);
		fluidHardenedOpaque = addDuct(OFFSET_FLUID + 3, true, 1, 1, "fluidHardened", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Hardened.Opaque(), "invar", "invar", null, 0, null, null, 0);

		fluidEnergy = addDuct(OFFSET_FLUID + 4, false, 1, 2, "fluidEnergy", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Energy.Transparent(), "invar_signalum", "invar", null, 0, null, null, 0);
		fluidEnergyOpaque = addDuct(OFFSET_FLUID + 5, true, 1, 2, "fluidEnergy", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Energy.Opaque(), "invar_signalum", "invar", null, 0, null, null, 0);

		fluidSuper = addDuct(OFFSET_FLUID + 6, false, 1, 3, "fluidSuper", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Super.Transparent(), "invar", "invar", null, 0, "bronze_large", null, 0);
		fluidSuperOpaque = addDuct(OFFSET_FLUID + 7, true, 1, 3, "fluidSuper", Type.FLUID, (duct, worldObj) -> new TileDuctFluid.Super.Opaque(), "invar", "invar", null, 0, "bronze_large", null, 0);

		fluidHardened.setRarity(1);
		fluidHardenedOpaque.setRarity(1);

		fluidEnergy.setRarity(1);
		fluidEnergyOpaque.setRarity(1);

		fluidSuper.setRarity(2);
		fluidSuperOpaque.setRarity(2);
	}

	static void addItemDucts() {

		itemBasic = addDuctItem(OFFSET_ITEM + 0, false, 1, 0, "itemBasic", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Basic.Transparent(), "tin", "tin", null, 0, null, null, 0);
		itemBasicOpaque = addDuctItem(OFFSET_ITEM + 1, true, 1, 0, "itemBasic", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Basic.Opaque(), "tin", "tin", null, 0, null, null, 0);

		itemFast = addDuctItem(OFFSET_ITEM + 2, false, 1, 1, "itemFast", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Fast.Transparent(), "tin", "tin", GLOWSTONE_STILL, 80, null, null, 0);
		itemFastOpaque = addDuctItem(OFFSET_ITEM + 3, true, 1, 1, "itemFast", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Fast.Opaque(), "tin_alt", "tin", null, 0, null, null, 0);

		itemEnergy = addDuctItem(OFFSET_ITEM + 4, false, 1, 2, "itemEnergy", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Energy.Transparent(), "tin_signalum", "tin", null, 0, null, null, 0);
		itemEnergyOpaque = addDuctItem(OFFSET_ITEM + 5, true, 1, 2, "itemEnergy", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Energy.Opaque(), "tin_signalum", "tin", null, 0, null, null, 0);

		itemEnergyFast = addDuctItem(OFFSET_ITEM + 6, false, 1, 3, "itemEnergyFast", Type.ITEM, (duct, worldObj) -> new TileDuctItem.EnergyFast.Transparent(), "tin_signalum", "tin", GLOWSTONE_STILL, 80, null, null, 0);
		itemEnergyFastOpaque = addDuctItem(OFFSET_ITEM + 7, true, 1, 3, "itemEnergyFast", Type.ITEM, (duct, worldObj) -> new TileDuctItem.EnergyFast.Opaque(), "tin_alt_signalum", "tin", null, 0, null, null, 0);

		//		TODO: Readd Omni/Warp Ducts
		//		itemEnder = addDuctItem(OFFSET_ITEM + 4, false, 0, 2, "itemEnder", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Warp.Transparent(), "enderium", "enderium", null, 48, null, null, 0);
		//		itemEnderOpaque = addDuctItem(OFFSET_ITEM + 5, true, 0, 2, "itemEnder", Type.ITEM, (duct, worldObj) -> new TileDuctItem.Warp.Opaque(), "enderium", "enderium", null, 48, null, null, 0);

		//		TODO: Readd Omni/Warp Ducts
		//		itemOmni = addDuctItem(OFFSET_ITEM + 8, false, 0, 0, "itemOmni", Type.ITEM, (duct, worldObj) -> new TileDuctOmni.Transparent(), "enderium", "enderium", null, 48, "enderium_trans_large", null, 0);
		//		itemOmniOpaque = addDuctItem(OFFSET_ITEM + 9, true, 0, 0, "itemOmni", Type.ITEM, (duct, worldObj) -> new TileDuctOmni.Opaque(), "enderium", "enderium", null, 48, "enderium_large", null, 0);

		itemFast.setRarity(1);
		itemFastOpaque.setRarity(1);

		itemEnergy.setRarity(1);
		itemEnergyOpaque.setRarity(1);

		itemEnergyFast.setRarity(1);
		itemEnergyFastOpaque.setRarity(1);

		//		TODO: Readd Omni/Warp Ducts
		//		itemOmni.setRarity(2);
		//		itemOmniOpaque.setRarity(2);
	}

	static void addTransportDucts() {

		transportBasic = addDuctTransport(OFFSET_TRANSPORT, false, 1, 0, "transportBasic", Type.TRANSPORT, (duct, worldObj) -> new TileTransportDuct(), null, null, null, 255, "copper", GREEN_GLASS, 96);
		transportLongRange = addDuctTransport(OFFSET_TRANSPORT + 1, false, 1, 1, "transportLongRange", Type.TRANSPORT, (duct, worldObj) -> new TileTransportDuct.LongRange(), null, null, null, 255, "lead", GREEN_GLASS, 80);
		transportLinking = addDuctTransport(OFFSET_TRANSPORT + 2, false, 1, 2, "transportLinking", Type.TRANSPORT, (duct, worldObj) -> new TileTransportDuct.Linking(), null, null, null, 255, "enderium", GREEN_GLASS, 128);
		transportFrame = addDuctTransport(OFFSET_TRANSPORT + 3, false, 1, -1, "transportFrame", Type.CRAFTING, STRUCTURAL, null, null, null, 255, "copper", null, 128);

		transportBasic.setRarity(1);
		transportLongRange.setRarity(1);
		transportLinking.setRarity(1);
		transportFrame.setRarity(1);
	}

	static void addSupportDucts() {

		structure = addDuct(OFFSET_STRUCTURE, true, 1, -1, "structure", Type.STRUCTURAL, STRUCTURAL, "structure", null, null, 0, null, null, 0);

		// lightDuct = registerDuct(new DuctLight(OFFSET_STRUCTURE + 1, 0, "structureLight", Type.STRUCTURAL, (duct, worldObj) -> new TileDuctLight(), "lumium", "lumium", null, 0));
	}

}
