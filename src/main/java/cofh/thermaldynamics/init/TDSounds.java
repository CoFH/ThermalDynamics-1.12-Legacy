package cofh.thermaldynamics.init;

import cofh.thermaldynamics.ThermalDynamics;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class TDSounds {

	public static final TDSounds INSTANCE = new TDSounds();

	private TDSounds() {

	}

	public static void preInit() {

		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	/* EVENT HANDLING */
	@SubscribeEvent
	public void registerSounds(RegistryEvent.Register<SoundEvent> event) {

		ductTransportWoosh = registerSoundEvent("duct_transport_woosh");
	}

	private static SoundEvent registerSoundEvent(String id) {

		SoundEvent sound = new SoundEvent(new ResourceLocation(ThermalDynamics.MOD_ID + ":" + id));
		sound.setRegistryName(id);
		ForgeRegistries.SOUND_EVENTS.register(sound);
		return sound;
	}

	public static SoundEvent ductTransportWoosh;

}
