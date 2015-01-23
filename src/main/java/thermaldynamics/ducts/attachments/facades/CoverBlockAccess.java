package thermaldynamics.ducts.attachments.facades;

import cofh.api.block.IBlockAppearance;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import static thermaldynamics.ducts.attachments.facades.CoverBlockAccess.Result.AIR;
import static thermaldynamics.ducts.attachments.facades.CoverBlockAccess.Result.BASE;
import static thermaldynamics.ducts.attachments.facades.CoverBlockAccess.Result.BEDROCK;
import static thermaldynamics.ducts.attachments.facades.CoverBlockAccess.Result.COVER;
import static thermaldynamics.ducts.attachments.facades.CoverBlockAccess.Result.ORIGINAL;

public class CoverBlockAccess implements IBlockAccess {

    IBlockAccess world;

    public static CoverBlockAccess instance = new CoverBlockAccess();

    public static CoverBlockAccess getInstance(IBlockAccess world, int blockX, int blockY, int blockZ, int side, Block block, int meta) {
        instance.world = world;
        instance.blockX = blockX;
        instance.blockY = blockY;
        instance.blockZ = blockZ;
        instance.side = side;
        instance.block = block;
        instance.meta = meta;
        return instance;
    }

    public int blockX, blockY, blockZ;
    public int side;

    public Block block;
    public int meta;

    public enum Result {
        ORIGINAL, AIR, BASE, BEDROCK, COVER
    }

    public Result getAction(int x, int y, int z) {
        if (x == blockX && y == blockY && z == blockZ)
            return BASE;

        if (x == blockX + Facing.offsetsXForSide[side] &&
                y == blockY + Facing.offsetsYForSide[side] &&
                z == blockZ + Facing.offsetsZForSide[side]) {
            return ORIGINAL;
        }

        if (((side == 0 && y > blockY) ||
                (side == 1 && y < blockY) ||
                (side == 2 && z > blockZ) ||
                (side == 3 && z < blockZ) ||
                (side == 4 && x > blockX) ||
                (side == 5 && x < blockX))) {
            return AIR;
        }

        Block worldBlock = world.getBlock(x, y, z);

        if (worldBlock instanceof IBlockAppearance) {
            IBlockAppearance blockAppearance = (IBlockAppearance) worldBlock;
            if (blockAppearance.supportsVisualConnections())
                return COVER;

            if (blockAppearance.getVisualBlock(world, x, y, z, ForgeDirection.getOrientation(side)) == block && blockAppearance.getVisualMeta(world, x, y, z, ForgeDirection.getOrientation(side)) == meta) {
                return block.isNormalCube() ? BEDROCK : AIR;
            } else
                return COVER;
        } else {
            if (worldBlock == block && world.getBlockMetadata(x, y, z) == meta)
                return block.isNormalCube() ? BEDROCK : AIR;
            else
                return ORIGINAL;
        }
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Result action = getAction(x, y, z);
        return action == ORIGINAL ? world.getBlock(x, y, z) :
                action == AIR ? Blocks.air :
                        action == BEDROCK ? Blocks.bedrock :
                                action == COVER ? ((IBlockAppearance) world.getBlock(x, y, z)).getVisualBlock(world, x, y, z, ForgeDirection.getOrientation(side)) : block;
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return getAction(x, y, z) == ORIGINAL ? world.getTileEntity(x, y, z) : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int t) {
        return world.getLightBrightnessForSkyBlocks(x, y, z, t);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        Result action = getAction(x, y, z);
        return action == ORIGINAL ? world.getBlockMetadata(x, y, z) :
                action == AIR || action == BEDROCK ? 0 :
                        action == COVER ? ((IBlockAppearance) world.getBlock(x, y, z)).getVisualMeta(world, x, y, z, ForgeDirection.getOrientation(side)) :
                                meta;
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int s) {
        return world.isBlockProvidingPowerTo(x, y, z, s);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        Result action = getAction(x, y, z);
        return action == AIR || (action == ORIGINAL && world.isAirBlock(x, y, z))
                || (action == COVER && getBlock(x, y, z).isAir(this, x, y, z));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return world.getBiomeGenForCoords(x, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getHeight() {
        return world.getHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean extendedLevelsInChunkCache() {
        return world.extendedLevelsInChunkCache();
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000) return _default;
        else return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
    }
}