package cofh.thermaldynamics.duct.entity;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.ResourceLocation;

public class SoundWoosh extends MovingSound {
    private static final ResourceLocation soundFile = new ResourceLocation("thermaldynamics", "ductsTransportWoosh");
    private final EntityTransport transport;

    protected SoundWoosh(EntityTransport transport) {
        super(new ResourceLocation("thermaldynamics", "ductsTransportWoosh"));
        this.transport = transport;
        this.repeat = true;
        this.field_147665_h = 0;
        this.volume = 0;
    }

    @Override
    public void update() {
        if (this.transport.isDead)
        {
            this.donePlaying = true;
        }
        else
        {
            if(this.volume < 1)
                this.volume += 0.0625f;
            this.xPosF = (float)this.transport.posX;
            this.yPosF = (float)this.transport.posY;
            this.zPosF = (float)this.transport.posZ;
        }
    }
}
