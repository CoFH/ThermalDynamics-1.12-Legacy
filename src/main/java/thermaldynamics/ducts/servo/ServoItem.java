package thermaldynamics.ducts.servo;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.item.PropsConduit;
import thermaldynamics.ducts.item.TileItemDuct;

import java.util.LinkedList;

public class ServoItem extends ServoBase {
    LinkedList<ItemStack> stuffedItems = new LinkedList<ItemStack>();

    TileItemDuct itemDuct;

    public ServoItem(TileMultiBlock tile, byte side, int type) {
        super(tile, side, type);
        itemDuct = ((TileItemDuct) tile);
    }

    public ServoItem(TileMultiBlock tile, byte side) {
        super(tile, side);
        itemDuct = ((TileItemDuct) tile);
    }

    @Override
    public int getID() {
        return AttachmentRegistry.SERVO_INV;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (!stuffedItems.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (ItemStack item : stuffedItems) {
                NBTTagCompound newTag = new NBTTagCompound();
                item.writeToNBT(newTag);
                list.appendTag(newTag);
            }
            tag.setTag("stuffed", list);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        stuffedItems.clear();
        if (tag.hasKey("stuffed", 9)) {
            NBTTagList tlist = tag.getTagList("stuffed", 10);
            for (int j = 0; j < tlist.tagCount(); j++) {
                ItemStack item = ItemStack.loadItemStackFromNBT(tlist.getCompoundTagAt(j));
                if (item != null)
                    stuffedItems.add(item);
            }
        }
    }

    public boolean canStuff() {
        return stuffedItems.size() < PropsConduit.STUFF_LIMIT;
    }

    public void stuffItem(ItemStack item) {
        stuffedItems.add(item.copy());
        item.stackSize = 0;
    }

    @Override
    public void tick(int pass) {
        super.tick(pass);

        if (!stuffedItems.isEmpty()) {

        }


    }


}
