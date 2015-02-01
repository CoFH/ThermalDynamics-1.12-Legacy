package thermaldynamics.ducts;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import thermaldynamics.render.TextureOverlay;
import thermaldynamics.render.TextureTransparent;

public class Duct {

	public static final String REDSTONE_BLOCK = "minecraft:redstone_block";
	public static final String SIDE_DUCTS = "sideDucts";

	public static enum Type {
		ITEM, FLUID, ENERGY, ENTITY, STRUCTURAL
	}

	public ItemStack itemStack = null;

	public IIcon iconBaseTexture;
	public IIcon iconConnectionTexture;
	public IIcon iconFluidTexture;
	public IIcon iconOverDuctTexture;
	public IIcon iconOverDuctInternalTexture;

	public byte overDuctType = 0;

	public final int id;
	public final String unlocalizedName;
	public final int pathWeight;
	public final Type ductType;
	public final DuctFactory factory;
	public final String baseTexture;
	public final String connectionTexture;
	public final String fluidTexture;
	public final byte fluidTransparency;
	public final String overDuct;
	public final String overDuct2;
	public final byte overDuct2Trans;
	public final boolean opaque;
	public final int type;

	public Duct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
			String connectionTexture, String fluidTexture, int fluidTransparency, String overDuct, String overDuct2, int overDuct2Trans) {

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
		this.overDuct = overDuct;
		this.overDuct2 = overDuct2;
		this.overDuct2Trans = (byte) overDuct2Trans;
	}

	public boolean isLargeTube() {

		return overDuct != null && !SIDE_DUCTS.equals(overDuct);
	}

	public void registerIcons(IIconRegister ir) {

		// iconBaseTexture = ir.registerIcon(baseTexture);

		iconBaseTexture = TextureOverlay.generateTexture(ir, baseTexture, opaque ? null : "trans", pathWeight == 1000 ? "dense"
				: pathWeight == -1000 ? "vacuum" : null);

		if (connectionTexture != null) {
			iconConnectionTexture = ir.registerIcon(connectionTexture);
		}
		if (fluidTexture != null) {
			iconFluidTexture = TextureTransparent.registerTransparentIcon(ir, fluidTexture, fluidTransparency);
		}
		if (overDuct != null) {
			if (SIDE_DUCTS.equals(overDuct)) {
				overDuctType = 1;
			} else {
				iconOverDuctTexture = ir.registerIcon(overDuct);
				overDuctType = 2;
			}
		}
		if (overDuct2 != null) {
			if (overDuctType == 0)
				overDuctType = 2;
			iconOverDuctInternalTexture = TextureTransparent.registerTransparentIcon(ir, overDuct2, overDuct2Trans);
		}
	}

	/* Comparator */
	public int compareTo(Duct other) {

		return this.id > other.id ? 1 : this.id < other.id ? -1 : 0;
	}

}
