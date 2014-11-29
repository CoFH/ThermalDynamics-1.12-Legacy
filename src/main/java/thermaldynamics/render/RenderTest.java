package thermaldynamics.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import thermaldynamics.util.ShaderHelper;

public class RenderTest {
    @SubscribeEvent
    public void renderStart(RenderLivingEvent.Pre event) {
        ShaderHelper.useShader(ShaderHelper.testShader, null);
    }

    @SubscribeEvent
    public void renderStop(RenderLivingEvent.Post event) {
        ShaderHelper.releaseShader();
    }
}
