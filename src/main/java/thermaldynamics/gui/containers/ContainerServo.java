package thermaldynamics.gui.containers;

import net.minecraft.entity.player.InventoryPlayer;
import thermaldynamics.block.Attachment;
import thermaldynamics.ducts.servo.ServoBase;

public class ContainerServo extends ContainerAttachmentBase {

    public ContainerServo(InventoryPlayer inventory, ServoBase tile) {
        super(inventory, tile);
    }
}
