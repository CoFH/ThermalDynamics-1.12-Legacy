package cofh.thermaldynamics.proxy;

import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.render.block.BlockRenderingRegistry;
import codechicken.lib.vec.Cuboid6;
import cofh.core.render.IModelRegister;
import cofh.core.render.hitbox.CustomHitBox;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.core.render.hitbox.RenderHitbox;
import cofh.core.util.RayTracer;
import cofh.core.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import cofh.thermaldynamics.duct.entity.RenderTransport;
import cofh.thermaldynamics.duct.tiles.TileDuctFluid;
import cofh.thermaldynamics.duct.tiles.TileDuctItem;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.item.ItemAttachment;
import cofh.thermaldynamics.render.RenderDuct;
import cofh.thermaldynamics.render.RenderDuctFluids;
import cofh.thermaldynamics.render.RenderDuctItems;
import cofh.thermaldynamics.render.item.RenderItemCover;
import cofh.thermaldynamics.util.TickHandlerClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ProxyClient extends Proxy {

	public static EnumBlockRenderType renderType;

	/* INIT */
	@Override
	public void preInit(FMLPreInitializationEvent event) {

		super.preInit(event);

		MinecraftForge.EVENT_BUS.register(TickHandlerClient.INSTANCE);

		ModelRegistryHelper.registerItemRenderer(TDItems.itemCover, RenderItemCover.INSTANCE);
		RenderingRegistry.registerEntityRenderingHandler(EntityTransport.class, RenderTransport::new);

		for (IModelRegister register : modelRegisters) {
			register.registerModels();
		}
	}

	@Override
	public void initialize(FMLInitializationEvent event) {

		super.initialize(event);

		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Basic.Transparent.class, RenderDuctItems.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Fast.Transparent.class, RenderDuctItems.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Energy.Transparent.class, RenderDuctItems.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.EnergyFast.Transparent.class, RenderDuctItems.INSTANCE);
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctItem.Warp.Transparent.class, RenderDuctItemsEnder.INSTANCE);
		//		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctOmni.Transparent.class, RenderDuctOmni.INSTANCE);

		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Basic.Transparent.class, RenderDuctFluids.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Super.Transparent.class, RenderDuctFluids.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Hardened.Transparent.class, RenderDuctFluids.INSTANCE);
		ClientRegistry.bindTileEntitySpecialRenderer(TileDuctFluid.Energy.Transparent.class, RenderDuctFluids.INSTANCE);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

		super.postInit(event);

		ProxyClient.renderType = BlockRenderingRegistry.createRenderType("thermaldynamics");
		BlockRenderingRegistry.registerRenderer(ProxyClient.renderType, RenderDuct.INSTANCE);
	}

	/* EVENT HANDLING */
	@SideOnly (Side.CLIENT)
	@SubscribeEvent
	public void handleTextureStitchEventPre(TextureStitchEvent.Pre event) {

		TDTextures.registerTextures(event.getMap());

		for (int i = 0; i < TDDucts.ductList.size(); i++) {
			if (TDDucts.isValid(i)) {
				TDDucts.ductList.get(i).registerIcons(event.getMap());
			}
		}
		TDDucts.structureInvis.registerIcons(event.getMap());
	}

	@SideOnly (Side.CLIENT)
	@SubscribeEvent
	public void handleTextureStitchEventPost(TextureStitchEvent.Post event) {

		RenderDuct.initialize();
	}

	@SideOnly (Side.CLIENT)
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onBlockHighlight(DrawBlockHighlightEvent event) {

		RayTraceResult target = event.getTarget();
		EntityPlayer player = event.getPlayer();
		float partialTicks = event.getPartialTicks();

		if (doAttachmentHighlight(target, player, partialTicks)) {
			event.setCanceled(true);
		} else if (doDuctHighlight(target, player, partialTicks)) {
			event.setCanceled(true);
		}
	}

	private boolean doAttachmentHighlight(RayTraceResult target, EntityPlayer player, float partialTicks) {

		if (!(ItemHelper.getHeldStack(player).getItem() instanceof ItemAttachment) || target.typeOfHit != Type.BLOCK) {
			return false;
		}
		RayTracer.retraceBlock(player.world, player, target.getBlockPos());
		ItemStack stack = ItemHelper.getHeldStack(player);
		Attachment attachment = ItemAttachment.getAttachment(stack, player, player.world, target.getBlockPos(), target.sideHit);

		if (attachment == null || !attachment.canAddToTile(attachment.baseTile)) {
			return false;
		}
		Cuboid6 c = attachment.getCuboid();
		c.max.subtract(c.min);

		RenderHitbox.drawSelectionBox(player, target, partialTicks, new CustomHitBox(c.max.y, c.max.z, c.max.x, attachment.baseTile.x() + c.min.x, attachment.baseTile.y() + c.min.y, attachment.baseTile.z() + c.min.z));
		attachment.drawSelectionExtra(player, target, partialTicks);
		return true;
	}

	private boolean doDuctHighlight(RayTraceResult target, EntityPlayer player, float partialTicks) {

		if (target.typeOfHit != Type.BLOCK) {
			return false;
		}
		RayTracer.retraceBlock(player.world, player, target.getBlockPos());
		TileEntity tile = player.world.getTileEntity(target.getBlockPos());

		if (tile instanceof ICustomHitBox) {
			ICustomHitBox hitbox = (ICustomHitBox) tile;

			if (hitbox.shouldRenderCustomHitBox(target.subHit, player)) {
				RenderHitbox.drawSelectionBox(player, target, partialTicks, hitbox.getCustomHitBox(target.subHit, player));
				return true;
			}
		}
		return false;
	}

	/* HELPERS */
	@Override
	public boolean addIModelRegister(IModelRegister register) {

		return modelRegisters.add(register);
	}

	private static List<IModelRegister> modelRegisters = new ArrayList<>();

}
