package thermaldynamics.block;

import cofh.api.core.IInitializer;
import cofh.core.block.TileCoFHBase;
import cofh.core.render.IconRegistry;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.core.render.hitbox.RenderHitbox;
import cofh.core.util.CoreUtils;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.core.TDProps;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.ducts.TileStructuralDuct;
import thermaldynamics.ducts.attachments.facades.Facade;
import thermaldynamics.ducts.energy.TileEnergyDuct;
import thermaldynamics.ducts.energy.TileEnergyDuctSuperConductor;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.fluid.TileFluidDuctFragile;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TileItemDuctEnder;
import thermaldynamics.ducts.item.TileItemDuctRedstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockDuct extends BlockMultiBlock implements IInitializer {
    public int offset;

    public BlockDuct(int offset) {
        super(Material.glass);
        setHardness(1.0F);
        setResistance(150.0F);
        setStepSound(soundTypeMetal);
        setBlockName("thermalducts.duct");
        setCreativeTab(ThermalDynamics.tab);
        this.offset = offset * 16;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < 16; i++) {
            if (Ducts.isValid(i + offset))
                list.add(Ducts.getDuct(i + offset).itemStack.copy());
        }
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return Ducts.isValid(metadata + offset);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        Ducts duct = Ducts.getType(metadata + offset);
        return duct.factory.createTileEntity(duct, world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return Ducts.getType(metadata + offset).iconBaseTexture;
    }

    @Override
    public int damageDropped(int i) {
        return i;
    }

    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onBlockHighlight(DrawBlockHighlightEvent event) {

        if (event.target.typeOfHit == MovingObjectType.BLOCK
                && event.player.worldObj.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ).getUnlocalizedName()
                .equals(getUnlocalizedName())) {
            RayTracer.retraceBlock(event.player.worldObj, event.player, event.target.blockX, event.target.blockY, event.target.blockZ);

            ICustomHitBox theTile = ((ICustomHitBox) event.player.worldObj.getTileEntity(event.target.blockX, event.target.blockY, event.target.blockZ));
            if (theTile.shouldRenderCustomHitBox(event.target.subHit, event.player)) {
                event.setCanceled(true);
                RenderHitbox.drawSelectionBox(event.player, event.target, event.partialTicks, theTile.getCustomHitBox(event.target.subHit, event.player));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister ir) {
        if (offset != 0)
            return;

        IconRegistry.addIcon("CenterLine", "thermaldynamics:duct/item/CenterLine", ir);
        IconRegistry.addIcon("TinDuct", "thermaldynamics:altDucts/tin_trans", ir);

//        IconRegistry.addIcon("Trans_Fluid_Ender_Still", TextureTransparent.registerTransparentIcon(ir, "thermalfoundation:fluid/Fluid_Ender_Still", (byte) 64));
//        IconRegistry.addIcon("Trans_Fluid_Glowstone_Still", TextureTransparent.registerTransparentIcon(ir, "thermalfoundation:fluid/Fluid_Glowstone_Still", (byte) 128));
//        IconRegistry.addIcon("Trans_Fluid_Redstone_Still", TextureTransparent.registerTransparentIcon(ir, "thermalfoundation:fluid/Fluid_Redstone_Still", (byte) 192));

        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 2; j++)
                IconRegistry.addIcon("ServoBase" + (i * 2 + j), "thermaldynamics:duct/servo/ServoBase" + i + "" + j, ir);

        for (int i = 0; i < 5; i++)
            IconRegistry.addIcon("FilterBase" + i, "thermaldynamics:duct/filter/Filter" + i + "0", ir);


        IconRegistry.addIcon("OverDuctBase", "thermaldynamics:duct/OverDuctBase", ir);

        IconRegistry.addIcon("SideDucts", "thermaldynamics:duct/sideDucts", ir);

        for (int i = 0; i < Ducts.values().length; i++) {
            Ducts ducts = Ducts.values()[i];
            ducts.registerIcons(ir);
        }


    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        if (target.subHit >= 14 && target.subHit < 20) {
            TileMultiBlock tileEntity = (TileMultiBlock) world.getTileEntity(x, y, z);
            ItemStack pickBlock = tileEntity.attachments[target.subHit - 14].getPickBlock();
            if (pickBlock != null) return pickBlock;
        }

        return super.getPickBlock(target, world, x, y, z);
    }

    @Override
    public int getRenderType() {
        return TDProps.renderDuctId;
    }

    @Override
    public boolean canRenderInPass(int pass) {
        renderPass = pass;
        return pass < 2;
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    public static enum ConnectionTypes {
        NONE(false), DUCT, TILECONNECTION, STRUCTURE;

        private final boolean renderDuct;

        private ConnectionTypes() {

            renderDuct = true;
        }

        private ConnectionTypes(boolean renderDuct) {

            this.renderDuct = renderDuct;
        }

        public boolean renderDuct() {

            return renderDuct;
        }
    }

    @Override
    public ArrayList<ItemStack> dismantleBlock(EntityPlayer player, NBTTagCompound nbt, World world, int x, int y, int z, boolean returnDrops, boolean simulate) {
        TileEntity tile = world.getTileEntity(x, y, z);
        int bMeta = world.getBlockMetadata(x, y, z);


        ItemStack dropBlock = new ItemStack(this, 1, bMeta);

        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        ret.add(dropBlock);

        if (tile instanceof TileMultiBlock) {
            TileMultiBlock multiBlock = (TileMultiBlock) tile;
            for (Attachment a : multiBlock.attachments) {
                if (a != null)
                    ret.addAll(a.getDrops());
            }
            for (Facade facade : multiBlock.facades) {
                if (facade != null)
                    ret.addAll(facade.getDrops());
            }


        }

        if (nbt != null) {
            dropBlock.setTagCompound(nbt);
        }
        if (!simulate) {
            if (tile instanceof TileCoFHBase) {
                ((TileCoFHBase) tile).blockDismantled();
            }
            world.setBlockToAir(x, y, z);

            if (!returnDrops) {
                float f = 0.3F;
                for (ItemStack stack : ret) {
                    double x2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double y2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    double z2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
                    EntityItem item = new EntityItem(world, x + x2, y + y2, z + z2, stack);
                    item.delayBeforeCanPickup = 10;
                    world.spawnEntityInWorld(item);
                }

                if (player != null) {
                    CoreUtils.dismantleLog(player.getCommandSenderName(), this, bMeta, x, y, z);
                }
            }
        }

        return ret;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random p_149734_5_) {
        super.randomDisplayTick(world, x, y, z, p_149734_5_);
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileMultiBlock) ((TileMultiBlock) tileEntity).randomDisplayTick();
    }

    @Override
    public float getSize(World world, int x, int y, int z) {
        return Ducts.getDuct(offset + world.getBlockMetadata(x, y, z)).isLargeTube() ? 0.05F : super.getSize(world, x, y, z);
    }

    /* IInitializer */
    @Override
    public boolean preInit() {
        GameRegistry.registerBlock(this, ItemBlockDuct.class, "ThermalDucts_" + offset);

        for (int i = 0; i < 16; i++) {
            if (Ducts.isValid(offset + i)) {
                Ducts.getType(offset + i).itemStack = new ItemStack(this, 1, i);
            }
        }

        return true;
    }

    @Override
    public boolean initialize() {
        MinecraftForge.EVENT_BUS.register(this);
        if (offset != 0)
            return true;

        GameRegistry.registerTileEntity(TileFluidDuct.class, "thermalducts.ducts.energy.TileFluidDuct");
        GameRegistry.registerTileEntity(TileFluidDuctFragile.class, "thermalducts.ducts.energy.TileFragileFluidDuct");
        GameRegistry.registerTileEntity(TileEnergyDuct.class, "thermalducts.ducts.energy.TileEnergyDuct");
        GameRegistry.registerTileEntity(TileEnergyDuctSuperConductor.class, "thermalducts.ducts.energy.TileEnergyDuctSuperConductor");
        GameRegistry.registerTileEntity(TileItemDuct.class, "thermalducts.ducts.energy.TileItemDuct");
        GameRegistry.registerTileEntity(TileItemDuctEnder.class, "thermalducts.ducts.energy.TileItemDuctEnder");
        GameRegistry.registerTileEntity(TileItemDuctRedstone.class, "thermalducts.ducts.energy.TileItemDuctRedstone");
        GameRegistry.registerTileEntity(TileStructuralDuct.class, "thermalducts.ducts.TileStructuralDuct");
        return true;
    }

    @Override
    public boolean postInit() {
        return true;
    }

}
