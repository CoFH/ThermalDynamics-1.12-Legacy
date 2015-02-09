package cofh.thermaldynamics.block;

import cofh.core.item.ItemBlockBase;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.ducts.Duct;
import cofh.thermaldynamics.ducts.DuctItem;
import cofh.thermaldynamics.ducts.TDDucts;
import cofh.thermaldynamics.ducts.energy.EnergyGrid;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class ItemBlockDuct extends ItemBlockBase {

	int offset;

	public ItemBlockDuct(Block block) {

		super(block);
		this.offset = ((BlockDuct) block).offset;
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return TDDucts.isValid(id(item)) ? "tile.thermaldynamics.duct." + TDDucts.getType(id(item)).unlocalizedName + ".name" : super.getUnlocalizedName(item);
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {

		if (TDDucts.isValid(id(item))) {
			StringBuilder builder = new StringBuilder();
			Duct type = TDDucts.getType(id(item));

			/* Dense / Vacuum */
			if (type instanceof DuctItem && item.stackTagCompound != null) {
				if (item.stackTagCompound.getByte(DuctItem.PATHWEIGHT_NBT) == DuctItem.PATHWEIGHT_DENSE) {
					builder.append(StringHelper.localize("tile.thermaldynamics.duct.dense.name")).append(" ");
				} else if (item.stackTagCompound.getByte(DuctItem.PATHWEIGHT_NBT) == DuctItem.PATHWEIGHT_VACUUM) {
					builder.append(StringHelper.localize("tile.thermaldynamics.duct.vacuum.name")).append(" ");
				}
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

		int ductId = id(stack);

		if (TDDucts.isValid(ductId)) {
			return TDDucts.getType(ductId).rarity;
		}
		return EnumRarity.uncommon;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extraInfo) {

		super.addInformation(stack, player, list, extraInfo);

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		int ductId = id(stack);

		if (!TDDucts.isValid(ductId)) {
			return;
		}
		Duct duct = TDDucts.getType(ductId);
		switch (duct.ductType) {
		case ENERGY:
			list.add(StringHelper.localize("info.thermaldynamics.duct.energy"));

			if (duct != TDDucts.energySuperCond) {
				list.add(StringHelper.localize("info.thermaldynamics.throughput") + ": " + StringHelper.YELLOW + EnergyGrid.NODE_TRANSFER[duct.type]
						+ StringHelper.LIGHT_GRAY + " RF/t.");
			} else {
				list.add(StringHelper.localize("info.thermaldynamics.throughput") + ": " + StringHelper.BRIGHT_BLUE
						+ StringHelper.localize("info.cofh.infinite") + StringHelper.LIGHT_GRAY + " RF/t.");
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.energySupercond.info"));
			}
			break;
		case FLUID:
			list.add(StringHelper.localize("info.thermaldynamics.duct.fluid"));

			if (duct.type == 0) {
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidBasic.info"));
			} else if (duct.type == 1) {
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidHardened.info"));
			}
			break;
		case ITEM:
			list.add(StringHelper.localize("info.thermaldynamics.duct.item"));

			if (duct.type == 0) {
				// list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.itemBasic.info"));
			} else if (duct.type == 1) {
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.itemFast.info"));
			} else if (duct.type == 2) {
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.itemEnder.info"));
			}
			break;
		case STRUCTURAL:
			list.add(StringHelper.localize("info.thermaldynamics.duct.structure"));
			break;
		default:
		}
	}
}
