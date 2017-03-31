package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.WorldGridList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class GridStructural extends MultiBlockGrid<DuctUnitStructural> {

	public GridStructural(WorldGridList worldGrid) {

		super(worldGrid);
	}

	public GridStructural(World worldObj) {

		super(worldObj);
	}

	@Override
	public boolean canAddBlock(IGridTile aBlock) {

		return true;
	}


	public boolean signalsUpToDate;

	public RedstoneControl rs;


	public void tickGrid() {

		if (rs != null && rs.nextRedstoneLevel != -128) {
			rs.redstoneLevel = rs.nextRedstoneLevel;
			rs.nextRedstoneLevel = -128;

			ArrayList<Attachment> signallersOut = rs.relaysOut;
			if (signallersOut != null) {
				for (Attachment output : signallersOut) {
					output.checkSignal();
				}
			}
		}

		if (signalsUpToDate) {
			return;
		}

		signalsUpToDate = true;

		if (rs == null || rs.relaysIn == null) {
			if (rs != null) {
				rs.relaysOut = null;
			}
			for (DuctUnitStructural multiBlock : nodeSet) {
				multiBlock.addRelays();
			}
		}

		if (rs == null) {
			return;
		}

		if (rs.relaysIn == null) {
			if (rs.relaysOut == null) {
				rs = null;
				return;
			} else {
				rs.nextRedstoneLevel = 0;
			}
			return;
		}

		int powered = 0;
		for (Relay signaller : rs.relaysIn) {
			powered = Math.max(powered, signaller.getPowerLevel());
			if (powered == 15) {
				break;
			}

		}

		rs.nextRedstoneLevel = (byte) powered;

	}


	public void addSignalInput(Relay signaller) {

		if (signaller.isInput()) {
			if (rs == null) {
				rs = new RedstoneControl();
			}

			if (rs.relaysIn == null) {
				rs.relaysIn = new ArrayList<>();
			}

			rs.relaysIn.add(signaller);
		}
	}



	public void addSignalOutput(Attachment attachment) {

		if (rs == null) {
			rs = new RedstoneControl();
		}

		if (rs.relaysOut == null) {
			rs.relaysOut = new ArrayList<>();
		}
		rs.relaysOut.add(attachment);
	}


	public void resetRelays() {

		if (rs != null) {
			rs.relaysIn = null;
			rs.relaysOut = null;
		}
		signalsUpToDate = false;
	}


	public void onMinorGridChange() {

		resetRelays();
	}

	public void onMajorGridChange() {

		resetRelays();
	}

	@Override
	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {
		if (rs != null) {
			int r = rs.redstoneLevel;
			if (rs.nextRedstoneLevel != -128) {
				r = rs.nextRedstoneLevel;
			}
			addInfo(info, "redstone", r);
		}
	}
}
