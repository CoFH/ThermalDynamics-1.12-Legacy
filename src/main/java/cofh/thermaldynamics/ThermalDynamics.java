package cofh.thermaldynamics;

import codechicken.lib.CodeChickenLib;
import cofh.CoFHCore;
import cofh.core.init.CoreProps;
import cofh.core.util.ConfigHandler;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.init.TDBlocks;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.init.TDSounds;
import cofh.thermaldynamics.proxy.Proxy;
import cofh.thermaldynamics.util.TickHandler;
import cofh.thermalfoundation.ThermalFoundation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod (modid = ThermalDynamics.MOD_ID, name = ThermalDynamics.MOD_NAME, version = ThermalDynamics.VERSION, dependencies = ThermalDynamics.DEPENDENCIES, updateJSON = ThermalDynamics.UPDATE_URL)
public class ThermalDynamics {

	public static final String MOD_ID = "thermaldynamics";
	public static final String MOD_NAME = "Thermal Dynamics";

	public static final String VERSION = "2.3.6";
	public static final String VERSION_MAX = "2.4.0";
	public static final String VERSION_GROUP = "required-after:" + MOD_ID + "@[" + VERSION + "," + VERSION_MAX + ");";
	public static final String UPDATE_URL = "https://raw.github.com/cofh/version/master/" + MOD_ID + "_update.json";

	public static final String DEPENDENCIES = CoFHCore.VERSION_GROUP + ThermalFoundation.VERSION_GROUP + CodeChickenLib.MOD_VERSION_DEP;
	public static final String MOD_GUI_FACTORY = "cofh.thermaldynamics.gui.GuiConfigTDFactory";

	@Instance (MOD_ID)
	public static ThermalDynamics instance;

	@SidedProxy (clientSide = "cofh.thermaldynamics.proxy.ProxyClient", serverSide = "cofh.thermaldynamics.proxy.Proxy")
	public static Proxy proxy;

	public static final Logger LOG = LogManager.getLogger(MOD_ID);
	public static final ConfigHandler CONFIG = new ConfigHandler(VERSION);
	public static final ConfigHandler CONFIG_CLIENT = new ConfigHandler(VERSION);
	public static final GuiHandler GUI_HANDLER = new GuiHandler();

	public static CreativeTabs tabCommon;
	public static CreativeTabs tabCovers;

	public ThermalDynamics() {

		super();
	}

	/* INIT */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		CONFIG.setConfiguration(new Configuration(new File(CoreProps.configDir, "/cofh/thermaldynamics/common.cfg"), true));
		CONFIG_CLIENT.setConfiguration(new Configuration(new File(CoreProps.configDir, "cofh/thermaldynamics/client.cfg"), true));

		TDProps.preInit();

		TDBlocks.preInit();
		TDItems.preInit();
		TDSounds.preInit();

		/* Register Handlers */
		registerHandlers();

		proxy.preInit(event);
	}

	@EventHandler
	public void initialize(FMLInitializationEvent event) {

		proxy.initialize(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		proxy.postInit(event);
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {

		CONFIG.cleanUp(false, true);
		CONFIG_CLIENT.cleanUp(false, true);

		LOG.info(MOD_NAME + ": Load Complete.");
	}

	/* HELPERS */
	private void registerHandlers() {

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, GUI_HANDLER);
		MinecraftForge.EVENT_BUS.register(TickHandler.INSTANCE);

		// PacketTDBase.initialize();
	}

}
