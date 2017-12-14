package cofh.thermaldynamics.duct.fluid;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cofh.core.network.PacketBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.core.util.helpers.BlockHelper;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DuctUnitFluidTemperate extends DuctUnitFluid {

	public static final int ROOM_TEMPERATURE = FluidRegistry.WATER.getTemperature();
	public static final int FREEZING_TEMPERATURE = 250;
	public static final int MELTING_TEMPERATURE = 800;

	public float internalTemperature = ROOM_TEMPERATURE;

	public DuctUnitFluidTemperate(TileGrid parent, Duct duct) {

		super(parent, duct);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		nbt.setFloat("temp", internalTemperature);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		internalTemperature = nbt.getFloat("temp");
	}

	@Override
	public void updateFluid() {

		if (!getDuctType().opaque) {
			sendRenderPacket();
		} else {
			sendOpaqueTempPacket();
		}
	}

	int prevTemperature = 0;

	public void sendOpaqueTempPacket() {

		if (grid == null) {
			return;
		}
		int temp = getTemperature(grid.getRenderFluid());
		if (temp != prevTemperature) {
			temp = prevTemperature;
			PacketTileInfo myPayload = newPacketTileInfo();
			myPayload.addByte(TileFluidPackets.TEMPERATURE);

			myPayload.addInt(temp);
			PacketHandler.sendToAllAround(myPayload, parent);
		}
	}

	@Override
	public void handleTileInfoPacketType(PacketBase payload, byte b) {

		if (b == TileFluidPackets.TEMPERATURE) {
			internalTemperature = payload.getInt();
		} else {
			super.handleTileInfoPacketType(payload, b);
		}
	}

	@Override
	public GridFluid createGrid() {

		GridFluid grid = new GridFluid(world());
		grid.doesPassiveTicking = true;
		return grid;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void randomDisplayTick() {

		if (getDuctType().opaque ? internalTemperature > MELTING_TEMPERATURE : myRenderFluid != null && myRenderFluid.amount > 0 && getTemperature(myRenderFluid) > MELTING_TEMPERATURE) {
			List<IndexedCuboid6> cuboids = new LinkedList<>();
			parent.addTraceableCuboids(cuboids);
			if (cuboids.size() == 0) {
				return;
			}
			Random rand = world().rand;
			Cuboid6 box = cuboids.get(rand.nextInt(cuboids.size()));
			Vector3 vec = (box.max.subtract(box.min)).multiply(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()).add(box.min);
			world().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, vec.x, vec.y, vec.z, 0, 0, 0);
		}
		super.randomDisplayTick();
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}
		if (pass == 2) {
			FluidStack fluid = grid.getFluid();
			int fluidTemp;
			fluidTemp = getTemperature(fluid);

			internalTemperature = internalTemperature + (fluidTemp - internalTemperature) * 0.0005F;

			if (internalTemperature < FREEZING_TEMPERATURE || internalTemperature > MELTING_TEMPERATURE) {
				if (world().rand.nextInt(50) == 0) {

					if (fluid != null && fluid.amount > 0) {
						fluid = fluid.copy();
						if (fluid.amount < 100 || world().rand.nextInt(5) == 0) {
							grid.myTank.setFluid(null);
						} else {
							grid.myTank.drain(world().rand.nextInt(fluid.amount), false);
						}
					}
					breakAndSpill(fluid);
					return false;
				}
			}
		}
		return true;
	}

	public static int getTemperature(FluidStack fluid) {

		if (fluid != null) {
			Fluid f = fluid.getFluid();
			if (f != null) {
				return f.getTemperature(fluid);
			}
		}
		return ROOM_TEMPERATURE;
	}

	public void breakAndSpill(FluidStack fluidStack) {

		NonNullList<ItemStack> ret = NonNullList.create();
		parent.getBlockType().getDrops(ret, world(), pos(), world().getBlockState(pos()), 0);

		for (ItemStack stack : ret) {
			ItemUtils.dropItem(world(), pos(), stack, 0.3);
		}
		world().setBlockToAir(pos());
		world().createExplosion(null, pos().getX() + 0.5, pos().getY() + 0.5, pos().getZ() + 0.5, 0.5F, false);

		if (fluidStack != null && fluidStack.getFluid().canBePlacedInWorld()) {
			Fluid fluid = fluidStack.getFluid();
			Block block = fluid.getBlock();

			boolean fullBucket = fluidStack.amount >= Fluid.BUCKET_VOLUME && world().rand.nextInt(6) == 0;

			if ("water".equals(fluid.getName())) {
				block = Blocks.FLOWING_WATER;
			} else if ("lava".equals(fluid.getName())) {
				block = Blocks.FLOWING_LAVA;
			}
			if (!"water".equals(fluid.getName()) || !BiomeDictionary.hasType(world().getBiome(pos()), Type.NETHER)) {
				if (block == Blocks.FLOWING_WATER || block == Blocks.FLOWING_LAVA) {
					IBlockState levelState = block.getDefaultState().withProperty(BlockLiquid.LEVEL, fullBucket ? 0 : (world().rand.nextInt(6) + 1));
					world().setBlockState(pos(), levelState, 3);
					world().scheduleBlockUpdate(pos(), block, world().rand.nextInt(30) + 10, 0);
				} else if (block instanceof BlockFluidClassic) {
					IBlockState levelState = block.getDefaultState().withProperty(BlockFluidBase.LEVEL, fullBucket ? 0 : 1);
					world().setBlockState(pos(), levelState, 3);
					world().scheduleBlockUpdate(pos(), block, world().rand.nextInt(30) + 10, 0);
				} else if (block instanceof BlockFluidFinite && fullBucket) {
					IBlockState levelState = block.getDefaultState().withProperty(BlockFluidBase.LEVEL, 0);
					world().setBlockState(pos(), levelState, 3);
					world().scheduleBlockUpdate(pos(), block, world().rand.nextInt(30) + 10, 0);
				}
				BlockHelper.callBlockUpdate(world(), pos());
			}
		}
	}

}
