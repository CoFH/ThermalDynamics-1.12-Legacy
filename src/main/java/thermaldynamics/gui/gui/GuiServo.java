package thermaldynamics.gui.gui;

import cofh.core.gui.GuiBaseAdv;
import cofh.core.gui.element.TabRedstone;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import thermaldynamics.ducts.servo.ServoBase;
import thermaldynamics.gui.containers.ContainerServo;

public class GuiServo extends GuiBaseAdv {
    InventoryPlayer inventory;
    ServoBase servoBase;
    static ResourceLocation texture = new ResourceLocation("thermaldynamics:textures/gui/guiServo.png");

    public GuiServo(InventoryPlayer inventory, ServoBase servoBase) {
        super(new ContainerServo(inventory, servoBase), texture);
        this.servoBase = servoBase;
        this.inventory = inventory;
        this.ySize = 204;
    }

    @Override
    public void initGui() {
        super.initGui();
        addTab(new TabRedstone(this, servoBase));
    }
}
