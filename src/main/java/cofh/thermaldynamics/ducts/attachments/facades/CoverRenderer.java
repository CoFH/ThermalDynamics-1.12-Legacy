package cofh.thermaldynamics.ducts.attachments.facades;

import cofh.lib.render.RenderHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Vector3;
import cofh.thermalfoundation.block.TFBlocks;
import java.nio.ByteOrder;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class CoverRenderer {

    private static RenderBlocks facadeRenderBlocks = new RenderBlocks();
    public static RenderBlocks renderBlocks = new RenderBlocks();

    public static final float size = 1 / 512F;

    final static int[] sideOffsets = {1, 1, 2, 2, 0, 0};
    final static float[] sideBound1 = {0, 1 - size, 0, 1 - size, 0, 1 - size};
    final static float[] sideBound2 = {size, 1, size, 1, size, 1};

    final static float[] sideSoftBounds = {0, 1, 0, 1, 0, 1};

    private final static float FACADE_RENDER_OFFSET = ((float) RenderHelper.RENDER_OFFSET) * 2;
    private final static float FACADE_RENDER_OFFSET2 = 1 - FACADE_RENDER_OFFSET;

    public static boolean renderCover(RenderBlocks renderBlocks, int x, int y, int z, int side, Block block, int meta, Cuboid6 bounds, boolean addNormals, boolean addTrans) {
        facadeRenderBlocks.blockAccess = CoverBlockAccess.getInstance(renderBlocks.blockAccess, x, y, z, side, block, meta);

        Tessellator tess = Tessellator.instance;
        int rawBufferIndex = tess.rawBufferIndex;

        boolean rendered = facadeRenderBlocks.renderBlockByRenderType(block, x, y, z);

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

            Vector3 normal;
            int intNormal = 0;

            IIcon icon = TFBlocks.blockStorage.getIcon(0, 3);

            for (int k = rawBufferIndex; k < rawBufferIndex2; k += 32) {
                flag = flag2 = false;
                for (int i = 0; i < 3; i++) {
                    flat[i] = true;
                }

                for (int k2 = 0; k2 < 4; k2++) {
                    int i = k + k2 * 8;
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

                if (flag && flag2)
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

                if (addNormals) {
                    normal = (new Vector3(
                            quad[1][0] - quad[0][0],
                            quad[1][1] - quad[0][1],
                            quad[1][2] - quad[0][2]));

                    normal.crossProduct(new Vector3(
                            quad[2][0] - quad[0][0],
                            quad[2][1] - quad[0][1],
                            quad[2][2] - quad[0][2]));

                    normal.normalize();

                    byte b0 = (byte) ((int) (normal.x * 127.0F));
                    byte b1 = (byte) ((int) (normal.y * 127.0F));
                    byte b2 = (byte) ((int) (normal.z * 127.0F));
                    intNormal = (b0 & 255) | (b1 & 255) << 8 | (b2 & 255) << 16;
                }

                for (int k2 = 0; k2 < 4; k2++) {
                    boolean flag3 = quad[k2][sideOffsets[side]] != sideSoftBounds[side];
                    for (int j = 0; j < 3; j++) {
                        if (j == sideOffsets[side]) {
                            quad[k2][j] = clampF(quad[k2][j], bounds, j);
                        } else {
                            if (flag && flag2 && flag3) {
                                quad[k2][j] = MathHelper.clampF(quad[k2][j], FACADE_RENDER_OFFSET, FACADE_RENDER_OFFSET2);
                            }
                        }
                    }

                    int i = k + k2 * 8;
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

                        u = MathHelper.clampF(u, 0, 1) * 16;
                        v = MathHelper.clampF(v, 0, 1) * 16;

                        u = icon.getInterpolatedU(u);
                        v = icon.getInterpolatedV(v);

                        rb[i + 3] = Float.floatToRawIntBits(u);
                        rb[i + 4] = Float.floatToRawIntBits(v);
                    }

                    if (addNormals) rb[i + 6] = intNormal;
                    if (addTrans) {
                        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                            rb[i + 5] = rb[i + 5] & 0x00FFFFFF | 0x80000000;
                        } else {
                            rb[i + 5] = rb[i + 5] & 0xFFFFFF00 | 0x00000080;
                        }
                    }
                }
            }

        }

        facadeRenderBlocks.blockAccess = null;

        return rendered;

    }

    private final static int[][] sides = {
            {4, 5},
            {0, 1},
            {2, 3}
    };

    private static float clampF(float x, Cuboid6 b, int j) {
        float l = (float) b.getSide(sides[j][0]);
        float u = (float) b.getSide(sides[j][1]);

        if (x < l) {
            return l - (l - x) * 0.001953125f;
        } else if (x > u) {
            return u + (x - u) * 0.001953125f;
        } else
            return x;
    }

    public static boolean renderSide(RenderBlocks renderBlocks, Block block, int x, int y, int z, IIcon icon, int side) {
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
    }

