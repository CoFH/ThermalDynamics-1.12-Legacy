package cofh.thermaldynamics.render;

import codechicken.lib.render.CCRenderState;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderDuctFluids extends TileEntitySpecialRenderer<TileFluidDuct> {

	public static final RenderDuctFluids instance = new RenderDuctFluids();

	@Override
	public void renderTileEntityAt(TileFluidDuct duct, double x, double y, double z, float frame, int destroyStage) {

		CCRenderState ccrs = CCRenderState.instance();
		ccrs.preRenderWorld(duct.getWorld(), duct.getPos());

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		int[] connections = RenderDuct.instance.getDuctConnections(duct);
		RenderDuct.instance.renderFluid(ccrs, duct.myRenderFluid, connections, duct.getRenderFluidLevel(), x, y, z);
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();

	}

}
