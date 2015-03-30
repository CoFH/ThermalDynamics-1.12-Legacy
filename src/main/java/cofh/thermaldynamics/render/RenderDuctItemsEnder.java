package cofh.thermaldynamics.render;

import cofh.core.render.RenderUtils;
import cofh.core.render.ShaderHelper;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.render.CCModel;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermalfoundation.fluid.TFFluids;
import cofh.thermalfoundation.render.shader.ShaderStarfield;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;

public class RenderDuctItemsEnder extends RenderDuctItems {

	public static final TileEntitySpecialRenderer instance = new RenderDuctItemsEnder();

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
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float frame) {

		TileItemDuctEnder duct = (TileItemDuctEnder) tile;

		if (duct.powered) {
			CCRenderState.reset();
			RenderUtils.preWorldRender(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);

			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			GL11.glColor4f(1, 1, 1, 1);

			RenderDuct.instance.getDuctConnections(duct);
			int[] connections = RenderDuct.connections;

			drawEnderStarfield(x, y, z, connections, frame, duct.centerLine, duct.centerLineSub);

			CCRenderState.reset();

		} else {
			renderItemDuct(duct, x, y, z, frame);
		}
	}

	public static void drawEnderStarfield(double x, double y, double z, int[] connections, float frame, int alpha, int[] alphaSub) {

		if (ShaderHelper.useShaders() || ShaderStarfield.starfieldShader == 0) {
			CCRenderState.changeTexture(ShaderStarfield.starsTexture);
		} else {
			CCRenderState.changeTexture(RenderHelper.MC_BLOCK_SHEET);
		}

		CCModel[] models = RenderDuct.modelFluid[5];

		if (alpha == 0) {
			ShaderStarfield.alpha = 0;
			ShaderHelper.useShader(ShaderStarfield.starfieldShader, ShaderStarfield.callback);
			CCRenderState.startDrawing();
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connections[s]].renderDuct() && connections[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					models[s].render(x, y, z, RenderUtils.getIconTransformation(TFFluids.fluidEnder.getIcon()));
				}
			}
			models[6].render(x, y, z, RenderUtils.getIconTransformation(TFFluids.fluidEnder.getIcon()));
			CCRenderState.draw();
			ShaderHelper.releaseShader();
		} else {

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connections[s]].renderDuct() && connections[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					ShaderStarfield.alpha = getAlphaLevel(alphaSub[s], frame) / 255F;
					ShaderHelper.useShader(ShaderStarfield.starfieldShader, ShaderStarfield.callback);
					CCRenderState.startDrawing();
					models[s].render(x, y, z, RenderUtils.getIconTransformation(TFFluids.fluidEnder.getIcon()));
					CCRenderState.draw();
					ShaderHelper.releaseShader();
				}
			}
			ShaderStarfield.alpha = getAlphaLevel(alpha, frame) / 255F;
			ShaderHelper.useShader(ShaderStarfield.starfieldShader, ShaderStarfield.callback);
			CCRenderState.startDrawing();
			models[6].render(x, y, z, RenderUtils.getIconTransformation(TFFluids.fluidEnder.getIcon()));
			CCRenderState.draw();
			ShaderHelper.releaseShader();
		}
	}

}
