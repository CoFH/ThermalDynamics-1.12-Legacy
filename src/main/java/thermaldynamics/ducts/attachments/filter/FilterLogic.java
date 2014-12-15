package thermaldynamics.ducts.attachments.filter;

import cofh.core.util.oredict.OreDictionaryArbiter;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ItemHelper;
import gnu.trove.set.hash.TIntHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.ducts.attachments.ConnectionBase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class FilterLogic implements IFilterItems, IFilterFluid, IFilterConfig {
    public static final int[] maxFilterItems =     {1, 4, 9, 12, 16};
    public static final int[] maxFilterItemWidth = {1, 2, 3, 4,  8};

    private final ItemStack[] items;
    boolean[] flags = {true, false, false, true};

    private final static int flagBlackList = 0;
    private final static int flagIngoreMetadata = 1;
    private final static int flagIgnoreNBT = 2;
    private final static int flagIgnoreOreDictionary = 3;

    TIntHashSet oreIds;
    LinkedList<ItemStack> quickItems;
    HashSet<Fluid> fluidHashSet;
    LinkedList<FluidStack> fluidsNBT;
    public boolean recalc = true;
    public ConnectionBase duct;

    public FilterLogic(int type, Ducts.Type transferType, ConnectionBase duct) {
        this.type = type;
        this.transferType = transferType;

        items = new ItemStack[maxFilterItems[type]];

        if (transferType == Ducts.Type.Item) {
            oreIds = new TIntHashSet();
            quickItems = new LinkedList<ItemStack>();
        } else if (transferType == Ducts.Type.Fluid) {
            fluidHashSet = new HashSet<Fluid>();
            fluidsNBT = new LinkedList<FluidStack>();
        }

        this.duct = duct;
    }

    public int type;
    private Ducts.Type transferType;

    public void calcItems() {
        if (isItem()) {
            oreIds.clear();
            quickItems.clear();
        } else if (isFluid()) {
            fluidsNBT.clear();
        }

        synchronized (items) {
            boolean flag = true;
            for (ItemStack item : items) {
                if (item != null) {
                    if (isItem()) {
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
                            if (fluidStack.tag == null)
                                fluidHashSet.add(fluidStack.getFluid());
                            else {
                                fluidStack.amount = 1;
                                fluidsNBT.add(fluidStack);
                            }
                        }

                    }
                }
            }
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
        if (!canAlterFlag(flagType)) {
            return;
        }

        if (duct.tile.world().isRemote) {
            duct.sendFilterConfigPacket(flagType, flag);
        } else duct.tile.markDirty();

        flags[flagType] = flag;
    }

    String[] flagTypes = {"whiteList", "metadata", "nbt", "oreDic"};

    @Override
    public String flagType(int flagType) {
        return flagTypes[flagType];
    }

    boolean[][] options = {
            {true, false, false, false},
            {true, true, false, false},
            {true, true, true, true},
            {true, true, true, true},
            {true, true, true, true},
    };

    @Override
    public int numFlags() {
        return 4;
    }

    @Override
    public boolean canAlterFlag(int flagType) {
        return options[type][flagType];
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
    }

    @Override
    public boolean allowFluid(FluidStack fluid) {
        if (recalc) calcItems();
        if (fluid == null) return false;
        else if (fluid.tag == null)
            return fluidHashSet.contains(fluid.getFluid());
        else {
            for (FluidStack fluidStack : fluidsNBT)
                if (fluidStack.isFluidEqual(fluid)) return true;
            return false;
        }
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
}
