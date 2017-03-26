package cofh.thermaldynamics.item;

import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.attachments.servo.ServoBase;
import cofh.thermaldynamics.duct.attachments.servo.ServoFluid;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

public class ItemServo extends ItemAttachment {

	public static EnumRarity[] rarity = { EnumRarity.COMMON, EnumRarity.COMMON, EnumRarity.UNCOMMON, EnumRarity.UNCOMMON, EnumRarity.RARE };
	public static ItemStack basicServo, hardenedServo, reinforcedServo, signalumServo, resonantServo;

	public ItemServo() {

		super();
		this.setUnlocalizedName("thermaldynamics.servo");
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return super.getUnlocalizedName(item) + "." + item.getItemDamage();
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {

		for (int i = 0; i < 5; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		return rarity[stack.getItemDamage() % 5];
	}

	@Override
	public Attachment getAttachment(EnumFacing side, ItemStack stack, TileDuctBase tile) {

		int type = stack.getItemDamage() % 5;
		if (tile instanceof TileFluidDuct) {
			return new ServoFluid(tile, (byte) (side.ordinal() ^ 1), type);
		}
		if (tile instanceof TileItemDuct) {
			return new ServoItem(tile, (byte) (side.ordinal() ^ 1), type);
		}
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean extraInfo) {

		super.addInformation(stack, player, list, extraInfo);

		int type = stack.getItemDamage() % 5;

		if (!StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.getInfoText("item.thermaldynamics.servo.info"));

			if (StringHelper.displayShiftForDetail) {
				list.add(StringHelper.shiftForDetails());
			}
			return;
		}
		if (ServoBase.canAlterRS(type)) {
			list.add(StringHelper.localize("info.thermaldynamics.servo.redstoneInt"));
		} else {
			list.add(StringHelper.localize("info.thermaldynamics.servo.redstoneExt"));
		}
		list.add(StringHelper.YELLOW + StringHelper.localize("info.cofh.items") + StringHelper.END);

		list.add("  " + StringHelper.localize("info.thermaldynamics.servo.extractRate") + ": " + StringHelper.WHITE + ((ServoItem.tickDelays[type] % 20) == 0 ? Integer.toString(ServoItem.tickDelays[type] / 20) : Float.toString(ServoItem.tickDelays[type] / 20F)) + "s" + StringHelper.END);
		list.add("  " + StringHelper.localize("info.thermaldynamics.servo.maxStackSize") + ": " + StringHelper.WHITE + ServoItem.maxSize[type] + StringHelper.END);
		addFiltering(list, type, Duct.Type.ITEM);

		if (ServoItem.multiStack[type]) {
			list.add("  " + StringHelper.localize("info.thermaldynamics.servo.slotMulti"));
		} else {
			list.add("  " + StringHelper.localize("info.thermaldynamics.servo.slotSingle"));
		}
		if (ServoItem.speedBoost[type] != 1) {
			list.add("  " + StringHelper.localize("info.thermaldynamics.servo.speedBoost") + ": " + StringHelper.WHITE + ServoItem.speedBoost[type] + "x " + StringHelper.END);
		}
		list.add(StringHelper.YELLOW + StringHelper.localize("info.cofh.fluids") + StringHelper.END);
		list.add("  " + StringHelper.localize("info.thermaldynamics.servo.extractRate") + ": " + StringHelper.WHITE + Integer.toString((int) (ServoFluid.throttle[type] * 100)) + "%" + StringHelper.END);
		addFiltering(list, type, Duct.Type.FLUID);
	}

	public static void addFiltering(List<String> list, int type, Duct.Type duct) {

		StringBuilder b = new StringBuilder();

		b.append(StringHelper.localize("info.thermaldynamics.filter.options")).append(": ").append(StringHelper.WHITE);
		boolean flag = false;
		for (int i = 0; i < FilterLogic.flagTypes.length; i++) {
			if (FilterLogic.canAlterFlag(duct, type, i)) {
				if (flag) {
					b.append(", ");
				} else {
					flag = true;
				}
				b.append(StringHelper.localize("info.thermaldynamics.filter." + FilterLogic.flagTypes[i]));
			}
		}
		flag = false;
		for (String s : Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(b.toString(), 140)) {
			if (flag) {
				s = "  " + StringHelper.WHITE + s;
			}
			flag = true;
			list.add("  " + s + StringHelper.END);
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.register(this.setRegistryName("servo"));

		basicServo = new ItemStack(this, 1, 0);
		hardenedServo = new ItemStack(this, 1, 1);
		reinforcedServo = new ItemStack(this, 1, 2);
		signalumServo = new ItemStack(this, 1, 3);
		resonantServo = new ItemStack(this, 1, 4);

		ServoBase.initialize();

		ThermalDynamics.proxy.addIModelRegister(this);
		return true;
	}

	@Override
	public void registerModels() {

		String[] names = { "basic", "hardened", "reinforced", "signalum", "resonant" };
		for (int i = 0; i < names.length; i++) {
			ModelResourceLocation location = new ModelResourceLocation("thermaldynamics:attachment", "type=" + this.getRegistryName().getResourcePath() + "_" + names[i]);
			ModelLoader.setCustomModelResourceLocation(this, i, location);
		}
	}
}
