package thermaldynamics.ducts.attachments.facades;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Rotation;
import cofh.repack.codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;

public class Cover extends Attachment {
    private static Cuboid6 bound = new Cuboid6(0, 0, 0, 1, 0.0625, 1);

    public static Cuboid6[] bounds = {
            bound,
            bound.copy().apply(Rotation.sideRotations[1].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[2].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[3].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[4].at(Vector3.center)),
            bound.copy().apply(Rotation.sideRotations[5].at(Vector3.center))
    };

    public Block block;
    public int meta;

    public Cover(TileMultiBlock tile, byte side, Block block, int meta) {
        super(tile, side);
        this.block = block;
        this.meta = meta;
    }

    public Cover(TileMultiBlock tile, byte side) {
        super(tile, side);
    }


    @Override
    public int getID() {
        return AttachmentRegistry.FACADE;
    }

    @Override
    public Cuboid6 getCuboid() {
        return bounds[side].copy();
    }

    @Override
    public boolean onWrenched() {
        tile.removeFacade(this);

        for (ItemStack stack : getDrops()) {
            dropItemStack(stack);
        }
        return true;
    }

    @Override
    public TileMultiBlock.NeighborTypes getNeighbourType() {
        return TileMultiBlock.NeighborTypes.NONE;
    }

    @Override
    public boolean isNode() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean render(int pass, RenderBlocks renderBlocks) {
        if (!block.canRenderInPass(pass))
            return false;

        return CoverRemderer.renderCover(renderBlocks, tile.xCoord, tile.yCoord, tile.zCoord, side, block, meta, getCuboid(), false);
    }

    @Override
    public boolean makesSideSolid() {
        return true;
    }

    @Override
    public ItemStack getPickBlock() {
        return CoverHelper.getFacadeItemStack(block, meta);
    }

    @Override
    public List<ItemStack> getDrops() {
        LinkedList<ItemStack> itemStacks = new LinkedList<ItemStack>();
        itemStacks.add(getPickBlock());
        return itemStacks;
    }

    @Override
    public boolean addToTile() {
        return tile.addFacade(this);
    }

    @Override
    public void addDescriptionToPacket(PacketCoFHBase packet) {
        packet.addShort(Block.getIdFromBlock(block));
        packet.addByte(meta);
    }

    @Override
    public void getDescriptionFromPacket(PacketCoFHBase packet) {
        block = Block.getBlockById(packet.getShort());
        meta = packet.getByte();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("block", Block.blockRegistry.getNameForObject(block));
        tag.setByte("meta", (byte) meta);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        block = Block.getBlockFromName(tag.getString("block"));
        if (block == null) block = Blocks.air;
        meta = tag.getByte("meta");
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void drawSelectionExtra(EntityPlayer player, MovingObjectPosition target, float partialTicks) {
        super.drawSelectionExtra(player, target, partialTicks);

        RenderHelper.setBlockTextureSheet();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(true);
        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        GL11.glColor4f(1, 1, 1, 0.5F);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glPushMatrix();
        {
            GL11.glTranslated(-d0, -d1, -d2);
            Tessellator tess = Tessellator.instance;
            tess.startDrawingQuads();
            Tessellator.instance.setNormal(0, 1, 0);
            tess.setColorRGBA_F(1, 1, 1, 0.5F);
            tess.setBrightness(tile.getBlockType().getMixedBrightnessForBlock(tile.world(), tile.xCoord, tile.yCoord, tile.zCoord));
            RenderBlocks renderBlocks = CoverRemderer.renderBlocks;
            renderBlocks.blockAccess = player.getEntityWorld();
            CoverRemderer.renderCover(renderBlocks, tile.xCoord, tile.yCoord, tile.zCoord, side, block, meta, getCuboid(), false);
            tess.draw();
        }
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
