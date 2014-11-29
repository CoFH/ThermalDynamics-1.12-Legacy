package thermaldynamics.block;

import thermaldynamics.ducts.facades.Facade;
import thermaldynamics.ducts.servo.ServoFluid;
import thermaldynamics.ducts.servo.ServoItem;

import java.util.HashMap;

public class AttachmentRegistry {
    public static HashMap<String, Class<? extends Attachment>> nameToAttachment = new HashMap<String, Class<? extends Attachment>>();
    public static HashMap<Class<? extends Attachment>, String> attachmentToName = new HashMap<Class<? extends Attachment>, String>();
    public final static byte FACADE = 0;
    public final static byte SERVO_FLUID = 1;
    public final static byte SERVO_INV = 2;
    public final static byte FILTER_FLUID = 3;
    public final static byte FILTER_INV = 4;


    public static Attachment createAttachment(TileMultiBlock tile, byte side, int id) {
        if (id == FACADE) {
            return new Facade(tile, side);
        } else if (id == SERVO_FLUID) {
            return new ServoFluid(tile, side);
        } else if (id == SERVO_INV) {
            return new ServoItem(tile, side);
        }

        return null;
    }
}
