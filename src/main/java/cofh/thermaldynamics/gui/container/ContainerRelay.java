package cofh.thermaldynamics.gui.container;

import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.inventory.ICrafting;

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
        if (grid != null) {
            if (grid.nextRedstoneLevel == -128)
                gridPower = grid.redstoneLevel;
            else
                gridPower = grid.nextRedstoneLevel;
        } else gridPower = 0;

        if (relayPower == prevRelay && gridPower == prevGrid)
            return;

        for (Object crafter : this.crafters) {
            ICrafting c = (ICrafting) crafter;
            if (gridPower != prevGrid)
                c.sendProgressBarUpdate(this, 0, gridPower);
            if (relayPower != prevRelay)
                c.sendProgressBarUpdate(this, 1, relayPower);
        }
    }

    @Override
    public void updateProgressBar(int i, int j) {
        if (i == 0)
            gridPower = j;
        if (i == 1)
            relayPower = j;
    }
}
