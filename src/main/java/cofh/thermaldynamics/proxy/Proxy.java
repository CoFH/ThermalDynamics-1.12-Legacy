package cofh.thermaldynamics.proxy;

import cofh.core.render.IModelRegister;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Proxy {

	/* INIT */
	public void preInit(FMLPreInitializationEvent event) {

		// MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);
	}

	public void initialize(FMLInitializationEvent event) {

	}

	public void postInit(FMLPostInitializationEvent event) {

	}

	/* HELPERS */
	public boolean addIModelRegister(IModelRegister register) {

		return false;
	}

}
