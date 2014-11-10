package thermaldynamics.core;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.render.RenderDuct;
import thermaldynamics.render.RenderDuctFluids;
import thermaldynamics.render.RenderDuctItems;

public class ProxyClient extends Proxy {

    @Override
    public void registerRenderInformation() {
        FMLCommonHandler.instance().bus().register(TickHandlerClient.INSTANCE);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ThermalDynamics.blockDuct[0]), RenderDuct.instance);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ThermalDynamics.blockDuct[1]), RenderDuct.instance);
        ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.class, RenderDuctItems.instance);
        ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.class, RenderDuctFluids.instance);
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
