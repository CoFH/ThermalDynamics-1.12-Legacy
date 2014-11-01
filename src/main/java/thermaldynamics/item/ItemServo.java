package thermaldynamics.item;

import cofh.lib.util.helpers.BlockHelper;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.servo.ServoBase;

public class ItemServo extends Item {
    public ItemServo() {
        super();
        setHasSubtypes(true);

    }

    IIcon[] icons;

    @Override
    public void registerIcons(IIconRegister ir) {
        for (int i = 0; i < 4; i++)
            icons[i] = ir.registerIcon("thermaldynamics:servo" + i);
        this.itemIcon = icons[0];
    }

    @Override
    public IIcon getIconFromDamage(int i) {
        return icons[i % icons.length];
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        int type = stack.getItemDamage() % 4;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileMultiBlock) {
            int s = -1;
            int subHit = RayTracer.retraceBlock(world, player, x, y, z).subHit;
            if (subHit < 6)
                s = subHit;
            else if (subHit < 12)
                s = subHit - 6;
            else if (subHit == 13)
                s = side;

            if (s != -1) {
                return ((TileMultiBlock) tile).addAttachment(new ServoBase((TileMultiBlock) tile, (byte) s, type));
            }
        } else {
            tile = BlockHelper.getAdjacentTileEntity(world, x, y, z, side);
            if (tile instanceof TileMultiBlock)
                return ((TileMultiBlock) tile).addAttachment(new ServoBase((TileMultiBlock) tile, (byte) (side ^ 1), type));
        }


        return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }
}
