package cofh.thermaldynamics.render.item;

import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.render.CCRenderState;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import cofh.thermaldynamics.duct.attachments.cover.CoverRenderer;
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
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

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

		//GlStateManager.pushMatrix();
		//double offset = -0.5;
		//if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
		//	offset = 0;
		//} else if (type == ItemRenderType.ENTITY) {
		//	GlStateManager.scale(0.5, 0.5, 0.5);
		//}
		//RenderUtils.preItemRender();

		//RenderHelper.enableGUIStandardItemLighting();

		//CCRenderState.startDrawing();
		//GlStateManager.translate(offset, offset - 128, offset);

		//SingleBlockAccess.instance.block = block;
		//SingleBlockAccess.instance.meta = meta;
		//CoverRenderer.renderBlocks.blockAccess = SingleBlockAccess.instance;
		//Tessellator.instance.setNormal(0.0F, 1.0F, 0.0F);

		EnumFacing side =  EnumFacing.NORTH; //type == ItemRenderType.EQUIPPED_FIRST_PERSON ? ForgeDirection.WEST : ForgeDirection.SOUTH;
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        CoverRenderer.renderItemCover(ccrs, side.ordinal(), block.getStateFromMeta(meta), Cover.bounds[side.ordinal()]);
        ccrs.draw();


		//GlStateManager.translate(-side.offsetX * 0.5, -side.offsetY * 0.5, -side.offsetZ * 0.5);
		//for (int pass = 0; pass < 2; pass++) {
		//	if (block.canRenderInPass(pass)) {
		//		CoverRenderer.renderCover(CoverRenderer.renderBlocks, 0, 128, 0, side.ordinal(), block, meta, Cover.bounds[side.ordinal()], true, false, null);
		//	}
		//}
		//CCRenderState.draw();
		//CCRenderState.useNormals = false;

		//RenderHelper.setItemTextureSheet();
		//RenderUtils.postItemRender();

		//net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();

		//GlStateManager.popMatrix();
	}

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        return MapWrapper.handlePerspective(this, TransformUtils.DEFAULT_BLOCK.getTransforms(), cameraTransformType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return new ArrayList<BakedQuad>();
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

	/*public static class SingleBlockAccess implements IBlockAccess {

		public static SingleBlockAccess instance = new SingleBlockAccess();

		public Block block;
		public int meta;

		public boolean isLoc(int x, int y, int z) {

			return x == 0 && y == 128 && z == 0;
		}

		public SingleBlockAccess() {

			super();
		}

		public SingleBlockAccess(Block block, int meta) {

			this.block = block;
			this.meta = meta;
		}

		@Override
		public Block getBlock(int x, int y, int z) {

			return isLoc(x, y, z) ? block : Blocks.air;
		}

		@Override
		public TileEntity getTileEntity(int x, int y, int z) {

			return null;
		}

		@Override
		public int getLightBrightnessForSkyBlocks(int x, int y, int z, int light) {

			return 15728880;
		}

		@Override
		public int getBlockMetadata(int x, int y, int z) {

			return isLoc(x, y, z) ? meta : 0;
		}

		@Override
		public int isBlockProvidingPowerTo(int x, int y, int z, int from) {

			return 0;
		}

		@Override
		public boolean isAirBlock(int x, int y, int z) {

			return isLoc(x, y, z);
		}

		@Override
		public BiomeGenBase getBiomeGenForCoords(int x, int z) {

			return BiomeGenBase.plains;
		}

		@Override
		public int getHeight() {

			return 140;
		}

		@Override
		public boolean extendedLevelsInChunkCache() {

			return false;
		}

		@Override
		public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {

			return isLoc(x, y, z) && block.isSideSolid(this, x, y, z, side);
		}
	}*/

}
