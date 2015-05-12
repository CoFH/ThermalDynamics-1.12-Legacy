package cofh.thermaldynamics.duct.lamp;

import cofh.core.network.PacketHandler;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctFactory;
import cofh.thermaldynamics.render.TextureOverlay;
import cofh.thermaldynamics.render.TextureTransparent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;

public class DuctGlow extends Duct {
    static {
        PacketHandler.instance.registerPacket(PacketLamp.class);
        GameRegistry.registerTileEntity(TileGlowDuct.class, "thermaldynamics.GlowDuct");
    }

    public DuctGlow(int id, int type, String name, Type ductType, DuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency) {
        super(id, false, 0, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, null, null, 0);
    }

    public void registerIcons(IIconRegister ir) {

        iconBaseTexture = TextureOverlay.generateBaseTexture(ir, baseTexture);

        if (connectionTexture != null) {
            iconConnectionTexture = TextureOverlay.generateConnectionTexture(ir, connectionTexture);
        }
        if (fluidTexture != null) {
            iconFluidTexture = TextureTransparent.registerTransparentIcon(ir, fluidTexture, fluidTransparency);
        }
    }
}
