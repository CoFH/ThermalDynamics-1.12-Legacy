package cofh.thermaldynamics.proxy;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.block.BlockRenderingRegistry;
import cofh.core.render.IModelRegister;
import cofh.thermaldynamics.duct.attachments.cover.CoverRenderer;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import cofh.thermaldynamics.duct.entity.RenderTransport;
import cofh.thermaldynamics.duct.tiles.TileDuctFluid;
import cofh.thermaldynamics.duct.tiles.TileDuctItem;
import cofh.thermaldynamics.duct.tiles.TileDuctOmni;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.render.*;
import cofh.thermaldynamics.render.item.RenderItemCover;
import cofh.thermaldynamics.util.CoverBlacklistCommand;
import cofh.thermaldynamics.util.TickHandlerClient;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;

public class ProxyClient extends Proxy {

	public static EnumBlockRenderType renderType;

	/* INIT */
	@Override
	public void preInit(FMLPreInitializationEvent event) {

		super.preInit(event);

		MinecraftForge.EVENT_BUS.register(EventHandlerClient.INSTANCE);
		MinecraftForge.EVENT_BUS.register(TickHandlerClient.INSTANCE);
		ClientCommandHandler.instance.registerCommand(new CoverBlacklistCommand());

		ModelRegistryHelper.registerItemRenderer(TDItems.itemCover, RenderItemCover.INSTANCE);
		RenderingRegistry.registerEntityRenderingHandler(EntityTransport.class, RenderTransport::new);

		for (IModelRegister register : modelRegisters) {
			register.registerModels();
		}
	}

	@Override
	public void initialize(FMLInitializationEvent event) {

		super.initialize(event);

		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Basic.Transparent.class, RenderDuctItems.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Fast.Transparent.class, RenderDuctItems.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Energy.Transparent.class, RenderDuctItems.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.EnergyFast.Transparent.class, RenderDuctItems.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Warp.Transparent.class, RenderDuctItemsEnder.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctOmni.Transparent.class, RenderDuctOmni.INSTANCE);

		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Basic.Transparent.class, RenderDuctFluids.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Super.Transparent.class, RenderDuctFluids.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Hardened.Transparent.class, RenderDuctFluids.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Energy.Transparent.class, RenderDuctFluids.INSTANCE);
		CoverRenderer.init();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

		super.postInit(event);

		ProxyClient.renderType = BlockRenderingRegistry.createRenderType("thermaldynamics");
		BlockRenderingRegistry.registerRenderer(ProxyClient.renderType, RenderDuct.INSTANCE);
	}

	/* HELPERS */
	@Override
	public boolean addIModelRegister(IModelRegister register) {

		return modelRegisters.add(register);
	}

	private static List<IModelRegister> modelRegisters = new ArrayList<>();

}
