package cofh.thermaldynamics.gui.client;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.duct.entity.DuctUnitTransport;
import com.google.common.base.Strings;
import net.minecraft.item.ItemStack;

public final class DirectoryEntry {

	public final String name;
	public final int x, y, z;
	public final ItemStack icon;

	public static void addDirectoryEntry(PacketTileInfo myPayload, DuctUnitTransport endPoint) {

		myPayload.addString(endPoint.data.name);
		myPayload.addInt(endPoint.x());
		myPayload.addInt(endPoint.y());
		myPayload.addInt(endPoint.z());
		myPayload.addItemStack(endPoint.data.item);
	}

	public DirectoryEntry(PacketCoFHBase packet) {

		this(packet.getString(), packet.getInt(), packet.getInt(), packet.getInt(), packet.getItemStack());
	}

	public DirectoryEntry(String name, int x, int y, int z, ItemStack icon) {

		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.icon = icon;

	}

	@Override
	public String toString() {

		return "DirectoryEntry{" + "name='" + name + '\'' + ", x=" + x + ", y=" + y + ", z=" + z + ", icon=" + icon + '}';
	}

	public String getName() {

		if (Strings.isNullOrEmpty(name)) {
			return StringHelper.localize("info.thermaldynamics.transport.unnamed");
		} else {
			return name;
		}
	}

}
