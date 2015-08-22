package cofh.thermaldynamics.render.item;

import cofh.core.render.RenderUtils;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import cofh.thermaldynamics.duct.attachments.cover.CoverRenderer;

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

public class RenderItemCover implements IItemRenderer {

	public static IItemRenderer instance = new RenderItemCover();

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
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			return;
		}
		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.air || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta)) {
			nbt.removeTag("Meta");
			nbt.removeTag("Block");
			if (nbt.hasNoTags()) {
				stack.setTagCompound(null);
			}
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

		RenderHelper.enableGUIStandardItemLighting();

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
				CoverRenderer.renderCover(CoverRenderer.renderBlocks, 0, 128, 0, side.ordinal(), block, meta, Cover.bounds[side.ordinal()], true, false, null);
			}
		}
		CCRenderState.draw();
		CCRenderState.useNormals = false;

		RenderHelper.setItemTextureSheet();
		RenderUtils.postItemRender();

		net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();

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
	}

}
