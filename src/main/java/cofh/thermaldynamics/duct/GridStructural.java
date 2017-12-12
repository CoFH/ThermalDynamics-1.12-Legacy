package cofh.thermaldynamics.duct;

import cofh.core.util.helpers.StringHelper;
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

		if (rs != null) {
			rs.updateLevels();

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
			for (DuctUnitStructural multiBlock : idleSet) {
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
				rs.setNextLevels((byte)0);
			}
			return;
		}

		int[] powered = new int[16];
		for (Relay signaller : rs.relaysIn) {
			int power = Math.max(powered[signaller.color], signaller.getPowerLevel());
			if (power > 0) {
				powered[signaller.color] = power;
			}

		}

		for(int i = 0; i < 16; i++) {
			rs.nextRedstoneLevel[i] = powered[i];
		}

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
			for(int i = 0; i < 16; i++) {
				int r = rs.redstoneLevels[i];
				if (rs.nextRedstoneLevel[i] != -128) {
					r = rs.nextRedstoneLevel[i];
				}
				if(r > 0) {
					addInfo(info, "redstone", StringHelper.localize("info.thermaldynamics.relay.color." + i) + " - " + r);
				}
			}
		}
		super.addInfo(info, player, debug);
	}
}
