package cofh.thermaldynamics.item;

import cofh.core.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.init.TDItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

import static cofh.core.util.helpers.RecipeHelper.addShapedRecipe;

public class ItemRelay extends ItemAttachment {

	public ItemRelay() {

		super();
		this.setUnlocalizedName("thermaldynamics.relay");
	}

	@Override
	public Attachment getAttachment(EnumFacing side, ItemStack stack, TileGrid tile) {

		return new Relay(tile, (byte) (side.ordinal() ^ 1));
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		super.addInformation(stack, worldIn, tooltip, flagIn);

		tooltip.add(StringHelper.getInfoText("item.thermaldynamics.relay.info"));
		tooltip.add(StringHelper.getNoticeText("info.thermaldynamics.toggle"));
	}

	/* IInitializer */
	@Override
	public boolean initialize() {

		ForgeRegistries.ITEMS.register(setRegistryName("relay"));

		ThermalDynamics.proxy.addIModelRegister(this);

		return true;
	}

	@Override
	public boolean register() {

		// @formatter:off

		addShapedRecipe(new ItemStack(TDItems.itemRelay, 2),
				"iGi",
				"IRI",
				'i', "nuggetSignalum",
				'G', "gemQuartz",
				'I', "ingotLead",
				'R', "dustRedstone"
		);

		// @formatter:on

		return true;
	}

	@Override
	public void registerModels() {

		ModelResourceLocation location = new ModelResourceLocation("thermaldynamics:attachment", "type=relay");
		ModelLoader.setCustomModelResourceLocation(this, 0, location);
	}

}
