package thermaldynamics.render;

import cofh.core.render.RenderUtils;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.render.CCRenderState;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import thermaldynamics.util.ShaderHelper;
import thermalexpansion.block.ender.TileTesseract;
import thermalexpansion.render.RenderTesseract;

public class RenderTesseractTest extends TileEntitySpecialRenderer {
    public static TileEntitySpecialRenderer instance = new RenderTesseractTest();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {
        if (!((TileTesseract) tile).isActive || !ShaderHelper.useShaders())
            return;

        GL11.glPushMatrix();

        CCRenderState.changeTexture(RenderDuctItemsEnder.starsTexture);

        GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
        GL11.glScaled(1 + RenderHelper.RENDER_OFFSET, 1 + RenderHelper.RENDER_OFFSET, 1 + RenderHelper.RENDER_OFFSET);
        RenderDuctItemsEnder.alpha = 0;

        ShaderHelper.useShader(ShaderHelper.testShader, RenderDuctItemsEnder.callback);
        CCRenderState.startDrawing();
        RenderTesseract.instance.renderCenter(0, (TileTesseract) tile, -0.5, -0.5, -0.5);
        CCRenderState.draw();
        ShaderHelper.releaseShader();

        GL11.glPopMatrix();
    }
}
