package thermalducts.block;

import cofh.api.core.IInitializer;
import cofh.render.IconRegistry;
import cofh.util.StringHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import thermalducts.ThermalDucts;

public class BlockDuct extends Block implements IInitializer {

	public BlockDuct() {

		super(Material.iron);
		setHardness(25.0F);
		setResistance(120.0F);
		setStepSound(soundTypeMetal);
		setBlockName("thermalducts.duct");
		setCreativeTab(ThermalDucts.tab);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < NAMES.length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public int damageDropped(int i) {

		return i;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {

		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {

		final float f = 0.0625F;
		return AxisAlignedBB.getAABBPool().getAABB(x + f, y, z + f, x + 1 - f, y + 1 - f, z + 1 - f);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {

		return 7;
	}

	@Override
	public IIcon getIcon(int side, int metadata) {

		return IconRegistry.getIcon("Duct", metadata);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {

		for (int i = 0; i < NAMES.length; i++) {
			IconRegistry.addIcon("Duct" + i, "redstonearsenal:storage/Block_" + StringHelper.titleCase(NAMES[i]), ir);
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.registerBlock(this, ItemBlockDuct.class, "TestDuct");

		blockDuct = new ItemStack(this, 1, 0);

		// ItemHelper.registerWithHandlers("testDuct", blockDuct);

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

	public static final String[] NAMES = { "testDuct" };

	public static ItemStack blockDuct;

}
