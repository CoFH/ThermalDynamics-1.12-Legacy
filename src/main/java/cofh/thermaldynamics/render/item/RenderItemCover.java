package cofh.thermaldynamics.render.item;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import cofh.thermaldynamics.duct.attachments.cover.CoverRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.model.IModelState;
import org.lwjgl.opengl.GL11;

public class RenderItemCover implements IItemRenderer {

	public static IItemRenderer instance = new RenderItemCover();

	@Override
	public void renderItem(ItemStack stack, TransformType transformType) {

		boolean invalid = false;

		int meta = 0;
		Block block = Blocks.AIR;

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			invalid = true;
		} else {
			meta = nbt.getByte("Meta");
			block = Block.getBlockFromName(nbt.getString("Block"));

			if (block == Blocks.AIR || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta)) {
				nbt.removeTag("Meta");
				nbt.removeTag("Block");
				if (nbt.hasNoTags()) {
					stack.setTagCompound(null);
				}
				invalid = true;
			}
		}
		if (invalid) {
			block = Blocks.BARRIER;
			meta = 0;
		}
		EnumFacing side = EnumFacing.NORTH;
		CCRenderState ccrs = CCRenderState.instance();
		ccrs.reset();
		ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		Cuboid6 bounds = Cover.bounds[side.ordinal()];
		CoverRenderer.renderItemCover(ccrs, side.ordinal(), block.getStateFromMeta(meta), bounds);
		ccrs.draw();

	}

	@Override
	public IModelState getTransforms() {

		return TransformUtils.DEFAULT_BLOCK;
	}

	@Override
	public boolean isAmbientOcclusion() {

		return false;
	}

	@Override
	public boolean isGui3d() {

		return false;
	}

}
