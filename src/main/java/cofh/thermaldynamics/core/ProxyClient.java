package cofh.thermaldynamics.core;

import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.debughelper.CommandServerDebug;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import cofh.thermaldynamics.duct.entity.RenderTransport;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.render.RenderDuct;
import cofh.thermaldynamics.render.RenderDuctFluids;
import cofh.thermaldynamics.render.RenderDuctItems;
import cofh.thermaldynamics.render.RenderDuctItemsEnder;
import cofh.thermaldynamics.render.item.RenderItemCover;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.item.Item;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;

public class ProxyClient extends Proxy {

	@Override
	public void registerRenderInformation() {

		FMLCommonHandler.instance().bus().register(TickHandlerClient.INSTANCE);

		for (BlockDuct duct : ThermalDynamics.blockDuct) {
			MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(duct), RenderDuct.instance);
		}
		MinecraftForgeClient.registerItemRenderer(ThermalDynamics.itemCover, RenderItemCover.instance);

		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuctEnder.class, RenderDuctItemsEnder.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.class, RenderDuctFluids.instance);

		ClientCommandHandler.instance.registerCommand(new CommandServerDebug());

		RenderingRegistry.registerEntityRenderingHandler(EntityTransport.class, new RenderTransport());
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void initializeIcons(TextureStitchEvent.Post event) {

		RenderDuct.initialize();
	}

}
