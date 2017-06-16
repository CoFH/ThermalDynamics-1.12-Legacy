package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.util.WorldGridList;
import com.google.common.math.DoubleMath;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public abstract class MultiBlockGridTracking<T extends IGridTile> extends MultiBlockGrid<T> {

	public MultiBlockGridTracking(WorldGridList worldGrid) {

		super(worldGrid);
	}

	public MultiBlockGridTracking(World worldObj) {

		super(worldObj);
	}

	private Tracker tracker;

	public Tracker getTracker() {

		if (tracker == null) {
			tracker = new Tracker(getLevel());
		}
		tracker.life = 0;
		return tracker;
	}

	public abstract int getLevel();

	protected abstract String getUnit();

	public int trackIn(int a, boolean simulate) {

		if (!simulate && tracker != null) {
			tracker.stuffIn(a);
		}
		return a;
	}

	public int trackOut(int a, boolean simulate) {

		if (!simulate && tracker != null) {
			tracker.stuffOut(a);
		}
		return a;
	}

	public int trackInOut(int a, boolean simulate) {

		if (!simulate && tracker != null) {
			tracker.stuffIn(a);
			tracker.stuffOut(a);
		}
		return a;
	}

	@Override
	public void tickGrid() {

		super.tickGrid();
		if (tracker != null) {
			tracker.newTick(getLevel());
			if (tracker.life > Tracker.LIFESPAN) {
				tracker = null;
			}
		}
	}

	@Override
	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {

		super.addInfo(info, player, debug);
		addInfo(info, "tracker.cur", format(getLevel()));

		if (tracker == null) {
			info.add(new TextComponentTranslation("info.thermaldynamics.info.tracker.activate"));
			getTracker();
			return;
		}
		tracker.life = 0;

		addInfo(info, "tracker.avg", format(tracker.avgStuff()) + getUnit());
		addInfo(info, "tracker.avgInOut", String.format("+%s%s/-%s%s", format(tracker.avgStuffIn()), getUnit(), format(tracker.avgStuffOut()), getUnit()));
	}

	private String format(double v) {

		if (v == 0) {
			return "0";
		}
		if (DoubleMath.isMathematicalInteger(v)) {
			return Integer.toString((int) v);
		}
		return String.format("%.2f", v);
	}

}
