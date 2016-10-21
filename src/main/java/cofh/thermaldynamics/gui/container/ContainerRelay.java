package cofh.thermaldynamics.gui.container;

import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraft.inventory.IContainerListener;

public class ContainerRelay extends ContainerAttachmentBase {

	private final Relay relay;
	public int relayPower = 0;
	public int gridPower = 0;

	public ContainerRelay(Relay relay) {

		super(relay);
		this.relay = relay;
	}

	@Override
	public void detectAndSendChanges() {

		super.detectAndSendChanges();

		int prevRelay = relayPower;
		int prevGrid = gridPower;

		relayPower = relay.getPowerLevel();

		MultiBlockGrid grid = relay.tile.myGrid;
		if (grid != null && grid.rs != null) {
			if (grid.rs.nextRedstoneLevel == -128) {
				gridPower = grid.rs.redstoneLevel;
			} else {
				gridPower = grid.rs.nextRedstoneLevel;
			}
		} else {
			gridPower = 0;
		}

		if (relayPower == prevRelay && gridPower == prevGrid) {
			return;
		}

		for (IContainerListener listener : this.listeners) {
            if (gridPower != prevGrid) {
				listener.sendProgressBarUpdate(this, 0, gridPower);
			}
			if (relayPower != prevRelay) {
				listener.sendProgressBarUpdate(this, 1, relayPower);
			}
		}
	}

	@Override
	public void updateProgressBar(int i, int j) {

		if (i == 0) {
			gridPower = j;
		}
		if (i == 1) {
			relayPower = j;
		}
	}
}
