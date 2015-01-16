package thermaldynamics.item;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.attachments.facades.Facade;
import thermaldynamics.ducts.attachments.facades.FacadeHelper;

public class ItemFacade extends ItemAttachment {
    public ItemFacade() {
        this.setCreativeTab(null);
        this.setUnlocalizedName("thermalducts.cover");
        this.setTextureName("thermaldynamics:cover_test");

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

        if (block == Blocks.air || meta < 0 || meta >= 16 || !FacadeHelper.isValid(block, meta)) {
            nbt.removeTag("Meta");
            nbt.removeTag("Block");
            if (nbt.hasNoTags()) stack.setTagCompound(null);
            return null;
        }

        int meta2 = block.onBlockPlaced(tile.world(), tile.xCoord, tile.yCoord, tile.zCoord, side, hitX[side], hitY[side], hitZ[side], meta);
        if (meta2 >= 0 && meta2 < 16 && FacadeHelper.isValid(block, meta2)) {
            meta = meta2;
        }

        return new Facade(tile, ((byte) (side ^ 1)), block, meta);
    }

    @Override
    public boolean preInit() {
        GameRegistry.registerItem(this, "cover");

        return true;
    }
}
