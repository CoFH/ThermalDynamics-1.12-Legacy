package cofh.thermaldynamics.duct.attachments.filter;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.core.render.RenderUtils;
import codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class FilterBase extends ConnectionBase {

	public FilterBase(TileTDBase tile, byte side) {

		super(tile, side);
	}

	public FilterBase(TileTDBase tile, byte side, int type) {

		super(tile, side, type);

	}

	@Override
	public String getName() {

		return "item.thermaldynamics.filter." + type + ".name";
	}

	@Override
	public TileTDBase.NeighborTypes getNeighborType() {

		return isValidInput ? TileTDBase.NeighborTypes.OUTPUT : TileTDBase.NeighborTypes.DUCT_ATTACHMENT;
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(ThermalDynamics.itemFilter, 1, type);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean render(BlockRenderLayer layer, CCRenderState ccRenderState) {
		if (layer != BlockRenderLayer.SOLID) {
			return false;
		}

		Translation trans = Vector3.fromTileCenter(tile).translation();
		RenderDuct.modelConnection[stuffed ? 2 : 1][side].render(ccRenderState, trans, new IconTransformation(RenderDuct.filterTexture[type]));
		return true;
	}

	/* IPortableData */
	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		super.writePortableData(player, tag);
		tag.setString("DisplayType", "item.thermaldynamics.filter.0.name");
	}

}
