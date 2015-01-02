package thermaldynamics.gui;

import cofh.core.ProxyClient;
import cofh.core.render.CoFHFontRender;
import cofh.hud.CoFHHUD;
import cofh.hud.HUDHelper;
import cofh.hud.IHUDModule;
import cofh.lib.render.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import thermaldynamics.debughelper.DebugTickHandler;

import java.util.ArrayList;
import java.util.List;

public class DebugHUDModule implements IHUDModule {


    public static int[] displayValues;

    static boolean init = false;

    public static void initialize() {
        if (!init) {
            CoFHHUD.registerHUDModule(instance);
            init = true;
        }
    }

    public static final DebugHUDModule instance = new DebugHUDModule();
    public static int moduleID;

    public List<String> toPrint() {

        ArrayList<String> strings = new ArrayList<String>();
        if (displayValues == null) return strings;
        DebugTickHandler.DebugEvent[] values = DebugTickHandler.DebugEvent.values();
        for (int i = 0; i < values.length; i++) {
            if (displayValues[i] != 0)
                strings.add(values[i].toString() + " " + displayValues[i]);
        }
        return strings;
    }

    @Override
    public void renderHUD(Minecraft mc, int scaledHeight, int scaledWidth) {
        List<String> strings = toPrint();
        if (strings.isEmpty()) return;
        CoFHFontRender fontRenderer = ProxyClient.fontRenderer;
        int width = 0;
        int height = 40;
        for (String s : strings) {
            width = Math.max(width, fontRenderer.getStringWidth(s));
            height += fontRenderer.FONT_HEIGHT;
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        width += 35;

        HUDHelper.drawTooltipBoxOnSide(width, height, scaledHeight, scaledWidth, ForgeDirection.EAST);

        RenderHelper.setDefaultFontTextureSheet();

        for (int i = 0; i < strings.size(); i++) {
            String s = strings.get(i);
            HUDHelper.drawTooltipStringOnSide(width, height, scaledHeight, scaledWidth, ForgeDirection.EAST, i, s);

        }

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }

    @Override
    public void setModuleID(int i) {
        moduleID = i;
    }

    @Override
    public void clientTick(Minecraft mc) {

    }
}
