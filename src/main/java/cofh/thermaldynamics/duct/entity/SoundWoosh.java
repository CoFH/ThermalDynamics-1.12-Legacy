//package cofh.thermaldynamics.duct.entity;
//
//import net.minecraft.client.audio.MovingSound;
//import net.minecraft.util.ResourceLocation;
//
//public class SoundWoosh extends MovingSound {
//
//	private final EntityTransport transport;
//
//	protected SoundWoosh(EntityTransport transport) {
//
//		super(new ResourceLocation("thermaldynamics", "ductsTransportWoosh"));
//		this.transport = transport;
//		this.repeat = true;
//		this.field_147665_h = 0;
//		this.volume = 0.0001F;
//	}
//
//	@Override
//	public void update() {
//
//		if (transport == null || this.transport.isDead) {
//			if (this.volume > 0) {
//				this.volume -= 0.25f;
//			} else {
//				this.donePlaying = true;
//			}
//		} else {
//			if (this.transport.pause > 0) {
//				if (this.volume > 0) {
//					this.volume -= 0.25f;
//				} else {
//					this.volume = 0;
//				}
//			} else {
//				if (this.volume < 0.5) {
//					this.volume += 0.0625f;
//				} else {
//					this.volume = 0.5F;
//				}
//			}
//			this.xPosF = (float) this.transport.posX;
//			this.yPosF = (float) this.transport.posY;
//			this.zPosF = (float) this.transport.posZ;
//		}
//	}
//}
