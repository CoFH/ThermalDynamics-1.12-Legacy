package thermaldynamics.ducts.attachments.filter;

import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.registry.GameData;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.ducts.attachments.ConnectionBase;

public class FilterLogic implements IFilterItems, IFilterFluid, IFilterConfig {
    public static final int[] maxFilterItems = {1, 4, 9, 12, 16};
    public static final int[] maxFilterItemWidth = {1, 2, 3, 4, 8};
    private final static int flagBlackList = 0;
    private final static int flagIngoreMetadata = 1;
    private final static int flagIgnoreNBT = 2;
    private final static int flagIgnoreOreDictionary = 3;
    private final static int flagIgnoreMod = 4;
    public final static String[] flagTypes = {"whiteList", "metadata", "nbt", "oreDic", "modSorting"};

    private final ItemStack[] items;

    public boolean recalc = true;
    public final ConnectionBase connection;
    public final int type;
    public static final boolean[] defaultflags = {true, false, false, true, true};
    boolean[] flags = {true, false, false, true, true};

    TIntHashSet oreIds;
    LinkedList<ItemStack> quickItems;

    HashSet<String> modNames;
    HashSet<FluidStack> fluidsNBT;

    public static int[] options = {0, 1, 6, 6, 6};
    private Ducts.Type transferType;
    private int[] validFlags;

    public FilterLogic(int type, Ducts.Type transferType, ConnectionBase connection) {
        this.type = type;
        this.transferType = transferType;

        items = new ItemStack[maxFilterItems[type]];

        if (transferType == Ducts.Type.Item) {
            quickItems = new LinkedList<ItemStack>();
        } else if (transferType == Ducts.Type.Fluid) {
            fluidsNBT = new HashSet<FluidStack>();
        }

        this.connection = connection;

        initLevels();
    }

    public int[] getValidLevels() {
        return validLevels;
    }

    public void calcItems() {
        if (isItem()) {

            quickItems.clear();


            if (!flags[flagIgnoreOreDictionary]) {
                if (oreIds == null) oreIds = new TIntHashSet();
                else oreIds.clear();
            } else oreIds = null;


            if (!flags[flagIgnoreMod]) {
                if (modNames == null) modNames = new HashSet<String>();
                else modNames.clear();
            } else modNames = null;

        } else if (isFluid()) {
            fluidsNBT.clear();
        }

        synchronized (items) {
            boolean flag = true;
            for (ItemStack item : items) {
                if (item != null) {
                    if (isItem()) {
                        if (!flags[flagIgnoreMod]) {
                            modNames.add(getModName(item.getItem()));
                        }

                        if (!flags[flagIgnoreOreDictionary]) {
                            ArrayList<Integer> allOreIDs = OreDictionaryArbiter.getAllOreIDs(item);
                            if (allOreIDs != null)
                                for (Integer integer : allOreIDs) {
                                    if (!oreIds.contains(integer))
                                        oreIds.add(integer.intValue());
                                }
                        }

                        ItemStack d = item.copy();
                        if (flags[flagIngoreMetadata]) d.setItemDamage(0);
                        if (flags[flagIgnoreNBT]) d.setTagCompound(null);

                        for (ItemStack i : quickItems) {
                            if (ItemHelper.itemsEqualWithMetadata(d, i)) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) quickItems.add(d);

                    } else if (isFluid()) {

                        FluidStack fluidStack = FluidHelper.getFluidForFilledItem(item);
                        if (fluidStack != null) {
                            fluidStack.amount = 1;
                            fluidsNBT.add(fluidStack);
                        }

                    }
                }
            }
            recalc = false;
        }

    }

    public boolean isFluid() {
        return transferType == Ducts.Type.Fluid;
    }

    public boolean isItem() {
        return transferType == Ducts.Type.Item;
    }

    public boolean matchesFilter(ItemStack item) {
        if (recalc) calcItems();

        if (!flags[flagIgnoreMod]) {
            if (modNames.contains(getModName(item.getItem())))
                return !flags[flagBlackList];
        }

        if (!flags[flagIgnoreOreDictionary] && !oreIds.isEmpty()) {
            ArrayList<Integer> allOreIDs = OreDictionaryArbiter.getAllOreIDs(item);
            if (allOreIDs != null)
                for (Integer integer : allOreIDs) {
                    if (oreIds.contains(integer))
                        return !flags[flagBlackList];
                }
        }

        for (ItemStack filter : quickItems) {
            if (filter.getItem() != item.getItem())
                continue;

            if (!flags[flagIngoreMetadata] && filter.getItemDamage() != item.getItemDamage())
                continue;

            if (!flags[flagIgnoreNBT] && !ItemHelper.doNBTsMatch(item.stackTagCompound, filter.stackTagCompound))
                continue;

            return !flags[flagBlackList];
        }
        return flags[flagBlackList];
    }

