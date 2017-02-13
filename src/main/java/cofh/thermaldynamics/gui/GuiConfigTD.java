package cofh.thermaldynamics.gui;

import cofh.thermaldynamics.ThermalDynamics;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class GuiConfigTD extends GuiConfig {

	public GuiConfigTD(GuiScreen parentScreen) {

		super(parentScreen, getConfigElements(parentScreen), ThermalDynamics.MOD_ID, false, false, ThermalDynamics.MOD_NAME);
	}

	public static final String[] CATEGORIES = {};

	private static List<IConfigElement> getConfigElements(GuiScreen parent) {

		List<IConfigElement> list = new ArrayList<IConfigElement>();

		for (int i = 0; i < CATEGORIES.length; i++) {
			list.add(new ConfigElement(ThermalDynamics.CONFIG.getCategory(CATEGORIES[i])));
		}
		return list;
	}

}
