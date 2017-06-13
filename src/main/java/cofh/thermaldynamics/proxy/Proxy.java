package cofh.thermaldynamics.proxy;

import cofh.core.render.IModelRegister;
import cofh.thermaldynamics.init.TDSounds;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Proxy {

	/* INIT */
	public void preInit(FMLPreInitializationEvent event) {

	}

	public void initialize(FMLInitializationEvent event) {

		TDSounds.initialize();
	}

	public void postInit(FMLPostInitializationEvent event) {

	}

	/* HELPERS */
	public boolean addIModelRegister(IModelRegister register) {

		return false;
	}

}
