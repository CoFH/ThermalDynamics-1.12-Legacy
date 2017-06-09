package cofh.thermaldynamics.proxy;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.block.BlockRenderingRegistry;
import cofh.core.render.IModelRegister;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import cofh.thermaldynamics.duct.entity.RenderTransport;
import cofh.thermaldynamics.duct.entity.SoundWoosh;
import cofh.thermaldynamics.duct.tiles.TileFluidDuct;
import cofh.thermaldynamics.duct.tiles.TileItemDuct;
import cofh.thermaldynamics.duct.tiles.TileItemDuct.Energy;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.render.RenderDuct;
import cofh.thermaldynamics.render.RenderDuctFluids;
import cofh.thermaldynamics.render.RenderDuctItems;
import cofh.thermaldynamics.render.item.RenderItemCover;
import cofh.thermaldynamics.util.TickHandlerClient;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public class ProxyClient extends Proxy {

	public static EnumBlockRenderType renderType;

	/* INIT */
	@Override
	public void preInit(FMLPreInitializationEvent event) {

		MinecraftForge.EVENT_BUS.register(TickHandlerClient.INSTANCE);

		ModelRegistryHelper.registerItemRenderer(TDItems.itemCover, RenderItemCover.instance);

		RenderingRegistry.registerEntityRenderingHandler(EntityTransport.class, RenderTransport::new);
		GameRegistry.register(SoundWoosh.WOOSH);
		for (IModelRegister register : modelRegisters) {
			register.registerModels();
		}
	}

	@Override
	public void initialize(FMLInitializationEvent event) {

		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Basic.Transparent.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Fast.Transparent.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Energy.Transparent.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.EnergyFast.Transparent.class, RenderDuctItems.instance);
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Warp.Transparent.class, RenderDuctItemsEnder.instance);
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctOmni.Transparent.class, RenderDuctOmni.instance);

		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Basic.Transparent.class, RenderDuctFluids.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Super.Transparent.class, RenderDuctFluids.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Hardened.Transparent.class, RenderDuctFluids.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Energy.Transparent.class, RenderDuctFluids.instance);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

		ProxyClient.renderType = BlockRenderingRegistry.createRenderType("thermaldynamics");
		BlockRenderingRegistry.registerRenderer(ProxyClient.renderType, RenderDuct.instance);
	}

	/* EVENT HANDLERS */
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {

		TDTextures.registerIcons(event);

		for (int i = 0; i < TDDucts.ductList.size(); i++) {
			if (TDDucts.isValid(i)) {
				TDDucts.ductList.get(i).registerIcons(event.getMap());
			}
		}
		TDDucts.structureInvis.registerIcons(event.getMap());
	}

	@SubscribeEvent
	public void initializeIcons(TextureStitchEvent.Post event) {

		RenderDuct.initialize();
	}

	/* HELPERS */
	@Override
	public boolean addIModelRegister(IModelRegister register) {

		return modelRegisters.add(register);
	}

	private static List<IModelRegister> modelRegisters = new ArrayList<>();

}
