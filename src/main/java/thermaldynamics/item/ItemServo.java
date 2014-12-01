package thermaldynamics.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.servo.ServoFluid;
import thermaldynamics.ducts.servo.ServoItem;

import java.util.List;

public class ItemServo extends ItemAttachment {
    public ItemServo() {
        super();
        this.setUnlocalizedName("thermalducts.servo");
    }

    @Override
    public String getUnlocalizedName(ItemStack item) {
        return super.getUnlocalizedName(item) + "." + item.getItemDamage();
    }


    @Override
    @SuppressWarnings("unchecked")
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
        for (int i = 0; i < 5; i++) {
            p_150895_3_.add(new ItemStack(p_150895_1_, 1, i));
        }
    }

    IIcon[] icons;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister ir) {
        icons = new IIcon[5];
        for (int i = 0; i < 5; i++)
            icons[i] = ir.registerIcon("thermaldynamics:servo" + i);
        this.itemIcon = icons[0];
    }

    @Override
    public IIcon getIconFromDamage(int i) {
        return icons[i % icons.length];
    }

    @Override
    public Attachment getAttachment(int side, ItemStack stack, TileMultiBlock tile) {
        int type = stack.getItemDamage() % 5;
        if (tile instanceof TileFluidDuct)
            return new ServoFluid(tile, (byte) (side ^ 1), type);
        if (tile instanceof TileItemDuct)
            return new ServoItem(tile, (byte) (side ^ 1), type);
        return null;
    }


    public static ItemStack iron, invar, electrum, signalum, ender;

    @Override
    public boolean preInit() {
        GameRegistry.registerItem(this, "servo");

        iron = new ItemStack(this, 1, 0);
        invar = new ItemStack(this, 1, 1);
        electrum = new ItemStack(this, 1, 2);
        signalum = new ItemStack(this, 1, 3);
        ender = new ItemStack(this, 1, 4);

        return true;
    }

}
