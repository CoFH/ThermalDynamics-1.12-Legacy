package cofh.thermaldynamics.gui.container;

import cofh.core.network.PacketHandler;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import cofh.thermaldynamics.gui.client.DirectoryEntry;
import cofh.thermaldynamics.multiblock.RouteCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;

public class ContainerTransport extends Container {

	public final TileTransportDuct transportDuct;
	public DirectoryEntry directoryEntry;

	public ContainerTransport(TileTransportDuct transportDuct) {

		this.transportDuct = transportDuct;
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {

		return !transportDuct.isInvalid() && (transportDuct.isOutput() || transportDuct.world().isRemote);
	}

	@Override
	public void addCraftingToCrafters(ICrafting p_75132_1_) {

		super.addCraftingToCrafters(p_75132_1_);
		if (p_75132_1_ instanceof EntityPlayerMP) {
			PacketHandler.sendTo(transportDuct.getDirectoryPacket(), (EntityPlayerMP) p_75132_1_);
			cache = transportDuct.getCache();
		}

	}

	@Override
	public void detectAndSendChanges() {

		super.detectAndSendChanges();

		if (!this.crafters.isEmpty()) {
			if (cache == null || cache.invalid) {
				if (transportDuct.internalGrid == null) {
					cache = null;
				} else {
					cache = transportDuct.getCache();
					for (Object crafter : crafters) {
						if (crafter instanceof EntityPlayerMP) {
							PacketHandler.sendTo(transportDuct.getDirectoryPacket(), (EntityPlayerMP) crafter);
						}
					}
				}
			}
		}
	}

	RouteCache cache;

	public ArrayList<DirectoryEntry> directory;

	public Comparator<DirectoryEntry> blockDist = new Comparator<DirectoryEntry>() {

		@Override
		public int compare(DirectoryEntry o1, DirectoryEntry o2) {

			//			int c;
			//			c = compareDists(o1.x - transportDuct.x(), o1.y - transportDuct.y(), o1.z - transportDuct.z(),
			//					o2.x - transportDuct.x(), o2.y - transportDuct.y(), o2.z - transportDuct.z());
			//			if (c != 0) return c;

			return compareStrings(o1.name, o2.name);
		}
	};

	public int compareDists(int x1, int y1, int z1, int x2, int y2, int z2) {

		int c;
		c = compareInts(x1 * x1 + y1 * y1 + z1 * z1, x2 * x2 + y2 * y2 + z2 * z2);
		if (c != 0) {
			return c;
		}
		c = compareInts(y1, y2);
		if (c != 0) {
			return c;
		}
		c = compareInts(x1, x2);
		if (c != 0) {
			return c;
		}
		return compareInts(z1, z2);
	}

	public int compareStrings(String name1, String name2) {

		return name1 == null ? name2 == null ? 0 : -1 : name2 == null ? 1 : name1.compareTo(name2);
	}

	public static int compareInts(int x, int y) {

		return (x < y) ? -1 : ((x == y) ? 0 : 1);
	}

	boolean needsResort = false;

	public void setDirectory(ArrayList<DirectoryEntry> entries) {

		directory = entries;
		Collections.sort(directory, blockDist);
		needsResort = true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_) {

		return null;
	}

	public void setEntry(DirectoryEntry directoryEntry) {

		this.directoryEntry = directoryEntry;
	}

}
