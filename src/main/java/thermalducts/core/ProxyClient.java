package thermalducts.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;

import thermalfoundation.core.Proxy;

public class ProxyClient extends Proxy {

	@Override
	public void registerRenderInformation() {

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
