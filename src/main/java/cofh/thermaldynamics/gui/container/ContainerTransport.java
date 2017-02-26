package cofh.thermaldynamics.gui.container;

import cofh.core.network.PacketHandler;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import cofh.thermaldynamics.gui.client.DirectoryEntry;
import cofh.thermaldynamics.multiblock.RouteCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;

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
	public void addListener(IContainerListener listener) {

		super.addListener(listener);
		if (listener instanceof EntityPlayerMP) {
			PacketHandler.sendTo(transportDuct.getDirectoryPacket(), (EntityPlayerMP) listener);
			cache = transportDuct.getCache();
		}

	}

	@Override
	public void detectAndSendChanges() {

		super.detectAndSendChanges();

		if (!this.listeners.isEmpty()) {
			if (cache == null || cache.invalid) {
				if (transportDuct.internalGrid == null) {
					cache = null;
				} else {
					cache = transportDuct.getCache();
					for (IContainerListener listener : listeners) {
						if (listener instanceof EntityPlayerMP) {
							PacketHandler.sendTo(transportDuct.getDirectoryPacket(), (EntityPlayerMP) listener);
						}
					}
				}
			}
		}
	}

	RouteCache cache;

	public ArrayList<DirectoryEntry> directory;

	public Comparator<DirectoryEntry> blockDist = (o1, o2) -> compareStrings(o1.name, o2.name);

	public int compareStrings(String name1, String name2) {

		return name1 == null ? name2 == null ? 0 : -1 : name2 == null ? 1 : name1.compareTo(name2);
	}

	boolean needsResort = false;

	public void setDirectory(ArrayList<DirectoryEntry> entries) {

		directory = entries;
		directory.sort(blockDist);
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
