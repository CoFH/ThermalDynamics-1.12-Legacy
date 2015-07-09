package cofh.thermaldynamics.core;

import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.debughelper.CommandServerDebug;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.item.ItemCover;
import cofh.thermaldynamics.render.ItemCoverRenderer;
import cofh.thermaldynamics.render.RenderDuct;
import cofh.thermaldynamics.render.RenderDuctFluids;
import cofh.thermaldynamics.render.RenderDuctItems;
import cofh.thermaldynamics.render.RenderDuctItemsEnder;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.item.Item;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;

public class ProxyClient extends Proxy {

	@Override
	public void registerRenderInformation() {

		FMLCommonHandler.instance().bus().register(TickHandlerClient.INSTANCE);

		for (BlockDuct duct : ThermalDynamics.blockDuct) {
			MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(duct), RenderDuct.instance);
		}
		MinecraftForgeClient.registerItemRenderer(ThermalDynamics.itemCover, ItemCoverRenderer.instance);

		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuctEnder.class, RenderDuctItemsEnder.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemDuct.class, RenderDuctItems.instance);
		ClientRegistry.bindTileEntitySpecialRenderer(TileFluidDuct.class, RenderDuctFluids.instance);

		ClientCommandHandler.instance.registerCommand(new CommandServerDebug());

		String comment = "This value affects the size of the inner duct model, such as fluids. Lower it if you experience texture z-fighting.";
		RenderDuct.smallInnerModelScaling = MathHelper.clampF((float) ThermalDynamics.configClient.get("Render", "InnerModelScaling", 0.99, comment), 0.50F,
				0.99F);

		comment = "This value affects the size of the inner duct model, such as fluids, on the large (octagonal) ducts. Lower it if you experience texture z-fighting.";
		RenderDuct.largeInnerModelScaling = MathHelper.clampF((float) ThermalDynamics.configClient.get("Render", "LargeInnerModelScaling", 0.99, comment),
				0.50F, 0.99F);

		ItemCover.enableCreativeTab = ThermalDynamics.configClient.get("Interface.CreativeTab", "Covers.Enable", ItemCover.enableCreativeTab);
		ItemCover.showInNEI = ThermalDynamics.configClient.get("Plugins.NEI", "Covers.Show", ItemCover.showInNEI, "Set to TRUE to show Covers in NEI.");
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerIcons(TextureStitchEvent.Pre event) {

	}

	@Override
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void initializeIcons(TextureStitchEvent.Post event) {

		RenderDuct.initialize();
	}

}
