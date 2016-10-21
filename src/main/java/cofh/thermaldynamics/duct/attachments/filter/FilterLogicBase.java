package cofh.thermaldynamics.duct.attachments.filter;

import static cofh.thermaldynamics.duct.attachments.servo.ServoItem.maxSize;

import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public abstract class FilterLogicBase implements IFilterConfig {

	public enum Perm {
		FILTER(true, false, Duct.Type.ITEM), SERVO(false, true, Duct.Type.ITEM), ALL(true, true, Duct.Type.ITEM);

		public final boolean filter;
		public final boolean servo;
		public final Duct.Type ductType;

		Perm(boolean filter, boolean servo, Duct.Type ductType) {

			this.filter = filter;
			this.servo = servo;
			this.ductType = ductType;
		}

		public boolean appliesTo(FilterLogicBase base) {

			return base.transferType == ductType && (base.connection.getId() != AttachmentRegistry.FILTER_FLUID || filter)
					&& (base.connection.getId() != AttachmentRegistry.FILTER_ITEM || filter)
					&& (base.connection.getId() != AttachmentRegistry.SERVO_ITEM || servo)
					&& (base.connection.getId() != AttachmentRegistry.SERVO_FLUID || servo)
					&& (base.connection.getId() != AttachmentRegistry.RETRIEVER_ITEM || servo)
					&& (base.connection.getId() != AttachmentRegistry.RETRIEVER_FLUID || servo);
		}
	}

	public static final Perm[] levelPerms = { Perm.SERVO, Perm.SERVO, Perm.FILTER };
	public static final int[][] minLevels = { { 1, 0, 0 }, { 1, 0, 0 }, { 1, 0, 0 }, { 1, 0, 0 }, { 1, 0, 0 }, };

	public static final int[][] maxLevels = { { maxSize[0], 0, 0 }, { maxSize[1], 0, 0 }, { maxSize[2], 3, 1 }, { maxSize[3], 3, 1 }, { maxSize[4], 3, 1 }, };
	public static final int[] defaultLevels = { 64, 0, 1 };
	public int[] validLevels;
	public final static String[] levelNames = { "stacksize", "routeType", "antiSpam" };

	private final int[] levels = new int[defaultLevels.length];

	public final static int levelStacksize = 0;
	public final static int levelRouteMode = 1;
	public final static int levelConservativeMode = 2;

	public static final int[] maxFilterItems = { 3, 6, 9, 12, 15 };
	public static final int[] maxFilterItemWidth = { 3, 3, 3, 4, 5 };
	protected final static int flagBlackList = 0;
	protected final static int flagIgnoreMetadata = 1;
	protected final static int flagIgnoreNBT = 2;
	protected final static int flagIgnoreOreDictionary = 3;
	protected final static int flagIgnoreMod = 4;
	public final static String[] flagTypes = { "whiteList", "metadata", "nbt", "oreDict", "modSorting" };

	protected final ItemStack[] items;

	public boolean recalc = true;
	public final ConnectionBase connection;
	public final int type;
	public static final boolean[] defaultflags = { true, false, false, true, true };
	boolean[] flags = { true, false, false, true, true };

	TIntHashSet oreIds;
	LinkedList<ItemStack> quickItems;

	HashSet<String> modNames;
	HashSet<Fluid> fluidsSimple;
	HashSet<FluidStack> fluidsNBT;

	public static int[] options = { 0, 1, 6, 6, 6 };
	protected final Duct.Type transferType;
	protected int[] validFlags;
	public boolean levelsChanged;

	public FilterLogicBase(int type, Duct.Type transferType, ConnectionBase connection) {

		this.type = type;
		this.transferType = transferType;

		items = new ItemStack[maxFilterItems[type]];

		if (transferType == Duct.Type.ITEM) {
			quickItems = new LinkedList<ItemStack>();
		} else if (transferType == Duct.Type.FLUID) {
			fluidsSimple = new HashSet<Fluid>();
			fluidsNBT = new HashSet<FluidStack>();
		}
		this.connection = connection;
		initLevels();
	}

	protected void initLevels() {

		TIntArrayList vLevels = new TIntArrayList(levels.length);

		for (int i = 0; i < levels.length; i++) {
			levels[i] = Math.max(Math.min(defaultLevels[i], maxLevels[type][i]), minLevels[type][i]);
			if (i != levelStacksize && levelPerms[i].appliesTo(this) && minLevels[type][i] < maxLevels[type][i]) {
				vLevels.add(i);
			}
		}
		validLevels = vLevels.toArray();

		vLevels.clear();

		for (int i = 0; i < numFlags(); i++) {
			if (canAlterFlag(i)) {
				vLevels.add(i);
			}
		}
		validFlags = vLevels.toArray();
	}

	public void incLevel(int i) {

		incLevel(i, 1, true);
	}

	public void decLevel(int i) {

		decLevel(i, 1, true);
	}

	public void incLevel(int i, int amount, boolean wrap) {

		int l = getLevel(i) + amount;
		if (l > maxLevels[type][i]) {
			if (wrap) {
				l = minLevels[type][i];
			} else {
				l = maxLevels[type][i];
			}
		}
		setLevel(i, l);
	}

	public void decLevel(int i, int amount, boolean wrap) {

		int l = getLevel(i) - amount;
		if (l < minLevels[type][i]) {
			if (wrap) {
				l = maxLevels[type][i];
			} else {
				l = minLevels[type][i];
			}
		}
		setLevel(i, l);
	}

	public void setLevel(int i, int level) {

		if (level < minLevels[type][i]) {
			level = minLevels[type][i];
		}
		if (level > maxLevels[type][i]) {
			level = maxLevels[type][i];
		}
		if (levels[i] == level) {
			return;
		}
		if (!levelPerms[i].appliesTo(this)) {
			return;
		}
		if (connection.tile.world().isRemote) {
			connection.sendFilterConfigPacketLevel(i, level);
		} else {
			connection.tile.markDirty();
			levelsChanged = true;
		}
		levels[i] = level;
	}

	public int getLevel(int i) {

		return levels[i];
	}

	/* IFilterConfig */
	@Override
	public ItemStack[] getFilterStacks() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onChange() {

		// TODO Auto-generated method stub

	}

	@Override
	public int filterStackGridWidth() {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getFlag(int flagType) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setFlag(int flagType, boolean flag) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canAlterFlag(int flagType) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String flagType(int flagType) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numFlags() {

		// TODO Auto-generated method stub
		return 0;
	}

}