//    FacadeBlockAccess.setEnclosingBedrock(true);
//    IIcon icon[] = new IIcon[6];
//    boolean flag = false;
//    boolean rendered = false;
//    if (block.hasTileEntity(meta) || block.getRenderType() == -1) {
//        for (int i = 0; i < 6; i++)
//            icon[i] = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
//    } else {
//        for (int i = 0; i < 6; i++) {
//            icon[i] = block.getIcon(facadeRenderBlocks.blockAccess, x, y, z, i);
//
//            if (icon[i] == null)
//                icon[i] = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
//        }
//
//        if (block.isNormalCube(facadeRenderBlocks.blockAccess, x, y, z) || block.getRenderType() == 0) {
//            flag = true;
//            for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS) {
//                if (s.ordinal() != side && block.shouldSideBeRendered(facadeRenderBlocks.blockAccess, x + s.offsetX, y + s.offsetY, z + s.offsetZ, s.ordinal())) {
//                    flag = false;
//                    break;
//                }
//            }
//        }
//    }
//
//    facadeRenderBlocks.overrideBlockTexture = icon[side];
//    facadeRenderBlocks.overrideBlockBounds(0, 0, 0, 1, 1, 1);
//    if (flag) {
//        facadeRenderBlocks.renderBlockByRenderType(block, x, y, z);
//        FacadeBlockAccess.setEnclosingBedrock(false);
//        facadeRenderBlocks.overrideBlockBounds(b.min.x, b.min.y, b.min.z, b.max.x, b.max.y, b.max.z);
//
//        for (int s = 0; s < 6; s++) {
//            if (side != s && (side == (s ^ 1) || (notSolid(facadeRenderBlocks.blockAccess, x, y, z, s) && noFacade(renderBlocks.blockAccess, x, y, z, s))))
//                renderSide(facadeRenderBlocks, block, x, y, z, icon[s], s);
//        }
//
//        rendered = true;
//    } else {
//        FacadeBlockAccess.setEnclosingBedrock(false);
//        facadeRenderBlocks.overrideBlockBounds(b.min.x, b.min.y, b.min.z, b.max.x, b.max.y, b.max.z);
//        rendered = facadeRenderBlocks.renderStandardBlock(Blocks.stone, x, y, z);
//    }


    private static float clampF(float vec, int side) {
        return MathHelper.clampF(sideSoftBounds[side] + (vec - sideSoftBounds[side]) * size, sideBound1[side], sideBound2[side]);
    }

    public static boolean noFacade(IBlockAccess world, int x, int y, int z, int side) {
        return !world.isSideSolid(x, y, z, ForgeDirection.values()[side], false);
    }

    public static boolean notSolid(IBlockAccess world, int x, int y, int z, int side) {

        ForgeDirection dir = ForgeDirection.values()[side];
        Block block = world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
        Block block2 = world.getBlock(x, y, z);

        return block2.shouldSideBeRendered(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, side);
    }
}
