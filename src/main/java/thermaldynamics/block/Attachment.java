package thermaldynamics.block;

import cofh.core.network.PacketCoFHBase;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public abstract class Attachment {
    public TileMultiBlock tile;
    public byte side;

    public Attachment(TileMultiBlock tile, byte side) {
        this.tile = tile;
        this.side = side;
    }

    public abstract int getID();

    public void writeToNBT(NBTTagCompound tag) {

    }

    public void readFromNBT(NBTTagCompound tag) {

    }

    public void addDescriptionToPacket(PacketCoFHBase packet) {

    }

    public void getDescriptionFromPacket(PacketCoFHBase packet) {

    }

    public abstract Cuboid6 getCuboid();

    public abstract boolean onWrenched();

    public abstract TileMultiBlock.NeighborTypes getNeighbourType();

    public abstract boolean isNode();

    public boolean doesTick() {
        return false;
    }

    public void tick(int pass) {

    }

    @SideOnly(Side.CLIENT)
    public abstract boolean render(int pass, RenderBlocks renderBlocks);


    public void addCollisionBoxesToList(AxisAlignedBB axis, List list, Entity entity) {
        Cuboid6 cuboid6 = getCuboid().add(new Vector3(tile.xCoord, tile.yCoord, tile.zCoord));
        if (cuboid6.intersects(new Cuboid6(axis))) {
            list.add(cuboid6.toAABB());
        }
    }

    public boolean makesSideSolid() {
        return false;
    }

    public void onNeighbourChange() {

    }

    public boolean canAddToTile(TileMultiBlock tileMultiBlock) {
        return true;
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

    public void sendGuiNetworkData(Container container, ICrafting player) {

    }

    public int getInvSlotCount() {
        return 0;
    }

    public boolean openGui(EntityPlayer player) {
        return false;
    }

    public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

    }
}
