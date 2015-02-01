package thermaldynamics.gui.client;

import cofh.core.gui.GuiBaseAdv;
import cofh.core.gui.element.TabRedstone;
import cofh.lib.gui.element.ElementButton;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import thermaldynamics.ducts.attachments.ConnectionBase;
import thermaldynamics.ducts.attachments.filter.FilterLogic;
import thermaldynamics.gui.container.ContainerDuctConnection;

public class GuiDuctConnection extends GuiBaseAdv {

	InventoryPlayer inventory;
	ConnectionBase servoBase;
	public static ResourceLocation TEX = new ResourceLocation("thermaldynamics:textures/gui/Connection.png");

	ContainerDuctConnection container;
	public ElementButton[] flagButtons = new ElementButton[0];
	public ElementButton[] levelButtons = new ElementButton[FilterLogic.defaultLevels.length];

	public ElementButton incStack;
	public ElementButton subStacksize;

	public int buttonSize;

	public GuiDuctConnection(InventoryPlayer inventory, ConnectionBase servoBase) {

		super(new ContainerDuctConnection(inventory, servoBase), TEX);
		this.servoBase = servoBase;
		this.inventory = inventory;
		container = (ContainerDuctConnection) inventorySlots;
		name = "Filter";
		this.ySize = 205;
	}

	private static final int[][] levelButtonPos = {
        	{-1, -1},
        	{0, 204},
        	{80, 204}
	};

	private static final int[][] flagButtonsPos = {
        	{176, 0},
        	{216, 0},
        	{176, 60},
        	{216, 60},
        	{176, 120},
        	{216, 120},
        	{176, 180},
        	{216, 180},
	};

	@Override
	public void initGui() {

		super.initGui();
		if (servoBase.canAlterRS())
			addTab(new TabRedstone(this, servoBase));

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
						buttonSize, TEX.toString());
				addElement(flagButtons[j]);
			}

			for (int i = 0; i < levelNums.length; i++) {
				int j = levelNums[i];
				levelButtons[j] = new ElementButton(this, x0 + button_offset * (i + flagNums.length), y0, "Level" + j, levelButtonPos[j][0],
						levelButtonPos[j][1], levelButtonPos[j][0], levelButtonPos[j][1] + buttonSize, buttonSize, buttonSize, TEX.toString());
				addElement(levelButtons[j]);
			}

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
				String s = "info.thermaldynamics.filter." + flagButtons[i].getName() + (b ? ".on" : ".off");
				flagButtons[i].setToolTip("info.thermaldynamics.filter." + flagButtons[i].getName() + (b ? ".on" : ".off"));
			}
		}

		for (int i = 0; i < levelButtons.length; i++) {
			if (levelButtons[i] != null) {
				int level = container.filter.getLevel(i);
				int x = levelButtonPos[i][0] + level * buttonSize;
				levelButtons[i].setSheetX(x);
				levelButtons[i].setHoverX(x);
				// String s = "info.thermaldynamics.filter." + flagButtons[i].getName() + (b ? ".on" : ".off");
				// flagButtons[i].setToolTip("info.thermaldynamics.filter." + flagButtons[i].getName() + (b ? ".on" : ".off"));
			}
		}
	}

	@Override
	protected void updateElementInformation() {

		super.updateElementInformation();
		setButtons();
	}

	@Override
	public void handleElementButtonClick(String buttonName, int mouseButton) {

		for (int i = 0; i < flagButtons.length; i++) {
			ElementButton button = flagButtons[i];
			if (button != null && button.getName().equals(buttonName)) {
				container.filter.setFlag(i, !container.filter.getFlag(i));
				setButtons();
				return;
			}
		}

		for (int i = 0; i < levelButtons.length; i++) {
			ElementButton button = levelButtons[i];
			if (button != null && button.getName().equals(buttonName)) {
				container.filter.incLevel(i);
				setButtons();
				return;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int x, int y) {

		super.drawGuiContainerBackgroundLayer(partialTick, x, y);
		drawSlots();
	}

	private void drawSlots() {

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
