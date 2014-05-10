package thermalducts;

import cofh.api.core.IInitializer;
import cofh.core.CoFHProps;
import cofh.mod.BaseMod;
import cofh.updater.UpdateManager;
import cofh.util.ConfigHandler;
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
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thermalducts.block.BlockDuct;
import thermalducts.core.Proxy;
import thermalfoundation.gui.TFCreativeTab;

@Mod(modid = ThermalDucts.modId, name = ThermalDucts.modName, version = ThermalDucts.version, dependencies = ThermalDucts.dependencies, canBeDeactivated = false)
public class ThermalDucts extends BaseMod {

	public static final String modId = "ThermalFoundation";
	public static final String modName = "Thermal Foundation";
	public static final String version = "1.7.2R1.0.0B1";
	public static final String dependencies = "required-after:CoFHCore@[" + CoFHProps.VERSION + ",)";
	public static final String releaseURL = "http://teamcofh.com/thermalfoundation/version/version.txt";

	@Instance(modId)
	public static ThermalDucts instance;

	@SidedProxy(clientSide = "thermalducts.core.ProxyClient", serverSide = "thermalducts.core.Proxy")
	public static Proxy proxy;

	public static final ConfigHandler config = new ConfigHandler(version);
	public static final Logger log = LogManager.getLogger(modId);

	public static final CreativeTabs tab = new TFCreativeTab();

	/* INIT SEQUENCE */
	public ThermalDucts() {

		super(log);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		UpdateManager.registerUpdater(new UpdateManager(this, releaseURL));

		config.setConfiguration(new Configuration(new File(CoFHProps.configDir, "/cofh/ThermalDucts.cfg")));

		blockDuct = new BlockDuct();
		((IInitializer) blockDuct).preInit();

		config.save();
	}

	@EventHandler
	public void initialize(FMLInitializationEvent event) {

		proxy.registerEntities();
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
