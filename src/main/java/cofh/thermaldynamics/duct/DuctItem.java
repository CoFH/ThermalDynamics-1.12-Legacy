package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.render.TextureOverlay;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

public class DuctItem extends Duct {

	public static final String PATHWEIGHT_NBT = "DenseType";

	public static final byte PATHWEIGHT_DENSE = 1;
	public static final byte PATHWEIGHT_VACUUM = 2;
	public IIcon iconBaseTextureVacuum;
	public IIcon iconBaseTextureDense;

	public DuctItem(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
			String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

		super(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, frameTexture,
				frameFluidTexture, frameFluidTransparency);
	}

	@Override
	public void registerIcons(IIconRegister ir) {

		super.registerIcons(ir);
		iconBaseTextureVacuum = TextureOverlay.generateBaseTexture(ir, baseTexture, opaque ? null : "trans", "vacuum");
		iconBaseTextureDense = TextureOverlay.generateBaseTexture(ir, baseTexture, opaque ? null : "trans", "dense");
	}

	@Override
	public IIcon getBaseTexture(ItemStack itemStack) {

		if (itemStack.stackTagCompound != null) {
			byte b = itemStack.stackTagCompound.getByte(PATHWEIGHT_NBT);
			if (b == PATHWEIGHT_DENSE) {
				return iconBaseTextureDense;
			} else if (b == PATHWEIGHT_VACUUM) {
				return iconBaseTextureVacuum;
			}
		}
		return super.getBaseTexture(itemStack);
	}

	public ItemStack getDenseItemStack() {

		ItemStack item = itemStack.copy();
		item.stackTagCompound = new NBTTagCompound();
		item.stackTagCompound.setByte(PATHWEIGHT_NBT, PATHWEIGHT_DENSE);
		return item;
	}

	public ItemStack getVacuumItemStack() {

		ItemStack item = itemStack.copy();
		item.stackTagCompound = new NBTTagCompound();
		item.stackTagCompound.setByte(PATHWEIGHT_NBT, PATHWEIGHT_VACUUM);
		return item;
	}

}
