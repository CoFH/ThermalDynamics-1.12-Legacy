package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.init.TDSounds;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;

public class SoundWoosh extends MovingSound {

	private final EntityTransport transport;

	// public static final SoundEvent WOOSH = new SoundEvent(new ResourceLocation("thermaldynamics", "duct_transport_woosh")).setRegistryName("thermaldynamics:ducts_transport_woosh");

	protected SoundWoosh(EntityTransport transport) {

		super(TDSounds.DUCT_TRANSPORT_WOOSH, SoundCategory.NEUTRAL);

		this.transport = transport;
		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.0001F;
	}

	@Override
	public void update() {

		if (transport == null || this.transport.isDead) {
			if (this.volume > 0) {
				this.volume -= 0.25f;
			} else {
				this.donePlaying = true;
			}
		} else {
			if (this.transport.pause > 0) {
				if (this.volume > 0) {
					this.volume -= 0.25f;
				} else {
					this.volume = 0;
				}
			} else {
				if (this.volume < 0.5) {
					this.volume += 0.0625f;
				} else {
					this.volume = 0.5F;
				}
			}
			this.xPosF = (float) this.transport.posX;
			this.yPosF = (float) this.transport.posY;
			this.zPosF = (float) this.transport.posZ;
		}
	}

}
