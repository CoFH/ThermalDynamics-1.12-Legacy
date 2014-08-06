package thermaldynamics.block;

import cofh.core.item.ItemBlockBase;

import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class ItemBlockDuct extends ItemBlockBase {

	public ItemBlockDuct(Block block) {

		super(block);
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return "tile.thermalducts.duct." + BlockDuct.NAMES[item.getItemDamage()] + ".name";
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		return EnumRarity.uncommon;
	}

}
