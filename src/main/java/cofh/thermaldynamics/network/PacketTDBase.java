package cofh.thermaldynamics.network;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;

public class PacketTDBase extends PacketCoFHBase {

	public static void initialize() {

		PacketHandler.instance.registerPacket(PacketTDBase.class);
	}

	@Override
	public void handlePacket(EntityPlayer player, boolean isServer) {

	}

}
