package thermaldynamics.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import cofh.core.render.ShaderHelper;
import thermalfoundation.render.shader.ShaderStarfield;

public class RenderTest {
    @SubscribeEvent
    public void renderStart(RenderLivingEvent.Pre event) {
        ShaderHelper.useShader(ShaderStarfield.starfieldShader, null);
    }

    @SubscribeEvent
    public void renderStop(RenderLivingEvent.Post event) {
        ShaderHelper.releaseShader();
    }
}
