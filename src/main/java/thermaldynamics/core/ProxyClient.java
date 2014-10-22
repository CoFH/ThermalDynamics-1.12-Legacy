package thermaldynamics.core;

import cofh.core.render.ItemRenderRegistry;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.TextureStitchEvent;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.render.RenderDuct;

public class ProxyClient extends Proxy {

    @Override
    public void registerRenderInformation() {
        ItemRenderRegistry.addItemRenderer(BlockDuct.blockDuct, RenderDuct.instance);
        ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.class, RenderDuct.instance);
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
