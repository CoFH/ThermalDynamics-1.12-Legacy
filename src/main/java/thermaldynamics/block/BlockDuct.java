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
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.core.TDProps;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.ducts.TileStructuralDuct;
import thermaldynamics.ducts.energy.TileEnergyDuct;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.fluid.TileFluidDuctFragile;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.render.TextureTransparent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockDuct extends BlockMultiBlock implements IInitializer {

    int offset;

    public BlockDuct(int offset) {
        super(Material.glass);
        setHardness(1.0F);
        setResistance(150.0F);
        setStepSound(soundTypeMetal);
        setBlockName("thermalducts.duct");
        setCreativeTab(ThermalDynamics.tab);
        this.offset = offset;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < Ducts.values().length; i++) {
            if (Ducts.isValid(i))
                list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return Ducts.isValid(metadata + offset);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int metadata) {
        return Ducts.getType(metadata + offset).factory.createTileEntity();
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
    public int getLightValue(IBlockAccess world, int x, int y, int z) {

        return 0;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister ir) {

        IconRegistry.addIcon("DuctEnergy00", "thermaldynamics:duct/energy/DuctEnergy00", ir);
        IconRegistry.addIcon("CenterLine", "thermaldynamics:duct/item/CenterLine", ir);

        IconRegistry.addIcon("Trans_Fluid_Ender_Still", TextureTransparent.registerTransparentIcon(ir, "thermalfoundation:fluid/Fluid_Ender_Still", (byte) 64));
        IconRegistry.addIcon("Trans_Fluid_Glowstone_Still", TextureTransparent.registerTransparentIcon(ir, "thermalfoundation:fluid/Fluid_Glowstone_Still", (byte) 128));
        IconRegistry.addIcon("Trans_Fluid_Redstone_Still", TextureTransparent.registerTransparentIcon(ir, "thermalfoundation:fluid/Fluid_Redstone_Still", (byte) 192));

        IconRegistry.addIcon("DuctStructure", "thermaldynamics:duct/structure", ir);

        IconRegistry.addIcon("RedstoneNoise", "thermaldynamics:duct/energy/redstone_noise", ir);

//        IconRegistry.addIcon("DuctEnergy00", TextureOverlay.generateTexture(ir, false, 0, 1, 0));
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 2; j++)
                IconRegistry.addIcon("ServoBase" + (i * 2 + j), "thermaldynamics:duct/servo/ServoBase" + i + "" + j, ir);


        IconRegistry.addIcon("OverDuctBase", "thermaldynamics:duct/OverDuctBase", ir);

        IconRegistry.addIcon("DuctEnergy10", "thermaldynamics:duct/energy/DuctEnergy10", ir);
        IconRegistry.addIcon("DuctEnergy20", "thermaldynamics:duct/energy/DuctEnergy20", ir);

        IconRegistry.addIcon("DuctFluid00", "thermaldynamics:duct/fluid/DuctFluid00", ir);
        IconRegistry.addIcon("DuctFluid10", "thermaldynamics:duct/fluid/DuctFluid10", ir);
        IconRegistry.addIcon("DuctFluid20", "thermaldynamics:duct/fluid/DuctFluid20", ir);
        IconRegistry.addIcon("DuctFluid30", "thermaldynamics:duct/fluid/DuctFluid30", ir);

        IconRegistry.addIcon("DuctItem00", "thermaldynamics:duct/item/DuctItem00", ir);
        IconRegistry.addIcon("DuctItem10", "thermaldynamics:duct/item/DuctItem10", ir);
        IconRegistry.addIcon("DuctItem20", "thermaldynamics:duct/item/DuctItem20", ir);
        IconRegistry.addIcon("DuctItem30", "thermaldynamics:duct/item/DuctItem30", ir);

        IconRegistry.addIcon("DuctItem01", "thermaldynamics:duct/item/DuctItem01", ir);
        IconRegistry.addIcon("DuctItem02", "thermaldynamics:duct/item/DuctItem02", ir);
        IconRegistry.addIcon("DuctItem03", "thermaldynamics:duct/item/DuctItem03", ir);

        IconRegistry.addIcon("DuctItem11", "thermaldynamics:duct/item/DuctItem11", ir);
        IconRegistry.addIcon("DuctItem12", "thermaldynamics:duct/item/DuctItem12", ir);
        IconRegistry.addIcon("DuctItem13", "thermaldynamics:duct/item/DuctItem13", ir);

        IconRegistry.addIcon("DuctItem21", "thermaldynamics:duct/item/DuctItem21", ir);
        IconRegistry.addIcon("DuctItem22", "thermaldynamics:duct/item/DuctItem22", ir);
        IconRegistry.addIcon("DuctItem23", "thermaldynamics:duct/item/DuctItem23", ir);

        IconRegistry.addIcon("DuctItem31", "thermaldynamics:duct/item/DuctItem31", ir);
        IconRegistry.addIcon("DuctItem32", "thermaldynamics:duct/item/DuctItem32", ir);
        IconRegistry.addIcon("DuctItem33", "thermaldynamics:duct/item/DuctItem33", ir);

        IconRegistry.addIcon("ConnectionEnergy0", "thermaldynamics:duct/energy/ConnectionEnergy00", ir);
        IconRegistry.addIcon("ConnectionEnergy1", "thermaldynamics:duct/energy/ConnectionEnergy10", ir);
        IconRegistry.addIcon("ConnectionEnergy2", "thermaldynamics:duct/energy/ConnectionEnergy20", ir);
        IconRegistry.addIcon("ConnectionFluid0", "thermaldynamics:duct/fluid/ConnectionFluid00", ir);
        IconRegistry.addIcon("ConnectionFluid1", "thermaldynamics:duct/fluid/ConnectionFluid10", ir);
        IconRegistry.addIcon("ConnectionItem0", "thermaldynamics:duct/item/ConnectionItem00", ir);
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

    public ItemStack blockDuct;

    public static enum ConnectionTypes {
        NONE(false), DUCT, ONEWAY, TILECONNECTION, STRUCTURE;

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
            for (Attachment a : ((TileMultiBlock) tile).attachments) {
                if (a != null)
                    ret.addAll(a.getDrops());
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
        ((TileMultiBlock) world.getTileEntity(x, y, z)).randomDisplayTick();
    }

    /* IInitializer */
    @Override
    public boolean preInit() {

        GameRegistry.registerBlock(this, ItemBlockDuct.class, "ThermalDucts_" + offset);

        blockDuct = new ItemStack(this, 1, 0);

        for (int i = offset; i < offset + 16; i++) {
            if (Ducts.isValid(i)) {
                Ducts.getType(i).itemStack = new ItemStack(this, 1, i);
            }
        }

        return true;
    }

    @Override
    public boolean initialize() {

        MinecraftForge.EVENT_BUS.register(ThermalDynamics.blockDuct);
        GameRegistry.registerTileEntity(TileFluidDuct.class, "thermalducts.ducts.energy.TileFluidDuct");
        GameRegistry.registerTileEntity(TileFluidDuctFragile.class, "thermalducts.ducts.energy.TileFragileFluidDuct");
        GameRegistry.registerTileEntity(TileEnergyDuct.class, "thermalducts.ducts.energy.TileEnergyDuct");
        GameRegistry.registerTileEntity(TileItemDuct.class, "thermalducts.ducts.energy.TileItemDuct");
        GameRegistry.registerTileEntity(TileStructuralDuct.class, "thermalducts.ducts.TileStructuralDuct");
        return true;
    }

    @Override
    public boolean postInit() {

        return true;
    }

}
