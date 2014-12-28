package thermaldynamics.debughelper;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;

public class PacketDebug extends PacketCoFHBase {
    public static void initialize() {

        PacketHandler.instance.registerPacket(PacketDebug.class);
    }




    @Override
    public void handlePacket(EntityPlayer player, boolean isServer) {
        if (isServer) {
            if (DebugTickHandler.debugPlayers.remove(player))
                DebugTickHandler.debugPlayers.add(player);
        }
    }
}
