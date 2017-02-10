package cofh.thermaldynamics.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import cofh.lib.render.RenderHelper;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.energy.TileEnergyDuctGlowing;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class RenderDuctEnergyGlowing extends TileEntitySpecialRenderer<TileEnergyDuctGlowing> {
	public static final RenderDuctEnergyGlowing instance = new RenderDuctEnergyGlowing();


	@Override
	public void renderTileEntityAt(TileEnergyDuctGlowing duct, double x, double y, double z, float frame, int destroyStage) {
		boolean hasFlux = false;
		float[] flux = new float[6];
		float maxFlux = 0;
		for (int i = 0; i < 6; i++) {
			float v = flux[i] = Math.max( duct.fluxIn[i] , duct.fluxOut[i]);
			if (v != 0) {
				hasFlux = true;
				maxFlux = Math.max(maxFlux, Math.abs(v));
			}
		}

		if (!hasFlux) {
			return;
		}

		GlStateManager.pushMatrix();
		CCRenderState ccrs = CCRenderState.instance();

		Translation trans = (new Vector3(x, y, z)).translation();

		ccrs.reset();
		GlStateManager.enableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
		//CCRenderState.useNormals = true;
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableLighting();

		GlStateManager.enableBlend();
		GlStateManager.enableCull();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderHelper.bindTexture(RenderHelper.MC_BLOCK_SHEET);
		ccrs.preRenderWorld(duct.getWorld(), duct.getPos());

		ccrs.brightness = 15728880;
		ccrs.alphaOverride = 255;

		RenderDuct.instance.getDuctConnections(duct);
		ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
		for (int s = 0; s < 6; s++) {
			float v = flux[s];
			if (v < 0) {
				ccrs.baseColour = 0xff0000ff;
			} else if (v > 0) {
				ccrs.baseColour = 0xff00ff00;
			} else {
				ccrs.baseColour = -1;
			}
			if (BlockDuct.ConnectionTypes.values()[RenderDuct.connections[s]].renderDuct() && v != 0) {
				RenderDuct.modelLine[s].render(ccrs, trans, RenderUtils.getIconTransformation(RenderDuct.textureCenterLine));
			} else {
				RenderDuct.modelLineCenter.render(ccrs, s * 4, s * 4 + 4, trans, RenderUtils.getIconTransformation(RenderDuct.textureCenterLine));
			}
		}
		ccrs.draw();
		ccrs.baseColour = -1;
		ccrs.alphaOverride = -1;
		ccrs.reset();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();

		//CCRenderState.useNormals = false;
		GlStateManager.popMatrix();
	}

}
