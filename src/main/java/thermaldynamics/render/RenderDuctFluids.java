package thermaldynamics.render;

import cofh.core.render.RenderUtils;
import cofh.repack.codechicken.lib.render.CCRenderState;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import thermaldynamics.ducts.fluid.TileFluidDuct;

public class RenderDuctFluids extends TileEntitySpecialRenderer {

	public static final RenderDuctFluids instance = new RenderDuctFluids();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float frame) {

		TileFluidDuct duct = (TileFluidDuct) tile;

		RenderUtils.preWorldRender(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);

		GL11.glPushMatrix();

		// something in the following prevents rendering from being messed up
		// todo: find out what at some point
		CCRenderState.reset();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		CCRenderState.reset();
		CCRenderState.useNormals = true;
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_LIGHTING);

		RenderDuct.instance.getDuctConnections(duct);
		RenderDuct.instance.renderFluid(duct.myRenderFluid, RenderDuct.connections, duct.getRenderFluidLevel(), x, y, z);
		CCRenderState.reset();
		GL11.glPopMatrix();

	}

}
