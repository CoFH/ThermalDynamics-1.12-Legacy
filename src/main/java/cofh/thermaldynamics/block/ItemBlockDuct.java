package cofh.thermaldynamics.block;

import cofh.core.block.ItemBlockCore;
import cofh.core.util.helpers.ItemHelper;
import cofh.core.util.helpers.StringHelper;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.GridEnergy;
import cofh.thermaldynamics.duct.tiles.TileDuctFluid;
import cofh.thermaldynamics.duct.tiles.TileDuctItem;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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
				if (StringHelper.canLocalize(unloc + ".opaque.name")) {
					unloc += ".opaque";
				} else {
					opaqueLocalized = "tile.thermaldynamics.duct.opaque.name";
				}
			}
			/* Dense / Vacuum */
			if (type instanceof DuctItem && item.getTagCompound() != null) {
				if (item.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT) == DuctItem.PATHWEIGHT_DENSE) {
					if (StringHelper.canLocalize(unloc + ".dense.name")) {
						unloc += ".dense";
					} else {
						modeLocalized = "tile.thermaldynamics.duct.dense.name";
					}
				} else if (item.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT) == DuctItem.PATHWEIGHT_VACUUM) {
					if (StringHelper.canLocalize(unloc + ".vacuum.name")) {
						unloc += ".vacuum";
					} else {
						modeLocalized = "tile.thermaldynamics.duct.vacuum.name";
					}
				}
			}
			String ret = StringHelper.localize(unloc + ".name");

			if (opaqueLocalized != null) {
				ret = StringHelper.localizeFormat(opaqueLocalized, ret);
			}
			if (modeLocalized != null) {
				ret = StringHelper.localizeFormat(modeLocalized, ret);
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
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		super.addInformation(stack, worldIn, tooltip, flagIn);

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			tooltip.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			if (ItemHelper.itemsIdentical(stack, TDDucts.structure.itemStack)) {
				tooltip.add(StringHelper.getInfoText("info.thermaldynamics.duct.cover"));
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
				tooltip.add(StringHelper.localize("info.thermaldynamics.duct.energy"));

				if (duct != TDDucts.energySuperCond) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.YELLOW + GridEnergy.NODE_TRANSFER[duct.type] + StringHelper.LIGHT_GRAY + " RF/t.");
				} else {
					tooltip.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.BRIGHT_BLUE + StringHelper.localize("info.cofh.infinite") + StringHelper.LIGHT_GRAY + " RF/t.");
					tooltip.add(StringHelper.getInfoText("tile.thermaldynamics.duct.energySuper.info"));
				}
				tooltip.add(StringHelper.getNoticeText("info.thermaldynamics.transferConnection"));
				break;
			case FLUID:
				if (duct.type == 0) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.fluid"));
					tooltip.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidBasic.info"));
				} else if (duct.type == 1) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.fluid"));
					tooltip.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidHardened.info"));
				} else if (duct.type == 2) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.fluidEnergy"));
					tooltip.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.YELLOW + TileDuctFluid.NODE_TRANSFER + StringHelper.LIGHT_GRAY + " RF/t.");
					tooltip.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidHardened.info"));
				} else if (duct.type == 3) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.fluid"));
					tooltip.add(StringHelper.getInfoText("tile.thermaldynamics.duct.fluidSuper.info"));
				}
				if (duct.type != 3) {
					tooltip.add(StringHelper.getNoticeText("info.thermaldynamics.transferFluid"));
				}
				break;
			case ITEM:
				if (duct.type == 0) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.item"));
				} else if (duct.type == 1) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.item"));
					tooltip.add(StringHelper.getInfoText("tile.thermaldynamics.duct.itemFast.info"));
				} else if (duct.type == 2) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.itemEnergy"));
					tooltip.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.YELLOW + TileDuctItem.NODE_TRANSFER + StringHelper.LIGHT_GRAY + " RF/t.");
				} else if (duct.type == 3) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.itemEnergy"));
					tooltip.add(StringHelper.localize("info.thermaldynamics.transfer") + ": " + StringHelper.YELLOW + TileDuctItem.NODE_TRANSFER + StringHelper.LIGHT_GRAY + " RF/t.");
					tooltip.add(StringHelper.getInfoText("tile.thermaldynamics.duct.itemFast.info"));
				}
				if (stack.hasTagCompound()) {
					byte pathWeight = stack.getTagCompound().getByte(DuctItem.PATHWEIGHT_NBT);
					if (pathWeight == DuctItem.PATHWEIGHT_DENSE) {
						tooltip.add(StringHelper.getInfoText("info.thermaldynamics.duct.dense"));
					} else if (pathWeight == DuctItem.PATHWEIGHT_VACUUM) {
						tooltip.add(StringHelper.getInfoText("info.thermaldynamics.duct.vacuum"));
					}
				}
				break;
			case STRUCTURAL:
				if (duct == TDDucts.structure) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.structure"));
					tooltip.add(StringHelper.getInfoText("info.thermaldynamics.duct.cover"));
				} else if (duct == TDDucts.lightDuct) {
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.structure"));
					tooltip.add(StringHelper.localize("info.thermaldynamics.duct.light"));
				}
				break;
			case TRANSPORT:
				tooltip.add(StringHelper.localize("info.thermaldynamics.duct.transport"));

				if (duct == TDDucts.transportLongRange) {
					tooltip.add(StringHelper.getInfoText("info.thermaldynamics.duct.transportLongRange"));
				} else if (duct == TDDucts.transportLinking) {
					tooltip.add(StringHelper.getInfoText("info.thermaldynamics.duct.transportCrossover"));
				}
				break;
			case CRAFTING:
				tooltip.add(StringHelper.localize("info.thermaldynamics.duct.crafting"));
				break;
			default:
		}
	}

}
