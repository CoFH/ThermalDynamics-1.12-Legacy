package thermaldynamics.render;

import cofh.core.render.RenderUtils;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.repack.codechicken.lib.vec.Vector3;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.debughelper.DebugHelper;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TravelingItem;

public class RenderDuctItems extends TileEntitySpecialRenderer {
    public static final int ITEMS_TO_RENDER_PER_DUCT = 16;

    static RenderItem travelingItemRender;
    static EntityItem travelingEntityItem = new EntityItem(null);
    static float travelingItemSpin = 0.25F;

    public static final RenderDuctItems instance = new RenderDuctItems();

    static final float ITEM_RENDER_SCALE = 0.6F;

    static {
        travelingItemRender = new RenderItem() {
            @Override
            public boolean shouldBob() {
                return false;
            }


            @Override
            public boolean shouldSpreadItems() {
                return false;
            }
        };

        travelingItemRender.setRenderManager(RenderManager.instance);
        travelingEntityItem.hoverStart = 0;
    }


    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float frame) {
        TileItemDuct duct = (TileItemDuct) tile;
        renderItemDuct(duct, x, y, z, frame);

    }

    public void renderItemDuct(TileItemDuct duct, double x, double y, double z, float frame) {
        if (!(duct.myItems.isEmpty() && duct.itemsToAdd.isEmpty())) {
            RenderUtils.preWorldRender(duct.getWorldObj(), duct.xCoord, duct.yCoord, duct.zCoord);
            CCRenderState.useNormals = true;

            renderTravelingItems(Iterators.concat(duct.itemsToAdd.iterator(), duct.myItems.iterator()), duct, duct.getWorldObj(), x, y, z, frame);
            CCRenderState.useNormals = false;
            CCRenderState.reset();
        }

        if (duct.centerLine > 0) {
            GL11.glPushMatrix();

            Translation trans = (new Vector3(x, y, z)).translation();

            CCRenderState.reset();
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            CCRenderState.useNormals = true;
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glDisable(GL11.GL_LIGHTING);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            RenderHelper.bindTexture(RenderHelper.MC_BLOCK_SHEET);
            RenderUtils.preWorldRender(duct.getWorldObj(), duct.xCoord, duct.yCoord, duct.zCoord);
            CCRenderState.setColour(-1);
            CCRenderState.setBrightness(15728880);


            RenderDuct.instance.getDuctConnections(duct);
            CCRenderState.startDrawing();
            for (int s = 0; s < 6; s++) {
                if (BlockDuct.ConnectionTypes.values()[RenderDuct.connections[s]].renderDuct() && duct.centerLineSub[s] != 0) {
                    CCRenderState.alphaOverride = getAlphaLevel(duct.centerLineSub[s], frame);
                    RenderDuct.modelLine[s].render(trans, RenderUtils.getIconTransformation(RenderDuct.textureCenterLine));
                } else {
                    CCRenderState.alphaOverride = getAlphaLevel(duct.centerLine, frame);
                    RenderDuct.modelLineCenter.render(s * 4, s * 4 + 4, trans, RenderUtils.getIconTransformation(RenderDuct.textureCenterLine));
                }
            }
            CCRenderState.draw();
            CCRenderState.alphaOverride = -1;
            CCRenderState.reset();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);

            CCRenderState.useNormals = false;
            GL11.glPopMatrix();

        }
    }

    public static int getAlphaLevel(int centerLine, float frame) {
        return (int) Math.min(255, 1.7 * ((centerLine - frame) * 255.0) / (TileItemDuct.maxCenterLine));
    }


    public void renderTravelingItems(Iterator<TravelingItem> items, TileItemDuct duct, World world, double x, double y, double z, float frame) {

        if (!items.hasNext()) {
            return;
        }

        GL11.glPushMatrix();

        travelingItemSpin += .001;
        travelingItemSpin %= 180;
        travelingEntityItem.hoverStart = travelingItemSpin;

        TravelingItem renderItem;

        for (int i = 0; items.hasNext() && i < ITEMS_TO_RENDER_PER_DUCT; i++) {
            renderItem = items.next();
            if (renderItem == null || renderItem.stack == null) {
                continue;
            }
            GL11.glPushMatrix();

            GL11.glTranslated(x + renderItem.x, y + renderItem.y, z + renderItem.z);

            float[] vec;
            for (int k = 0; k < renderItem.step - 1; k++) {
                vec = TravelingItem.getVec(renderItem.progress + k, renderItem, duct);
                GL11.glTranslated(vec[0], vec[1], vec[2]);
            }

            vec = TravelingItem.getVec(renderItem.progress + renderItem.step, renderItem, duct);
            GL11.glTranslated(vec[0] * frame, vec[1] * frame, vec[2] * frame);


            GL11.glScalef(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);

            travelingEntityItem.setEntityItemStack(renderItem.stack);
            travelingItemRender.doRender(travelingEntityItem, 0, -0.1F, 0, 0, 0);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }
}
