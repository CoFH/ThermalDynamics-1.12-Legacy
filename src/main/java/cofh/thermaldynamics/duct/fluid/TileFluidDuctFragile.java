package cofh.thermaldynamics.duct.fluid;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.util.BlockUtils;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TileFluidDuctFragile extends TileFluidDuct {

    public static final int ROOM_TEMPERATURE = FluidRegistry.WATER.getTemperature();
    public static final int FREEZING_TEMPERATURE = 274;
    public static final int MELTING_TEMPERATURE = 800;

    public float internalTemperature = ROOM_TEMPERATURE;

    public TileFluidDuctFragile() {

        super();
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

        if (fluidGrid == null) {
            return;
        }

        int temp = getTemperature(fluidGrid.getRenderFluid());
        if (temp != prevTemperature) {
            temp = prevTemperature;
            PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
            myPayload.addByte(0);
            myPayload.addByte(TileFluidPackets.TEMPERATURE);

            myPayload.addInt(temp);
            PacketHandler.sendToAllAround(myPayload, this);
        }
    }

    @Override
    public void handleTileInfoPacketType(PacketCoFHBase payload, byte b) {

        if (b == TileFluidPackets.TEMPERATURE) {
            internalTemperature = payload.getInt();
        } else {
            super.handleTileInfoPacketType(payload, b);
        }
    }

    @Override
    public MultiBlockGrid getNewGrid() {

        FluidGrid grid = new FluidGrid(worldObj);
        grid.doesPassiveTicking = true;
        return grid;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick() {

        if (getDuctType().opaque ? internalTemperature > MELTING_TEMPERATURE : myRenderFluid != null && myRenderFluid.amount > 0 && getTemperature(myRenderFluid) > MELTING_TEMPERATURE) {
            List<IndexedCuboid6> cuboids = new LinkedList<IndexedCuboid6>();
            addTraceableCuboids(cuboids);
            if (cuboids.size() == 0) {
                return;
            }
            Random rand = worldObj.rand;
            Cuboid6 box = cuboids.get(rand.nextInt(cuboids.size()));
            Vector3 vec = (box.max.subtract(box.min)).multiply(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()).add(box.min);
            worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, vec.x, vec.y, vec.z, 0, 0, 0);

        }
        super.randomDisplayTick();
    }

    @Override
    public boolean tickPass(int pass) {

        if (!super.tickPass(pass)) {
            return false;
        }
        if (pass == 2) {
            FluidStack fluid = fluidGrid.getFluid();
            int fluidTemp;
            fluidTemp = getTemperature(fluid);

            internalTemperature = internalTemperature + (fluidTemp - internalTemperature) * 0.0005F;

            if (internalTemperature < FREEZING_TEMPERATURE || internalTemperature > MELTING_TEMPERATURE) {
                if (worldObj.rand.nextInt(50) == 0) {

                    if (fluid != null && fluid.amount > 0) {
                        fluid = fluid.copy();
                        if (fluid.amount < 100 || worldObj.rand.nextInt(5) == 0) {
                            fluidGrid.myTank.setFluid(null);
                        } else {
                            fluidGrid.myTank.drain(worldObj.rand.nextInt(fluid.amount), false);
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

        List<ItemStack> drops = getBlockType().getDrops(world(), getPos(), world().getBlockState(getPos()), 0);

        for (ItemStack stack : drops) {
            ItemUtils.dropItem(world(), getPos(), stack, 0.3);
            /*float f = 0.3F;
			double x2 = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double y2 = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double z2 = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			EntityItem item = new EntityItem(worldObj, xCoord + x2, yCoord + y2, zCoord + z2, stack);
			item.delayBeforeCanPickup = 10;
			worldObj.spawnEntityInWorld(item);*/
        }

        worldObj.setBlockToAir(getPos());
        worldObj.createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, 0.5F, false);

        if (fluidStack != null && fluidStack.getFluid().canBePlacedInWorld()) {
            Fluid fluid = fluidStack.getFluid();
            Block block = fluid.getBlock();

            boolean fullBucket = fluidStack.amount >= FluidContainerRegistry.BUCKET_VOLUME && worldObj.rand.nextInt(6) == 0;

            if ("water".equals(fluid.getName())) {
                block = Blocks.FLOWING_WATER;
            } else if ("lava".equals(fluid.getName())) {
                block = Blocks.FLOWING_LAVA;
            }

            if (!"water".equals(fluid.getName()) || !worldObj.getBiomeGenForCoords(getPos()).getBiomeName().toLowerCase(Locale.US).equals("hell")) {
                if (block == Blocks.FLOWING_WATER || block == Blocks.FLOWING_LAVA) {
                    IBlockState levelState = block.getDefaultState().withProperty(BlockLiquid.LEVEL, fullBucket ? 0 : (worldObj.rand.nextInt(6) + 1));
                    worldObj.setBlockState(getPos(), levelState, 3);
                    worldObj.scheduleBlockUpdate(getPos(), block, worldObj.rand.nextInt(30) + 10, 0);
                } else if (block instanceof BlockFluidClassic) {
                    IBlockState levelState = block.getDefaultState().withProperty(BlockFluidBase.LEVEL, fullBucket ? 0 : 1);
                    worldObj.setBlockState(getPos(), levelState, 3);
                    worldObj.scheduleBlockUpdate(getPos(), block, worldObj.rand.nextInt(30) + 10, 0);
                    // block.updateTick(world Obj, xCoord, yCoord, zCoord, worldObj.rand);
                } else if (block instanceof BlockFluidFinite && fullBucket) {
                    IBlockState levelState = block.getDefaultState().withProperty(BlockFluidBase.LEVEL, 0);
                    worldObj.setBlockState(getPos(), levelState, 3);
                    worldObj.scheduleBlockUpdate(getPos(), block, worldObj.rand.nextInt(30) + 10, 0);
                    // block.updateTick(worldObj, xCoord, yCoord, zCoord, worldObj.rand);
                }
                BlockUtils.fireBlockUpdate(world(), getPos());
            }

        }
    }

}
