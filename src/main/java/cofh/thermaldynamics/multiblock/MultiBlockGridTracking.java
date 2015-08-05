package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.core.WorldGridList;
import com.google.common.math.DoubleMath;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public abstract class MultiBlockGridTracking extends MultiBlockGrid {

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


	public int trackIn(int a, boolean simulate) {
		if (!simulate && tracker != null)
			tracker.stuffIn(a);
		return a;
	}

	public int trackOut(int a, boolean simulate) {
		if (!simulate && tracker != null)
			tracker.stuffOut(a);
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
			if(tracker.life > Tracker.LIFESPAN)
				tracker = null;
		}
	}

	@Override
	public void addInfo(List<IChatComponent> info, EntityPlayer player, boolean debug) {
		super.addInfo(info, player, debug);
		addInfo(info, "tracker.cur", format(getLevel()));

		if (tracker == null) {
			info.add(new ChatComponentTranslation("info.thermaldynamics.info.tracker.activate"));
			getTracker();
			return;
		}

		tracker.life = 0;

		addInfo(info, "tracker.avg", format(tracker.avgStuff()) + getUnit());
		addInfo(info, "tracker.avgInOut", String.format("+%s%s/-%s%s", format(tracker.avgStuffIn()), getUnit(), format(tracker.avgStuffOut()), getUnit()));
	}

	private String format(double v) {
		if (v == 0) return "0";
		if (DoubleMath.isMathematicalInteger(v)) return Integer.toString((int) v);
		return String.format("%.2f", v);
	}

	protected abstract String getUnit();
}
