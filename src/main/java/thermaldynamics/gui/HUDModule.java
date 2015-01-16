package thermaldynamics.gui;


import cofh.core.ProxyClient;
import cofh.hud.CoFHHUD;
import cofh.hud.HUDHelper;
import cofh.hud.IHUDModule;
import cofh.lib.render.RenderHelper;
import cofh.lib.util.TimeTracker;
import cofh.lib.util.helpers.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class HUDModule implements IHUDModule {

    public static void initialize() {

//        if (TEProps.enableFluidModule) {
        CoFHHUD.registerHUDModule(instance);
//        }
    }

    public static final HUDModule instance = new HUDModule();
    public static int moduleID;

    public static final int UPDATE_DELAY = 64;

    public TimeTracker myTimeTracker = new TimeTracker();
    int blockX;
    int blockY;
    int blockZ;

    int widthModifier = 0;

    public FluidStack fluidToRender;

    @Override
    public void renderHUD(Minecraft mc, int scaledHeight, int scaledWidth) {

        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            TileEntity theTile = mc.theWorld.getTileEntity(mc.objectMouseOver.blockX, mc.objectMouseOver.blockY, mc.objectMouseOver.blockZ);
            if (theTile != null) {

                fluidToRender = new FluidStack(FluidRegistry.WATER, 10);
                doRender(false, scaledHeight, scaledWidth);
                return;

            }
        } else {
            blockX = blockY = blockZ = 0;
        }
        if (widthModifier > 0) {
            doRender(true, scaledHeight, scaledWidth);
        } else {
            widthModifier = 0;
        }
    }

    public void doRender(boolean reverseAnimation, int scaledHeight, int scaledWidth) {

        if (fluidToRender == null || fluidToRender.getFluid() == null) {
            widthModifier -= 5;
            return;
        }
        int width = 0;
        int absWidth = 0;

        width = ProxyClient.fontRenderer.getStringWidth(fluidToRender.getFluid().getLocalizedName(fluidToRender));

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        width += 28 + 7;
        absWidth = width;
        if (reverseAnimation) {
            widthModifier -= 5;
            width = widthModifier;
        } else {
            widthModifier += 5;
            if (widthModifier < width) {
                width = widthModifier;
            } else {
                widthModifier = width;
            }
        }
        int height = 40;

        HUDHelper.drawTooltipBoxOnSide(width, height, scaledHeight, scaledWidth, ForgeDirection.EAST);

        HUDHelper.drawTooltipString(scaledWidth - width + 2
                        + ((absWidth - ProxyClient.fontRenderer.getStringWidth(StringHelper.localize("info.cofh.fluid"))) / 2), scaledHeight / 2 - height / 2 + 6,
                0xFFFFFFFF, StringHelper.localize("info.cofh.fluid")
        );


        RenderHelper.setDefaultFontTextureSheet();
        HUDHelper.drawTooltipStringOnSide(width - 2, height, scaledHeight, scaledWidth, ForgeDirection.EAST, 1, fluidToRender.getFluid().getLocalizedName());

        RenderHelper.setBlockTextureSheet();
        RenderHelper.setColor3ub(fluidToRender.getFluid().getColor(fluidToRender));
        HUDHelper.drawFixedIconOnSide(fluidToRender.getFluid().getIcon(fluidToRender), width, height, scaledHeight, scaledWidth, ForgeDirection.EAST, 1);

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
