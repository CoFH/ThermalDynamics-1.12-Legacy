package cofh.thermaldynamics.block;

import cofh.core.network.PacketCoFHBase;
import cofh.core.util.CoreUtils;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;

public abstract class Attachment {

	public final TileMultiBlock tile;
	public final byte side;

	public Attachment(TileMultiBlock tile, byte side) {

		this.tile = tile;
		this.side = side;
	}

	public abstract String getName();

	public abstract int getId();

	public void writeToNBT(NBTTagCompound tag) {

	}

	public void readFromNBT(NBTTagCompound tag) {

	}

	public void addDescriptionToPacket(PacketCoFHBase packet) {

	}

	public void getDescriptionFromPacket(PacketCoFHBase packet) {

	}

	public abstract Cuboid6 getCuboid();

	public boolean onWrenched() {

		tile.removeAttachment(this);
		for (ItemStack stack : getDrops()) {
			dropItemStack(stack);
		}
		return true;
	}

	public abstract TileMultiBlock.NeighborTypes getNeighborType();

	public abstract boolean isNode();

	public boolean doesTick() {

		return false;
	}

	public void tick(int pass) {

	}

	public void dropItemStack(ItemStack item) {

		Cuboid6 c = getCuboid();
		CoreUtils.dropItemStackIntoWorldWithVelocity(item, tile.getWorldObj(), tile.x() + c.min.x + tile.world().rand.nextFloat() * (c.max.x - c.min.x),
				tile.y() + c.min.y + tile.world().rand.nextFloat() * (c.max.y - c.min.y), tile.z() + c.min.z + tile.world().rand.nextFloat()
						* (c.max.z - c.min.z)

		);
	}

	@SideOnly(Side.CLIENT)
	public abstract boolean render(int pass, RenderBlocks renderBlocks);

	@SuppressWarnings("unchecked")
	public void addCollisionBoxesToList(AxisAlignedBB axis, List list, Entity entity) {

		Cuboid6 cuboid6 = getCuboid().add(new Vector3(tile.xCoord, tile.yCoord, tile.zCoord));
		if (cuboid6.intersects(new Cuboid6(axis))) {
			list.add(cuboid6.toAABB());
		}
	}

	public boolean makesSideSolid() {

		return false;
	}

	public void onNeighborChange() {

	}

	public abstract ItemStack getPickBlock();

	public boolean canAddToTile(TileMultiBlock tileMultiBlock) {

		return tileMultiBlock.attachments[side] == null;
	}

	public abstract List<ItemStack> getDrops();

	@SideOnly(Side.CLIENT)
	public Object getGuiClient(InventoryPlayer inventory) {

		return null;
	}

	public Object getGuiServer(InventoryPlayer inventory) {

		return null;
	}

	public boolean isUseable(EntityPlayer player) {

		return tile.isUseable(player);
	}

	public void receiveGuiNetworkData(int i, int j) {

	}

	public void sendGuiNetworkData(Container container, List player, boolean newGuy) {

	}

	public int getInvSlotCount() {

		return 0;
	}

	public boolean openGui(EntityPlayer player) {

		return false;
	}

	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

	}

	public BlockDuct.ConnectionTypes getRenderConnectionType() {

		return TileMultiBlock.getDefaultConnectionType(getNeighborType(), TileMultiBlock.ConnectionTypes.NORMAL);
	}

	public boolean allowPipeConnection() {

		return false;
	}

	public boolean addToTile() {

		return canAddToTile(tile) && tile.addAttachment(this);
	}

	public void drawSelectionExtra(EntityPlayer player, MovingObjectPosition target, float partialTicks) {

	}

    public void postNeighbourChange() {

    }
}
