package thermaldynamics.item;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.attachments.facades.Cover;
import thermaldynamics.ducts.attachments.facades.CoverHelper;

public class ItemCover extends ItemAttachment {
    public ItemCover() {
        this.setCreativeTab(null);
        this.setUnlocalizedName("thermalducts.cover");
        this.setTextureName("thermaldynamics:cover");
    }

    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
        super.getSubItems(p_150895_1_, p_150895_2_, p_150895_3_);

        Iterator iterator = Item.itemRegistry.iterator();

        ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

        while (iterator.hasNext()) {
            Item item = (Item) iterator.next();

            if (item != null && item != this) {
                item.getSubItems(item, null, stacks);
            }
        }

        for (ItemStack stack : stacks) {
            if (!(stack.getItem() instanceof ItemBlock))
                continue;

            if (!CoverHelper.isValid(
                    ((ItemBlock) stack.getItem()).field_150939_a,
                    stack.getItem().getMetadata(stack.getItemDamage())))
                continue;

            p_150895_3_.add(
                    CoverHelper.getCoverStack(((ItemBlock) stack.getItem()).field_150939_a,
                            stack.getItem().getMetadata(stack.getItemDamage()))
            );
        }


    }

    private static float[] hitX = {0.5F, 0.5F, 0.5F, 0.5F, 0, 1};
    private static float[] hitY = {0, 1, 0.5F, 0.5F, 0.5F, 0.5F};
    private static float[] hitZ = {0.5F, 0.5F, 0, 1, 0.5F, 0.5F};

    @Override
    public Attachment getAttachment(int side, ItemStack stack, TileMultiBlock tile) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) return null;

        int meta = nbt.getByte("Meta");
        Block block = Block.getBlockFromName(nbt.getString("Block"));

        if (block == Blocks.air || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta)) {
            nbt.removeTag("Meta");
            nbt.removeTag("Block");
            if (nbt.hasNoTags()) stack.setTagCompound(null);
            return null;
        }


        return new Cover(tile, ((byte) (side ^ 1)), block, meta);
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) {
        StringBuilder builder = new StringBuilder();
        ItemStack b = CoverHelper.getCoverItemStack(item, true);
        if (b != null) {
            builder.append(b.getDisplayName());
            builder.append(" ");
        }
        builder.append(super.getItemStackDisplayName(item));
        return builder.toString();
    }

//    @SuppressWarnings("unchecked")
//    @Override
//    public void addInformation(ItemStack stack, EntityPlayer p_77624_2_, List list, boolean p_77624_4_) {
//        super.addInformation(stack, p_77624_2_, list, p_77624_4_);
//        ItemStack b = getFacadeItemStack(stack);
//        if (b == null) return;
//
//        list.add(b.getDisplayName());
//    }

    @Override
    public boolean preInit() {
        GameRegistry.registerItem(this, "cover");
        return true;
    }
}
