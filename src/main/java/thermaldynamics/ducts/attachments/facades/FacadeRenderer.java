package thermaldynamics.ducts.attachments.facades;

import cofh.repack.codechicken.lib.vec.Cuboid6;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class FacadeRenderer {

    private static RenderBlocks facadeRenderBlocks = new RenderBlocks();

    public static boolean renderFacade(RenderBlocks renderBlocks, int x, int y, int z, int side, Block block, int meta, Cuboid6 b) {
        facadeRenderBlocks.blockAccess = FacadeBlockAccess.getInstance(renderBlocks.blockAccess, x, y, z, side, block, meta);

        FacadeBlockAccess.setEnclosingBedrock(true);
        IIcon icon[] = new IIcon[6];
        boolean flag = false;
        boolean rendered = false;
        if (block.hasTileEntity(meta) || block.getRenderType() == -1) {
            for (int i = 0; i < 6; i++)
                icon[i] = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
        } else {
            for (int i = 0; i < 6; i++) {
                icon[i] = block.getIcon(facadeRenderBlocks.blockAccess, x, y, z, i);

                if (icon[i] == null)
                    icon[i] = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
            }

            if (block.isNormalCube(facadeRenderBlocks.blockAccess, x, y, z) || block.getRenderType() == 0) {
                flag = true;
                for (ForgeDirection s : ForgeDirection.VALID_DIRECTIONS) {
                    if (s.ordinal() != side && block.shouldSideBeRendered(facadeRenderBlocks.blockAccess, x + s.offsetX, y + s.offsetY, z + s.offsetZ, s.ordinal())) {
                        flag = false;
                        break;
                    }
                }
            }
        }

        facadeRenderBlocks.overrideBlockTexture = icon[side];
        facadeRenderBlocks.overrideBlockBounds(0, 0, 0, 1, 1, 1);
        if (flag) {
            facadeRenderBlocks.renderBlockByRenderType(block, x, y, z);
            FacadeBlockAccess.setEnclosingBedrock(false);
            facadeRenderBlocks.overrideBlockBounds(b.min.x, b.min.y, b.min.z, b.max.x, b.max.y, b.max.z);

            for (int s = 0; s < 6; s++) {
                if (side != s && (side == (s ^ 1) || (notSolid(facadeRenderBlocks.blockAccess, x, y, z, s) && noFacade(renderBlocks.blockAccess, x, y, z, s))))
                    renderSide(facadeRenderBlocks, block, x, y, z, icon[s], s);
            }

            rendered = true;
        } else {
            FacadeBlockAccess.setEnclosingBedrock(false);
            facadeRenderBlocks.overrideBlockBounds(b.min.x, b.min.y, b.min.z, b.max.x, b.max.y, b.max.z);
            rendered = facadeRenderBlocks.renderStandardBlock(Blocks.stone, x, y, z);
        }

        Blocks.stone.setBlockBounds(0, 0, 0, 1, 1, 1);
        facadeRenderBlocks.overrideBlockTexture = null;
        facadeRenderBlocks.blockAccess = null;
        return rendered;
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
