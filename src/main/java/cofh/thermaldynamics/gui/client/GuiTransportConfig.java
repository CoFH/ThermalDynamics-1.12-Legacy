package cofh.thermaldynamics.gui.client;

import cofh.core.gui.GuiBaseAdv;
import cofh.lib.gui.element.ElementTextField;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import cofh.thermaldynamics.gui.container.ContainerTransportConfig;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiTransportConfig extends GuiBaseAdv {

    static final String TEX_PATH = "thermaldynamics:textures/gui/transport_config.png";
    static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);
    private final InventoryPlayer inventory;
    private final TileTransportDuct transportDuct;

    public GuiTransportConfig(InventoryPlayer inventory, TileTransportDuct transportDuct) {

        super(new ContainerTransportConfig(inventory, transportDuct), new ResourceLocation(TEX_PATH));
        this.inventory = inventory;
        this.transportDuct = transportDuct;

        this.ySize = 134;
    }

    @Override
    public void initGui() {

        super.initGui();

        final boolean drawBack = inventory != null;

        addElement(new ElementTextField(this, 32, 18, 135, 10) {

            @Override
            protected void onCharacterEntered(boolean success) {

                super.onCharacterEntered(success);
                transportDuct.setName(this.getText());
            }

            @Override
            public void drawBackground(int mouseX, int mouseY, float gameTicks) {

                if (drawBack) {
                    super.drawBackground(mouseX, mouseY, gameTicks);
                }
            }
        }.setText(transportDuct.data.name).setBackgroundColor(0, 0, 0));
    }

}
