package cofh.thermaldynamics.duct;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cofh.core.network.PacketBase;
import cofh.core.network.PacketTileInfo;
import cofh.core.util.CoreUtils;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender.CoverTransformer;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class Attachment {

	public final TileGrid baseTile;
	public final byte side;

	public Attachment(TileGrid tile, byte side) {

		this.baseTile = tile;
		this.side = side;
	}

	public abstract String getName();

	public abstract ResourceLocation getId();

	public abstract boolean isNode();

	@Nonnull
	public abstract BlockDuct.ConnectionType getNeighborType();

	public abstract Cuboid6 getCuboid();

	/**
	 * Whether or not retrievers can pull from the attached inventory
	 */
	public abstract boolean canSend();

	public void addCollisionBoxesToList(AxisAlignedBB entityBox, List<AxisAlignedBB> list, Entity entity) {

		Cuboid6 cuboid6 = getCuboid().add(baseTile.getPos());
		if (cuboid6.intersects(new Cuboid6(entityBox))) {
			list.add(cuboid6.aabb());
		}
	}

	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {

	}

	public void checkSignal() {

	}

	public void drawSelectionExtra(EntityPlayer player, RayTraceResult target, float partialTicks) {

	}

	public void dropItemStack(ItemStack item) {

		Cuboid6 c = getCuboid();
		Vector3 vec = Vector3.fromBlockPos(baseTile.getPos()).add(c.min);
		CoreUtils.dropItemStackIntoWorld(item, baseTile.getWorld(), vec.vec3());
	}

	public void onNeighborChange() {

	}

	public void postNeighborChange() {

	}

	public void tick(int pass) {

	}

	public DuctToken tickUnit() {

		return null;
	}

	public boolean addToTile() {

		return canAddToTile(baseTile) && baseTile.addAttachment(this);
	}

	public boolean allowDuctConnection() {

		return true;
	}

	public boolean allowEnergyConnection() {

		return true;
	}

	public boolean canAddToTile(TileGrid tile) {

		return tile.getAttachment(side) == null;
	}

	public boolean isUseable(EntityPlayer player) {

		return baseTile.isUsable(player);
	}

	public boolean makesSideSolid() {

		return false;
	}

	public boolean onWrenched() {

		baseTile.removeAttachment(this);
		for (ItemStack stack : getDrops()) {
			dropItemStack(stack);
		}
		return true;
	}

	public boolean respondsToSignalum() {

		return false;
	}

	public boolean shouldRSConnect() {

		return false;
	}

	public int getRSOutput() {

		return 0;
	}

	public abstract List<ItemStack> getDrops();

	public abstract ItemStack getPickBlock();

	/* NBT METHODS */
	public void readFromNBT(NBTTagCompound tag) {

	}

	public void writeToNBT(NBTTagCompound tag) {

	}

	/* NETWORK METHODS */
	public void addDescriptionToPacket(PacketBase packet) {

	}

	public void getDescriptionFromPacket(PacketBase packet) {

	}

	public void handleInfoPacket(PacketBase payload, boolean isServer, EntityPlayer player) {

	}

	public PacketTileInfo getNewPacket() {

		PacketTileInfo packet = PacketTileInfo.newPacket(baseTile);
		packet.addByte(1 + side);
		return packet;
	}

	/* GUI METHODS */
	public Object getGuiClient(InventoryPlayer inventory) {

		return null;
	}

	public Object getGuiServer(InventoryPlayer inventory) {

		return null;
	}

	public int getInvSlotCount() {

		return 0;
	}

	public boolean openGui(EntityPlayer player) {

		return false;
	}

	public void receiveGuiNetworkData(int i, int j) {

	}

	public void sendGuiNetworkData(Container container, List<IContainerListener> player, boolean newListener) {

	}

	/* RENDER */
	@SideOnly (Side.CLIENT)
	public abstract boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccRenderState);

	@SideOnly (Side.CLIENT)
	public CoverTransformer getHollowMask() {

		return null;
	}

}
