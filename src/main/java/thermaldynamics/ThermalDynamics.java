package thermaldynamics;

import cofh.api.core.IInitializer;
import cofh.core.CoFHProps;
import cofh.core.util.ConfigHandler;
import cofh.mod.BaseMod;
import cofh.mod.updater.UpdateManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import thermaldynamics.block.BlockDuct;
import thermaldynamics.core.Proxy;
import thermaldynamics.core.TickHandler;
import thermaldynamics.debughelper.CommandThermalDebug;
import thermaldynamics.debughelper.DebugHelper;
import thermaldynamics.ducts.TDDucts;
import thermaldynamics.gui.GuiHandler;
import thermaldynamics.gui.TDCreativeTab;
import thermaldynamics.item.ItemCover;
import thermaldynamics.item.ItemFilter;
import thermaldynamics.item.ItemServo;
import thermaldynamics.util.crafting.TDCrafting;
import thermalfoundation.ThermalFoundation;

@Mod(modid = ThermalDynamics.modId, name = ThermalDynamics.modName, version = ThermalDynamics.version, dependencies = ThermalDynamics.dependencies)
public class ThermalDynamics extends BaseMod {

	public static final String modId = "ThermalDynamics";
	public static final String modName = "Thermal Dynamics";
	public static final String version = "1.7.10R1.0.0B1";
	public static final String dependencies = "required-after:ThermalFoundation@[" + ThermalFoundation.version + ",)";
	public static final String releaseURL = "https://raw.github.com/CoFH/VERSION/master/ThermalDynamics";
	public static final String modGuiFactory = "thermaldynamics.gui.GuiConfigTDFactory";

	@Instance(modId)
	public static ThermalDynamics instance;

	@SidedProxy(clientSide = "thermaldynamics.core.ProxyClient", serverSide = "thermaldynamics.core.Proxy")
	public static Proxy proxy;

	public static final Logger log = LogManager.getLogger(modId);

	public static final ConfigHandler config = new ConfigHandler(version);
	public static final GuiHandler guiHandler = new GuiHandler();

	public static final CreativeTabs tab = new TDCreativeTab();

	/* INIT SEQUENCE */
	public ThermalDynamics() {

		super(log);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		UpdateManager.registerUpdater(new UpdateManager(this, releaseURL));
		proxy.registerPacketInformation();
		config.setConfiguration(new Configuration(new File(CoFHProps.configDir, "/cofh/ThermalDynamics.cfg")));

		int numBlocks = (int) Math.ceil(TDDucts.ductList.size() / 16.0);
		blockDuct = new BlockDuct[numBlocks];
		for (int i = 0; i < numBlocks; i++) {
			blockDuct[i] = addBlock(new BlockDuct(i));
		}
		itemServo = addItem(new ItemServo());
		itemFilter = addItem(new ItemFilter());
		itemCover = addItem(new ItemCover());

		for (IInitializer initializer : initializerList) {
			initializer.preInit();
		}
		config.save();
	}

	@EventHandler
	public void initialize(FMLInitializationEvent event) {

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, guiHandler);
		MinecraftForge.EVENT_BUS.register(proxy);

		for (IInitializer initializer : initializerList) {
			initializer.initialize();
		}
		FMLCommonHandler.instance().bus().register(TickHandler.INSTANCE);

		DebugHelper.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		for (IInitializer initializer : initializerList) {
			initializer.postInit();
		}
		proxy.registerRenderInformation();
		TDCrafting.loadRecipes();

		config.cleanUp(false, true);
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {

	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {

		if (DebugHelper.debug)
			event.registerServerCommand(new CommandThermalDebug());
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

	@EventHandler
	public void checkMappings(FMLMissingMappingsEvent event) {

		for (FMLMissingMappingsEvent.MissingMapping map : event.get()) {
			if ((modId + ":TestDuct").equals(map.name)) {
				if (map.type == GameRegistry.Type.BLOCK)
					map.remap(blockDuct[0]);
				else
					map.remap(Item.getItemFromBlock(blockDuct[0]));
			}
		}
	}

}
