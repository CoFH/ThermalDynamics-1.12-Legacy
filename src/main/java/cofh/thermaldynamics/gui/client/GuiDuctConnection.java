package cofh.thermaldynamics.gui.client;

import cofh.core.gui.GuiCore;
import cofh.core.gui.element.ElementButton;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.gui.element.tab.TabRedstoneControl;
import cofh.core.util.helpers.StringHelper;
import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.gui.container.ContainerDuctConnection;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiDuctConnection extends GuiCore {

	static final String TEX_PATH = "thermaldynamics:textures/gui/connection.png";
	static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);

	public String myInfo = "";

	InventoryPlayer inventory;
	ConnectionBase conBase;

	ContainerDuctConnection container;
	public ElementButton[] flagButtons = new ElementButton[0];
	public ElementButton[] levelButtons = new ElementButton[FilterLogic.defaultLevels.length];

	boolean isItemServo;
	boolean isAdvItemFilter;
	public ElementButton decStackSize;
	public ElementButton incStackSize;

	public ElementButton decRetainSize;
	public ElementButton incRetainSize;

	int minStackSize;
	int maxStackSize;

	int minRetainSize;
	int maxRetainSize;

	public int buttonSize;

	private static final int[][] levelButtonPos = { { -1, -1 }, { 0, 204 }, { 80, 204 } };
	private static final int[][] flagButtonsPos = { { 176, 0 }, { 176, 60 }, { 216, 0 }, { 216, 60 }, { 176, 120 }, { 216, 120 }, { 176, 180 }, { 216, 180 }, };

	public GuiDuctConnection(InventoryPlayer inventory, ConnectionBase conBase) {

		super(new ContainerDuctConnection(inventory, conBase), TEXTURE);
		this.conBase = conBase;
		this.inventory = inventory;
		container = (ContainerDuctConnection) inventorySlots;
		name = conBase.getName();
		this.ySize = 204;
		this.isItemServo = conBase.getId() == AttachmentRegistry.SERVO_ITEM || conBase.getId() == AttachmentRegistry.RETRIEVER_ITEM;
		this.isAdvItemFilter = (conBase.getId() == AttachmentRegistry.FILTER_ITEM || conBase.getId() == AttachmentRegistry.RETRIEVER_ITEM) && conBase.filter.canAlterFlag(FilterLogic.levelRetainSize);

		switch (conBase.getId()) {
			case AttachmentRegistry.SERVO_ITEM:
				generateInfo("tab.thermaldynamics.servoItem");
				break;
			case AttachmentRegistry.FILTER_ITEM:
				generateInfo("tab.thermaldynamics.filterItem");
				break;
			case AttachmentRegistry.RETRIEVER_ITEM:
				generateInfo("tab.thermaldynamics.retrieverItem");
				break;
			default:
				break;
		}
	}

	@Override
	public void initGui() {

		super.initGui();

		if (!myInfo.isEmpty()) {

			myInfo += "\n\n" + StringHelper.localize("tab.thermaldynamics.conChange");

			addTab(new TabInfo(this, myInfo));
		}
		if (conBase.canAlterRS()) {
			addTab(new TabRedstoneControl(this, conBase));
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
				flagButtons[j] = new ElementButton(this, x0 + button_offset * i, y0, container.filter.flagType(j), flagButtonsPos[j][0], flagButtonsPos[j][1], flagButtonsPos[j][0], flagButtonsPos[j][1] + buttonSize, flagButtonsPos[j][0], flagButtonsPos[j][1] + buttonSize * 2, buttonSize, buttonSize, TEX_PATH);
				addElement(flagButtons[j]);
			}
			for (int i = 0; i < levelNums.length; i++) {
				int j = levelNums[i];
				levelButtons[j] = new ElementButton(this, x0 + button_offset * (i + flagNums.length), y0, FilterLogic.levelNames[j], levelButtonPos[j][0], levelButtonPos[j][1], levelButtonPos[j][0], levelButtonPos[j][1] + buttonSize, buttonSize, buttonSize, TEX_PATH);
				addElement(levelButtons[j]);
			}
		}
		decStackSize = new ElementButton(this, 137, 57, "DecStackSize", 216, 120, 216, 134, 216, 148, 14, 14, TEX_PATH).setToolTip("info.thermaldynamics.servo.decStackSize");
		incStackSize = new ElementButton(this, 153, 57, "IncStackSize", 230, 120, 230, 134, 230, 148, 14, 14, TEX_PATH).setToolTip("info.thermaldynamics.servo.incStackSize");

		decRetainSize = new ElementButton(this, 137, 28, "DecRetainSize", 216, 120, 216, 134, 216, 148, 14, 14, TEX_PATH).setToolTip("info.thermaldynamics.filter.decRetainSize");
		incRetainSize = new ElementButton(this, 153, 28, "IncRetainSize", 230, 120, 230, 134, 230, 148, 14, 14, TEX_PATH).setToolTip("info.thermaldynamics.filter.incRetainSize");

		if (isAdvItemFilter) {
			addElement(decRetainSize);
			addElement(incRetainSize);
			minRetainSize = FilterLogic.minLevels[conBase.filter.type][FilterLogic.levelRetainSize];
			maxRetainSize = FilterLogic.maxLevels[conBase.filter.type][FilterLogic.levelRetainSize];
		}

		if (isItemServo) {
			addElement(decStackSize);
			addElement(incStackSize);
			minStackSize = FilterLogic.minLevels[conBase.filter.type][0];
			maxStackSize = FilterLogic.maxLevels[conBase.filter.type][0];
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
				levelButtons[i].setToolTip("info.thermaldynamics.filter." + levelButtons[i].getName() + "." + level);
			}
		}
	}

	@Override
	protected void updateElementInformation() {

		super.updateElementInformation();

		if (isAdvItemFilter) {
			int qty = conBase.filter.getLevel(FilterLogic.levelRetainSize);
			if (qty > minRetainSize) {
				decRetainSize.setActive();
			} else {
				decRetainSize.setDisabled();
			}
			if (qty < maxRetainSize) {
				incRetainSize.setActive();
			} else {
				incRetainSize.setDisabled();
			}
		}

		if (isItemServo) {
			int qty = conBase.filter.getLevel(0);
			if (qty > minStackSize) {
				decStackSize.setActive();
			} else {
				decStackSize.setDisabled();
			}
			if (qty < maxStackSize) {
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
						playClickSound(1.0F, 0.8F);
					} else {
						playClickSound(1.0F, 0.6F);
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
					playClickSound(1.0F, 0.8F);
				} else if (mouseButton == 1) {
					container.filter.decLevel(i);
					playClickSound(1.0F, 0.6F);
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
			container.filter.decLevel(FilterLogic.levelStackSize, change, false);
			pitch -= 0.1F;
		} else if (buttonName.equalsIgnoreCase("IncStackSize")) {
			container.filter.incLevel(FilterLogic.levelStackSize, change, false);
			pitch += 0.1F;
		}

		if (buttonName.equalsIgnoreCase("DecRetainSize")) {
			container.filter.decLevel(FilterLogic.levelRetainSize, change, false);
			pitch -= 0.1F;
		} else if (buttonName.equalsIgnoreCase("IncRetainSize")) {
			container.filter.incLevel(FilterLogic.levelRetainSize, change, false);
			pitch += 0.1F;
		}

		playClickSound(1.0F, pitch);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {

		if (isAdvItemFilter) {
			int xQty = 146;
			int qty = conBase.filter.getLevel(FilterLogic.levelRetainSize);
			if (qty == 0) {
				xQty -= 9;
				fontRenderer.drawString(StringHelper.localize("info.thermaldynamics.filter.zeroRetainSize"), xQty, 18, 0x404040);
			} else {
				if (qty < 10) {
					xQty += 6;
				} else if (qty >= 100) {
					xQty -= 3;
				}
				fontRenderer.drawString("" + qty, xQty, 18, 0x404040);
			}
		}

		if (isItemServo) {
			int xQty = 146;
			int qty = conBase.filter.getLevel(0);
			if (qty < 10) {
				xQty += 6;
			}
			fontRenderer.drawString("" + qty, xQty, 46, 0x404040);
		}

		super.drawGuiContainerForegroundLayer(x, y);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {

		super.drawGuiContainerBackgroundLayer(partialTick, x, y);

		drawSlots();
	}

	private void drawSlots() {

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
