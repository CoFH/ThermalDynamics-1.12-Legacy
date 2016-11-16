package cofh.thermaldynamics.duct.attachments.cover;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourARGB;
import codechicken.lib.model.bakery.CCQuad;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cofh.lib.render.RenderHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.render.RenderDuct;
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
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CoverRenderer {

    public static int VERTEX_SIZE = 8;

    public static final float size = 1 / 512F;

    final static int[] sideOffsets = { 1, 1, 2, 2, 0, 0 };
    final static float[] sideBound1 = { 0, 1 - size, 0, 1 - size, 0, 1 - size };
    final static float[] sideBound2 = { size, 1, size, 1, size, 1 };

    final static float[] sideSoftBounds = { 0, 1, 0, 1, 0, 1 };

    private final static float FACADE_RENDER_OFFSET = ((float) RenderHelper.RENDER_OFFSET) * 2;
    private final static float FACADE_RENDER_OFFSET2 = 1 - FACADE_RENDER_OFFSET;

    private static final ThreadLocal<VertexLighterFlat> lighterFlat = new ThreadLocal<VertexLighterFlat>(){
        @Override
        protected VertexLighterFlat initialValue() {
            return new VertexLighterFlat(Minecraft.getMinecraft().getBlockColors());
        }
    };
    private static final ThreadLocal<VertexLighterFlat> lighterSmooth = new ThreadLocal<VertexLighterFlat>(){
        @Override
        protected VertexLighterFlat initialValue() {
            return new VertexLighterSmoothAo(Minecraft.getMinecraft().getBlockColors());
        }
    };

    private static VertexLighterFlat setupLighter(CCRenderState ccrs, IBlockState state, IBlockAccess access, BlockPos pos,  IBakedModel model) {
        boolean renderAO = Minecraft.isAmbientOcclusionEnabled() && state.getLightValue(access, pos) == 0 && model.isAmbientOcclusion();
        VertexLighterFlat lighter = renderAO ? lighterSmooth.get() : lighterFlat.get();

        VertexBufferConsumer consumer = new VertexBufferConsumer(ccrs.getBuffer());
        lighter.setParent(consumer);
        consumer.setOffset(pos);
        return lighter;
    }

    public static boolean renderBlockQuads(VertexLighterFlat lighter, IBlockAccess access, IBlockState state, List<CCQuad> quads, BlockPos pos) {
        if (!quads.isEmpty()) {
            lighter.setWorld(access);
            lighter.setState(state);
            lighter.setBlockPos(pos);
            for (CCQuad quad : quads) {
                lighter.updateBlockInfo();
                quad.pipe(lighter);
            }
            return true;
        }
        return false;
    }

    public static boolean renderItemQuads(IVertexConsumer consumer, List<CCQuad> quads, ItemStack stack) {
        for (CCQuad quad : quads) {
            int colour = -1;


            if (quad.hasTint()) {
                colour = Minecraft.getMinecraft().getItemColors().getColorFromItemstack(stack, quad.tintIndex);

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

            copyQuad.pipe(consumer);

        }

        return false;
    }

    public static boolean renderBlockCover(CCRenderState ccrs, IBlockAccess world, BlockPos pos, int side, IBlockState state, Cuboid6 bounds, CoverHoleRender.ITransformer[] hollowCover) {
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

        List<BakedQuad> bakedQuads = new LinkedList<BakedQuad>();
        long posRand = net.minecraft.util.math.MathHelper.getPositionRandom(pos);
        bakedQuads.addAll(model.getQuads(state, null, posRand));

        for (EnumFacing face2 : EnumFacing.VALUES) {
            bakedQuads.addAll(model.getQuads(state, face2, posRand));
        }

        List<CCQuad> quads = CCQuad.fromArray(bakedQuads);

        if (hollowCover != null) {
            quads = CoverHoleRender.holify(quads, side, hollowCover);
        }

        quads = sliceQuads(quads, side, bounds);

        if (!quads.isEmpty()){
            VertexLighterFlat lighter = setupLighter(ccrs, state, coverAccess, pos, model);
            return renderBlockQuads(lighter, coverAccess, state, quads, pos);
        }

        return false;
    }

    public static boolean renderItemCover(CCRenderState ccrs, BlockPos pos, int side, IBlockState state, Cuboid6 bounds) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        IBakedModel model = renderItem.getItemModelWithOverrides(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), null, null);
        List<BakedQuad> quads = new ArrayList<BakedQuad>();
        quads.addAll(model.getQuads(null, null, 0));
        for (EnumFacing face : EnumFacing.VALUES) {
            quads.addAll(model.getQuads(null, face, 0));
        }

        List<CCQuad> slicedQuads = sliceQuads(CCQuad.fromArray(quads), side, bounds);

        VertexBufferConsumer consumer = new VertexBufferConsumer(ccrs.getBuffer());

        return renderItemQuads(consumer, slicedQuads, new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
    }

    public static List<CCQuad> sliceQuads(List<CCQuad> quads, int side, Cuboid6 bounds) {

        boolean flag, flag2;

        float quadPos[][] = new float[4][3];
        float vecPos[] = new float[3];
        boolean flat[] = new boolean[3];

        TextureAtlasSprite icon = RenderDuct.coverBase;

        final int vertexSize = VERTEX_SIZE;
        final int verticiesPerFace = 4;
        final int incrementAmt = vertexSize * verticiesPerFace;

        List<CCQuad> finalQuads = new LinkedList<CCQuad>();

        for (CCQuad quad : quads) {

            flag = flag2 = false;
            for (int i = 0; i < 3; i++) {
                flat[i] = true;
            }

            for (int v = 0; v < 4; v++) {
                Vector3 posVect = quad.vertices[v].vec.copy();
                quadPos[v][0] = (float) posVect.x;
                quadPos[v][1] = (float) posVect.y;
                quadPos[v][2] = (float) posVect.z;

                flag = flag || quadPos[v][sideOffsets[side]] != sideSoftBounds[side];
                flag2 = flag2 || quadPos[v][sideOffsets[side]] != (1 - sideSoftBounds[side]);

                if (v == 0) {
                    System.arraycopy(quadPos[v], 0, vecPos, 0, 3);
                } else {
                    for (int vi = 0; vi < 3; vi++) {
                        flat[vi] = flat[vi] && quadPos[v][vi] == vecPos[vi];
                    }
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


            CCQuad finalQuad = quad.copy();
            for (int k2 = 0; k2 < verticiesPerFace; k2++) {
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
                    float u, v;

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
                    finalQuad.vertices[k2].uv.set(u, v);
                }
                finalQuad.vertices[k2].vec.set(quadPos[k2]);

                //int norm = -64 << 8;
                //This should be ok.. Hopefully..
                //finalQuad.normals[k2].set((norm & 255) / 127.0F, ((norm << 8) & 255) / 127.0F, ((norm << 16) & 255) / 127.0F);

                int oldColour = quad.colours[k2].rgba();
                int colour = oldColour & 0xFFFFFF00 | (((oldColour & 0x000000FF) >>> 1) & 0x000000FF);
                finalQuad.colours[k2].set(colour);
            }
            finalQuads.add(finalQuad);
        }
        return finalQuads;
    }


	/*public static boolean renderCover(RenderBlocks renderBlocks, int x, int y, int z, int side, Block block, int meta, Cuboid6 bounds, boolean addNormals,
            boolean addTrans, CoverHoleRender.ITransformer[] hollowCover) {

		return renderCover(renderBlocks, x, y, z, side, block, meta, bounds, addNormals, addTrans, hollowCover, null);
	}

	public static boolean renderCover(RenderBlocks renderBlocks, int x, int y, int z, int side, Block block, int meta, Cuboid6 bounds, boolean addNormals,
			boolean addTrans, CoverHoleRender.ITransformer[] hollowCover, Cover[] covers) {

		facadeRenderBlocks.blockAccess = CoverBlockAccess.getInstance(renderBlocks.blockAccess, x, y, z, side, block, meta);

		Tessellator tess = Tessellator.instance;
		int rawBufferIndex = tess.rawBufferIndex;

		boolean rendered = facadeRenderBlocks.renderBlockByRenderType(block, x, y, z);

		if (hollowCover != null) {
			CoverHoleRender.holify(rawBufferIndex, x, y, z, side, hollowCover);
		}

		int rawBufferIndex2 = tess.rawBufferIndex;

		if (rawBufferIndex != rawBufferIndex2) {
			int[] rb = tess.rawBuffer;

			boolean flag, flag2;

			float dx = (float) tess.xOffset;
			float dy = (float) tess.yOffset;
			float dz = (float) tess.zOffset;

			float quad[][] = new float[4][3];
			float vec[] = new float[3];
			boolean flat[] = new boolean[3];

			int intNormal = 0;

			IIcon icon = RenderDuct.coverBase;

			final int vertexSize = VERTEX_SIZE;
			final int verticiesPerFace = 4, incrementAmt = vertexSize * verticiesPerFace;

			for (int k = rawBufferIndex; k < rawBufferIndex2; k += incrementAmt) {
				flag = flag2 = false;
				for (int i = 0; i < 3; i++) {
					flat[i] = true;
				}

				for (int k2 = 0; k2 < verticiesPerFace; k2++) {
					int i = k + k2 * vertexSize;
					quad[k2][0] = Float.intBitsToFloat(rb[i]) - dx - x;
					quad[k2][1] = Float.intBitsToFloat(rb[i + 1]) - dy - y;
					quad[k2][2] = Float.intBitsToFloat(rb[i + 2]) - dz - z;

					flag = flag || quad[k2][sideOffsets[side]] != sideSoftBounds[side];
					flag2 = flag2 || quad[k2][sideOffsets[side]] != (1 - sideSoftBounds[side]);

					if (k2 == 0) {
						System.arraycopy(quad[k2], 0, vec, 0, 3);
					} else {
						for (int vi = 0; vi < 3; vi++) {
							flat[vi] = flat[vi] && quad[k2][vi] == vec[vi];
						}
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

				if (addNormals) {
					intNormal = -64 << 8;
				}

				for (int k2 = 0; k2 < verticiesPerFace; k2++) {
					boolean flag3 = quad[k2][sideOffsets[side]] != sideSoftBounds[side];
					for (int j = 0; j < 3; j++) {
						if (j == sideOffsets[side]) {
							quad[k2][j] = clampF(quad[k2][j], bounds, j);
						} else {
							if (flag && flag2 && flag3) {
								// TODO: only clamp here when covers[] != null && has a cover on the side this vertex is on
								quad[k2][j] = MathHelper.clamp(quad[k2][j], FACADE_RENDER_OFFSET, FACADE_RENDER_OFFSET2);
							}
						}
					}

					int i = k + k2 * vertexSize;
					rb[i] = Float.floatToRawIntBits(quad[k2][0] + dx + x);
					rb[i + 1] = Float.floatToRawIntBits(quad[k2][1] + dy + y);
					rb[i + 2] = Float.floatToRawIntBits(quad[k2][2] + dz + z);

					if (s != -1) {
						float u, v;

						if (s == 0) {
							u = quad[k2][1];
							v = quad[k2][2];
						} else if (s == 1) {
							u = quad[k2][0];
							v = quad[k2][2];
						} else {
							u = quad[k2][0];
							v = quad[k2][1];
						}

						u = MathHelper.clamp(u, 0, 1) * 16;
						v = MathHelper.clamp(v, 0, 1) * 16;

						u = icon.getInterpolatedU(u);
						v = icon.getInterpolatedV(v);

						rb[i + 3] = Float.floatToRawIntBits(u);
						rb[i + 4] = Float.floatToRawIntBits(v);
					}

					if (addNormals) {

						rb[i + 6] = intNormal;
					}
					if (addTrans) {
						if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
							rb[i + 5] = rb[i + 5] & 0x00FFFFFF | (((rb[i + 5] & 0xFF000000) >>> 1) & 0xFF000000);
						} else {
							rb[i + 5] = rb[i + 5] & 0xFFFFFF00 | (((rb[i + 5] & 0x000000FF) >>> 1) & 0x000000FF);
						}
					}
				}
			}

		}

		facadeRenderBlocks.blockAccess = null;

		return rendered;

	}*/

    private final static int[][] sides = { { 4, 5 }, { 0, 1 }, { 2, 3 } };

    private static float clampF(float x, Cuboid6 b, int j) {

        float l = (float) b.getSide(sides[j][0]);
        float u = (float) b.getSide(sides[j][1]);

        if (x < l) {
            return l - (l - x) * 0.001953125f;
        } else if (x > u) {
            return u + (x - u) * 0.001953125f;
        } else {
            return x;
        }
    }

	/*public static boolean renderSide(RenderBlocks renderBlocks, Block block, int x, int y, int z, IIcon icon, int side) {

		switch (side) {
		case 0:
			renderBlocks.renderFaceYNeg(block, x, y, z, icon);
			break;
		case 1:
			renderBlocks.renderFaceYPos(block, x, y, z, icon);
			break;
		case 2:
			renderBlocks.renderFaceZNeg(block, x, y, z, icon);
			break;
		case 3:
			renderBlocks.renderFaceZPos(block, x, y, z, icon);
			break;
		case 4:
			renderBlocks.renderFaceXNeg(block, x, y, z, icon);
			break;
		case 5:
			renderBlocks.renderFaceXPos(block, x, y, z, icon);
			break;
		default:
			return false;
		}
		return true;
	}*/

    // FacadeBlockAccess.setEnclosingBedrock(true);
    // IIcon icon[] = new IIcon[6];
    // boolean flag = false;
    // boolean rendered = false;
    // if (block.hasTileEntity(meta) || block.getRenderType() == -1) {
    // for (int i = 0; i < 6; i++)
    // icon[i] = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
    // } else {
    // for (int i = 0; i < 6; i++) {
    // icon[i] = block.getIcon(facadeRenderBlocks.blockAccess, x, y, z, i);
    //
    // if (icon[i] == null)
    // icon[i] = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
    // }
    //
    // if (block.isNormalCube(facadeRenderBlocks.blockAccess, x, y, z) || block.getRenderType() == 0) {
    // flag = true;
    // for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS) {
    // if (s.ordinal() != side && block.shouldSideBeRendered(facadeRenderBlocks.blockAccess, x + s.offsetX, y + s.offsetY, z + s.offsetZ, s.ordinal())) {
    // flag = false;
    // break;
    // }
    // }
    // }
    // }
    //
    // facadeRenderBlocks.overrideBlockTexture = icon[side];
    // facadeRenderBlocks.overrideBlockBounds(0, 0, 0, 1, 1, 1);
    // if (flag) {
    // facadeRenderBlocks.renderBlockByRenderType(block, x, y, z);
    // FacadeBlockAccess.setEnclosingBedrock(false);
    // facadeRenderBlocks.overrideBlockBounds(b.min.x, b.min.y, b.min.z, b.max.x, b.max.y, b.max.z);
    //
    // for (int s = 0; s < 6; s++) {
    // if (side != s && (side == (s ^ 1) || (notSolid(facadeRenderBlocks.blockAccess, x, y, z, s) && noFacade(renderBlocks.blockAccess, x, y, z, s))))
    // renderSide(facadeRenderBlocks, block, x, y, z, icon[s], s);
    // }
    //
    // rendered = true;
    // } else {
    // FacadeBlockAccess.setEnclosingBedrock(false);
    // facadeRenderBlocks.overrideBlockBounds(b.min.x, b.min.y, b.min.z, b.max.x, b.max.y, b.max.z);
    // rendered = facadeRenderBlocks.renderStandardBlock(Blocks.stone, x, y, z);
    // }*/

    @SuppressWarnings("unused")
    private static float clampF(float vec, int side) {

        return MathHelper.clamp(sideSoftBounds[side] + (vec - sideSoftBounds[side]) * size, sideBound1[side], sideBound2[side]);
    }

    public static boolean noFacade(IBlockAccess world, BlockPos pos, EnumFacing face) {

        return !world.isSideSolid(pos, face, false);
    }

    public static boolean notSolid(IBlockAccess world, BlockPos pos, EnumFacing face) {

        IBlockState block2 = world.getBlockState(pos);

        return block2.shouldSideBeRendered(world, pos.offset(face), face);
    }
}

