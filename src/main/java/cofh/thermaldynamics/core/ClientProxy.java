package cofh.thermaldynamics.core;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.block.BlockRenderingRegistry;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.debughelper.CommandServerDebug;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.render.RenderDuct;
import cofh.thermaldynamics.render.RenderDuctFluids;
import cofh.thermaldynamics.render.RenderDuctItems;
import cofh.thermaldynamics.render.RenderDuctItemsEnder;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.TextureStitchEvent;

public class ClientProxy extends CommonProxy {

    public static EnumBlockRenderType renderType;

    @Override
	public void preInit() {

		FMLCommonHandler.instance().bus().register(TickHandlerClient.instance);

		for (BlockDuct duct : ThermalDynamics.blockDuct) {
            StateMap.Builder stateMapBuilder = new StateMap.Builder();
            stateMapBuilder.ignore(BlockDuct.META);
            ModelLoader.setCustomStateMapper(duct, stateMapBuilder.build());
            ModelRegistryHelper.registerItemRenderer(Item.getItemFromBlock(duct), RenderDuct.instance);
		}
        //MinecraftForgeClient.registerItemRenderer(ThermalDynamics.itemCover, RenderItemCover.instance);

		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuctEnder.class, RenderDuctItemsEnder.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.class, RenderDuctFluids.instance);

		ClientCommandHandler.instance.registerCommand(new CommandServerDebug());

		//RenderingRegistry.registerEntityRenderingHandler(EntityTransport.class, new RenderTransport());
	}

    @Override
    public void init() {
    }

    @Override
    public void postInit() {
        ClientProxy.renderType = BlockRenderingRegistry.createRenderType("TD");
        BlockRenderingRegistry.registerRenderer(ClientProxy.renderType, RenderDuct.instance);
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