    @Override
    public boolean shouldIncRouteItems() {
        return levels[levelConservativeMode] == 1;
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
    public void setFlag(int flagType, boolean flag) {
        if (!canAlterFlag(transferType, type, flagType)) {
            return;
        }

        if (connection.tile.world().isRemote) {
            connection.sendFilterConfigPacketFlag(flagType, flag);
        } else connection.tile.markDirty();

        flags[flagType] = flag;
        recalc = true;
    }

    @Override
    public String flagType(int flagType) {
        return flagTypes[flagType];
    }

    @Override
    public int numFlags() {
        return transferType == Ducts.Type.Item ? flags.length : 0;
    }

    public int[] validFlags() {
        return validFlags;
    }

    @Override
    public boolean canAlterFlag(int flagType) {
        return canAlterFlag(transferType, type, flagType);
    }

    public static boolean canAlterFlag(Ducts.Type transferType, int type, int flagType) {
        return transferType == Ducts.Type.Item && options[type] >= flagType;
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
                levels[i] = Math.max(minLevels[type][i], Math.min(maxLevels[type][i], nbt.getInteger("Level" + i))
                );
            } else
                levels[i] = defaultLevels[i];

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
        if (fluid == null) return false;
        if (recalc) calcItems();
        fluid = fluid.copy();
        fluid.amount = 1;
        return fluidsNBT.contains(fluid);
    }

    public int getFlagByte() {
        int t = 0;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) t = t | (1 << i);
        }
        return t;
    }

    public void handleFlagByte(int t) {
        for (int i = 0; i < flags.length; i++) {
            flags[i] = (t & (1 << i)) != 0;
        }
    }

    //TODO: Move to COFHCore somewhere
    public static String getModName(Item item) {
        String s = GameData.getItemRegistry().getNameForObject(item);
        return s.substring(0, s.indexOf(':'));
    }

    public int getNumLevels() {
        return validLevels.length;
    }

    public static enum Perm {
        FILTER(true, false, Ducts.Type.Item),
        SERVO(false, true, Ducts.Type.Item),
        ALL(true, true, Ducts.Type.Item);

        public final boolean filter;
        public final boolean servo;
        public final Ducts.Type ductType;

        Perm(boolean filter, boolean servo, Ducts.Type ductType) {
            this.filter = filter;
            this.servo = servo;
            this.ductType = ductType;
        }

        public boolean appliesTo(FilterLogic base) {
            return base.transferType == ductType &&
                    (base.connection.getID() != AttachmentRegistry.FILTER_FLUID || filter) &&
                    (base.connection.getID() != AttachmentRegistry.FILTER_INV || filter) &&
                    (base.connection.getID() != AttachmentRegistry.SERVO_INV || servo) &&
                    (base.connection.getID() != AttachmentRegistry.SERVO_FLUID || servo);
        }
    }

    public static final Perm[] levelPerms = {Perm.SERVO, Perm.SERVO, Perm.FILTER};
    public static final int[][] minLevels = {
            {1, 2, 0},
            {1, 0, 0},
            {1, 0, 0},
            {1, 0, 0},
            {1, 0, 0},
    };

    public static final int[][] maxLevels = {
            {8, 2, 0},
            {16, 0, 0},
            {64, 3, 1},
            {64, 3, 1},
            {64, 3, 1},
    };
    public static final int[] defaultLevels = {64, 0, 1};
    public static int[] validLevels;

    private int[] levels = new int[defaultLevels.length];

    private void initLevels() {
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
            if (canAlterFlag(i)) vLevels.add(i);
        }

        validFlags = vLevels.toArray();


    }

    public final static int levelStacksize = 0;
    public final static int levelRouteMode = 1;
    public final static int levelConservativeMode = 2;

    public void incLevel(int i) {
        int l = getLevel(i) + 1;
        if (l > maxLevels[type][i]) l = minLevels[type][i];
        setLevel(i, l);
    }

    public void setLevel(int i, int level) {
        if (level < minLevels[type][i]) level = minLevels[type][i];
        if (level > maxLevels[type][i]) level = maxLevels[type][i];

        if (levels[i] == level)
            return;

        if (!levelPerms[i].appliesTo(this))
            return;

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

    public boolean levelsChanged;
}
