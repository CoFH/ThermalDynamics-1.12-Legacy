package thermaldynamics.render;

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
                int time = ARBShaderObjects.glGetUniformLocationARB(shader, "t");
                ARBShaderObjects.glUniform1fARB(time, alpha);
                prevAlpha = alpha;
            }
        }
    }

    public static float prevAlpha = -1;
    public static float alpha = 0;
}
