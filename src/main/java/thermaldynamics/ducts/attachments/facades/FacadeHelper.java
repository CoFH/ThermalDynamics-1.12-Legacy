package thermaldynamics.ducts.attachments.facades;

import net.minecraft.block.Block;

public class FacadeHelper {


    public static boolean isValid(Block block, int meta){
        if(block.hasTileEntity(meta) || block.hasTileEntity())
            return false;

        if(block.isOpaqueCube())
            return true;

        return true;
    }

}
