package cofh.thermaldynamics.proxy;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.block.BlockRenderingRegistry;
import cofh.core.render.IModelRegister;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import cofh.thermaldynamics.duct.entity.RenderTransport;
import cofh.thermaldynamics.duct.entity.SoundWoosh;
import cofh.thermaldynamics.duct.tiles.TileDuctOmni;
import cofh.thermaldynamics.duct.tiles.TileFluidDuct;
import cofh.thermaldynamics.duct.tiles.TileItemDuct;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.render.*;
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

		ModelRegistryHelper.registerItemRenderer(TDItems.itemCover, RenderItemCover.instance);

		RenderingRegistry.registerEntityRenderingHandler(EntityTransport.class, RenderTransport::new);
		GameRegistry.register(SoundWoosh.WOOSH);
		for (IModelRegister register : modelRegisters) {
			register.registerModels();
		}
	}

	@Override
	public void initialize(FMLInitializationEvent event) {


		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Basic.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Fast.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Flux.Transparent.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.Warp.Transparent.class, RenderDuctItemsEnder.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctOmni.Transparent.class, RenderDuctOmni.instance);

		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Fragile.Transparent.class, RenderDuctFluids.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Fragile.Super.class, RenderDuctFluids.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Fragile.Hardened.class, RenderDuctFluids.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.Fragile.Flux.class, RenderDuctFluids.instance);
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

		TDTextures.registerIcons(event);

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
