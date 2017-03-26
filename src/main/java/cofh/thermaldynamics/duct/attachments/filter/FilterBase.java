package cofh.thermaldynamics.duct.attachments.filter;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.thermaldynamics.duct.NeighborType;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class FilterBase extends ConnectionBase {

	public FilterBase(TileGrid tile, byte side) {

		super(tile, side);
	}

	public FilterBase(TileGrid tile, byte side, int type) {

		super(tile, side, type);

	}

	@Override
	public String getName() {

		return "item.thermaldynamics.filter." + type + ".name";
	}

	@Override
	public NeighborType getNeighborType() {

		return isValidInput ? NeighborType.OUTPUT : NeighborType.DUCT_ATTACHMENT;
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(TDItems.itemFilter, 1, type);
	}

	@Override
	@SideOnly (Side.CLIENT)
	public boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccRenderState) {

		if (layer != BlockRenderLayer.SOLID) {
			return false;
		}

		Translation trans = Vector3.fromTileCenter(tile).translation();
		RenderDuct.modelConnection[stuffed ? 2 : 1][side].render(ccRenderState, trans, new IconTransformation(TDTextures.FILTER_BASE[type]));
		return true;
	}

	/* IPortableData */
	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		super.writePortableData(player, tag);
		tag.setString("DisplayType", "item.thermaldynamics.filter.0.name");
	}

}
