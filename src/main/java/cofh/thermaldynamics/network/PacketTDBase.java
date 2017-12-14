package cofh.thermaldynamics.network;

import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;

public class PacketTDBase extends PacketBase {

	public static void initialize() {

		PacketHandler.INSTANCE.registerPacket(PacketTDBase.class);
	}

	@Override
	public void handlePacket(EntityPlayer player, boolean isServer) {

	}

}
