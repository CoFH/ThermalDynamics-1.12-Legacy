package cofh.thermaldynamics.gui.client;

import cofh.core.gui.GuiBaseAdv;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.gui.element.ElementButtonManaged;
import cofh.lib.gui.element.listbox.SliderVertical;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import cofh.thermaldynamics.gui.container.ContainerTransport;
import cofh.thermaldynamics.gui.element.ElementDirectoryButton;

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;

public class GuiTransport extends GuiBaseAdv {

	final ContainerTransport container;

	public static final String TEX_PATH = "thermaldynamics:textures/gui/Transport.png";
	static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

	ElementDirectoryButton[] directoryButtons;
	SliderVertical vertical;
	public ElementButtonManaged buttonConfig;
	public int x0;
	public int y0;

	public GuiTransport(ContainerTransport container) {

		super(container, TEXTURE);
		this.container = container;
		this.ySize = 204;
		this.drawInventory = false;
		this.drawTitle = true;
		this.name = "info.thermaldynamics.transport.name";
	}

	public final static int NUM_ENTRIES = 7;
	public final static int BUTTON_WIDTH = 155;
	public final static int BUTTON_HEIGHT = 22;
	public final static int BUTTON_OFFSET = 1;

	public final static int GUI_BUTTON_X0_BASE = 0;
	public final static int GUI_BUTTON_Y0_BASE = 204;

	public final static int GUI_BUTTON_X0_HOVER = GUI_BUTTON_X0_BASE;
	public final static int GUI_BUTTON_Y0_HOVER = GUI_BUTTON_Y0_BASE + BUTTON_HEIGHT;

	final static int SLIDER_WIDTH = 6;

	public GuiTransport(TileTransportDuct transportDuct) {

		this(new ContainerTransport(transportDuct));
	}

	@Override
	public void initGui() {

		super.initGui();

		x0 = (xSize - BUTTON_WIDTH) / 2 - SLIDER_WIDTH;
		y0 = getFontRenderer().FONT_HEIGHT + 28;

		vertical = new SliderVertical(this, xSize - 6 - SLIDER_WIDTH, y0, SLIDER_WIDTH, NUM_ENTRIES * BUTTON_HEIGHT + (NUM_ENTRIES - 1) * BUTTON_OFFSET, 10);
		vertical.setVisible(false);
		addElement(vertical);

		directoryButtons = new ElementDirectoryButton[NUM_ENTRIES];
		for (int i = 0; i < NUM_ENTRIES; i++) {
			directoryButtons[i] = new ElementDirectoryButton(i, this, x0, y0);
			addElement(directoryButtons[i]);
		}

		Mouse.setCursorPosition((directoryButtons[0].getPosX() + (directoryButtons[0].getWidth() >> 1) + guiLeft) * this.mc.displayWidth / this.width,
				(this.height - (1 + directoryButtons[0].getPosY() + (directoryButtons[0].getHeight() >> 1) + guiTop + 1)) * this.mc.displayHeight / this.height);

		final String configText = StringHelper.localize("info.thermaldynamics.transport.config");
		int stringWidth = getFontRenderer().getStringWidth(configText);
		buttonConfig = new ElementButtonManaged(this, xSize - 12 - stringWidth, 16, stringWidth + 8, 16, configText) {

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

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {

		super.drawGuiContainerForegroundLayer(x, y);

		DirectoryEntry directoryEntry = container.directoryEntry;
		if (directoryEntry != null) {
			int dy = 15;

			int by = directoryEntry.icon != null ? BUTTON_HEIGHT : 0;
			String text = getFontRenderer().trimStringToWidth(directoryEntry.getName(), xSize - buttonConfig.getWidth() - 16 - by);
			getFontRenderer().drawString(text, x0 + by + 4, dy + (BUTTON_HEIGHT - 8) / 2, 0x404040);

			if (directoryEntry.icon != null) {
				drawItemStack(directoryEntry.icon, x0 + 3, dy + 3, false, null);
			}
		}

		ArrayList<DirectoryEntry> directory = container.directory;
		if (directory == null) {
			fontRendererObj.drawString(StringHelper.localize("info.thermaldynamics.transport.waiting"),
					getCenteredOffset(StringHelper.localize("info.thermaldynamics.transport.waiting")), ySize / 2, 0x404040);
		} else if (directory.isEmpty()) {
			fontRendererObj.drawString(StringHelper.localize("info.thermaldynamics.transport.nodest"),
					getCenteredOffset(StringHelper.localize("info.thermaldynamics.transport.nodest")), ySize / 2, 0x404040);
		}
	}

	public void goToDest(DirectoryEntry directoryEntry) {

		container.transportDuct.sendRequest(directoryEntry.x, directoryEntry.y, directoryEntry.z);
	}

	@Override
	protected void updateElementInformation() {

		ArrayList<DirectoryEntry> directory = container.directory;
		if (directory == null) {
			return;
		}
		boolean needSlider = directory.size() > NUM_ENTRIES;

		int additionalEntries = directory.size() - NUM_ENTRIES;

		vertical.setVisible(needSlider);
		vertical.setLimits(0, needSlider ? additionalEntries : 0);

		x0 = (xSize - BUTTON_WIDTH) / 2 - (needSlider ? SLIDER_WIDTH : 0);

		int offset = vertical.getValue();

		for (int i = 0; i < directoryButtons.length; i++) {
			int index = offset + i;
			directoryButtons[i].setPosX(x0);
			directoryButtons[i].setEntry(index >= directory.size() ? null : directory.get(index));
		}
		//		buttonConfig.setPosition(x0, buttonConfig.getPosY());

	}

	@Override
	protected boolean onMouseWheel(int mouseX, int mouseY, int wheelMovement) {

		return vertical.isVisible() && vertical.onMouseWheel(mouseX, mouseY, wheelMovement);
	}

}
