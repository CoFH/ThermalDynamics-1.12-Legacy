package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.render.TextureOverlay;
import cofh.thermaldynamics.render.TextureTransparent;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class Duct {

	public static final String REDSTONE_BLOCK = "minecraft:redstone_block";
	public static final String SIDE_DUCTS = "sideDucts";

	public static enum Type {
		ENERGY, FLUID, ITEM, ENTITY, STRUCTURAL, CRAFTING
	}

	public ItemStack itemStack = null;

	public IIcon iconBaseTexture;
	public IIcon iconConnectionTexture;
	public IIcon iconFluidTexture;
	public IIcon iconFrameTexture;
	public IIcon iconFrameBandTexture;
	public IIcon iconFrameFluidTexture;

	public byte frameType = 0;

	public final int id;
	public final String unlocalizedName;
	public final int pathWeight;
	public final Type ductType;
	public final DuctFactory factory;
	public final String baseTexture;
	public final String connectionTexture;
	public final String fluidTexture;
	public final byte fluidTransparency;
	public final String frameTexture;
	public final String frameFluidTexture;
	public final byte frameFluidTransparency;
	public final boolean opaque;
	public final int type;

	public EnumRarity rarity = EnumRarity.common;

	public Duct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
			String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

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
	}

	public Duct setRarity(int rarity) {

		this.rarity = EnumRarity.values()[rarity %= EnumRarity.values().length];
		return this;
	}

	public boolean isLargeTube() {

		return frameType == 2;
	}

	public void registerIcons(IIconRegister ir) {

		iconBaseTexture = TextureOverlay.generateBaseTexture(ir, baseTexture, opaque ? null : "trans", null);

		if (connectionTexture != null) {
			iconConnectionTexture = TextureOverlay.generateConnectionTexture(ir, connectionTexture);
		}
		if (fluidTexture != null) {
			iconFluidTexture = TextureTransparent.registerTransparentIcon(ir, fluidTexture, fluidTransparency);
		}
		if (frameTexture != null) {
			if (frameTexture.endsWith("_large")) {
				frameType = 3;
				iconFrameTexture = ir.registerIcon("thermaldynamics:duct/base/" + frameTexture);
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
	public int compareTo(Duct other) {

		return this.id > other.id ? 1 : this.id < other.id ? -1 : 0;
	}

	public IIcon getBaseTexture(ItemStack itemStack) {

		return iconBaseTexture;
	}
}
