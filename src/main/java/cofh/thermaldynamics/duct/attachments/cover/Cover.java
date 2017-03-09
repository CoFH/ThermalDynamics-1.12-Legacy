package cofh.thermaldynamics.duct.attachments.cover;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.render.RenderHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.TileDuctBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

public class Cover extends Attachment {

	//@formatter:off
	private static Cuboid6 bound = new Cuboid6(0, 0, 0, 1, 0.0625, 1);

	public static Cuboid6[] bounds = { bound,
			bound.copy().apply(Rotation.sideRotations[1].at(Vector3.center)),
			bound.copy().apply(Rotation.sideRotations[2].at(Vector3.center)),
			bound.copy().apply(Rotation.sideRotations[3].at(Vector3.center)),
			bound.copy().apply(Rotation.sideRotations[4].at(Vector3.center)),
			bound.copy().apply(Rotation.sideRotations[5].at(Vector3.center))
	};
	//@formatter:on

	public IBlockState state;

	public Cover(TileDuctBase tile, byte side, IBlockState state) {

		super(tile, side);
		this.state = state;
	}

	public Cover(TileDuctBase tile, byte side) {

		super(tile, side);
	}

	@Override
	public String getName() {

		return "item.thermalfoundation.cover.name";
	}

	@Override
	public int getId() {

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
	public TileDuctBase.NeighborTypes getNeighborType() {

		return TileDuctBase.NeighborTypes.NONE;
	}

	@Override
	public boolean isNode() {

		return false;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccrs) {

		if (!state.getBlock().canRenderInLayer(state, layer)) {
			return false;
		}

		Attachment attachment = tile.attachments[side];
		CoverHoleRender.ITransformer[] hollowMask = null;
		if (attachment != null) {
			hollowMask = attachment.getHollowMask();
		}
		if (hollowMask == null) {
			hollowMask = tile.getHollowMask(side);
		}

		return CoverRenderer.renderBlockCover(ccrs, world, tile.getPos(), side, state, getCuboid(), hollowMask);
	}

	@Override
	public boolean makesSideSolid() {

		return true;
	}

	@Override
	public ItemStack getPickBlock() {

		return CoverHelper.getCoverStack(state);
	}

	@Override
	public List<ItemStack> getDrops() {

		LinkedList<ItemStack> itemStacks = new LinkedList<>();
		itemStacks.add(getPickBlock());
		return itemStacks;
	}

	@Override
	public boolean addToTile() {

		return tile.addFacade(this);
	}

	@Override
	public void addDescriptionToPacket(PacketCoFHBase packet) {

		packet.addShort(Block.getIdFromBlock(state.getBlock()));
		packet.addByte(state.getBlock().getMetaFromState(state));
	}

	@SuppressWarnings ("deprecation")
	@Override
	public void getDescriptionFromPacket(PacketCoFHBase packet) {

		Block block = Block.getBlockById(packet.getShort());
		int meta = packet.getByte();
		state = block.getStateFromMeta(meta);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {

		super.writeToNBT(tag);
		tag.setString("block", ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString());
		tag.setByte("meta", (byte) state.getBlock().getMetaFromState(state));
	}

	@Override
	public boolean canAddToTile(TileDuctBase tileMultiBlock) {

		return tileMultiBlock.covers[side] == null;
	}

	@SuppressWarnings ("deprecation")
	@Override
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		Block block = Block.getBlockFromName(tag.getString("block"));
		if (block == null) {
			state = Blocks.AIR.getDefaultState();
		} else {
			state = block.getStateFromMeta(tag.getByte("meta"));
		}
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void drawSelectionExtra(EntityPlayer player, RayTraceResult target, float partialTicks) {

		super.drawSelectionExtra(player, target, partialTicks);

		RenderHelper.setBlockTextureSheet();
		net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableTexture2D();

		GlStateManager.depthMask(false);
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
		GlStateManager.color(1, 1, 1, 0.5F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.pushMatrix();
		{

			GlStateManager.translate(-d0, -d1, -d2);
			GlStateManager.translate(tile.x() + 0.5, tile.y() + 0.5, tile.z() + 0.5);
			GlStateManager.scale(1 + RenderHelper.RENDER_OFFSET, 1 + RenderHelper.RENDER_OFFSET, 1 + RenderHelper.RENDER_OFFSET);
			GlStateManager.translate(-tile.x() - 0.5, -tile.y() - 0.5, -tile.z() - 0.5);

			CCRenderState ccrs = CCRenderState.instance();
			ccrs.reset();
			ccrs.startDrawing(7, DefaultVertexFormats.BLOCK);
			ccrs.alphaOverride = 80;
			CoverRenderer.renderBlockCover(ccrs, tile.world(), tile.getPos(), side, state, getCuboid(), null);
			ccrs.draw();
		}
		GlStateManager.popMatrix();
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.depthMask(true);
		GlStateManager.disableAlpha();
		GlStateManager.disableColorMaterial();
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
	}

}
