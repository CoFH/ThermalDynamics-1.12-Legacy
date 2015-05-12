package cofh.thermaldynamics.render;

import cofh.repack.codechicken.lib.render.TextureDataHolder;
import cofh.repack.codechicken.lib.render.TextureSpecial;
import cofh.repack.codechicken.lib.render.TextureUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class TextureOverlay {

	public static final String PATH_BASE = "thermaldynamics:textures/blocks/duct/base/";
    public static final String PATH_CONNECTION = "thermaldynamics:textures/blocks/duct/connection/";
    public static final String PATH_FRAME = "thermaldynamics:textures/blocks/duct/base/";

	public static ResourceLocation toLoc(String path, String name) {

		return new ResourceLocation(path + name + ".png");
	}

	public static TextureDataHolder incSize(TextureDataHolder tex, int newWidth) {

		int n = newWidth / tex.width;
		TextureDataHolder newTex = new TextureDataHolder(newWidth, tex.height * n);

		for (int i = 0; i < newTex.data.length; i++) {
			newTex.data[i] = 0x98769876;
		}
		for (int x = 0; x < tex.width; x++) {
			for (int y = 0; y < tex.height; y++) {
				int col = tex.data[x + y * tex.width];
				for (int dx = 0; dx < n; dx++) {
					for (int dy = 0; dy < n; dy++) {
						newTex.data[(x * n + dx) + (y * n + dy) * newTex.width] = col;
					}
				}
			}
		}
		return newTex;
	}

	public static IIcon generateBaseTexture(IIconRegister register, String base, String... textures) {

		TextureDataHolder image, newimage;
		image = TextureUtils.loadTexture(toLoc(PATH_BASE, base));

		StringBuilder builder = new StringBuilder("thermaldynamics:Duct_").append(base);

		for (String texture : textures) {
			if (texture == null) {
				continue;
			}
			builder.append('_').append(texture);
		}
		String name = builder.toString();

		TextureAtlasSprite entry = ((TextureMap) register).getTextureExtry(name);
		if (entry != null) {
			return entry;
		}
		for (String texture : textures) {
			if (texture == null) {
				continue;
			}
			newimage = TextureUtils.loadTexture(toLoc(PATH_BASE, texture));

			if (image.width != newimage.width) {
				if (image.width < newimage.width) {
					image = incSize(image, newimage.width);
				} else {
					newimage = incSize(newimage, image.width);
				}
			}
			if ("trans".equals(texture)) {
				for (int j = 0; j < newimage.data.length; j++) {
					if (((newimage.data[j] >> 24) & 0xFF) != 0) {
						image.data[j] = 0;
					}
				}
			} else {
				for (int j = 0; j < newimage.data.length; j++) {
					int colour = newimage.data[j];
					if (((colour >> 24) & 0xFF) != 0) {
						image.data[j] = colour;
					}
				}
			}
		}
		TextureSpecial texture = TextureUtils.getTextureSpecial(register, name);
		texture.addTexture(image);

		return texture;
	}

	public static IIcon generateConnectionTexture(IIconRegister register, String connection) {

		TextureDataHolder image;
		image = TextureUtils.loadTexture(toLoc(PATH_CONNECTION, connection));

		String name = "thermaldynamics:Conn_" + connection;

		TextureAtlasSprite entry = ((TextureMap) register).getTextureExtry(name);
		if (entry != null) {
			return entry;
		}
		TextureSpecial texture = TextureUtils.getTextureSpecial(register, name);
		texture.addTexture(image);

		return texture;
	}

	public static IIcon generateFrameTexture(IIconRegister register, String frame) {

		TextureDataHolder image;
		image = TextureUtils.loadTexture(toLoc(PATH_FRAME, frame + "_trans"));

		String name = "thermaldynamics:Frame_" + frame;

		TextureAtlasSprite entry = ((TextureMap) register).getTextureExtry(name);
		if (entry != null) {
			return entry;
		}
		TextureSpecial texture = TextureUtils.getTextureSpecial(register, name);
		texture.addTexture(image);

		return texture;
	}

	public static IIcon generateFrameBandTexture(IIconRegister register, String frame) {

		TextureDataHolder image;
		image = TextureUtils.loadTexture(toLoc(PATH_FRAME, frame + "_band"));

		String name = "thermaldynamics:Band_" + frame;

		TextureAtlasSprite entry = ((TextureMap) register).getTextureExtry(name);
		if (entry != null) {
			return entry;
		}
		TextureSpecial texture = TextureUtils.getTextureSpecial(register, name);
		texture.addTexture(image);

		return texture;
	}

}
