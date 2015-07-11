package cofh.thermaldynamics;

import cofh.api.core.IInitializer;
import cofh.core.CoFHProps;
import cofh.core.util.ConfigHandler;
import cofh.mod.BaseMod;
import cofh.mod.updater.UpdateManager;
import cofh.thermaldynamics.core.Proxy;
import cofh.thermaldynamics.core.TickHandler;
import cofh.thermaldynamics.debughelper.CommandThermalDebug;
import cofh.thermaldynamics.debughelper.DebugHelper;
import cofh.thermaldynamics.debughelper.PacketDebug;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.gui.TDCreativeTab;
import cofh.thermaldynamics.gui.TDCreativeTabCovers;
import cofh.thermaldynamics.item.ItemCover;
import cofh.thermaldynamics.item.ItemFilter;
import cofh.thermaldynamics.item.ItemRelay;
import cofh.thermaldynamics.item.ItemRetriever;
import cofh.thermaldynamics.item.ItemServo;
import cofh.thermaldynamics.util.crafting.RecipeCover;
import cofh.thermaldynamics.util.crafting.TDCrafting;
import cofh.thermalfoundation.ThermalFoundation;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.CustomProperty;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.RecipeSorter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ThermalDynamics.modId, name = ThermalDynamics.modName, version = ThermalDynamics.version, dependencies = ThermalDynamics.dependencies,
		guiFactory = ThermalDynamics.modGuiFactory, customProperties = @CustomProperty(k = "cofhversion", v = "true"))
public class ThermalDynamics extends BaseMod {

	public static final String modId = "ThermalDynamics";
	public static final String modName = "Thermal Dynamics";
	public static final String version = "1.7.10R1.1.0B3";
	public static final String dependencies = "required-after:ThermalFoundation@[" + ThermalFoundation.version + ",)";
	public static final String releaseURL = "https://raw.github.com/CoFH/VERSION/master/ThermalDynamics";
	public static final String modGuiFactory = "cofh.thermaldynamics.gui.GuiConfigTDFactory";

	@Instance(modId)
	public static ThermalDynamics instance;

	@SidedProxy(clientSide = "cofh.thermaldynamics.core.ProxyClient", serverSide = "cofh.thermaldynamics.core.Proxy")
	public static Proxy proxy;

	public static final Logger log = LogManager.getLogger(modId);
	public static final ConfigHandler config = new ConfigHandler(version);
	public static final ConfigHandler configClient = new ConfigHandler(version);
	public static final GuiHandler guiHandler = new GuiHandler();

	public static CreativeTabs tabCommon;
	public static CreativeTabs tabCovers;

	/* INIT SEQUENCE */
	public ThermalDynamics() {

		super(log);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		UpdateManager.registerUpdater(new UpdateManager(this, releaseURL, CoFHProps.DOWNLOAD_URL));
		config.setConfiguration(new Configuration(new File(CoFHProps.configDir, "/cofh/thermaldynamics/common.cfg"), true));
		configClient.setConfiguration(new Configuration(new File(CoFHProps.configDir, "cofh/thermaldynamics/client.cfg"), true));

		tabCommon = new TDCreativeTab();
		tabCovers = new TDCreativeTabCovers();

		RecipeSorter.register("thermaldynamics:cover", RecipeCover.class, RecipeSorter.Category.UNKNOWN, "after:forge:shapedore");

		TDDucts.addDucts();

		int numBlocks = (int) Math.ceil(TDDucts.ductList.size() / 16.0);
		blockDuct = new BlockDuct[numBlocks];
		for (int i = 0; i < numBlocks; i++) {
			blockDuct[i] = addBlock(new BlockDuct(i));
		}
		itemServo = addItem(new ItemServo());
		itemFilter = addItem(new ItemFilter());
		itemCover = addItem(new ItemCover());
		itemRetriever = addItem(new ItemRetriever());
		itemRelay = addItem(new ItemRelay());

		for (IInitializer initializer : initializerList) {
			initializer.preInit();
		}
	}

	@EventHandler
	public void initialize(FMLInitializationEvent event) {

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, guiHandler);
		MinecraftForge.EVENT_BUS.register(proxy);

		for (IInitializer initializer : initializerList) {
			initializer.initialize();
		}
		FMLCommonHandler.instance().bus().register(TickHandler.INSTANCE);

		PacketDebug.initialize();
		DebugHelper.initialize();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		for (IInitializer initializer : initializerList) {
			initializer.postInit();
		}
		TDCrafting.loadRecipes();

		proxy.registerRenderInformation();
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {

		config.cleanUp(false, true);
		configClient.cleanUp(false, true);

		log.info("Load Complete.");
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {

		if (DebugHelper.debug) {
			event.registerServerCommand(new CommandThermalDebug());
		}
	}

	/* LOADING FUNCTIONS */
	void configOptions() {

	}

	LinkedList<IInitializer> initializerList = new LinkedList<IInitializer>();

	public <T extends Block> T addBlock(T a) {

		initializerList.add((IInitializer) a);
		return a;
	}

	public <T extends Item> T addItem(T a) {

		initializerList.add((IInitializer) a);
		return a;
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

	public static BlockDuct[] blockDuct;
	public static ItemServo itemServo;
	public static ItemFilter itemFilter;
	public static ItemCover itemCover;
	public static ItemRetriever itemRetriever;
	public static ItemRelay itemRelay;

	@EventHandler
	public void checkMappings(FMLMissingMappingsEvent event) {

		for (FMLMissingMappingsEvent.MissingMapping map : event.get()) {
			if ((modId + ":TestDuct").equals(map.name)) {
				if (map.type == GameRegistry.Type.BLOCK) {
					map.remap(blockDuct[0]);
				} else {
					map.remap(Item.getItemFromBlock(blockDuct[0]));
				}
			}
		}
	}

}
