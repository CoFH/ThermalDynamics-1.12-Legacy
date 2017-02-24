package cofh.thermaldynamics.gui.element;

import cofh.lib.gui.element.ElementButton;
import cofh.thermaldynamics.gui.client.DirectoryEntry;
import cofh.thermaldynamics.gui.client.GuiTransport;

import java.util.List;

public class ElementDirectoryButton extends ElementButton {

	final int index;
	final GuiTransport gui;
	DirectoryEntry entry;

	public ElementDirectoryButton(int index, GuiTransport gui, int posX, int posY, int sizeX, int sizeY, int sheetX, int sheetY, int hoverX, int hoverY, String texture) {

		super(gui, posX, posY, sizeX, sizeY, sheetX, sheetY, hoverX, hoverY, texture);
		this.index = index;
		this.gui = gui;
	}

	public ElementDirectoryButton(int i, GuiTransport gui, int x0, int y0) {

		this(i, gui, x0, y0 + i * (GuiTransport.BUTTON_HEIGHT + GuiTransport.BUTTON_OFFSET), GuiTransport.BUTTON_WIDTH, GuiTransport.BUTTON_HEIGHT, GuiTransport.GUI_BUTTON_X0_BASE, GuiTransport.GUI_BUTTON_Y0_BASE, GuiTransport.GUI_BUTTON_X0_HOVER, GuiTransport.GUI_BUTTON_Y0_HOVER, GuiTransport.TEX_PATH);
	}

	public void setEntry(DirectoryEntry entry) {

		this.entry = entry;
	}

	@Override
	public boolean isVisible() {

		return super.isVisible() && entry != null;
	}

	@Override
	public void onClick() {

		if (entry != null) {
			gui.goToDest(entry);
		}
	}

	@Override
	public void drawBackground(int mouseX, int mouseY, float gameTicks) {

		super.drawBackground(mouseX, mouseY, gameTicks);
	}

	@Override
	public void drawForeground(int mouseX, int mouseY) {

		super.drawForeground(mouseX, mouseY);

		if (entry == null) {
			return;
		}

		String text = getFontRenderer().trimStringToWidth(entry.getName(), sizeX - sizeY - 4);

		getFontRenderer().drawStringWithShadow(text, posX + sizeY + 4, posY + (sizeY - 8) / 2, getTextColor(mouseX, mouseY));

		if (entry.icon != null) {
			gui.drawItemStack(entry.icon, posX + 3, posY + 3, false, null);
		}
	}

	protected int getTextColor(int mouseX, int mouseY) {

		if (!isEnabled()) {
			return -6250336;
		} else if (intersectsWith(mouseX, mouseY)) {
			return 16777120;
		} else {
			return 14737632;
		}
	}

	public void setPosX(int x0) {

		posX = x0;
	}

	@Override
	public void addTooltip(List<String> list) {

		list.add(entry.getName());

		//		list.add(
		//				String.format("x=%d, y=%d, z=%d", entry.x, entry.y, entry.z)
		//		);
		list.add(String.format("x: %d", entry.x));
		list.add(String.format("y: %d", entry.y));
		list.add(String.format("z: %d", entry.z));

		//		Math.abs(entry.x - gui.container.directoryEntry.x)
	}

}
