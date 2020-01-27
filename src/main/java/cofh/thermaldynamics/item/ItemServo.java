package cofh.thermaldynamics.item;

import cofh.core.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.attachments.servo.ServoBase;
import cofh.thermaldynamics.duct.attachments.servo.ServoFluid;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.helpers.ItemHelper.cloneStack;
import static cofh.core.util.helpers.RecipeHelper.addShapedRecipe;
import static cofh.core.util.helpers.RecipeHelper.addShapelessRecipe;

public class ItemServo extends ItemAttachment {

	public static EnumRarity[] rarity = { EnumRarity.COMMON, EnumRarity.COMMON, EnumRarity.UNCOMMON, EnumRarity.UNCOMMON, EnumRarity.RARE };
	public static ItemStack servoBasic, servoHardened, servoReinforced, servoSignalum, servoResonant;

	public ItemServo() {

		super();
		this.setUnlocalizedName("thermaldynamics.servo");
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return super.getUnlocalizedName(item) + "." + item.getItemDamage();
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {

		if (isInCreativeTab(tab)) {
			for (int i = 0; i < 5; i++) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		return rarity[stack.getItemDamage() % 5];
	}

	@Override
	public Attachment getAttachment(EnumFacing side, ItemStack stack, TileGrid tile) {

		int type = stack.getItemDamage() % 5;
		if (tile.getDuct(DuctToken.FLUID) != null) {
			return new ServoFluid(tile, (byte) (side.ordinal() ^ 1), type);
		}
		if (tile.getDuct(DuctToken.ITEMS) != null) {
			return new ServoItem(tile, (byte) (side.ordinal() ^ 1), type);
		}
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		super.addInformation(stack, worldIn, tooltip, flagIn);

		int type = stack.getItemDamage() % 5;

		if (!StringHelper.isShiftKeyDown()) {
			tooltip.add(StringHelper.getInfoText("item.thermaldynamics.servo.info"));

			if (StringHelper.displayShiftForDetail) {
				tooltip.add(StringHelper.shiftForDetails());
			}
			return;
		}
		if (ServoBase.canAlterRS(type)) {
			tooltip.add(StringHelper.localize("info.thermaldynamics.servo.redstoneInt"));
		} else {
			tooltip.add(StringHelper.localize("info.thermaldynamics.servo.redstoneExt"));
		}
		tooltip.add(StringHelper.YELLOW + StringHelper.localize("info.cofh.items") + StringHelper.END);

		tooltip.add("  " + StringHelper.localize("info.thermaldynamics.servo.extractRate") + ": " + StringHelper.WHITE + ((ServoItem.tickDelays[type] % 20) == 0 ? Integer.toString(ServoItem.tickDelays[type] / 20) : Float.toString(ServoItem.tickDelays[type] / 20F)) + "s" + StringHelper.END);
		tooltip.add("  " + StringHelper.localize("info.thermaldynamics.servo.maxStackSize") + ": " + StringHelper.WHITE + ServoItem.maxSize[type] + StringHelper.END);
		addFiltering(tooltip, type, Duct.Type.ITEM);

		if (ServoItem.multiStack[type]) {
			tooltip.add("  " + StringHelper.localize("info.thermaldynamics.servo.slotMulti"));
		} else {
			tooltip.add("  " + StringHelper.localize("info.thermaldynamics.servo.slotSingle"));
		}
		if (ServoItem.speedBoost[type] != 1) {
			tooltip.add("  " + StringHelper.localize("info.thermaldynamics.servo.speedBoost") + ": " + StringHelper.WHITE + ServoItem.speedBoost[type] + "x " + StringHelper.END);
		}
		tooltip.add(StringHelper.YELLOW + StringHelper.localize("info.cofh.fluids") + StringHelper.END);
		tooltip.add("  " + StringHelper.localize("info.thermaldynamics.servo.extractRate") + ": " + StringHelper.WHITE + Integer.toString((int) (ServoFluid.throttle[type] * 100)) + "%" + StringHelper.END);
		addFiltering(tooltip, type, Duct.Type.FLUID);
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
		for (String s : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(b.toString(), 140)) {
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

		ForgeRegistries.ITEMS.register(setRegistryName("servo"));
		ThermalDynamics.proxy.addIModelRegister(this);

		servoBasic = new ItemStack(this, 1, 0);
		servoHardened = new ItemStack(this, 1, 1);
		servoReinforced = new ItemStack(this, 1, 2);
		servoSignalum = new ItemStack(this, 1, 3);
		servoResonant = new ItemStack(this, 1, 4);

		ServoBase.initialize();

		return true;
	}

	@Override
	public boolean initialize() {

		// @formatter:off
		addShapedRecipe(cloneStack(servoBasic, 2),
				"iGi",
				"IRI",
				'i', "nuggetIron",
				'G', "blockGlass",
				'I', "ingotIron",
				'R', "dustRedstone"
		);

		addShapedRecipe(cloneStack(servoHardened, 2),
				"iGi",
				"IRI",
				'i', "nuggetIron",
				'G', "blockGlass",
				'I', "ingotInvar",
				'R', "dustRedstone"
		);
		addShapelessRecipe(servoHardened, servoBasic, "ingotInvar");

		addShapedRecipe(cloneStack(servoReinforced, 2),
				"iGi",
				"IRI",
				'i', "nuggetIron",
				'G', "blockGlass",
				'I', "ingotElectrum",
				'R', "dustRedstone"
		);
		addShapelessRecipe(servoReinforced, servoBasic, "ingotElectrum");
		addShapelessRecipe(servoReinforced, servoHardened, "ingotElectrum");

		addShapedRecipe(cloneStack(servoSignalum, 2),
				"iGi",
				"IRI",
				'i', "nuggetIron",
				'G', "blockGlass",
				'I', "ingotSignalum",
				'R', "dustRedstone"
		);
		addShapelessRecipe(servoSignalum, servoBasic, "ingotSignalum");
		addShapelessRecipe(servoSignalum, servoHardened, "ingotSignalum");
		addShapelessRecipe(servoSignalum, servoReinforced, "ingotSignalum");

		addShapedRecipe(cloneStack(servoResonant, 2),
				"iGi",
				"IRI",
				'i', "nuggetIron",
				'G', "blockGlass",
				'I', "ingotEnderium",
				'R', "dustRedstone"
		);
		addShapelessRecipe(servoResonant, servoBasic, "ingotEnderium");
		addShapelessRecipe(servoResonant, servoHardened, "ingotEnderium");
		addShapelessRecipe(servoResonant, servoReinforced, "ingotEnderium");
		addShapelessRecipe(servoResonant, servoSignalum, "ingotEnderium");

		// @formatter:on
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
