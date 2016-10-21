package cofh.thermaldynamics.render;

import codechicken.lib.texture.TextureCustomAnim;
import cofh.thermaldynamics.ThermalDynamics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

public class TextureTransparent extends TextureCustomAnim {

	byte transparency;
	ResourceLocation location;

	public static TextureAtlasSprite registerTransparentIcon(TextureMap textureMap, String name, byte transparency) {

		if (transparency == (byte) 255) {
			return textureMap.registerSprite(new ResourceLocation(name));
		}

		TextureMap map = textureMap;

		TextureAtlasSprite icon = map.getTextureExtry(transformedName(name, transparency));
		if (icon == null) {
			icon = new TextureTransparent(name, transparency);
			map.setTextureEntry(icon.getIconName(), icon);
		}
		return icon;
	}

	protected TextureTransparent(String icon, byte transparency) {

		super(transformedName(icon, transparency));
		this.transparency = transparency;

		String s1 = "minecraft";
		String s2 = icon;
		int i = icon.indexOf(58);

		if (i >= 0) {
			s2 = icon.substring(i + 1, icon.length());
			if (i > 1) {
				s1 = icon.substring(0, i);
			}
		}

		this.location = new ResourceLocation(s1, "textures/blocks/" + s2 + ".png");
	}

	private static String transformedName(String icon, byte transparency) {

		return icon + "_trans_" + transparency;
	}

	@Override
	public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {

		return true;
	}

	@Override
	public boolean load(IResourceManager p_110571_1_, ResourceLocation location) {

		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		try {
			IResource iresource = p_110571_1_.getResource(this.location);

			BufferedImage image = ImageIO.read(iresource.getInputStream());

			int data[] = new int[image.getWidth() * image.getHeight()];
			image.getRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());

			for (int i = 0; i < data.length; i++) {
				if ((data[i] & 0xFF000000) != 0) {
					data[i] = (data[i] & 0x00FFFFFF) | (transparency << 24);
				}
			}

			image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, image.getWidth());

			BufferedImage[] img = new BufferedImage[1 + settings.mipmapLevels];
			img[0] = image;

			AnimationMetadataSection animationmetadatasection = iresource.getMetadata("animation");
			loadSprite(img, animationmetadatasection);
		} catch (IOException ioexception1) {
			ThermalDynamics.log.error("Using missing texture, unable to load " + this.location, ioexception1);
			return true;
		}

		return false;
	}
}
