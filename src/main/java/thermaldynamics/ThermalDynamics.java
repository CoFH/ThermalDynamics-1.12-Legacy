package thermaldynamics;

import cofh.api.core.IInitializer;
import cofh.core.CoFHProps;
import cofh.mod.BaseMod;
import cofh.updater.UpdateManager;
import cofh.util.ConfigHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thermaldynamics.block.BlockDuct;
import thermaldynamics.core.Proxy;
import thermaldynamics.core.TickHandler;
import thermaldynamics.gui.TDCreativeTab;
import thermalfoundation.ThermalFoundation;

@Mod(modid = ThermalDynamics.modId, name = ThermalDynamics.modName, version = ThermalDynamics.version, dependencies = ThermalDynamics.dependencies)
public class ThermalDynamics extends BaseMod {

	public static final String modId = "ThermalDynamics";
	public static final String modName = "Thermal Dynamics";
	public static final String version = "1.7.10R1.0.0B1";
	public static final String dependencies = "required-after:ThermalFoundation@[" + ThermalFoundation.version + ",)";
	public static final String releaseURL = "http://teamcofh.com/thermaldynamics/version/version.txt";

	@Instance(modId)
	public static ThermalDynamics instance;

	@SidedProxy(clientSide = "thermaldynamics.core.ProxyClient", serverSide = "thermaldynamics.core.Proxy")
	public static Proxy proxy;

	public static final Logger log = LogManager.getLogger(modId);

	public static final ConfigHandler config = new ConfigHandler(version);
	// public static final GuiHandler guiHandler = new GuiHandler();

	public static final CreativeTabs tab = new TDCreativeTab();

	/* INIT SEQUENCE */
	public ThermalDynamics() {

		super(log);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		UpdateManager.registerUpdater(new UpdateManager(this, releaseURL));

		config.setConfiguration(new Configuration(new File(CoFHProps.configDir, "/cofh/ThermalDynamics.cfg")));

		blockDuct = new BlockDuct();
		((IInitializer) blockDuct).preInit();

		config.save();
	}

	@EventHandler
	public void initialize(FMLInitializationEvent event) {

		MinecraftForge.EVENT_BUS.register(proxy);

		((IInitializer) blockDuct).initialize();

		FMLCommonHandler.instance().bus().register(TickHandler.INSTANCE);

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		proxy.registerRenderInformation();

		config.cleanUp(false, true);
	}

	/* BaseMod */
	@Override
	public String getModId() {

		return modId;
	}

	@Override
	public String getModName() {

		return modName;
	}

	@Override
	public String getModVersion() {

		return version;
	}

	public static Block blockDuct;

}
