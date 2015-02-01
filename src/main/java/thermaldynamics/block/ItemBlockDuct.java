package thermaldynamics.block;

import cofh.core.item.ItemBlockBase;
import cofh.lib.util.helpers.StringHelper;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

import thermaldynamics.ducts.Ducts;

public class ItemBlockDuct extends ItemBlockBase {

	int offset;

	public ItemBlockDuct(Block block) {

		super(block);
		this.offset = ((BlockDuct) block).offset;
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return Ducts.isValid(id(item)) ? "tile.thermaldynamics.duct." + Ducts.getType(id(item)).unlocalizedName + ".name" : super.getUnlocalizedName(item);
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {

		if (Ducts.isValid(id(item))) {
			StringBuilder builder = new StringBuilder();
			Ducts type = Ducts.getType(id(item));
			if (type.pathWeight == 1000) {
				builder.append(StringHelper.localize("tile.thermaldynamics.duct.dense.name")).append(" ");
			} else if (type.pathWeight == -1000) {
				builder.append(StringHelper.localize("tile.thermaldynamics.duct.vacuum.name")).append(" ");
			}
			builder.append(super.getItemStackDisplayName(item));

			if (type.opaque) {
				builder.append(" ").append(StringHelper.localize("tile.thermaldynamics.duct.opaque.name"));
			}

			return builder.toString();
		} else {
			return super.getItemStackDisplayName(item);
		}
	}

	public int id(ItemStack item) {

		return offset + item.getItemDamage();
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		return EnumRarity.uncommon;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean check) {

		if (!Ducts.isValid(id(stack))) {
			return;
		}

		switch (Ducts.getType(id(stack)).ductType) {
		case Energy:
			list.add(StringHelper.localize("info.thermaldynamics.duct.energy"));
			break;
		case Fluid:
			list.add(StringHelper.localize("info.thermaldynamics.duct.fluid"));
			break;
		case Item:
			list.add(StringHelper.localize("info.thermaldynamics.duct.item"));
			break;
		case Structural:
			list.add(StringHelper.localize("info.thermaldynamics.duct.structure"));
			break;
		default:
		}

		// if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
		// list.add(StringHelper.shiftForDetails());
		// }
		// if (!StringHelper.isShiftKeyDown()) {
		// return;
		// }

		// list.add(StringHelper.localize("tile.thermaldynamics.duct." + Ducts.getType(id(stack)).unlocalizedName + ".info"));
	}
}
