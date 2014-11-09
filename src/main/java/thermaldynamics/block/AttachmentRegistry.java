package thermaldynamics.block;

import thermaldynamics.ducts.facades.Facade;
import thermaldynamics.ducts.servo.ServoBase;
import thermaldynamics.ducts.servo.ServoFluid;

import java.util.HashMap;

public class AttachmentRegistry {
    public static HashMap<String, Class<? extends Attachment>> nameToAttachment = new HashMap<String, Class<? extends Attachment>>();
    public static HashMap<Class<? extends Attachment>, String> attachmentToName = new HashMap<Class<? extends Attachment>, String>();


    public static Attachment createAttachment(TileMultiBlock tile, byte side, int id) {
        if (id == 0) {
            return new Facade(tile, side);
        } else if (id == 1) {
            return new ServoFluid(tile, side);
        }

        return null;
    }
}
