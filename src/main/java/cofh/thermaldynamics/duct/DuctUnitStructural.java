package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.DuctUnit;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.duct.tiles.TileStructuralDuct;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitStructural extends DuctUnit<DuctUnitStructural, GridStructural, Void> {

	private static final Void[] VOIDS = new Void[6];

	@Nullable
	private final DuctUnit mainDuct;

	public DuctUnitStructural(TileGrid parent, @Nonnull DuctUnit mainDuct) {

		super(parent, mainDuct.getDuctType());
		this.mainDuct = mainDuct;
	}

	public DuctUnitStructural(TileStructuralDuct parent, Duct duct) {

		super(parent, duct);
		this.mainDuct = null;
	}

	@Override
	protected Void[] createTileCache() {

		return VOIDS;
	}

	@Override
	protected DuctUnitStructural[] createDuctCache() {

		return new DuctUnitStructural[6];
	}

	@Nonnull
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

	@SuppressWarnings ("unchecked")
	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitStructural, GridStructural, Void> adjDuct, byte side, byte oppositeSide) {

		if (this.mainDuct == null) {
			return true;
		}
		DuctUnit otherMainDuct = adjDuct.cast().mainDuct;
		if (otherMainDuct == null) {
			return true;
		}
		if (mainDuct.getToken() != otherMainDuct.getToken()) {
			return false;
		}
		return this.mainDuct.canConnectToOtherDuct(otherMainDuct, side, oppositeSide);
	}

	@Nonnull
	@Override
	protected BlockDuct.ConnectionType getConnectionTypeDuct(DuctUnitStructural duct, int side) {

		return BlockDuct.ConnectionType.STRUCTURE_CLEAN;
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
					} else if (attachment.respondsToSignalum()) {
						grid.addSignalOutput(attachment);
					}
				}
			}
		}
	}
}
