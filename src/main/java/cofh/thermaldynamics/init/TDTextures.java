package cofh.thermaldynamics.init;

import cofh.core.init.CoreProps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public class TDTextures {

	private TDTextures() {

	}

	public static void registerIcons(TextureStitchEvent.Pre event) {

		TextureMap map = event.getMap();

	}

	// Bouncer to make the class readable.
	private static TextureAtlasSprite register(TextureMap map, String sprite) {

		return map.registerSprite(new ResourceLocation(sprite));
	}

	// Bouncer for registering ColorBlind textures.
	private static TextureAtlasSprite registerCB(TextureMap map, String sprite) {

		if (CoreProps.enableColorBlindTextures) {
			sprite += CB_POSTFIX;
		}
		return register(map, sprite);
	}

	private static String CB_POSTFIX = "_cb";

	private static final String BLOCKS_ = "thermaldynamics:blocks/";

	/* REFERENCES */

}
