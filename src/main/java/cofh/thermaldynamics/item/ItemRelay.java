package cofh.thermaldynamics.item;

import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

public class ItemRelay extends ItemAttachment {

	public ItemRelay() {

		super();
		this.setUnlocalizedName("thermaldynamics.relay");
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {

	}

	@Override
	public Attachment getAttachment(EnumFacing side, ItemStack stack, TileGrid tile) {

		return new Relay(tile, (byte) (side.ordinal() ^ 1));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean extraInfo) {

		super.addInformation(stack, player, list, extraInfo);
		list.add(StringHelper.getInfoText("item.thermaldynamics.relay.info"));
		list.add(StringHelper.getNoticeText("info.thermaldynamics.toggle"));
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.register(this.setRegistryName("relay"));

		ThermalDynamics.proxy.addIModelRegister(this);

		return true;
	}

	@Override
	public boolean initialize() {

		return true;
	}

	@Override
	public void registerModels() {

		ModelResourceLocation location = new ModelResourceLocation("thermaldynamics:attachment", "type=relay");
		ModelLoader.setCustomModelResourceLocation(this, 0, location);
	}

}
