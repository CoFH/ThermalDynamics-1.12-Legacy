package thermaldynamics.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.ARBShaderObjects;
import thermaldynamics.util.ShaderHelper;

public class RenderStarfield {
    public static final ResourceLocation starsTexture = new ResourceLocation("textures/entity/end_portal.png");

    public static EnderCallBack callback = new EnderCallBack();

    public static class EnderCallBack extends ShaderHelper.ShaderCallback {
        @Override
        public void call(int shader, boolean newFrame) {


            if (alpha != prevAlpha) {
                int alpha = ARBShaderObjects.glGetUniformLocationARB(shader, "alpha");
                ARBShaderObjects.glUniform1fARB(alpha, RenderStarfield.alpha);
                prevAlpha = RenderStarfield.alpha;
            }

            Minecraft mc = Minecraft.getMinecraft();

            int x = ARBShaderObjects.glGetUniformLocationARB(shader, "yaw");
            ARBShaderObjects.glUniform1fARB(x, (float) ((mc.thePlayer.rotationYaw * 2 * Math.PI) / 360.0));

            int z = ARBShaderObjects.glGetUniformLocationARB(shader, "pitch");
            ARBShaderObjects.glUniform1fARB(z, -(float) ((mc.thePlayer.rotationPitch * 2 * Math.PI) / 360.0));
        }
    }

    public static float prevAlpha = -1;
    public static float alpha = 0;
}
