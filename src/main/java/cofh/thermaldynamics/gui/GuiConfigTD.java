package cofh.thermaldynamics.gui;

import cofh.thermaldynamics.ThermalDynamics;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;

public class GuiConfigTD extends GuiConfig {

	public GuiConfigTD(GuiScreen parentScreen) {

		super(parentScreen, getConfigElements(parentScreen), ThermalDynamics.modId, false, false, ThermalDynamics.modName);
	}

	public static final String[] CATEGORIES = {};

	@SuppressWarnings("rawtypes")
	private static List<IConfigElement> getConfigElements(GuiScreen parent) {

		List<IConfigElement> list = new ArrayList<IConfigElement>();

		for (int i = 0; i < CATEGORIES.length; i++) {
			list.add(new ConfigElement<ConfigCategory>(ThermalDynamics.config.getCategory(CATEGORIES[i])));
		}
		return list;
	}

}
