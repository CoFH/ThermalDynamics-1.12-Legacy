package cofh.thermaldynamics.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.texture.TextureUtils;
import cofh.core.render.ShaderHelper;
import cofh.lib.util.helpers.RenderHelper;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.item.DuctUnitItemEnder;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileItemDuct;
import cofh.thermalfoundation.init.TFFluids;
import cofh.thermalfoundation.render.shader.ShaderStarfield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

public class RenderDuctItemsEnder extends RenderDuctItems {

	public static final TileEntitySpecialRenderer<TileItemDuct> instance = new RenderDuctItemsEnder();

	// TEMA: this is the shader callback where the uniforms are set for this particular shader.
	// it's called each frame when the shader is bound. Probably the most expensive part of the whole thing.
	// you might be able to even call this once per frame instead of once per draw, pointing call at the program instead of passing this in useShader.
	private final ShaderHelper.ShaderCallback shaderCallback = new ShaderHelper.ShaderCallback() {

		@Override
		public void call(int shader, boolean newFrame) {

			if (!newFrame) {
				return;
			}

			Minecraft mc = Minecraft.getMinecraft();
			float fov = mc.gameSettings.fovSetting * 2f;

			ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(shader, "xpos"), mc.thePlayer.rotationYaw / fov);

			ARBShaderObjects.glUniform1fARB(ARBShaderObjects.glGetUniformLocationARB(shader, "zpos"), -mc.thePlayer.rotationPitch / fov);
		}
	};

	@Override
	public void renderTileEntityAt(TileItemDuct tile, double x, double y, double z, float frame, int destroyStage) {

		DuctUnitItemEnder duct = (DuctUnitItemEnder) tile.getDuct(DuctToken.ITEMS);

		if (duct != null && duct.powered) {
			CCRenderState ccrs = CCRenderState.instance();
			ccrs.reset();
			ccrs.preRenderWorld(tile.getWorld(), tile.getPos());

			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			GlStateManager.color(1, 1, 1, 1);

			int[] connections = RenderDuct.instance.getDuctConnections(tile);

			drawEnderStarfield(ccrs, x, y, z, connections, frame, duct.centerLine, duct.centerLineSub);

			ccrs.reset();
		} else {
			super.renderTileEntityAt(tile, x, y, z, frame, destroyStage);
		}
	}

	public static void drawEnderStarfield(CCRenderState ccrs, double x, double y, double z, int[] connections, float frame, int alpha, int[] alphaSub) {

		if (ShaderHelper.useShaders() || ShaderStarfield.starfieldShader == 0) {
			TextureUtils.changeTexture(ShaderStarfield.starsTexture);
		} else {
			TextureUtils.changeTexture(RenderHelper.MC_BLOCK_SHEET);
		}

		CCModel[] models = RenderDuct.modelFluid[5];

		if (alpha == 0) {
			ShaderStarfield.alpha = 0;
			ShaderHelper.useShader(ShaderStarfield.starfieldShader, ShaderStarfield.callback);
			ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionType.values()[connections[s]].renderDuct() && connections[s] != BlockDuct.ConnectionType.STRUCTURE.ordinal()) {
					models[s].render(ccrs, x, y, z, RenderUtils.getIconTransformation(TextureUtils.getBlockTexture(TFFluids.fluidEnder.getStill())));
				}
			}
			models[6].render(ccrs, x, y, z, RenderUtils.getIconTransformation(TextureUtils.getBlockTexture(TFFluids.fluidEnder.getStill())));
			ccrs.draw();
			ShaderHelper.releaseShader();
		} else {

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionType.values()[connections[s]].renderDuct() && connections[s] != BlockDuct.ConnectionType.STRUCTURE.ordinal()) {
					ShaderStarfield.alpha = getAlphaLevel(alphaSub[s], frame) / 255F;
					ShaderHelper.useShader(ShaderStarfield.starfieldShader, ShaderStarfield.callback);
					ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					models[s].render(ccrs, x, y, z, RenderUtils.getIconTransformation(TextureUtils.getBlockTexture(TFFluids.fluidEnder.getStill())));
					ccrs.draw();
					ShaderHelper.releaseShader();
				}
			}
			ShaderStarfield.alpha = getAlphaLevel(alpha, frame) / 255F;
			ShaderHelper.useShader(ShaderStarfield.starfieldShader, ShaderStarfield.callback);
			ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			models[6].render(ccrs, x, y, z, RenderUtils.getIconTransformation(TextureUtils.getBlockTexture(TFFluids.fluidEnder.getStill())));
			ccrs.draw();
			ShaderHelper.releaseShader();
		}
	}

}
