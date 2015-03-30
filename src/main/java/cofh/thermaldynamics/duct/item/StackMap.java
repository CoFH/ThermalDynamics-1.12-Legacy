package cofh.thermaldynamics.duct.item;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Iterator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class StackMap extends TObjectIntHashMap<StackMap.ItemEntry> {

	public boolean addItemstack(ItemStack itemStack, int side) {

		adjustOrPutValue(new ItemEntry(itemStack, side), itemStack.stackSize, itemStack.stackSize);
		return true;
	}

	public boolean addItemEntry(ItemEntry entry, int amount) {

		adjustOrPutValue(entry, amount, amount);
		return true;
	}

	public final static class ItemEntry {

		public final Item item;
		public final int item_id;
		public final int metadata;
		public NBTTagCompound tag;
		public final int side;

		public ItemEntry(ItemStack item, int side) {

			this(item.getItem(), item.getItemDamage(), item.stackTagCompound, side);
		}

		public ItemEntry(Item item, int metadata, NBTTagCompound tag, int side) {

			this.item = item;
			this.metadata = metadata;
			this.tag = (tag != null) ? (NBTTagCompound) tag.copy() : null;
			this.side = side;
			this.item_id = getId();
		}

		public ItemStack toItemStack(int amount) {

			if (tag == null) {
				return new ItemStack(item, amount, metadata);
			}

			ItemStack itemStack = new ItemStack(item, amount, metadata);
			itemStack.stackTagCompound = (NBTTagCompound) tag.copy();
			return itemStack;
		}

		// '0' is null. '-1' is an unmapped item (missing in this World)
		protected final int getId() {

			return Item.getIdFromItem(item);
		}

		@Override
		public String toString() {

			return "ItemEntry{" + "item=" + item + ", item_id=" + item_id + ", metadata=" + metadata + ", tag=" + tag + ", side=" + side + '}';
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if (!(o instanceof ItemEntry)) {
				return false;
			}

			ItemEntry itemEntry = (ItemEntry) o;

			if (side != itemEntry.side) {
				return false;
			}
			if (metadata != itemEntry.metadata) {
				return false;
			}
			if (!item.equals(itemEntry.item)) {
				return false;
			}

			if (tag != null) {
				if (itemEntry.tag == null) {
					return false;
				} else if (tag != itemEntry.tag) {
					if (tag.equals(itemEntry.tag)) {
						tag = itemEntry.tag;
					} else {
						return false;
					}
				}
			} else if (itemEntry.tag != null) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {

			return (metadata & 16383) | item_id << 14 | side << 28;
		}
	}

	public IteratorItemstack getItems() {

		return new IteratorItemstack();
	}

	public class IteratorItemstack implements Iterator<ItemStack>, Iterable<ItemStack> {

		public final TObjectIntIterator<ItemEntry> iterator;

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

		@Override
		public void remove() {

			throw new UnsupportedOperationException("remove");
		}

	}
}
