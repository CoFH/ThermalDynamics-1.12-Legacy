package cofh.thermaldynamics.duct;

import cofh.core.item.ItemBlockCore;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.duct.energy.EnergyGrid;
import cofh.thermaldynamics.duct.energy.subgrid.SubTileEnergyRedstone;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.util.List;

public class ItemBlockDuct extends ItemBlockCore {

	int offset;

	public ItemBlockDuct(Block block) {

		super(block);
		this.offset = ((BlockDuct) block).offset;
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return TDDucts.isValid(id(item)) ? "tile.thermaldynamics.duct." + TDDucts.getType(id(item)).unlocalizedName : super.getUnlocalizedName(item);
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {

		if (TDDucts.isValid(id(item))) {
			Duct type = TDDucts.getType(id(item));

			String opaqueLocalized = null, modeLocalized = null;
			String unloc = getUnlocalizedNameInefficiently(item);

			if (type.opaque) {
				if (I18n.canTranslate(unloc + ".opaque.name")) {
					unloc += ".opaque";
				} else {
					opaqueLocalized = "tile.thermaldynamics.duct.opaque.name";
				}
			}
			/* Dense / Vacuum */
			if (type instanceof DuctItem && item.getTagCompound() != null) {
				if (item.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT) == DuctItem.PATHWEIGHT_DENSE) {
					if (I18n.canTranslate(unloc + ".dense.name")) {
						unloc += ".dense";
					} else {
						modeLocalized = "tile.thermaldynamics.duct.dense.name";
					}
				} else if (item.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT) == DuctItem.PATHWEIGHT_VACUUM) {
					if (I18n.canTranslate(unloc + ".vacuum.name")) {
						unloc += ".vacuum";
					} else {
						modeLocalized = "tile.thermaldynamics.duct.vacuum.name";
					}
				}
			}
			String ret = StringHelper.localize(unloc + ".name");

			if (opaqueLocalized != null) {
				ret = I18n.translateToLocalFormatted(opaqueLocalized, ret);
			}
			if (modeLocalized != null) {
				ret = I18n.translateToLocalFormatted(modeLocalized, ret);
			}
			return ret;
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
		return EnumRarity.UNCOMMON;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extraInfo) {

		super.addInformation(stack, player, list, extraInfo);

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			if (ItemHelper.itemsIdentical(stack, TDDucts.structure.itemStack)) {
				list.add(StringHelper.getInfoText("info.thermaldynamics.duct.cover"));
			}
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
				list.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.YELLOW + EnergyGrid.NODE_TRANSFER[duct.type]
						+ StringHelper.LIGHT_GRAY + " RF/t.");
			} else {
				list.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.BRIGHT_BLUE + StringHelper.localize("info.cofh.infinite")
						+ StringHelper.LIGHT_GRAY + " RF/t.");
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.energySupercond.info"));
			}
			list.add(StringHelper.getNoticeText("info.thermaldynamics.transferConnection"));
			break;
		case FLUID:
			list.add(StringHelper.localize("info.thermaldynamics.duct.fluid"));

			if (duct.type != 3) {
				list.add(StringHelper.getNoticeText("info.thermaldynamics.transferFluid"));
			}
			if (duct.type == 0) {
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidBasic.info"));
			} else if (duct.type == 1) {
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidHardened.info"));
			} else if (duct.type == 2) {
				list.add(StringHelper.localize("info.thermaldynamics.duct.energy"));
				list.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.YELLOW + SubTileEnergyRedstone.NODE_TRANSFER
						+ StringHelper.LIGHT_GRAY + " RF/t.");
			} else if (duct.type == 3) {
				list.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidSuper.info"));
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
			} else if (duct.type == 3) {
				list.add(StringHelper.localize("info.thermaldynamics.duct.energy"));
				list.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.YELLOW + SubTileEnergyRedstone.NODE_TRANSFER
						+ StringHelper.LIGHT_GRAY + " RF/t.");
				list.add(StringHelper.getNoticeText("info.thermaldynamics.transferConnection"));
			}
			if (stack.hasTagCompound()) {
				byte pathWeight = stack.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT);
				if (pathWeight == DuctItem.PATHWEIGHT_DENSE) {
					list.add(StringHelper.getInfoText("info.thermaldynamics.duct.dense"));
				} else if (pathWeight == DuctItem.PATHWEIGHT_VACUUM) {
					list.add(StringHelper.getInfoText("info.thermaldynamics.duct.vacuum"));
				}
			}
			break;
		case STRUCTURAL:
			if (duct == TDDucts.structure) {
				list.add(StringHelper.localize("info.thermaldynamics.duct.structure"));
				list.add(StringHelper.getInfoText("info.thermaldynamics.duct.cover"));
			} else if (duct == TDDucts.lightDuct) {
				list.add(StringHelper.localize("info.thermaldynamics.duct.structure"));
				list.add(StringHelper.localize("info.thermaldynamics.duct.light"));
			}
			break;
		case TRANSPORT:
			list.add(StringHelper.localize("info.thermaldynamics.duct.transport"));

			if (duct == TDDucts.transportLongRange) {
				list.add(StringHelper.getInfoText("info.thermaldynamics.duct.transportLongRange"));
			} else if (duct == TDDucts.transportCrossover) {
				list.add(StringHelper.getInfoText("info.thermaldynamics.duct.transportCrossover"));
			}
			break;
		case CRAFTING:
			list.add(StringHelper.localize("info.thermaldynamics.duct.crafting"));
			break;
		default:
		}
	}
}
