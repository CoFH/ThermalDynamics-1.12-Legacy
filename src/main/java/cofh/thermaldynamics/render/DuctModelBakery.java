package cofh.thermaldynamics.render;

import codechicken.lib.model.bakedmodels.ModelProperties.PerspectiveProperties;
import codechicken.lib.model.bakery.generation.IItemBakery;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.BakingVertexBuffer;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import cofh.core.util.helpers.RenderHelper;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class DuctModelBakery implements IItemBakery {

	public static final DuctModelBakery INSTANCE = new DuctModelBakery();

	@Override
	public List<BakedQuad> bakeItemQuads(EnumFacing face, ItemStack stack) {

		List<BakedQuad> quads = new ArrayList<>();
		if (face == null) {
			CCRenderState ccrs = CCRenderState.instance();
			BakingVertexBuffer buffer = BakingVertexBuffer.create();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
			ccrs.reset();
			ccrs.bind(buffer);

			Block blockFromItem = Block.getBlockFromItem(stack.getItem());

			Duct ductType = TDDucts.getDuct(((BlockDuct) blockFromItem).offset + stack.getItemDamage());

			RenderDuct.INSTANCE.renderBase(ccrs, true, ductType, RenderDuct.INV_CONNECTIONS, Translation.CENTER, ductType.getBaseTexture(stack));
			RenderDuct.INSTANCE.renderWorldExtra(ccrs, true, ductType, RenderDuct.INV_CONNECTIONS, Vector3.center.copy().subtract(0, RenderHelper.RENDER_OFFSET, 0).translation());

			buffer.finishDrawing();
			quads.addAll(buffer.bake());
		}
		return quads;
	}

	@Override
	public PerspectiveProperties getModelProperties(ItemStack stack) {

		return new PerspectiveProperties(TransformUtils.DEFAULT_BLOCK, true, false);
	}
}
