package cofh.thermaldynamics.duct;

import codechicken.lib.texture.TextureUtils;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import cofh.thermaldynamics.render.TextureOverlay;
import cofh.thermaldynamics.render.TextureTransparent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class Duct implements IIconRegister, Comparable<Duct> {

//	public static final String REDSTONE_BLOCK = "minecraft:blocks/redstone_block";
public static final String REDSTONE_BLOCK = "thermaldynamics:blocks/duct/base/redstone_background";


	public static final String SIDE_DUCTS = "sideDucts";

	public enum Type {
		ENERGY, FLUID, ITEM, TRANSPORT, STRUCTURAL, CRAFTING
	}

	public ItemStack itemStack = null;

	public TextureAtlasSprite iconBaseTexture;
	public TextureAtlasSprite iconConnectionTexture;
	public TextureAtlasSprite iconFluidTexture;
	public TextureAtlasSprite iconFrameTexture;
	public TextureAtlasSprite iconFrameBandTexture;
	public TextureAtlasSprite iconFrameFluidTexture;

	public byte frameType = 0;

	public final int id;
	public final String unlocalizedName;
	public final int pathWeight;
	public final Type ductType;
	public final IDuctFactory factory;
	public final String baseTexture;
	public final String connectionTexture;
	public final String fluidTexture;
	public final byte fluidTransparency;
	public final String frameTexture;
	public final String frameFluidTexture;
	public final byte frameFluidTransparency;
	public final boolean opaque;
	public final int type;

	public EnumRarity rarity = EnumRarity.COMMON;

	public Duct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, IDuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

		this.id = id;
		this.pathWeight = pathWeight;
		this.ductType = ductType;
		this.opaque = opaque;
		this.type = type;
		this.unlocalizedName = name;
		this.factory = factory;
		this.baseTexture = baseTexture;
		this.connectionTexture = connectionTexture;
		this.fluidTexture = fluidTexture;
		this.fluidTransparency = (byte) fluidTransparency;
		this.frameTexture = frameTexture;
		this.frameFluidTexture = frameFluidTexture;
		this.frameFluidTransparency = (byte) frameFluidTransparency;
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			TextureUtils.addIconRegister(this);
		}
	}

	public Duct setRarity(int rarity) {

		this.rarity = EnumRarity.values()[rarity %= EnumRarity.values().length];
		return this;
	}

	public boolean isLargeTube() {

		return frameType == 2 || frameType == 4;
	}

	@Override
	public void registerIcons(TextureMap ir) {

		if (baseTexture != null) {
			iconBaseTexture = TextureOverlay.generateBaseTexture(ir, baseTexture, opaque ? null : "trans", null);
		}
		if (connectionTexture != null) {
			iconConnectionTexture = TextureOverlay.generateConnectionTexture(ir, connectionTexture);
		}
		if (fluidTexture != null) {
			iconFluidTexture = TextureTransparent.registerTransparentIcon(ir, fluidTexture, fluidTransparency);
		}
		if (frameTexture != null) {
			if (frameTexture.endsWith("_large")) {
				frameType = 3;
				iconFrameTexture = ir.registerSprite(new ResourceLocation("thermaldynamics:blocks/duct/base/" + frameTexture));
			} else if (SIDE_DUCTS.equals(frameTexture)) {
				frameType = 1;
			} else {
				iconFrameTexture = TextureOverlay.generateFrameTexture(ir, frameTexture);
				iconFrameBandTexture = TextureOverlay.generateFrameBandTexture(ir, frameTexture);
				frameType = 2;
			}
		}
		if (frameFluidTexture != null) {
			if (frameType == 0) {
				frameType = 2;
			}
			iconFrameFluidTexture = TextureTransparent.registerTransparentIcon(ir, frameFluidTexture, frameFluidTransparency);
		}
	}

	/* Comparator */
	public int compareTo(@Nonnull Duct other) {

		return Integer.compare(this.id, other.id);
	}

	public TextureAtlasSprite getBaseTexture(ItemStack itemStack) {

		return iconBaseTexture;
	}
}
