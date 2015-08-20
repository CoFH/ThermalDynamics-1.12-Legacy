package cofh.thermaldynamics.duct.entity.gui;

import cofh.core.gui.GuiBaseAdv;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.gui.element.ElementButtonManaged;
import cofh.lib.gui.element.listbox.SliderVertical;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import java.util.ArrayList;
import net.minecraft.util.ResourceLocation;

public class GuiTransport extends GuiBaseAdv {
	private final ContainerTransport container;

	static final String TEX_PATH = "thermaldynamics:textures/gui/Transport.png";
	static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

	ElementDirectoryButton[] directoryButtons;
	SliderVertical vertical;
	public ElementButtonManaged buttonConfig;

	public GuiTransport(ContainerTransport container) {
		super(container, TEXTURE);
		this.container = container;
		this.ySize = 204;
		this.drawInventory = false;
		this.drawTitle = true;
		this.name = "thermaldynamics.transport.name";
	}

	final static int NUM_ENTRIES = 7;
	final static int BUTTON_WIDTH = 155;
	final static int BUTTON_HEIGHT = 22;
	final static int BUTTON_OFFSET = 2;

	final static int GUI_BUTTON_X0_BASE = 0;
	final static int GUI_BUTTON_Y0_BASE = 204;

	final static int GUI_BUTTON_X0_HOVER = GUI_BUTTON_X0_BASE;
	final static int GUI_BUTTON_Y0_HOVER = GUI_BUTTON_Y0_BASE + BUTTON_HEIGHT;

	final static int SLIDER_WIDTH = 6;

	public GuiTransport(TileTransportDuct transportDuct) {
		this(new ContainerTransport(transportDuct));
	}

	@Override
	public void initGui() {
		super.initGui();

		int x0 = (xSize - BUTTON_WIDTH ) / 2 - SLIDER_WIDTH;
		int y0 = getFontRenderer().FONT_HEIGHT + 6;

		vertical = new SliderVertical(this, xSize - 6 - SLIDER_WIDTH, y0, SLIDER_WIDTH, NUM_ENTRIES * BUTTON_HEIGHT + (NUM_ENTRIES - 1) * BUTTON_OFFSET, 10);
		vertical.setVisible(false);
		addElement(vertical);

		directoryButtons = new ElementDirectoryButton[NUM_ENTRIES];
		for (int i = 0; i < NUM_ENTRIES; i++) {
			directoryButtons[i] = new ElementDirectoryButton(i, this, x0, y0);
			addElement(directoryButtons[i]);
		}

		buttonConfig = new ElementButtonManaged(this, 8, ySize - 20, 50, 16, "Config") {

			@Override
			public void onClick() {
				PacketTileInfo myPayload = PacketTileInfo.newPacket(container.transportDuct);
				myPayload.addByte(0);
				myPayload.addByte(TileTransportDuct.NETWORK_CONFIG);
				PacketHandler.sendToServer(myPayload);
			}
		};
		addElement(buttonConfig);
	}

	public void goToDest(DirectoryEntry directoryEntry) {
		container.transportDuct.sendRequest(directoryEntry.x, directoryEntry.y, directoryEntry.z);
	}


	@Override
	protected void updateElementInformation() {
		ArrayList<DirectoryEntry> directory = container.directory;
		if (directory == null)
			return;

		boolean needSlider = directory.size() > NUM_ENTRIES;

		int additionalEntries = directory.size() - NUM_ENTRIES;

		vertical.setVisible(needSlider);
		vertical.setLimits(0, needSlider ? additionalEntries : 0);

		int x0 = (xSize - BUTTON_WIDTH ) / 2 - (needSlider ? SLIDER_WIDTH : 0);

		int offset = vertical.getValue();

		for (int i = 0; i < directoryButtons.length; i++) {
			int index = offset + i;
			directoryButtons[i].setPosX(x0);
			directoryButtons[i].setEntry(index >= directory.size() ? null : directory.get(index));
		}

		buttonConfig.setPosition(x0, buttonConfig.getPosY());

	}

	@Override
	protected boolean onMouseWheel(int mouseX, int mouseY, int wheelMovement) {
		return vertical.isVisible() && vertical.onMouseWheel(mouseX, mouseY, wheelMovement);
	}
}
