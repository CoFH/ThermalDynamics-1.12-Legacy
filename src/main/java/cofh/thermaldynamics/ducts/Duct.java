package cofh.thermaldynamics.ducts;

import cofh.thermaldynamics.render.TextureOverlay;
import cofh.thermaldynamics.render.TextureTransparent;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class Duct {

	public static final String REDSTONE_BLOCK = "minecraft:redstone_block";
	public static final String SIDE_DUCTS = "sideDucts";

	public static enum Type {
		ENERGY, FLUID, ITEM, ENTITY, STRUCTURAL
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
    public final boolean pathModifiable;
    public final byte fluidTransparency;
	public final String frameTexture;
	public final String frameFluidTexture;
	public final byte frameFluidTransparency;
	public final boolean opaque;
	public final int type;

	public Duct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
                String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency, boolean pathModifiable) {

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
        this.pathModifiable = pathModifiable;
        this.fluidTransparency = (byte) fluidTransparency;
		this.frameTexture = frameTexture;
		this.frameFluidTexture = frameFluidTexture;
		this.frameFluidTransparency = (byte) frameFluidTransparency;
	}

	public boolean isLargeTube() {

		return frameTexture != null && !SIDE_DUCTS.equals(frameTexture);
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
			if (SIDE_DUCTS.equals(frameTexture)) {
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


    public IIcon getBaseTexture(ItemStack itemStack){
        return iconBaseTexture;
    }
}
