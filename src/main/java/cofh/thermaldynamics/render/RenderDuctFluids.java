package cofh.thermaldynamics.render;

import codechicken.lib.render.CCRenderState;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluid;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileFluidDuct;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;

public class RenderDuctFluids extends TileEntitySpecialRenderer<TileFluidDuct> {

	public static final RenderDuctFluids instance = new RenderDuctFluids();

	@Override
	public void renderTileEntityAt(TileFluidDuct duct, double x, double y, double z, float frame, int destroyStage) {

		DuctUnitFluid fluid = Validate.notNull(duct.getDuct(DuctToken.FLUID));

		renderFluids(fluid, x, y, z);

	}

	public void renderFluids(DuctUnitFluid fluid, double x, double y, double z) {

		int[] connections = fluid.getRenderFluidConnections();
		CCRenderState ccrs = CCRenderState.instance();
		ccrs.preRenderWorld(fluid.parent.getWorld(), fluid.parent.getPos());

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.disableLighting();

		RenderDuct.instance.renderFluid(ccrs, fluid.myRenderFluid, connections, fluid.getRenderFluidLevel(), x, y, z);

		GlStateManager.enableLighting();
		GlStateManager.disableBlend();

		GlStateManager.popMatrix();
	}

}
