package cofh.thermaldynamics.render;

import cofh.thermaldynamics.duct.fluid.TileFluidDuct;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class RenderDuctFluids extends TileEntitySpecialRenderer {

	public static final RenderDuctFluids instance = new RenderDuctFluids();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float frame) {

		TileFluidDuct duct = (TileFluidDuct) tile;
		// RenderUtils.preWorldRender(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);

		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		RenderDuct.instance.getDuctConnections(duct);
		RenderDuct.instance.renderFluid(duct.myRenderFluid, RenderDuct.connections, duct.getRenderFluidLevel(), x, y, z);
		GL11.glEnable(GL11.GL_LIGHTING);

		GL11.glPopMatrix();

	}

}
