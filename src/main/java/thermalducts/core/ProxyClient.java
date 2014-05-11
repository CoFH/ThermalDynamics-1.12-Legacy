package thermalducts.core;

import cofh.render.ItemRenderRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;

import thermalducts.block.BlockDuct;
import thermalducts.render.RenderDuct;

public class ProxyClient extends Proxy {

	@Override
	public void registerRenderInformation() {

		ItemRenderRegistry.addItemRenderer(BlockDuct.blockDuct, RenderDuct.instance);
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

	}

	public static void registerFluidIcons(Fluid fluid, IIconRegister ir) {

	}

	public static void setFluidIcons(Fluid fluid) {

	}

}
