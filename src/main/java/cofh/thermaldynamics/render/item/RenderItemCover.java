package cofh.thermaldynamics.render.item;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import cofh.thermaldynamics.duct.attachments.cover.CoverRenderer;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

import static codechicken.lib.util.TransformUtils.get;
import static codechicken.lib.util.TransformUtils.leftify;
import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.*;

public class RenderItemCover implements IItemRenderer, IPerspectiveAwareModel {

	public static IItemRenderer instance = new RenderItemCover();

	@Override
	public void renderItem(ItemStack stack) {

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			return;
		}
		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.AIR || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta)) {
			nbt.removeTag("Meta");
			nbt.removeTag("Block");
			if (nbt.hasNoTags()) {
				stack.setTagCompound(null);
			}
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
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
		ImmutableMap.Builder<TransformType, TRSRTransformation> builder = ImmutableMap.builder();
		builder.putAll(TransformUtils.DEFAULT_BLOCK.getTransforms());
		//builder.put(GUI, get(-4, 2, 0, 30, 225, 0, 0.625f));
		return MapWrapper.handlePerspective(this, builder.build(), cameraTransformType);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		return new ArrayList<>();
	}

	@Override
	public boolean isAmbientOcclusion() {

		return false;
	}

	@Override
	public boolean isGui3d() {

		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {

		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {

		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {

		return ItemOverrideList.NONE;
	}
}
