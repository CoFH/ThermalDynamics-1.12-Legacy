package cofh.thermaldynamics.duct.attachments.filter;

import cofh.core.util.CoreUtils;
import cofh.core.util.item.ISpecialFilterFluid;
import cofh.core.util.item.ISpecialFilterItem;
import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import static cofh.thermaldynamics.duct.attachments.servo.ServoItem.maxSize;

public class FilterLogic implements IFilterItems, IFilterFluid, IFilterConfig {

	public static final int[] maxFilterItems = { 3, 6, 9, 12, 15 };
	public static final int[] maxFilterItemWidth = { 3, 3, 3, 4, 5 };
	private final static int flagBlackList = 0;
	private final static int flagIgnoreMetadata = 1;
	private final static int flagIgnoreNBT = 2;
	private final static int flagIgnoreOreDictionary = 3;
	private final static int flagIgnoreMod = 4;
	public final static String[] flagTypes = { "whiteList", "metadata", "nbt", "oreDict", "modSorting" };

	private final ItemStack[] items;

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
	LinkedList<CustomFilterItem> customFilterItems;
	LinkedList<CustomFilterFluid> customFilterFluids;

	public static int[] options = { 0, 1, 6, 6, 6 };
	private final Duct.Type transferType;
	private int[] validFlags;
	public boolean levelsChanged;

	public FilterLogic(int type, Duct.Type transferType, ConnectionBase connection) {

		this.type = type;
		this.transferType = transferType;

		items = new ItemStack[maxFilterItems[type]];

		if (transferType == Duct.Type.ITEM) {
			quickItems = new LinkedList<>();
		} else if (transferType == Duct.Type.FLUID) {
			fluidsSimple = new HashSet<>();
			fluidsNBT = new HashSet<>();
		}
		this.connection = connection;

		initLevels();
	}

	public int[] getValidLevels() {

		return validLevels;
	}

	public void calcItems() {

		synchronized (items) {
			recalc = false;

			if (isItem()) {
				quickItems.clear();

				if (!flags[flagIgnoreOreDictionary]) {
					if (oreIds == null) {
						oreIds = new TIntHashSet();
					} else {
						oreIds.clear();
					}
				} else {
					oreIds = null;
				}

				if (!flags[flagIgnoreMod]) {
					if (modNames == null) {
						modNames = new HashSet<>();
					} else {
						modNames.clear();
					}
				} else {
					modNames = null;
				}

				customFilterItems = null;

			} else if (isFluid()) {
				fluidsSimple.clear();
				fluidsNBT.clear();

				customFilterFluids = null;
			}

			itemLoop:
			for (ItemStack item : items) {
				if (item != null) {
					if (isItem()) {
						if (item.getItem() instanceof ISpecialFilterItem) {
							if (customFilterItems == null) {
								customFilterItems = new LinkedList<>();
							}

							customFilterItems.add(new CustomFilterItem(item));
						}

						if (!flags[flagIgnoreMod]) {
							modNames.add(CoreUtils.getModName(item.getItem()));
						}
						if (!flags[flagIgnoreOreDictionary]) {
							ArrayList<Integer> allOreIDs = OreDictionaryArbiter.getAllOreIDs(item);
							if (allOreIDs != null) {
								for (Integer integer : allOreIDs) {
									if (!oreIds.contains(integer)) {
										oreIds.add(integer);
									}
								}
							}
						}
						ItemStack d = item.copy();
						if (flags[flagIgnoreMetadata]) {
							d.setItemDamage(0);
						}
						if (flags[flagIgnoreNBT]) {
							d.setTagCompound(null);
						}

						for (ItemStack i : quickItems) {
							if (ItemHelper.itemsEqualWithMetadata(d, i)) {
								continue itemLoop;
							}
						}

						quickItems.add(d);

					} else if (isFluid()) {
						if (item.getItem() instanceof ISpecialFilterFluid) {
							if (customFilterFluids == null) {
								customFilterFluids = new LinkedList<>();
							}

							customFilterFluids.add(new CustomFilterFluid(item));
						}

						FluidStack fluidStack = FluidHelper.getFluidForFilledItem(item);
						if (fluidStack != null) {
							fluidStack = fluidStack.copy();
							fluidStack.amount = 1;

							if (fluidStack.tag == null || flags[flagIgnoreNBT]) {
								fluidsSimple.add(fluidStack.getFluid());
							} else {
								fluidsNBT.add(fluidStack);
							}
						}
					}
				}
			}

		}

	}

