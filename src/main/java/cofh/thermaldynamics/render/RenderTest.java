package cofh.thermaldynamics.render;

import cofh.core.render.ShaderHelper;
import cofh.thermalfoundation.render.shader.ShaderStarfield;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import net.minecraftforge.client.event.RenderLivingEvent;

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
