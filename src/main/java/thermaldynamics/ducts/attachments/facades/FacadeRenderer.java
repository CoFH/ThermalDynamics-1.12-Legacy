package thermaldynamics.ducts.attachments.facades;

import cofh.lib.render.RenderHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.repack.codechicken.lib.lighting.LightModel;
import cofh.repack.codechicken.lib.render.CCModel;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Vector3;
import com.google.common.base.Throwables;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class FacadeRenderer {

    final static Field rawBufferIndex = ReflectionHelper.findField(Tessellator.class, "rawBufferIndex");
    final static Field rawBuffer = ReflectionHelper.findField(Tessellator.class, "rawBuffer");
    final static Field xOffset = ReflectionHelper.findField(Tessellator.class, "xOffset");
    final static Field yOffset = ReflectionHelper.findField(Tessellator.class, "yOffset");
    final static Field zOffset = ReflectionHelper.findField(Tessellator.class, "zOffset");

    private static RenderBlocks facadeRenderBlocks = new RenderBlocks();
    public static RenderBlocks renderBlocks = new RenderBlocks();

    public static final float size = 1 / 512F;

    final static int[] sideOffsets = {1, 1, 2, 2, 0, 0};
    final static float[] sideBound1 = {0, 1 - size, 0, 1 - size, 0, 1 - size};
    final static float[] sideBound2 = {size, 1, size, 1, size, 1};

    final static float[] sideSoftBounds = {0, 1, 0, 1, 0, 1};
    final static float[] sideMult = {1, -1, 1, -1, 1, -1};


    private final static float FACADE_RENDER_OFFSET = ((float) RenderHelper.RENDER_OFFSET) * 2;
    private final static float FACADE_RENDER_OFFSET2 = 1 - FACADE_RENDER_OFFSET;

    static final CCModel[] leadModels = new CCModel[6];

    static {
        leadModels[0] = CCModel.quadModel(24);
        leadModels[0].generateBlock(0, 0, size, 0, 1, 0.125f, 1, 62);
        leadModels[0].generateBlock(4, RenderHelper.RENDER_OFFSET, size, RenderHelper.RENDER_OFFSET, 1 - RenderHelper.RENDER_OFFSET, 0.125f, 1 - RenderHelper.RENDER_OFFSET, 1);
        //CCModel.generateBackface(leadModels[0], 0, leadModels[0], 24, 24);
        CCModel.generateSidedModels(leadModels, 0, Vector3.center);
        for (int i = 0; i < 6; i++) {
            leadModels[i].computeNormals().computeLighting(LightModel.standardLightModel).shrinkUVs(RenderHelper.RENDER_OFFSET);
        }
//        leadModels[0] = CCModel.quadModel(40).generateBlock(0, RenderHelper.RENDER_OFFSET, 0, RenderHelper.RENDER_OFFSET, 1 - RenderHelper.RENDER_OFFSET, 0.0625F * (1 + RenderHelper.RENDER_OFFSET), 1 - RenderHelper.RENDER_OFFSET, 1);
//        CCModel.generateBackface(leadModels[0], 0, leadModels[0], 20, 20);
    }


    public static boolean renderFacade(RenderBlocks renderBlocks, int x, int y, int z, int side, Block block, int meta, Cuboid6 bounds) {
        try {
            facadeRenderBlocks.blockAccess = FacadeBlockAccess.getInstance(renderBlocks.blockAccess, x, y, z, side, block, meta);
            facadeRenderBlocks.overrideBlockBounds(bounds.min.x, bounds.min.y, bounds.min.z, bounds.max.x, bounds.max.y, bounds.max.z);

            int rawBufferIndex = FacadeRenderer.rawBufferIndex.getInt(Tessellator.instance);

            boolean rendered = facadeRenderBlocks.renderBlockByRenderType(block, x, y, z);
            boolean renderFacade = false;


            int rawBufferIndex2 = FacadeRenderer.rawBufferIndex.getInt(Tessellator.instance);

            if (rawBufferIndex != rawBufferIndex2) {
                int[] rb = (int[]) FacadeRenderer.rawBuffer.get(Tessellator.instance);
                float[] vec = new float[3];
                boolean flag;
                float[] boundsL = {0.5F, 0.5F, 0.5F};
                float[] boundsU = {0.5F, 0.5F, 0.5F};


                float dx = (float) xOffset.getDouble(Tessellator.instance);
                float dy = (float) yOffset.getDouble(Tessellator.instance);
                float dz = (float) zOffset.getDouble(Tessellator.instance);

                for (int i = rawBufferIndex; i < rawBufferIndex2; i += 8) {


                    vec[0] = Float.intBitsToFloat(rb[i]) - dx - x;
                    vec[1] = Float.intBitsToFloat(rb[i + 1]) - dy - y;
                    vec[2] = Float.intBitsToFloat(rb[i + 2]) - dz - z;

                    flag = vec[sideOffsets[side]] != sideSoftBounds[side];

                    float v = sideSoftBounds[side] + sideMult[side] * vec[sideOffsets[side]];
                    if(v > 0.5)v=1-v;

                    for (int j = 0; j < 3; j++) {
                        if (j == sideOffsets[side]) {
                            vec[j] = clampF(vec[j], bounds, j);
                        } else {
                            if (flag) {
                                vec[j] = MathHelper.clampF(vec[j], FACADE_RENDER_OFFSET, FACADE_RENDER_OFFSET2);
//                                vec[j] = MathHelper.clampF(vec[j], v, 1 - v);
                            } else {
                                boundsL[j] = Math.min(boundsL[j], vec[j]);
                                boundsU[j] = Math.max(boundsU[j], vec[j]);
                            }
                        }
                    }

                    rb[i] = Float.floatToRawIntBits(vec[0] + dx + x);
                    rb[i + 1] = Float.floatToRawIntBits(vec[1] + dy + y);
                    rb[i + 2] = Float.floatToRawIntBits(vec[2] + dz + z);
                }

                for (int j = 0; j < 3; j++) {
                    if (j != sideOffsets[side]) {
                        if (boundsL[j] > 0 || boundsU[j] < 1) {
                            renderFacade = true;
                            break;
                        }
                    }
                }
                if (!renderFacade) {
                    renderFacade = false;
                }

            }

            facadeRenderBlocks.blockAccess = null;

            return rendered;
        } catch (IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
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
