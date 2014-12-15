package thermaldynamics.gui.gui;

import cofh.core.gui.GuiBaseAdv;
import cofh.core.gui.element.TabRedstone;
import cofh.lib.gui.element.ElementButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import thermaldynamics.ducts.attachments.ConnectionBase;
import thermaldynamics.gui.containers.ContainerDuctConnection;

public class GuiDuctConnection extends GuiBaseAdv {
    InventoryPlayer inventory;
    ConnectionBase servoBase;
    public static ResourceLocation TEX = new ResourceLocation("thermaldynamics:textures/gui/guiServo.png");

    ContainerDuctConnection container;
    public ElementButton[] elementButtons;
    public int buttonSize;

    public GuiDuctConnection(InventoryPlayer inventory, ConnectionBase servoBase) {
        super(new ContainerDuctConnection(inventory, servoBase), TEX);
        this.servoBase = servoBase;
        this.inventory = inventory;
        container = (ContainerDuctConnection) inventorySlots;
        name = "Filter";
        this.ySize = 205;
    }

    int[][] buttons = {
            {176, 0},
            {216, 0},
            {176, 60},
            {216, 60},
    };

    @Override
    public void initGui() {
        super.initGui();
        if (servoBase.canAlterRS())
            addTab(new TabRedstone(this, servoBase));

        int flagNo = container.filter.numFlags();
        if (flagNo != 0) {
            elementButtons = new ElementButton[flagNo];
            buttonSize = 20;
            int button_offset = buttonSize + 6;
            int x0 = xSize / 2 - flagNo * (button_offset / 2);
            int y0 = container.gridY0 + container.gridHeight * 18 + 8;

            for (int i = 0; i < flagNo; i++) {
                elementButtons[i] = new ElementButton(
                        this,
                        x0 + button_offset * i, y0,
                        container.filter.flagType(i),
                        buttons[i][0], buttons[i][1],
                        buttons[i][0], buttons[i][1] + buttonSize,
                        buttons[i][0], buttons[i][1] + buttonSize * 2,
                        buttonSize, buttonSize,
                        TEX.toString()
                );

                addElement(elementButtons[i]);

                if (!container.filter.canAlterFlag(i)) elementButtons[i].setDisabled();
            }

            setButtons();
        }
    }

    private void setButtons() {
        for (int i = 0; i < elementButtons.length; i++) {
            boolean b = container.filter.getFlag(i);
            int x = buttons[i][0] + (b ? buttonSize : 0);
            elementButtons[i].setSheetX(x);
            elementButtons[i].setHoverX(x);
        }
    }

    @Override
    protected void updateElementInformation() {
        super.updateElementInformation();
        setButtons();
    }

    @Override
    public void handleElementButtonClick(String buttonName, int mouseButton) {
        for (int i = 0; i < elementButtons.length; i++) {
            ElementButton button = elementButtons[i];
            if (button.getName().equals(buttonName)) {
                container.filter.setFlag(i, !container.filter.getFlag(i));
                setButtons();
                return;
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {
        super.drawGuiContainerBackgroundLayer(partialTick, x, y);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture(TEX);

        int x0 = container.gridX0 - 1;
        int y0 = container.gridY0 - 1;

        int w = container.gridWidth * 18;
        int h = container.gridHeight * 18;

        int x1 = x0 + w;
        int y1 = y0 + h;
        for (int dx = x0; dx < x1; dx += 9 * 18) {
            for (int dy = y0; dy < y1; dy += 3 * 18) {
                int dw = Math.min(x1 - dx, 9 * 18);
                int dh = Math.min(y1 - dy, 3 * 18);

                drawTexturedModalRect(guiLeft + dx, guiTop + dy, 7, 122, dw, dh);
            }
        }
    }

}
