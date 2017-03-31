package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class DuctUnitStructural extends DuctUnit<DuctUnitStructural, GridStructural, Void> {

	private final DuctUnit mainDuct;

	public DuctUnitStructural(TileGrid parent, DuctUnit mainDuct) {
		super(parent, mainDuct.getDuctType());
		this.mainDuct = mainDuct;
	}

	@Override
	public DuctToken<DuctUnitStructural, GridStructural, Void> getToken() {
		return DuctToken.STRUCTURAL;
	}

	@Override
	public GridStructural createGrid() {
		return new GridStructural(world());
	}

	@Override
	public Void cacheTile(@Nonnull TileEntity tile, byte side) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitStructural, GridStructural, Void> adjDuct, byte side) {
		return mainDuct.canConnectToOtherDuct(adjDuct.cast().mainDuct, side);
	}

	public void addRelays() {
		if (parent.attachmentData != null && grid != null) {
			for (Attachment attachment : parent.attachmentData.attachments) {
				if (attachment != null) {
					if (attachment.getId() == AttachmentRegistry.RELAY) {
						Relay signaller = (Relay) attachment;
						if (signaller.isInput()) {
							grid.addSignalInput(signaller);
						} else {
							grid.addSignalOutput(attachment);
						}
					} else if (attachment.respondsToSignallum()) {
						grid.addSignalOutput(attachment);
					}
				}
			}
		}
	}
}
