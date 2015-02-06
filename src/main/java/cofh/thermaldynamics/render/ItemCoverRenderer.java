package cofh.thermaldynamics.render;

import cofh.core.render.RenderUtils;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.thermaldynamics.ducts.attachments.facades.Cover;
import cofh.thermaldynamics.ducts.attachments.facades.CoverHelper;
import cofh.thermaldynamics.ducts.attachments.facades.CoverRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

public class ItemCoverRenderer implements IItemRenderer {

	public static IItemRenderer instance = new ItemCoverRenderer();

	/* IItemRenderer */
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {

		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {

		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8))
			return;

		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.air || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta)) {
			nbt.removeTag("Meta");
			nbt.removeTag("Block");
			if (nbt.hasNoTags())
				stack.setTagCompound(null);
		}

		GL11.glPushMatrix();
		double offset = -0.5;
		if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			offset = 0;
		} else if (type == ItemRenderType.ENTITY) {
			GL11.glScaled(0.5, 0.5, 0.5);
		}
		RenderHelper.setBlockTextureSheet();
		RenderUtils.preItemRender();

		CCRenderState.startDrawing();
		GL11.glTranslated(offset, offset - 128, offset);

		SingleBlockAccess.instance.block = block;
		SingleBlockAccess.instance.meta = meta;
		CoverRenderer.renderBlocks.blockAccess = SingleBlockAccess.instance;
		Tessellator.instance.setNormal(0.0F, 1.0F, 0.0F);

		ForgeDirection side = type == ItemRenderType.EQUIPPED_FIRST_PERSON ? ForgeDirection.WEST : ForgeDirection.SOUTH;
		GL11.glTranslated(-side.offsetX * 0.5, -side.offsetY * 0.5, -side.offsetZ * 0.5);
		for (int pass = 0; pass < 2; pass++) {
			if (block.canRenderInPass(pass)) {
				CoverRenderer.renderCover(CoverRenderer.renderBlocks, 0, 128, 0, side.ordinal(), block, meta, Cover.bounds[side.ordinal()], true, false);
			}
		}
		CCRenderState.draw();

		CCRenderState.useNormals = false;

		RenderHelper.setItemTextureSheet();
		RenderUtils.postItemRender();

		GL11.glPopMatrix();
	}

	public static class SingleBlockAccess implements IBlockAccess {

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
		public Block getBlock(int p_147439_1_, int p_147439_2_, int p_147439_3_) {

			return isLoc(p_147439_1_, p_147439_2_, p_147439_3_) ? block : Blocks.air;
		}

		@Override
		public TileEntity getTileEntity(int p_147438_1_, int p_147438_2_, int p_147438_3_) {

			return null;
		}

		@Override
		public int getLightBrightnessForSkyBlocks(int p_72802_1_, int p_72802_2_, int p_72802_3_, int p_72802_4_) {

			return 15728880;
		}

		@Override
		public int getBlockMetadata(int p_72805_1_, int p_72805_2_, int p_72805_3_) {

			return isLoc(p_72805_1_, p_72805_2_, p_72805_3_) ? meta : 0;
		}

		@Override
		public int isBlockProvidingPowerTo(int p_72879_1_, int p_72879_2_, int p_72879_3_, int p_72879_4_) {

			return 0;
		}

		@Override
		public boolean isAirBlock(int p_147437_1_, int p_147437_2_, int p_147437_3_) {

			return isLoc(p_147437_1_, p_147437_2_, p_147437_3_);
		}

		@Override
		public BiomeGenBase getBiomeGenForCoords(int p_72807_1_, int p_72807_2_) {

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
	}

}
