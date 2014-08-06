package thermaldynamics.block;

import cofh.api.core.IInitializer;
import cofh.core.render.IconRegistry;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.core.render.hitbox.RenderHitbox;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;

import thermaldynamics.ThermalDynamics;
import thermaldynamics.core.TDProps;
import thermaldynamics.ducts.energy.TileEnergyDuct;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockFormer;

public class BlockDuct extends BlockMultiBlock implements IInitializer {

	public BlockDuct() {

		super(Material.iron);
		setHardness(25.0F);
		setResistance(120.0F);
		setStepSound(soundTypeMetal);
		setBlockName("thermalducts.duct");
		setCreativeTab(ThermalDynamics.tab);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < NAMES.length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {

		switch (var2) {
		case 1:
			return new TileEnergyDuct();
		}
		return new TileMultiBlock();
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

		return 7;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {

		IconRegistry.addIcon("ConduitEnergy00", "thermalducts:conduit/energy/ConduitEnergy00", ir);
		IconRegistry.addIcon("ConduitEnergy10", "thermalducts:conduit/energy/ConduitEnergy10", ir);
		IconRegistry.addIcon("ConduitEnergy20", "thermalducts:conduit/energy/ConduitEnergy20", ir);

		IconRegistry.addIcon("ConduitFluid00", "thermalducts:conduit/fluid/ConduitFluid00", ir);
		IconRegistry.addIcon("ConduitFluid10", "thermalducts:conduit/fluid/ConduitFluid10", ir);

		IconRegistry.addIcon("ConduitItem00", "thermalducts:conduit/item/ConduitItem00", ir);
		IconRegistry.addIcon("ConduitItem10", "thermalducts:conduit/item/ConduitItem10", ir);
		IconRegistry.addIcon("ConduitItem20", "thermalducts:conduit/item/ConduitItem20", ir);
		IconRegistry.addIcon("ConduitItem30", "thermalducts:conduit/item/ConduitItem30", ir);

		IconRegistry.addIcon("ConduitItem01", "thermalducts:conduit/item/ConduitItem01", ir);
		IconRegistry.addIcon("ConduitItem02", "thermalducts:conduit/item/ConduitItem02", ir);
		IconRegistry.addIcon("ConduitItem03", "thermalducts:conduit/item/ConduitItem03", ir);

		IconRegistry.addIcon("ConduitItem11", "thermalducts:conduit/item/ConduitItem11", ir);
		IconRegistry.addIcon("ConduitItem12", "thermalducts:conduit/item/ConduitItem12", ir);
		IconRegistry.addIcon("ConduitItem13", "thermalducts:conduit/item/ConduitItem13", ir);

		IconRegistry.addIcon("ConduitItem21", "thermalducts:conduit/item/ConduitItem21", ir);
		IconRegistry.addIcon("ConduitItem22", "thermalducts:conduit/item/ConduitItem22", ir);
		IconRegistry.addIcon("ConduitItem23", "thermalducts:conduit/item/ConduitItem23", ir);

		IconRegistry.addIcon("ConduitItem31", "thermalducts:conduit/item/ConduitItem31", ir);
		IconRegistry.addIcon("ConduitItem32", "thermalducts:conduit/item/ConduitItem32", ir);
		IconRegistry.addIcon("ConduitItem33", "thermalducts:conduit/item/ConduitItem33", ir);

		IconRegistry.addIcon("Connection" + ConnectionTypes.ENERGY_BASIC.ordinal(), "thermalducts:conduit/energy/ConnectionEnergy00", ir);
		IconRegistry.addIcon("Connection" + ConnectionTypes.ENERGY_HARDENED.ordinal(), "thermalducts:conduit/energy/ConnectionEnergy10", ir);
		IconRegistry.addIcon("Connection" + ConnectionTypes.ENERGY_REINFORCED.ordinal(), "thermalducts:conduit/energy/ConnectionEnergy20", ir);

		IconRegistry.addIcon("Connection" + ConnectionTypes.FLUID_NORMAL.ordinal(), "thermalducts:conduit/fluid/ConnectionFluid00", ir);
		IconRegistry.addIcon("Connection" + ConnectionTypes.FLUID_INPUT_ON.ordinal(), "thermalducts:conduit/fluid/ConnectionFluid01", ir);

		IconRegistry.addIcon("Connection" + ConnectionTypes.ITEM_NORMAL.ordinal(), "thermalducts:conduit/item/ConnectionItem00", ir);
		IconRegistry.addIcon("Connection" + ConnectionTypes.ITEM_INPUT_ON.ordinal(), "thermalducts:conduit/item/ConnectionItem01", ir);
		IconRegistry.addIcon("Connection" + ConnectionTypes.ITEM_STUFFED_ON.ordinal(), "thermalducts:conduit/item/ConnectionItem02", ir);
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

		MinecraftForge.EVENT_BUS.register(ThermalDynamics.blockDuct);
		GameRegistry.registerTileEntity(TileEnergyDuct.class, "thermalducts.ducts.energy.TileEnergyDuct");
		return true;
	}

	@Override
	public boolean postInit() {

		return true;
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

	@Override
	public void onBlockClicked(World p_149699_1_, int p_149699_2_, int p_149699_3_, int p_149699_4_, EntityPlayer p_149699_5_) {

		p_149699_5_.addChatMessage(new ChatComponentText("Forming Grid..."));
		MultiBlockFormer theFormer = new MultiBlockFormer();
		IMultiBlock theTile = (IMultiBlock) p_149699_1_.getTileEntity(p_149699_2_, p_149699_3_, p_149699_4_);
		theFormer.formGrid(theTile);
		p_149699_5_.addChatMessage(new ChatComponentText("Conduits Found: " + theTile.getGrid().idleSet.size()));
	}

	public static final String[] NAMES = { "testDuct", "energyDuct" };

	public static ItemStack blockDuct;

	public static enum ConduitTypes {
		ENERGY_BASIC, ENERGY_HARDENED, ENERGY_REINFORCED, FLUID_TRANS, FLUID_OPAQUE, ITEM_TRANS, ITEM_OPAQUE, ITEM_FAST_TRANS, ITEM_FAST_OPAQUE
	}

	public static enum RenderTypes {
		ENERGY_BASIC, ENERGY_HARDENED, ENERGY_REINFORCED, FLUID_TRANS, FLUID_OPAQUE, ITEM_TRANS, ITEM_OPAQUE, ITEM_FAST_TRANS, ITEM_FAST_OPAQUE, ITEM_TRANS_SHORT, ITEM_TRANS_LONG, ITEM_TRANS_ROUNDROBIN, ITEM_OPAQUE_SHORT, ITEM_OPAQUE_LONG, ITEM_OPAQUE_ROUNDROBIN, ITEM_FAST_TRANS_SHORT, ITEM_FAST_TRANS_LONG, ITEM_FAST_TRANS_ROUNDROBIN, ITEM_FAST_OPAQUE_SHORT, ITEM_FAST_OPAQUE_LONG, ITEM_FAST_OPAQUE_ROUNDROBIN;
	}

	public static enum ConnectionTypes {
		NONE(false), DUCT, ENERGY_BASIC, ENERGY_BASIC_BLOCKED(false), ENERGY_HARDENED, ENERGY_HARDENED_BLOCKED(false), ENERGY_REINFORCED, ENERGY_REINFORCED_BLOCKED(
				false), FLUID_NORMAL, FLUID_BLOCKED(false), FLUID_INPUT_ON, FLUID_INPUT_OFF, ITEM_NORMAL, ITEM_BLOCKED(false), ITEM_INPUT_ON, ITEM_INPUT_OFF, ITEM_STUFFED_ON, ITEM_STUFFED_OFF;

		private final boolean renderConduit;

		private ConnectionTypes() {

			renderConduit = true;
		}

		private ConnectionTypes(boolean renderConduit) {

			this.renderConduit = renderConduit;
		}

		public boolean renderConduit() {

			return renderConduit;
		}
	}

	@Override
	public ArrayList<ItemStack> dismantleBlock(EntityPlayer player, NBTTagCompound nbt, World world, int x, int y, int z, boolean returnDrops, boolean simulate) {

		return null;
	}

}
