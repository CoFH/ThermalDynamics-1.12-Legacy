package cofh.thermaldynamics.plugins.nei;

import codechicken.nei.ItemStackMap;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.item.ItemCover;

public class NEIThermalDynamicsConfig implements IConfigureNEI {

	@Override
	public void loadConfig() {

		API.registerRecipeHandler(RecipeHandlerCover.instance);
		API.registerUsageHandler(RecipeHandlerCover.instance);

		if (!ItemCover.showInNEI) {
			API.hideItem(ItemStackMap.wildcard(ThermalDynamics.itemCover));
		}
		/* ItemDuct Variants */
		for (Duct d : TDDucts.getSortedDucts()) {
			if (d instanceof DuctItem) {
				API.addItemListEntry(((DuctItem) d).getDenseItemStack());
				API.addItemListEntry(((DuctItem) d).getVacuumItemStack());
			}
		}
	}

	@Override
	public String getName() {

		return ThermalDynamics.modName;
	}

	@Override
	public String getVersion() {

		return ThermalDynamics.version;
	}

}
