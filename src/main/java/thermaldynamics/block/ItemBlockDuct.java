package thermaldynamics.block;

import cofh.core.item.ItemBlockBase;
import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import thermaldynamics.ducts.Ducts;

public class ItemBlockDuct extends ItemBlockBase {

    public ItemBlockDuct(Block block) {

        super(block);
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        return Ducts.isValid(item.getItemDamage()) ? "tile.thermalducts.duct." + Ducts.getType(item.getItemDamage()).unlocalizedName + ".name" : super.getUnlocalizedName(item);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.uncommon;
    }

}
