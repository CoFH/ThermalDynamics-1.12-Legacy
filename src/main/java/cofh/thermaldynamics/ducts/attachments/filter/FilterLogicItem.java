package cofh.thermaldynamics.ducts.attachments.filter;
//package thermaldynamics.ducts.attachments.filter;
//
//import static thermaldynamics.ducts.attachments.servo.ServoItem.maxSize;
//
//import cofh.core.util.CoreUtils;
//import cofh.core.util.oredict.OreDictionaryArbiter;
//import cofh.lib.util.helpers.ItemHelper;
//import cofh.lib.util.helpers.ServerHelper;
//
//import gnu.trove.list.array.TIntArrayList;
//import gnu.trove.set.hash.TIntHashSet;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.LinkedList;
//
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.nbt.NBTTagList;
//
//import thermaldynamics.ducts.attachments.ConnectionBase;
//
//public class FilterLogicItem implements IFilterItems, IFilterConfig {
//
//	public static final int[] maxFilter = { 3, 6, 9, 12, 15 };
//	public static final int[] maxFilterWidth = { 3, 3, 3, 4, 5 };
//	private final static int flagBlackList = 0;
//	private final static int flagIgnoreMetadata = 1;
//	private final static int flagIgnoreNBT = 2;
//	private final static int flagIgnoreOreDictionary = 3;
//	private final static int flagIgnoreMod = 4;
//	public final static String[] flagTypes = { "whiteList", "metadata", "nbt", "oreDict", "modSorting" };
//
//	private final ItemStack[] filterStacks;
//
//	public boolean recalc = true;
//	public final ConnectionBase connection;
//	public final int type;
//	public static final boolean[] defaultflags = { true, false, false, true, true };
//	boolean[] flags = { true, false, false, true, true };
//
//	TIntHashSet oreIds;
//	LinkedList<ItemStack> quickItems;
//	HashSet<String> modNames;
//
//	public static int[] options = { 0, 1, 6, 6, 6 };
//	private int[] validFlags;
//	public boolean levelsChanged;
//
//	public FilterLogicItem(int type, ConnectionBase connection) {
//
//		this.type = type;
//		filterStacks = new ItemStack[maxFilter[type]];
//		quickItems = new LinkedList<ItemStack>();
//		this.connection = connection;
//
//		initLevels();
//	}
//
//	public int[] getValidLevels() {
//
//		return validLevels;
//	}
//
//	public void calcItems() {
//
//		quickItems.clear();
//		if (!flags[flagIgnoreOreDictionary]) {
//			if (oreIds == null) {
//				oreIds = new TIntHashSet();
//			} else {
//				oreIds.clear();
//			}
//		} else {
//			oreIds = null;
//		}
//
//		if (!flags[flagIgnoreMod]) {
//			if (modNames == null) {
//				modNames = new HashSet<String>();
//			} else {
//				modNames.clear();
//			}
//		} else {
//			modNames = null;
//		}
//
//		synchronized (filterStacks) {
//			boolean flag = true;
//			for (ItemStack item : filterStacks) {
//				if (item != null) {
//					if (!flags[flagIgnoreMod]) {
//						modNames.add(CoreUtils.getModName(item.getItem()));
//					}
//					if (!flags[flagIgnoreOreDictionary]) {
//						ArrayList<Integer> allOreIDs = OreDictionaryArbiter.getAllOreIDs(item);
//						if (allOreIDs != null)
//							for (Integer integer : allOreIDs) {
//								if (!oreIds.contains(integer))
//									oreIds.add(integer.intValue());
//							}
//					}
//					ItemStack d = item.copy();
//					if (flags[flagIgnoreMetadata]) {
//						d.setItemDamage(0);
//					}
//					if (flags[flagIgnoreNBT]) {
//						d.setTagCompound(null);
//					}
//					for (ItemStack i : quickItems) {
//						if (ItemHelper.itemsEqualWithMetadata(d, i)) {
//							flag = false;
//							break;
//						}
//					}
//					if (flag) {
//						quickItems.add(d);
//					}
//				}
//			}
//			recalc = false;
//		}
//	}
//
//	@Override
//	public boolean matchesFilter(ItemStack item) {
//
//		if (recalc) {
//			calcItems();
//		}
//
//		if (!flags[flagIgnoreMod]) {
//			if (modNames.contains(CoreUtils.getModName(item.getItem()))) {
//				return !flags[flagBlackList];
//			}
//		}
//		if (!flags[flagIgnoreOreDictionary] && !oreIds.isEmpty()) {
//			ArrayList<Integer> allOreIDs = OreDictionaryArbiter.getAllOreIDs(item);
//			if (allOreIDs != null) {
//				for (Integer integer : allOreIDs) {
//					if (oreIds.contains(integer)) {
//						return !flags[flagBlackList];
//					}
//				}
//			}
//		}
//
//		for (ItemStack filter : quickItems) {
//			if (filter.getItem() != item.getItem()) {
//				continue;
//			}
//			if (!flags[flagIgnoreMetadata] && filter.getItemDamage() != item.getItemDamage()) {
//				continue;
//			}
//			if (!flags[flagIgnoreNBT] && !ItemHelper.doNBTsMatch(item.stackTagCompound, filter.stackTagCompound)) {
//				continue;
//			}
//			return !flags[flagBlackList];
//		}
//		return flags[flagBlackList];
//	}
//
//	@Override
//	public boolean shouldIncRouteItems() {
//
//		return levels[levelConservativeMode] == 1;
//	}
//
//	@Override
//	public ItemStack[] getFilterStacks() {
//
//		return filterStacks;
//	}
//
//	@Override
//	public void onChange() {
//
//		recalc = true;
//	}
//
//	@Override
//	public int filterStackGridWidth() {
//
//		return maxFilterWidth[type];
//	}
//
//	@Override
//	public boolean getFlag(int flagType) {
//
//		return flags[flagType];
//	}
//
//	@Override
//	public boolean setFlag(int flagType, boolean flag) {
//
//		if (!canAlterFlag(type, flagType)) {
//			return false;
//		}
//		if (connection.tile.world().isRemote) {
//			connection.sendFilterConfigPacketFlag(flagType, flag);
//		} else {
//			connection.tile.markDirty();
//		}
//		flags[flagType] = flag;
//		recalc = true;
//		return true;
//	}
//
//	@Override
//	public String flagType(int flagType) {
//
//		return flagTypes[flagType];
//	}
//
//	@Override
//	public int numFlags() {
//
//		return flags.length;
//	}
//
//	public int[] validFlags() {
//
//		return validFlags;
//	}
//
//	@Override
//	public boolean canAlterFlag(int flagType) {
//
//		return canAlterFlag(type, flagType);
//	}
//
//	public static boolean canAlterFlag(int type, int flagType) {
//
//		return options[type] >= flagType;
//	}
//
//	public void readFromNBT(NBTTagCompound tag) {
//
//		NBTTagList list = tag.getTagList("Inventory", 10);
//		for (int i = 0; i < list.tagCount(); i++) {
//			NBTTagCompound nbt = list.getCompoundTagAt(i);
//			int slot = nbt.getInteger("Slot");
//
//			if (slot >= 0 && slot < filterStacks.length) {
//				filterStacks[slot] = ItemStack.loadItemStackFromNBT(nbt);
//			}
//		}
//		recalc = true;
//
//		handleFlagByte(tag.getByte("Flags"));
//
//		NBTTagCompound nbt = tag.getCompoundTag("Levels");
//		for (int i = 0; i < levels.length; i++) {
//			if (nbt.hasKey("Level" + i)) {
//				levels[i] = Math.max(minLevels[type][i], Math.min(maxLevels[type][i], nbt.getInteger("Level" + i)));
//			} else
//				levels[i] = defaultLevels[i];
//
//		}
//	}
//
//	public void writeToNBT(NBTTagCompound tag) {
//
//		NBTTagList list = new NBTTagList();
//		for (int i = 0; i < filterStacks.length; i++) {
//			if (filterStacks[i] != null) {
//				NBTTagCompound nbt = new NBTTagCompound();
//				nbt.setInteger("Slot", i);
//				filterStacks[i].writeToNBT(nbt);
//				list.appendTag(nbt);
//			}
//		}
//		tag.setTag("Inventory", list);
//		tag.setByte("Flags", (byte) getFlagByte());
//
//		NBTTagCompound nbt = new NBTTagCompound();
//		for (int i = 0; i < levels.length; i++) {
//			nbt.setInteger("Level" + i, levels[i]);
//		}
//		tag.setTag("Levels", nbt);
//	}
//
//	public int getFlagByte() {
//
//		int t = 0;
//		for (int i = 0; i < flags.length; i++) {
//			if (flags[i]) {
//				t = t | (1 << i);
//			}
//		}
//		return t;
//	}
//
//	public void handleFlagByte(int t) {
//
//		for (int i = 0; i < flags.length; i++) {
//			flags[i] = (t & (1 << i)) != 0;
//		}
//	}
//
//	public int getNumLevels() {
//
//		return validLevels.length;
//	}
//
//	public static final int[][] minLevels = { { 1, 2, 0 }, { 1, 0, 0 }, { 1, 0, 0 }, { 1, 0, 0 }, { 1, 0, 0 }, };
//	public static final int[][] maxLevels = { { maxSize[0], 2, 0 }, { maxSize[1], 0, 0 }, { maxSize[2], 3, 1 }, { maxSize[3], 3, 1 }, { maxSize[4], 3, 1 }, };
//	public static final int[] defaultLevels = { 64, 0, 1 };
//	public int[] validLevels;
//
//	private final int[] levels = new int[defaultLevels.length];
//
//	private void initLevels() {
//
//		TIntArrayList vLevels = new TIntArrayList(levels.length);
//
//		for (int i = 0; i < levels.length; i++) {
//			levels[i] = Math.max(Math.min(defaultLevels[i], maxLevels[type][i]), minLevels[type][i]);
//			if (i != levelStacksize && minLevels[type][i] < maxLevels[type][i]) {
//				vLevels.add(i);
//			}
//		}
//		validLevels = vLevels.toArray();
//
//		vLevels.clear();
//
//		for (int i = 0; i < numFlags(); i++) {
//			if (canAlterFlag(i))
//				vLevels.add(i);
//		}
//		validFlags = vLevels.toArray();
//	}
//
//	public final static int levelStacksize = 0;
//	public final static int levelRouteMode = 1;
//	public final static int levelConservativeMode = 2;
//
//	public void incLevel(int i) {
//
//		incLevel(i, 1, true);
//	}
//
//	public void decLevel(int i) {
//
//		decLevel(i, 1, true);
//	}
//
//	public void incLevel(int i, int amount, boolean wrap) {
//
//		int l = getLevel(i) + amount;
//		if (l > maxLevels[type][i]) {
//			if (wrap) {
//				l = minLevels[type][i];
//			} else {
//				l = maxLevels[type][i];
//			}
//		}
//		setLevel(i, l);
//	}
//
//	public void decLevel(int i, int amount, boolean wrap) {
//
//		int l = getLevel(i) - amount;
//		if (l < minLevels[type][i]) {
//			if (wrap) {
//				l = maxLevels[type][i];
//			} else {
//				l = minLevels[type][i];
//			}
//		}
//		setLevel(i, l);
//	}
//
//	public void setLevel(int i, int level) {
//
//		if (level < minLevels[type][i]) {
//			level = minLevels[type][i];
//		}
//		if (level > maxLevels[type][i]) {
//			level = maxLevels[type][i];
//		}
//		if (levels[i] == level) {
//			return;
//		}
//		if (ServerHelper.isClientWorld(connection.tile.world())) {
//			connection.sendFilterConfigPacketLevel(i, level);
//		} else {
//			connection.tile.markDirty();
//			levelsChanged = true;
//		}
//		levels[i] = level;
//	}
//
//	public int getLevel(int i) {
//
//		return levels[i];
//	}
//
// }
