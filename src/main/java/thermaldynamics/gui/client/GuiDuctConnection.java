package thermaldynamics.gui.client;

import cofh.core.gui.GuiBaseAdv;
import cofh.core.gui.element.TabInfo;
import cofh.core.gui.element.TabRedstone;
import cofh.lib.gui.element.ElementButton;
import cofh.lib.util.helpers.StringHelper;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.ducts.attachments.ConnectionBase;
import thermaldynamics.ducts.attachments.filter.FilterLogic;
import thermaldynamics.gui.container.ContainerDuctConnection;

public class GuiDuctConnection extends GuiBaseAdv {

	static final String TEX_PATH = "thermaldynamics:textures/gui/Connection.png";
	static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

	public String myInfo = "";

	InventoryPlayer inventory;
	ConnectionBase conBase;

	ContainerDuctConnection container;
	public ElementButton[] flagButtons = new ElementButton[0];
	public ElementButton[] levelButtons = new ElementButton[FilterLogic.defaultLevels.length];

	boolean isItemServo;
	public ElementButton decStackSize;
	public ElementButton incStackSize;
	int minSize;
	int maxSize;

	public int buttonSize;

	private static final int[][] levelButtonPos = { { -1, -1 }, { 0, 204 }, { 80, 204 } };
	private static final int[][] flagButtonsPos = { { 176, 0 }, { 216, 0 }, { 176, 60 }, { 216, 60 }, { 176, 120 }, { 216, 120 }, { 176, 180 }, { 216, 180 }, };

	public GuiDuctConnection(InventoryPlayer inventory, ConnectionBase conBase) {

		super(new ContainerDuctConnection(inventory, conBase), TEXTURE);
		this.conBase = conBase;
		this.inventory = inventory;
		container = (ContainerDuctConnection) inventorySlots;
		name = conBase.getName();
		this.ySize = 205;
		this.isItemServo = conBase.getId() == AttachmentRegistry.SERVO_ITEM;
	}

	protected void generateInfo(String tileString, int lines) {

		myInfo = StringHelper.localize(tileString + "." + 0);
		for (int i = 1; i < lines; i++) {
			myInfo += "\n\n" + StringHelper.localize(tileString + "." + i);
		}
	}

	@Override
	public void initGui() {

		super.initGui();

		if (conBase.canAlterRS()) {
			addTab(new TabRedstone(this, conBase));
		}
		if (!myInfo.isEmpty()) {
			addTab(new TabInfo(this, myInfo));
		}

		int[] flagNums = container.filter.validFlags();
		flagButtons = new ElementButton[container.filter.numFlags()];

		int[] levelNums = container.filter.getValidLevels();
		levelButtons = new ElementButton[FilterLogic.defaultLevels.length];

		int buttonNo = flagNums.length + levelNums.length;
		if (buttonNo != 0) {
			buttonSize = 20;
			int button_offset = buttonSize + 6;
			int x0 = xSize / 2 - buttonNo * (button_offset / 2) + 3;
			int y0 = container.gridY0 + container.gridHeight * 18 + 8;

			for (int i = 0; i < flagNums.length; i++) {
				int j = flagNums[i];
				flagButtons[j] = new ElementButton(this, x0 + button_offset * i, y0, container.filter.flagType(j), flagButtonsPos[j][0], flagButtonsPos[j][1],
						flagButtonsPos[j][0], flagButtonsPos[j][1] + buttonSize, flagButtonsPos[j][0], flagButtonsPos[j][1] + buttonSize * 2, buttonSize,
						buttonSize, TEX_PATH);
				addElement(flagButtons[j]);
			}

			for (int i = 0; i < levelNums.length; i++) {
				int j = levelNums[i];
				levelButtons[j] = new ElementButton(this, x0 + button_offset * (i + flagNums.length), y0, "level" + j, levelButtonPos[j][0],
						levelButtonPos[j][1], levelButtonPos[j][0], levelButtonPos[j][1] + buttonSize, buttonSize, buttonSize, TEX_PATH);
				addElement(levelButtons[j]);
			}
		}
		decStackSize = new ElementButton(this, 137, 57, "DecStackSize", 216, 120, 216, 134, 216, 148, 14, 14, TEX_PATH);
		incStackSize = new ElementButton(this, 153, 57, "IncStackSize", 230, 120, 230, 134, 230, 148, 14, 14, TEX_PATH);

		if (isItemServo) {
			addElement(decStackSize);
			addElement(incStackSize);
			minSize = conBase.filter.minLevels[conBase.filter.type][0];
			maxSize = conBase.filter.maxLevels[conBase.filter.type][0];
		}
		setButtons();
	}

