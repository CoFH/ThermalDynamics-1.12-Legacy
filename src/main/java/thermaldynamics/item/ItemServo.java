package thermaldynamics.item;

import cofh.api.core.IInitializer;
import cofh.lib.util.helpers.BlockHelper;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.servo.ServoFluid;

import java.util.List;

public class ItemServo extends Item implements IInitializer {
    public ItemServo() {
        super();
        setHasSubtypes(true);
        this.setUnlocalizedName("thermalducts.servo");
        this.setCreativeTab(ThermalDynamics.tab);

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
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        int type = stack.getItemDamage() % 5;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileMultiBlock) {
            int s = -1;
            MovingObjectPosition movingObjectPosition = RayTracer.retraceBlock(world, player, x, y, z);
            if (movingObjectPosition != null) {
                int subHit = movingObjectPosition.subHit;
                if (subHit < 6)
                    s = subHit;
                else if (subHit < 12)
                    s = (subHit - 6);
                else if (subHit == 13)
                    s = side;

                if (s != -1) {
                    Attachment servo = getServoType(s^1, type, (TileMultiBlock) tile);
                    if (((TileMultiBlock) tile).addAttachment(servo)) {
                        stack.stackSize--;
                        return true;
                    }
                }

                return false;
            }
        }

        tile = BlockHelper.getAdjacentTileEntity(world, x, y, z, side);
        if (tile instanceof TileMultiBlock) {
            Attachment servo = getServoType(side, type, (TileMultiBlock) tile);
            if (((TileMultiBlock) tile).addAttachment(servo)) {
                stack.stackSize--;
                return true;
            }
        }


        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    public ServoFluid getServoType(int side, int type, TileMultiBlock tile) {
        return new ServoFluid((TileMultiBlock) tile, (byte) (side ^ 1), type);
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

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public boolean postInit() {
        return true;
    }
}
