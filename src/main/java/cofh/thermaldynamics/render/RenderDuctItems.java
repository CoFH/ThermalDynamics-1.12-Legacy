package cofh.thermaldynamics.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import cofh.lib.util.helpers.RenderHelper;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import com.google.common.collect.Iterators;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class RenderDuctItems extends TileEntitySpecialRenderer<TileGrid> {

	public static final int ITEMS_TO_RENDER_PER_DUCT = 16;

	static RenderEntityItem travelingItemRender;
	static EntityItem travelingEntityItem = new EntityItem(null);
	static float travelingItemSpin = 0.25F;

	public static final RenderDuctItems instance = new RenderDuctItems();

	static final float ITEM_RENDER_SCALE = 0.7F;

	static {
		Minecraft minecraft = Minecraft.getMinecraft();
		travelingItemRender = new RenderEntityItem(minecraft.getRenderManager(), minecraft.getRenderItem()) {

			@Override
			public boolean shouldBob() {

				return false;
			}

			@Override
			public boolean shouldSpreadItems() {

				return false;
			}
		};
		travelingEntityItem.hoverStart = 0;

		MinecraftForge.EVENT_BUS.register(instance);
	}

	public static float spinStep = 0.026175f;

	@SubscribeEvent
	public void clientTick(TickEvent.ClientTickEvent event) {

		if (Minecraft.getMinecraft().isGamePaused()) {
			return;
		}
		travelingItemSpin += spinStep;
		travelingItemSpin %= 180;
	}

	@Override
	public void renderTileEntityAt(TileGrid tile, double x, double y, double z, float frame, int destroyStage) {

		DuctUnitItem duct = tile.getDuct(DuctToken.ITEMS);

		if (duct == null) {
			return;
		}

		CCRenderState ccrs = CCRenderState.instance();
		if (!(duct.myItems.isEmpty() && duct.itemsToAdd.isEmpty())) {
			renderTravelingItems(Iterators.concat(duct.itemsToAdd.iterator(), duct.myItems.iterator()), duct, tile.getWorld(), x, y, z, frame);
		}

		if (duct.centerLine > 0) {
			GlStateManager.pushMatrix();

			Translation trans = (new Vector3(x, y, z)).translation();

			ccrs.reset();
			GlStateManager.enableAlpha();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.disableLighting();

			GlStateManager.enableBlend();
			GlStateManager.enableCull();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			RenderHelper.bindTexture(RenderHelper.MC_BLOCK_SHEET);
			ccrs.preRenderWorld(tile.getWorld(), tile.getPos());
			ccrs.colour = -1;
			ccrs.brightness = 15728880;

			int[] connections = RenderDuct.instance.getDuctConnections(tile);
			ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionType.values()[connections[s]].renderDuct() && duct.centerLineSub[s] != 0) {
					ccrs.alphaOverride = getAlphaLevel(duct.centerLineSub[s], frame);
					RenderDuct.modelLine[s].render(ccrs, trans, RenderUtils.getIconTransformation(RenderDuct.textureCenterLine));
				} else {
					ccrs.alphaOverride = getAlphaLevel(duct.centerLine, frame);
					RenderDuct.modelLineCenter.render(ccrs, s * 4, s * 4 + 4, trans, RenderUtils.getIconTransformation(RenderDuct.textureCenterLine));
				}
			}
			ccrs.draw();
			ccrs.alphaOverride = -1;
			ccrs.reset();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();

			GlStateManager.popMatrix();

		}
	}

	public static int getAlphaLevel(int centerLine, float frame) {

		return (int) Math.min(80, 0.7 * ((centerLine - frame) * 255.0) / (DuctUnitItem.MAX_CENTER_LINE));
	}

	public void renderTravelingItems(Iterator<TravelingItem> items, DuctUnitItem duct, World world, double x, double y, double z, float frame) {

		if (!items.hasNext()) {
			return;
		}

		TravelingItem renderItem;

		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
			for (int i = 0; items.hasNext() && i < ITEMS_TO_RENDER_PER_DUCT; i++) {
				renderItem = items.next();
				if (renderItem == null || renderItem.stack.isEmpty()) {
					continue;
				}
				double v = (renderItem.progress + frame * renderItem.step) / (duct.getDuctLength());

				v -= 0.5;

				if (renderItem.shouldDie && v > 0) {
					continue;
				}

				GlStateManager.pushMatrix();
				{
					if (v < 0) {
						translateItem(renderItem.oldDirection, v);
					} else {
						translateItem(renderItem.direction, v);
					}
					GlStateManager.scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);

					travelingEntityItem.hoverStart = travelingItemSpin + frame * spinStep;
					travelingEntityItem.setEntityItemStack(ItemHandlerHelper.copyStackWithSize(renderItem.stack, 1));
					travelingItemRender.doRender(travelingEntityItem, 0, -0.3F, 0, 0, 0);
				}
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
	}

	private void translateItem(byte direction, double v) {

		EnumFacing face = EnumFacing.VALUES[direction];
		GlStateManager.translate(face.getFrontOffsetX() * v, face.getFrontOffsetY() * v, face.getFrontOffsetZ() * v);
	}

}