	private void setButtons() {

		for (int i = 0; i < flagButtons.length; i++) {
			if (flagButtons[i] != null) {
				boolean b = container.filter.getFlag(i);
				int x = flagButtonsPos[i][0] + (b ? buttonSize : 0);
				flagButtons[i].setSheetX(x);
				flagButtons[i].setHoverX(x);
				flagButtons[i].setToolTip("info.thermaldynamics.filter." + flagButtons[i].getName() + (b ? ".on" : ".off"));
			}
		}

		for (int i = 0; i < levelButtons.length; i++) {
			if (levelButtons[i] != null) {
				int level = container.filter.getLevel(i);
				int x = levelButtonPos[i][0] + level * buttonSize;
				levelButtons[i].setSheetX(x);
				levelButtons[i].setHoverX(x);
				levelButtons[i].setToolTip("info.thermaldynamics.filter." + levelButtons[i].getName() + level);
			}
		}
	}

	@Override
	protected void updateElementInformation() {

		super.updateElementInformation();

		if (isItemServo) {
			int qty = conBase.filter.getLevel(0);
			if (qty > minSize) {
				decStackSize.setActive();
			} else {
				decStackSize.setDisabled();
			}
			if (qty < maxSize) {
				incStackSize.setActive();
			} else {
				incStackSize.setDisabled();
			}
		}
		setButtons();
	}

	@Override
	public void handleElementButtonClick(String buttonName, int mouseButton) {

		for (int i = 0; i < flagButtons.length; i++) {
			ElementButton button = flagButtons[i];
			if (button != null && button.getName().equals(buttonName)) {
				if (container.filter.setFlag(i, !container.filter.getFlag(i))) {
					if (container.filter.getFlag(i)) {
						playSound("random.click", 1.0F, 0.8F);
					} else {
						playSound("random.click", 1.0F, 0.6F);
					}
				}
				setButtons();
				return;
			}
		}
		for (int i = 0; i < levelButtons.length; i++) {
			ElementButton button = levelButtons[i];
			if (button != null && button.getName().equals(buttonName)) {
				if (mouseButton == 0) {
					container.filter.incLevel(i);
					playSound("random.click", 1.0F, 0.8F);
				} else if (mouseButton == 1) {
					container.filter.decLevel(i);
					playSound("random.click", 1.0F, 0.6F);
				}
				setButtons();
				return;
			}
		}
		int change = 1;
		float pitch = 0.7F;
		if (GuiScreen.isShiftKeyDown()) {
			change = 16;
			pitch = 0.9F;
			if (mouseButton == 1) {
				change = 4;
				pitch = 0.8F;
			}
		}
		if (buttonName.equalsIgnoreCase("DecStackSize")) {
			container.filter.decLevel(0, change, false);
			pitch -= 0.1F;
		} else if (buttonName.equalsIgnoreCase("IncStackSize")) {
			container.filter.incLevel(0, change, false);
			pitch += 0.1F;
		}
		playSound("random.click", 1.0F, pitch);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {

		if (isItemServo) {
			int xQty = 146;
			int qty = conBase.filter.getLevel(0);
			if (qty < 10) {
				xQty += 6;
			}
			fontRendererObj.drawString("" + qty, xQty, 44, 0x404040);
		}

		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {

		super.drawGuiContainerBackgroundLayer(partialTick, x, y);

		drawSlots();
	}

	private void drawSlots() {

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(TEXTURE);

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
