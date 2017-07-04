package cofh.thermaldynamics.gui.client;

import cofh.core.gui.GuiCore;
import cofh.core.gui.element.ElementButton;
import cofh.core.gui.element.ElementSlider;
import cofh.core.gui.element.listbox.SliderHorizontal;
import cofh.core.gui.element.tab.TabInfo;
import cofh.core.util.helpers.StringHelper;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.gui.container.ContainerRelay;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiRelay extends GuiCore {

	static final String TEX_PATH = "thermaldynamics:textures/gui/relay.png";
	static final ResourceLocation TEXTURE = new ResourceLocation(TEX_PATH);
	private final Relay relay;
	public ElementButton buttonType;
	public ElementSlider slider;
	private ElementButton buttonInvert;
	ContainerRelay container;

	public GuiRelay(Relay relay) {

		super(new ContainerRelay(relay), TEXTURE);

		this.relay = relay;
		this.drawInventory = false;
		this.name = "item.thermaldynamics.relay.name";
		this.container = (ContainerRelay) inventorySlots;
		this.ySize = 74;
	}

	@Override
	public void initGui() {

		super.initGui();

		if (!"".equals(myInfo)) {
			addTab(new TabInfo(this, myInfo));
		}
		buttonType = new ElementButton(this, 8, 16, "ButtonType", 0, 204, 0, 224, 20, 20, TEX_PATH);
		addElement(buttonType);

		buttonInvert = new ElementButton(this, 34, 16, "ButtonInvert", 0, 204, 0, 224, 20, 20, TEX_PATH);
		addElement(buttonInvert);

		slider = new SliderHorizontal(this, 62, 16, 100, 20, 15) {

			@Override
			public void onValueChanged(int value) {

				relay.setThreshold((byte) value);
				relay.sendUpdatePacket();
			}

			@Override
			public void onStopDragging() {

				relay.sendUpdatePacket();
			}

			@Override
			public void addTooltip(List<String> list) {

				list.add(StringHelper.localize("info.thermaldynamics.relay.threshold") + " " + _value);
			}
		}.setValue(relay.threshold);
		addElement(slider);

		update();
	}

	private void update() {

		slider.setEnabled(relay.shouldThreshold());
		slider.setVisible(relay.shouldThreshold());

		buttonType.setSheetX(20 * relay.type);
		buttonType.setHoverX(20 * relay.type);

		buttonType.setToolTip("info.thermaldynamics.relay.type." + relay.type);

		buttonInvert.setSheetX(60 + 20 * relay.invert);
		buttonInvert.setHoverX(60 + 20 * relay.invert);

		buttonInvert.setToolTip("info.thermaldynamics.relay.invert." + relay.invert);

	}

	@Override
	public void handleElementButtonClick(String buttonName, int mouseButton) {

		super.handleElementButtonClick(buttonName, mouseButton);

		int v = mouseButton == 0 ? 1 : -1;
		if ("ButtonInvert".equals(buttonName)) {
			relay.invert = (byte) ((relay.invert + 4 + v) % 4);
			relay.sendUpdatePacket();
		} else if ("ButtonType".equals(buttonName)) {
			relay.type = (byte) ((relay.type + 3 + v) % 3);
			relay.sendUpdatePacket();
		}
		update();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {

		super.drawGuiContainerForegroundLayer(x, y);

		int rY;
		int gY;
		if (relay.isInput()) {
			rY = 45;
			gY = 58;
		} else {
			gY = 45;
			rY = 58;
		}
		fontRenderer.drawString(StringHelper.localize("info.thermaldynamics.relay.relayRS") + ": " + container.relayPower, 8, rY, 0x404040);
		fontRenderer.drawString(StringHelper.localize("info.thermaldynamics.relay.gridRS") + ": " + container.gridPower, 8, gY, 0x404040);
	}

}
