package cofh.thermaldynamics.duct.attachments.cover;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.render.CCQuad;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.consumer.CCRSConsumer;
import codechicken.lib.util.ResourceUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.RenderHelper;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender.CoverTransformer;
import cofh.thermaldynamics.init.TDTextures;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CoverRenderer {

	final static int[] sideOffsets = { 1, 1, 2, 2, 0, 0 };

	final static float[] sideSoftBounds = { 0, 1, 0, 1, 0, 1 };

	private final static float FACADE_RENDER_OFFSET = (float) RenderHelper.RENDER_OFFSET * 2;
	private final static float FACADE_RENDER_OFFSET2 = 1 - FACADE_RENDER_OFFSET;

	private static final ThreadLocal<VertexLighterFlat> lighterFlat = ThreadLocal.withInitial(() -> new VertexLighterFlat(Minecraft.getMinecraft().getBlockColors()));
	private static final ThreadLocal<VertexLighterFlat> lighterSmooth = ThreadLocal.withInitial(() -> new VertexLighterSmoothAo(Minecraft.getMinecraft().getBlockColors()));

	//Stop inventory churn of models being sliced.
	public static final Cache<String, List<CCQuad>> itemQuadCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	public static void init() {
		ResourceUtils.registerReloadListener(resourceManager -> itemQuadCache.invalidateAll());
	}

	private static VertexLighterFlat setupLighter(CCRenderState ccrs, IBlockState state, IBlockAccess access, BlockPos pos, IBakedModel model) {

		boolean renderAO = Minecraft.isAmbientOcclusionEnabled() && state.getLightValue(access, pos) == 0 && model.isAmbientOcclusion();
		VertexLighterFlat lighter = renderAO ? lighterSmooth.get() : lighterFlat.get();

		CCRSConsumer consumer = new CCRSConsumer(ccrs);
		lighter.setParent(consumer);
		consumer.setOffset(pos);
		return lighter;
	}

	public static boolean renderBlockQuads(VertexLighterFlat lighter, IBlockAccess access, IBlockState state, List<CCQuad> quads, BlockPos pos) {

		if (!quads.isEmpty()) {
			lighter.setWorld(access);
			lighter.setState(state);
			lighter.setBlockPos(pos);
			lighter.updateBlockInfo();
			for (CCQuad quad : quads) {
				quad.pipe(lighter);
			}
			return true;
		}
		return false;
	}

	public static List<CCQuad> applyItemTint(List<CCQuad> quads, ItemStack stack) {

		List<CCQuad> retQuads = new LinkedList<>();
		for (CCQuad quad : quads) {
			int colour = -1;

			if (quad.hasTint()) {
				colour = Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, quad.tintIndex);

				if (EntityRenderer.anaglyphEnable) {
					colour = TextureUtil.anaglyphColor(colour);
				}
				colour = colour | 0xFF000000;
			}
			CCQuad copyQuad = quad.copy();

			Colour c = new ColourARGB(colour);
			for (Colour qC : copyQuad.colours) {
				qC.multiply(c);
			}
			retQuads.add(copyQuad);
		}

		return retQuads;
	}

	public static boolean renderBlockCover(CCRenderState ccrs, IBlockAccess world, BlockPos pos, int side, IBlockState state, Cuboid6 bounds, CoverTransformer coverTransformer) {
		EnumFacing face = EnumFacing.VALUES[side];

		IBlockAccess coverAccess = CoverBlockAccess.getInstance(world, pos, face, state);
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		try {
			state = state.getActualState(coverAccess, pos);
		} catch (Exception ignored) {
		}

		IBakedModel model = dispatcher.getModelForState(state);

		try {
			state = state.getBlock().getExtendedState(state, coverAccess, pos);
		} catch (Exception ignored) {
		}

		List<BakedQuad> bakedQuads = new LinkedList<>();
		long posRand = net.minecraft.util.math.MathHelper.getPositionRandom(pos);
		bakedQuads.addAll(model.getQuads(state, null, posRand));

		for (EnumFacing face2 : EnumFacing.VALUES) {
			bakedQuads.addAll(model.getQuads(state, face2, posRand));
		}

		List<CCQuad> quads = CCQuad.fromArray(bakedQuads);

		if (coverTransformer != null) {
			quads = CoverHoleRender.holify(quads, side, coverTransformer);
		}

		quads = sliceQuads(quads, side, bounds);

		if (!quads.isEmpty()) {
			VertexLighterFlat lighter = setupLighter(ccrs, state, coverAccess, pos, model);
			return renderBlockQuads(lighter, coverAccess, state, quads, pos);
		}
		return false;
	}

	public static void renderItemCover(CCRenderState ccrs, int side, IBlockState state, Cuboid6 bounds) {

		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		IBakedModel model = renderItem.getItemModelWithOverrides(stack, null, null);

		String cacheKey = state.getBlock().getRegistryName() + "|" + state.getBlock().getMetaFromState(state);

		List<CCQuad> renderQuads = itemQuadCache.getIfPresent(cacheKey);
		if (renderQuads == null) {
			List<BakedQuad> quads = new ArrayList<>();

			quads.addAll(model.getQuads(null, null, 0));
			for (EnumFacing face : EnumFacing.VALUES) {
				quads.addAll(model.getQuads(null, face, 0));
			}

			renderQuads = applyItemTint(sliceQuads(CCQuad.fromArray(quads), side, bounds), stack);
			itemQuadCache.put(cacheKey, renderQuads);
		}

		CCRSConsumer consumer = new CCRSConsumer(ccrs);
		consumer.setOffset(Vector3.center.copy().subtract(bounds.center()));

		for (CCQuad quad : renderQuads) {
			quad.pipe(consumer);
		}

	}

	public static List<CCQuad> sliceQuads(List<CCQuad> quads, int side, Cuboid6 bounds) {

		boolean flag, flag2;

		double quadPos[][] = new double[4][3];
		boolean flat[] = new boolean[3];

		TextureAtlasSprite icon = TDTextures.COVER_SIDE;

		int verticesPerFace = 4;

		List<CCQuad> finalQuads = new LinkedList<>();

		for (CCQuad quad : quads) {

			flag = flag2 = false;
			for (int i = 0; i < 3; i++) {
				flat[i] = true;
			}

			Vector3 first = quad.vertices[0].vec;

			for (int v = 0; v < 4; v++) {
				quadPos[v] = quad.vertices[v].vec.toArrayD();

				flag = flag || quadPos[v][sideOffsets[side]] != sideSoftBounds[side];
				flag2 = flag2 || quadPos[v][sideOffsets[side]] != (1 - sideSoftBounds[side]);

				if (v != 0) {
					flat[0] = flat[0] && quad.vertices[v].vec.x == first.x;
					flat[1] = flat[1] && quad.vertices[v].vec.y == first.y;
					flat[2] = flat[2] && quad.vertices[v].vec.z == first.z;
				}
			}

			int s = -1;

			if (flag && flag2) {
				for (int vi = 0; vi < 3; vi++) {
					if (flat[vi]) {
						if (vi != sideOffsets[side]) {
							s = vi;
							break;
						} else {
							flag = false;
						}
					}
				}
			}

			for (int k2 = 0; k2 < verticesPerFace; k2++) {
				boolean flag3 = quadPos[k2][sideOffsets[side]] != sideSoftBounds[side];
				for (int j = 0; j < 3; j++) {
					if (j == sideOffsets[side]) {
						quadPos[k2][j] = clampF(quadPos[k2][j], bounds, j);
					} else {
						if (flag && flag2 && flag3) {
							// TODO: only clamp here when covers[] != null && has a cover on the side this vertex is on
							quadPos[k2][j] = MathHelper.clamp(quadPos[k2][j], FACADE_RENDER_OFFSET, FACADE_RENDER_OFFSET2);
						}
					}
				}

				if (s != -1) {
					double u, v;

					if (s == 0) {
						u = quadPos[k2][1];
						v = quadPos[k2][2];
					} else if (s == 1) {
						u = quadPos[k2][0];
						v = quadPos[k2][2];
					} else {
						u = quadPos[k2][0];
						v = quadPos[k2][1];
					}

					u = MathHelper.clamp(u, 0, 1) * 16;
					v = MathHelper.clamp(v, 0, 1) * 16;

					u = icon.getInterpolatedU(u);
					v = icon.getInterpolatedV(v);
					quad.vertices[k2].uv.set(u, v);
					quad.tintIndex = -1;
				}
				quad.vertices[k2].vec.set(quadPos[k2]);
			}
			finalQuads.add(quad);
		}

		return finalQuads;
	}

	private final static int[][] sides = { { 4, 5 }, { 0, 1 }, { 2, 3 } };

	private static double clampF(double x, Cuboid6 b, int j) {

		double l = b.getSide(sides[j][0]);
		double u = b.getSide(sides[j][1]);

		if (x < l) {
			return l - (l - x) * 0.001953125f;
		} else if (x > u) {
			return u + (x - u) * 0.001953125f;
		} else {
			return x;
		}
	}
}

