package cofh.thermaldynamics.ducts.item;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.Iterator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class StackMap extends TObjectIntHashMap<StackMap.ItemEntry> {

    public boolean addItemstack(ItemStack itemStack, int side) {
        adjustOrPutValue(new ItemEntry(itemStack, side), itemStack.stackSize, itemStack.stackSize);
        return true;
    }

    public final static class ItemEntry {
        public Item item;
        public int metadata;
        public NBTTagCompound tag;
        public int nbtHashCode;
        public int side;

        private ItemEntry(ItemStack item, int side) {
            this(item.getItem(), item.getItemDamage(), item.stackTagCompound, side);
        }

        private ItemEntry(Item item, int metadata, NBTTagCompound tag, int side) {
            this(item, metadata, tag, side, tag == null ? -1 : tag.hashCode());
        }

        private ItemEntry(Item item, int metadata, NBTTagCompound tag, int side, int nbtHashCode) {
            this.item = item;
            this.metadata = metadata;
            this.tag = tag;
            this.nbtHashCode = nbtHashCode;
            this.side = side;
        }

        public ItemStack toItemStack(int amount) {
            if (tag == null)
                return new ItemStack(item, amount, metadata);

            ItemStack itemStack = new ItemStack(item, amount, metadata);
            itemStack.stackTagCompound = tag;
            return itemStack;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemEntry)) return false;

            ItemEntry itemEntry = (ItemEntry) o;

            if(side != itemEntry.side) return false;
            if (metadata != itemEntry.metadata) return false;
            if (nbtHashCode != itemEntry.nbtHashCode) return false;
            if (!item.equals(itemEntry.item)) return false;
            if (tag != null) {
                if(tag != itemEntry.tag) {
                    if (!tag.equals(itemEntry.tag)) return false;
                }
            } else {
                if (itemEntry.tag != null) return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = item.hashCode();
            result = 31 * result + side;
            result = 31 * result + metadata;
            result = 31 * result + nbtHashCode;
            return result;
        }
    }

    public IteratorItemstack getItems() {
        return new IteratorItemstack();
    }

    public class IteratorItemstack implements Iterator<ItemStack>, Iterable<ItemStack> {

        public final TObjectIntIterator<ItemEntry> iterator;

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        public IteratorItemstack() {
            iterator = StackMap.this.iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public ItemStack next() {
            iterator.advance();
            return iterator.key().toItemStack(iterator.value());
        }

        @Override
        public Iterator<ItemStack> iterator() {
            return this;
        }
    }

    public long quickHash(NBTTagCompound tag){


        return 0;
    }

    public long quickHash(NBTBase tag){
        long t = tag.getId();

    }
}
