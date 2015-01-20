package thermaldynamics.core;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.debughelper.CommandServerDebug;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TileItemDuctEnder;
import thermaldynamics.render.ItemCoverRenderer;
import thermaldynamics.render.RenderDuct;
import thermaldynamics.render.RenderDuctFluids;
import thermaldynamics.render.RenderDuctItems;
import thermaldynamics.render.RenderDuctItemsEnder;

public class ProxyClient extends Proxy {

    @Override
    public void registerRenderInformation() {
        FMLCommonHandler.instance().bus().register(TickHandlerClient.INSTANCE);
        for (BlockDuct duct : ThermalDynamics.blockDuct) {
            MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(duct), RenderDuct.instance);
        }

        MinecraftForgeClient.registerItemRenderer(ThermalDynamics.itemFacade, ItemCoverRenderer.instance);

        ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuctEnder.class, RenderDuctItemsEnder.instance);

        ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.class, RenderDuctItems.instance);
        ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.class, RenderDuctFluids.instance);

        ClientCommandHandler.instance.registerCommand(new CommandServerDebug());
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
