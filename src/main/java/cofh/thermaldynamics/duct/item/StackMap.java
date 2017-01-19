package cofh.thermaldynamics.duct.item;

import cofh.core.util.nbt.NBTCopyHelper;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Iterator;

public class StackMap extends TObjectIntHashMap<StackMap.ItemEntry> {

	public boolean addItemstack(ItemStack itemStack, int side) {

		adjustOrPutValue(new ItemEntry(itemStack, side), itemStack.stackSize, itemStack.stackSize);
		return true;
	}

	public boolean addItemEntry(ItemEntry entry, int amount) {

		if (entry == null) {
			return false;
		}
		adjustOrPutValue(entry, amount, amount);
		return true;
	}

	public IteratorItemstack getItems() {

		return new IteratorItemstack();
	}

	public final static class ItemEntry {

		public final Item item;
		public final int metadata;
		public final int side;
		private final int hash;
		public NBTTagCompound tag;

		public ItemEntry(ItemStack item, int side) {

			this(item.getItem(), item.getItemDamage(), item.getTagCompound(), side);
		}

		public ItemEntry(Item item, int metadata, NBTTagCompound tag, int side) {

			this.item = item;
			this.metadata = metadata;
			this.side = side;
			int item_id = getId();

			if (tag == null) {
				this.tag = null;
				this.hash = (metadata & 16383) | item_id << 14 | side << 28;
			} else {
				NBTCopyHelper.ResultNBT resultNBT = NBTCopyHelper.copyAndHashNBT(tag);
				this.tag = resultNBT.copy;
				this.hash = ((metadata & 16383) | item_id << 14 | side << 28) ^ resultNBT.hash;
			}
		}

		public ItemStack toItemStack(int amount) {

			if (tag == null) {
				return new ItemStack(item, amount, metadata);
			}

			ItemStack itemStack = new ItemStack(item, amount, metadata);
			itemStack.setTagCompound(tag.copy());
			return itemStack;
		}

		// '0' is null. '-1' is an unmapped item (missing in this World)
		protected final int getId() {

			return Item.getIdFromItem(item);
		}

		@Override
		public String toString() {

			return "ItemEntry{" + "item=" + item + ", hash=" + hash + ", metadata=" + metadata + ", tag=" + tag + ", side=" + side + '}';
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

			if (hash != itemEntry.hash) {
				return false;
			}

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

			return hash;
		}
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
