package cofh.thermaldynamics.proxy;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.block.BlockRenderingRegistry;
import cofh.api.core.IModelRegister;
import cofh.core.render.IconRegistry;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import cofh.thermaldynamics.duct.entity.RenderTransport;
import cofh.thermaldynamics.duct.entity.SoundWoosh;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.render.RenderDuct;
import cofh.thermaldynamics.render.RenderDuctFluids;
import cofh.thermaldynamics.render.RenderDuctItems;
import cofh.thermaldynamics.render.RenderDuctItemsEnder;
import cofh.thermaldynamics.render.item.RenderItemCover;
import cofh.thermaldynamics.util.TickHandlerClient;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ProxyClient extends Proxy {

	public static EnumBlockRenderType renderType;
	public static List<IModelRegister> modelRegisters = new ArrayList<>();

	/* INIT */
	@Override
	public void preInit(FMLPreInitializationEvent event) {

		MinecraftForge.EVENT_BUS.register(TickHandlerClient.INSTANCE);

		String[] names = { "basic", "hardened", "reinforced", "signalum", "resonant" };
		Item[] items = { TDItems.itemFilter, TDItems.itemRetriever, TDItems.itemServo };
		for (Item item : items) {
			for (int i = 0; i < names.length; i++) {
				ModelResourceLocation location = new ModelResourceLocation("thermaldynamics:attachment", "type=" + item.getRegistryName().getResourcePath() + "_" + names[i]);
				ModelLoader.setCustomModelResourceLocation(item, i, location);
			}
		}
		ModelResourceLocation location = new ModelResourceLocation("thermaldynamics:attachment", "type=relay");
		ModelLoader.setCustomModelResourceLocation(TDItems.itemRelay, 0, location);

		ModelRegistryHelper.registerItemRenderer(TDItems.itemCover, RenderItemCover.instance);

		RenderingRegistry.registerEntityRenderingHandler(EntityTransport.class, RenderTransport::new);
		GameRegistry.register(SoundWoosh.WOOSH);
		for (IModelRegister register : modelRegisters) {
			register.registerModels();
		}
	}

	@Override
	public void initialize(FMLInitializationEvent event) {

		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuctEnder.class, RenderDuctItemsEnder.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.class, RenderDuctFluids.instance);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

		ProxyClient.renderType = BlockRenderingRegistry.createRenderType("TD");
		BlockRenderingRegistry.registerRenderer(ProxyClient.renderType, RenderDuct.instance);
	}

	@Override
	public void addIModelRegister(IModelRegister register) {
		modelRegisters.add(register);
	}

	@Override
	@SideOnly (Side.CLIENT)
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {

		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 2; j++) {
				IconRegistry.addIcon("ServoBase" + (i * 2 + j), "thermaldynamics:blocks/duct/attachment/servo/servo_base_" + i + "" + j, event.getMap());
				IconRegistry.addIcon("RetrieverBase" + (i * 2 + j), "thermaldynamics:blocks/duct/attachment/retriever/retriever_base_" + i + "" + j, event.getMap());
			}
		}

		IconRegistry.addIcon("Signaller", "thermaldynamics:blocks/duct/attachment/signallers/signaller", event.getMap());

		IconRegistry.addIcon("CoverBase", "thermaldynamics:blocks/duct/attachment/cover/support", event.getMap());

		for (int i = 0; i < 5; i++) {
			IconRegistry.addIcon("FilterBase" + i, "thermaldynamics:blocks/duct/attachment/filter/filter_" + i + "0", event.getMap());
		}
		IconRegistry.addIcon("SideDucts", "thermaldynamics:blocks/duct/side_ducts", event.getMap());

		for (int i = 0; i < TDDucts.ductList.size(); i++) {
			if (TDDucts.isValid(i)) {
				TDDucts.ductList.get(i).registerIcons(event.getMap());
			}
		}
		TDDucts.structureInvis.registerIcons(event.getMap());
	}

	@Override
	@SideOnly (Side.CLIENT)
	@SubscribeEvent
	public void initializeIcons(TextureStitchEvent.Post event) {

		RenderDuct.initialize();
	}

}
