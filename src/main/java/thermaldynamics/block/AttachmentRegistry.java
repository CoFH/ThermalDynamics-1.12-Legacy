package thermaldynamics.block;

import com.google.common.collect.HashBiMap;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.ducts.facades.Facade;

import java.util.HashMap;

public class AttachmentRegistry {
    public static HashMap<String, Class<? extends Attachment>> nameToAttachment = new HashMap<String, Class<? extends Attachment>>();
    public static HashMap<Class<? extends Attachment>, String> attachmentToName = new HashMap<Class<? extends Attachment>, String>();


    public static Attachment createAttachment(TileMultiBlock tile, byte side, int id) {
        if(id == 0){
            return new Facade(tile,side);
        }

        return null;
    }
}
