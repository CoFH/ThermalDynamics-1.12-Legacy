package thermaldynamics.debughelper;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;

import net.minecraft.entity.player.EntityPlayer;

public class PacketDebug extends PacketCoFHBase {

	public static void initialize() {

		PacketHandler.instance.registerPacket(PacketDebug.class);
	}

	public PacketDebug() {

	}

	public PacketDebug(int[] displayValue) {

		for (int i = 0; i < displayValue.length; i++) {
			addInt(displayValue[i]);
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {

		int[] d = new int[DebugTickHandler.DebugEvent.n];
		for (int i = 0; i < DebugTickHandler.DebugEvent.n; i++) {
			d[i] = getInt();
		}
	}

	@Override
	public void handlePacket(EntityPlayer player, boolean isServer) {

		if (isServer) {
			if (!DebugTickHandler.debugPlayers.remove(player)) {
				DebugTickHandler.debugPlayers.add(player);
			}
		}
	}

}
