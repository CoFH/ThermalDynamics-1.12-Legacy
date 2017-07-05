package cofh.thermaldynamics.duct.light;

import cofh.core.network.PacketHandler;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.IDuctFactory;
import cofh.thermaldynamics.render.TextureOverlay;
import cofh.thermaldynamics.render.TextureTransparent;
import net.minecraft.client.renderer.texture.TextureMap;

public class DuctLight extends Duct {

	static {
		PacketHandler.INSTANCE.registerPacket(PacketLight.class);
	}

	public DuctLight(int id, int type, String name, Type ductType, IDuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency) {

		super(id, false, 0, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, null, null, 0);
	}

	@Override
	public void registerIcons(TextureMap textureMap) {

		iconBaseTexture = TextureOverlay.generateBaseTexture(textureMap, baseTexture);

		if (connectionTexture != null) {
			iconConnectionTexture = TextureOverlay.generateConnectionTexture(textureMap, connectionTexture);
		}
		if (fluidTexture != null) {
			iconFluidTexture = TextureTransparent.registerTransparentIcon(textureMap, fluidTexture, fluidTransparency);
		}
	}

}
