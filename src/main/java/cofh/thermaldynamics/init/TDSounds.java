package cofh.thermaldynamics.init;

import cofh.thermaldynamics.ThermalDynamics;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class TDSounds {

	private TDSounds() {

	}

	public static void initialize() {

		DUCT_TRANSPORT_WOOSH = getRegisteredSoundEvent("duct_transport_woosh");
	}

	private static SoundEvent getRegisteredSoundEvent(String id) {

		SoundEvent sound = new SoundEvent(new ResourceLocation(ThermalDynamics.MOD_ID + ":" + id));
		sound.setRegistryName(id);
		ForgeRegistries.SOUND_EVENTS.register(sound);
		return sound;
	}

	public static SoundEvent DUCT_TRANSPORT_WOOSH;

}
