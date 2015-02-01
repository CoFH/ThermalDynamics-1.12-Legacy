package thermaldynamics.item;

import cofh.lib.util.helpers.StringHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.ducts.attachments.filter.FilterLogic;
import thermaldynamics.ducts.attachments.servo.ServoBase;
import thermaldynamics.ducts.attachments.servo.ServoFluid;
import thermaldynamics.ducts.attachments.servo.ServoItem;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.item.TileItemDuct;

public class ItemServo extends ItemAttachment {

	public final String tab = "  ";

	public ItemServo() {

		super();

		this.setUnlocalizedName("thermaldynamics.servo");
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return super.getUnlocalizedName(item) + "." + item.getItemDamage();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < 5; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) {

		icons = new IIcon[5];
		for (int i = 0; i < 5; i++) {
			icons[i] = ir.registerIcon("thermaldynamics:servo" + i);
		}
		this.itemIcon = icons[0];
	}

	@Override
	public IIcon getIconFromDamage(int i) {

		return icons[i % icons.length];
	}

	@Override
	public Attachment getAttachment(int side, ItemStack stack, TileMultiBlock tile) {

		int type = stack.getItemDamage() % 5;
		if (tile instanceof TileFluidDuct) {
			return new ServoFluid(tile, (byte) (side ^ 1), type);
		}
		if (tile instanceof TileItemDuct) {
			return new ServoItem(tile, (byte) (side ^ 1), type);
		}
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extraInfo) {

		super.addInformation(stack, player, list, extraInfo);

		int type = stack.getItemDamage() % 5;

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		if (ServoBase.canAlterRS(type))
			list.add("Internal Redstone Control");
		else
			list.add("Requires External Redstone Signal");

		list.add(StringHelper.WHITE + "Items" + StringHelper.END);

		list.add(tab
				+ "Send Delay: "
				+ StringHelper.GRAY
				+ ((ServoItem.tickDelays[type] % 20) == 0 ? Integer.toString(ServoItem.tickDelays[type] / 20) : Float
						.toString(ServoItem.tickDelays[type] / 20F)) + " Secs" + StringHelper.END);

		list.add(tab + "Max Stack Size: " + StringHelper.GRAY + ServoItem.maxSize[type] + StringHelper.END);

		list.add(tab + "Max Range: " + StringHelper.GRAY + (ServoItem.range[type] == Integer.MAX_VALUE ? "Infinite" : (ServoItem.range[type] + " Blocks"))
				+ StringHelper.END);

		addFiltering(list, type, tab);

		if (ServoItem.multiStack[type])
			list.add(tab + "Extracts over multiple slots");
		else
			list.add(tab + "Extracts from a single slot");

		if (ServoItem.speedBoost[type] != 1)
			list.add(tab + "Item Speed Boost: " + StringHelper.GRAY + ServoItem.speedBoost[type] + "x " + StringHelper.END);

		list.add(StringHelper.WHITE + "Fluid" + StringHelper.END);
		// String.format(Locale.ENGLISH, "%,d", a)
		list.add(tab + "Throttle Multiplier: " + Integer.toString((int) (ServoFluid.throttle[type] * 100)) + "%");

	}

	public static void addFiltering(List list, int type, String tab) {

		StringBuilder b = new StringBuilder();

		b.append("Filter Options: " + StringHelper.GRAY);
		boolean flag = false;
		for (int i = 0; i < FilterLogic.flagTypes.length; i++) {
			if (FilterLogic.canAlterFlag(Ducts.Type.ITEM, type, i)) {
				if (flag) {
					b.append(", ");
				} else {
					flag = true;
				}
				b.append(StringHelper.localize("info.thermaldynamics.filter." + FilterLogic.flagTypes[i]));
			}
		}
		flag = false;
		for (String s : (List<String>) Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(b.toString(), 140)) {
			if (flag)
				s = tab + StringHelper.GRAY + s;
			flag = true;
			list.add(tab + s + StringHelper.END);
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.registerItem(this, "servo");

		basicServo = new ItemStack(this, 1, 0);
		hardenedServo = new ItemStack(this, 1, 1);
		reinforcedServo = new ItemStack(this, 1, 2);
		signalumServo = new ItemStack(this, 1, 3);
		resonantServo = new ItemStack(this, 1, 4);

		return true;
	}

	IIcon[] icons;

	public static ItemStack basicServo, hardenedServo, reinforcedServo, signalumServo, resonantServo;

}
