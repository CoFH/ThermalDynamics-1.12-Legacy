package cofh.thermaldynamics.duct.attachments.signaller;

import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.item.ItemAttachment;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;

public class ItemSignaler extends ItemAttachment {

    public ItemSignaler() {

        super();
        this.setUnlocalizedName("thermaldynamics.signaller");
        this.setTextureName("thermaldynamics:signaller");
    }


    @Override
    public Attachment getAttachment(int side, ItemStack stack, TileTDBase tile) {

        return new Signaller(tile, (byte) (side^1));
    }

    @Override
    public boolean preInit() {
        GameRegistry.registerItem(this, "signaller");
        return true;
    }
}