	public boolean isFluid() {

		return transferType == Duct.Type.FLUID;
	}

	public boolean isItem() {

		return transferType == Duct.Type.ITEM;
	}

	@Override
	public boolean matchesFilter(ItemStack item) {

		if (recalc) {
			calcItems();
		}

		boolean returnValue = !flags[flagBlackList];

		if (customFilterItems != null) {
			for (CustomFilterItem customFilterItem : customFilterItems) {
				if (customFilterItem.filter.matchesItem(customFilterItem.filterStack, item)) {
					return returnValue;
				}
			}
		}

		if (!flags[flagIgnoreMod]) {
			if (modNames.contains(item.getItem().getRegistryName().getResourceDomain())) {
				return returnValue;
			}
		}

		if (!flags[flagIgnoreOreDictionary] && !oreIds.isEmpty()) {
			ArrayList<Integer> allOreIDs = OreDictionaryArbiter.getAllOreIDs(item);
			if (allOreIDs != null) {
				for (Integer integer : allOreIDs) {
					if (oreIds.contains(integer)) {
						return returnValue;
					}
				}
			}
		}

		for (ItemStack filter : quickItems) {
			if (filter.getItem() != item.getItem()) {
				continue;
			}

			if (!flags[flagIgnoreMetadata] && filter.getItemDamage() != item.getItemDamage()) {
				continue;
			}

			if (!flags[flagIgnoreNBT] && !ItemHelper.doNBTsMatch(item.getTagCompound(), filter.getTagCompound())) {
				continue;
			}

			return returnValue;
		}
		return !returnValue;
	}

	@Override
	public boolean shouldIncRouteItems() {

		return levels[levelConservativeMode] == 1;
	}

	@Override
	public int getMaxStock() {

		return levels[levelRetainSize] == 0 ? Integer.MAX_VALUE : levels[levelRetainSize];
	}

	@Override
	public ItemStack[] getFilterStacks() {

		return items;
	}

	@Override
	public void onChange() {

		recalc = true;
	}

	@Override
	public int filterStackGridWidth() {

		return maxFilterItemWidth[type];
	}

	@Override
	public boolean getFlag(int flagType) {

		return flags[flagType];
	}

	@Override
	public boolean setFlag(int flagType, boolean flag) {

		if (!canAlterFlag(transferType, type, flagType)) {
			return false;
		}
		if (connection.tile.world().isRemote) {
			connection.sendFilterConfigPacketFlag(flagType, flag);
		} else {
			connection.tile.markChunkDirty();
		}
		flags[flagType] = flag;
		recalc = true;
		return true;
	}

	@Override
	public String flagType(int flagType) {

		return flagTypes[flagType];
	}

	@Override
	public int numFlags() {

		return flags.length;
	}

	public int[] validFlags() {

		return validFlags;
	}

	@Override
	public boolean canAlterFlag(int flagType) {

		return canAlterFlag(transferType, type, flagType);
	}

	public static boolean canAlterFlag(Duct.Type transferType, int type, int flagType) {

		return (transferType == Duct.Type.ITEM && options[type] >= flagType) || (transferType == Duct.Type.FLUID && (flagType == flagBlackList || flagType == flagIgnoreNBT));
	}

	public void readFromNBT(NBTTagCompound tag) {

		NBTTagList list = tag.getTagList("Inventory", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			int slot = nbt.getInteger("Slot");

			if (slot >= 0 && slot < items.length) {
				items[slot] = ItemStack.loadItemStackFromNBT(nbt);
			}
		}
		recalc = true;

		handleFlagByte(tag.getByte("Flags"));

		NBTTagCompound nbt = tag.getCompoundTag("Levels");
		for (int i = 0; i < levels.length; i++) {
			if (nbt.hasKey("Level" + i)) {
				levels[i] = Math.max(minLevels[type][i], Math.min(maxLevels[type][i], nbt.getInteger("Level" + i)));
			} else {
				levels[i] = defaultLevels[i];
			}
		}
	}

