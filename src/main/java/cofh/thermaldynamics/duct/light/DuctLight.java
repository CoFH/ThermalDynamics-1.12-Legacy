package cofh.thermaldynamics.duct.light;

import cofh.core.network.PacketHandler;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctFactory;
import cofh.thermaldynamics.render.TextureOverlay;
import cofh.thermaldynamics.render.TextureTransparent;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.client.renderer.texture.IIconRegister;

public class DuctLight extends Duct {

	static {
		PacketHandler.instance.registerPacket(PacketLight.class);
		GameRegistry.registerTileEntity(TileLightDuct.class, "thermaldynamics.LightDuct");
	}

	public DuctLight(int id, int type, String name, Type ductType, DuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture,
			int fluidTransparency) {

		super(id, false, 0, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, null, null, 0);
	}

	@Override
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