	public void writeToNBT(NBTTagCompound tag) {

		NBTTagList list = new NBTTagList();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("Slot", i);
				items[i].writeToNBT(nbt);
				list.appendTag(nbt);
			}
		}
		tag.setTag("Inventory", list);
		tag.setByte("Flags", (byte) getFlagByte());

		NBTTagCompound nbt = new NBTTagCompound();
		for (int i = 0; i < levels.length; i++) {
			nbt.setInteger("Level" + i, levels[i]);
		}
		tag.setTag("Levels", nbt);
	}

	@Override
	public boolean allowFluid(FluidStack fluid) {

		if (fluid == null) {
			return flags[flagBlackList];
		}
		if (recalc) {
			calcItems();
		}

		if (customFilterFluids != null) {
			for (CustomFilterFluid customFilterFluids : this.customFilterFluids) {
				if (customFilterFluids.filter.matchesFluid(customFilterFluids.filterStack, fluid)) {
					return !flags[flagBlackList];
				}
			}
		}

		if (fluid.tag == null || flags[flagIgnoreNBT]) {
			return !flags[flagBlackList] == fluidsSimple.contains(fluid.getFluid());
		} else {
			fluid = fluid.copy();
			fluid.amount = 1;
			return !flags[flagBlackList] == fluidsNBT.contains(fluid);
		}
	}

	public int getFlagByte() {

		int t = 0;
		for (int i = 0; i < flags.length; i++) {
			if (flags[i]) {
				t = t | (1 << i);
			}
		}
		return t;
	}

	public void handleFlagByte(int t) {

		for (int i = 0; i < flags.length; i++) {
			if (canAlterFlag(i)) {
				flags[i] = (t & (1 << i)) != 0;
			} else {
				flags[i] = defaultflags[i];
			}
		}
	}

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

		public boolean appliesTo(FilterLogic base) {

			return base.transferType == ductType && (base.connection.getId() != AttachmentRegistry.FILTER_FLUID || filter) && (base.connection.getId() != AttachmentRegistry.FILTER_ITEM || filter) && (base.connection.getId() != AttachmentRegistry.SERVO_ITEM || servo) && (base.connection.getId() != AttachmentRegistry.SERVO_FLUID || servo) && (base.connection.getId() != AttachmentRegistry.RETRIEVER_ITEM || servo) && (base.connection.getId() != AttachmentRegistry.RETRIEVER_FLUID || servo);
		}
	}

	public static final Perm[] levelPerms = { Perm.SERVO, Perm.SERVO, Perm.FILTER, Perm.ALL };
	public static final int[][] minLevels = { { 1, 0, 0, 0 }, { 1, 0, 0, 0 }, { 1, 0, 0, 0 }, { 1, 0, 0, 0 }, { 1, 0, 0, 0 }, };

	public static final int[][] maxLevels = { { maxSize[0], 0, 1, 0 }, { maxSize[1], 0, 1, 0 }, { maxSize[2], 3, 1, 64 }, { maxSize[3], 3, 1, 128 }, { maxSize[4], 3, 1, 320 } };

	public static final int[] defaultLevels = { 64, 0, 1, 0 };
	public int[] validLevels;
	public final static String[] levelNames = { "stacksize", "routeType", "antiSpam", "stockSize" };

	private final int[] levels = new int[defaultLevels.length];

	private void initLevels() {

		TIntArrayList vLevels = new TIntArrayList(levels.length);

		for (int i = 0; i < levels.length; i++) {
			levels[i] = Math.max(Math.min(defaultLevels[i], maxLevels[type][i]), minLevels[type][i]);
			if (i != levelStackSize && i != levelRetainSize && levelPerms[i].appliesTo(this) && minLevels[type][i] < maxLevels[type][i]) {
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

	public final static int levelStackSize = 0;
	public final static int levelRouteMode = 1;
	public final static int levelConservativeMode = 2;
	public final static int levelRetainSize = 3;

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

		setLevel(i, level, true);
	}

	public void setLevel(int i, int level, boolean sendUpdate) {

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
		if (connection.tile.world().isRemote && sendUpdate) {
			connection.sendFilterConfigPacketLevel(i, level);
		} else {
			connection.tile.markChunkDirty();
			levelsChanged = true;
		}
		levels[i] = level;
	}

	public int getLevel(int i) {

		return levels[i];
	}

	private static class CustomFilterItem {

		public ISpecialFilterItem filter;
		public ItemStack filterStack;

		public CustomFilterItem(ItemStack filterStack) {

			this.filter = (ISpecialFilterItem) filterStack.getItem();
			this.filterStack = filterStack;
		}
	}

	private static class CustomFilterFluid {

		public ISpecialFilterFluid filter;
		public ItemStack filterStack;

		public CustomFilterFluid(ItemStack filterStack) {

			this.filter = (ISpecialFilterFluid) filterStack.getItem();
			this.filterStack = filterStack;
		}
	}

}
