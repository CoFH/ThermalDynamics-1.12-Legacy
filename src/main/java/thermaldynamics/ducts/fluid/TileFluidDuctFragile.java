package thermaldynamics.ducts.fluid;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.repack.codechicken.lib.raytracer.IndexedCuboid6;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thermaldynamics.multiblock.MultiBlockGrid;

public class TileFluidDuctFragile extends TileFluidDuct {
    public static final int roomTemperature = FluidRegistry.WATER.getTemperature();
    public static final int freezingTemperature = 274;
    public static final int meltingTemperature = 800;

    public float internalTemperature = roomTemperature;

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setFloat("temp", internalTemperature);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        internalTemperature = nbt.getFloat("temp");
    }

    public void updateFluid() {
        if (!getDuctType().opaque) {
            sendRenderPacket();
        } else
            sendOpaqueTempPacket();
    }

    int prevTemperature = 0;

    public void sendOpaqueTempPacket() {
        if (fluidGrid == null)
            return;

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
        } else
            super.handleTileInfoPacketType(payload, b);
    }

    public TileFluidDuctFragile() {
        super();
    }


    @Override
    public MultiBlockGrid getNewGrid() {
        FluidGrid grid = new FluidGrid(worldObj, getDuctType().type);
        grid.doesPassiveTicking = true;
        return grid;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick() {
        if (getDuctType().opaque ?
                internalTemperature > meltingTemperature :
                myRenderFluid != null && myRenderFluid.amount > 0 && getTemperature(myRenderFluid) > meltingTemperature
                ) {
            List<IndexedCuboid6> cuboids = new LinkedList<IndexedCuboid6>();
            addTraceableCuboids(cuboids);
            if (cuboids.size() == 0)
                return;
            Random rand = worldObj.rand;
            Cuboid6 box = cuboids.get(rand.nextInt(cuboids.size()));
            Vector3 vec = (box.max.sub(box.min)).multiply(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()).add(box.min);
            worldObj.spawnParticle("smoke", vec.x, vec.y, vec.z, 0, 0, 0);

        }
        super.randomDisplayTick();
    }

    @Override
    public boolean tickPass(int pass) {
        if (!super.tickPass(pass)) return false;

        if (pass == 2) {

            FluidStack fluid = fluidGrid.getFluid();
            int fluidTemp;
            fluidTemp = getTemperature(fluid);

            internalTemperature = internalTemperature + (fluidTemp - internalTemperature) * 0.01F;

            if (internalTemperature < freezingTemperature || internalTemperature > meltingTemperature) {
                if (worldObj.rand.nextInt(20) == 0) {


                    if (fluid != null && fluid.amount > 0) {
                        fluid = fluid.copy();
                        if (fluid.amount < 100 || worldObj.rand.nextInt(5) == 0)
                            fluidGrid.myTank.setFluid(null);
                        else
                            fluidGrid.myTank.drain(worldObj.rand.nextInt(fluid.amount), false);
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
            if (f != null)
                return f.getTemperature(fluid);
        }


        return roomTemperature;
    }

    public void breakAndSpill(FluidStack fluidStack) {
        List<ItemStack> drops = getBlockType().getDrops(world(),xCoord,yCoord,zCoord,getBlockMetadata(),0);

        for (ItemStack stack : drops) {
            float f = 0.3F;
            double x2 = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double y2 = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            double z2 = worldObj.rand.nextFloat() * f + (1.0F - f) * 0.5D;
            EntityItem item = new EntityItem(worldObj, xCoord + x2, yCoord + y2, zCoord + z2, stack);
            item.delayBeforeCanPickup = 10;
            worldObj.spawnEntityInWorld(item);
        }

        worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        worldObj.createExplosion(null, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, 0.5F, false);

        if (fluidStack != null && fluidStack.getFluid().canBePlacedInWorld()) {
            Fluid fluid = fluidStack.getFluid();
            Block block = fluid.getBlock();

            boolean fullBucket = fluidStack.amount >= FluidContainerRegistry.BUCKET_VOLUME && worldObj.rand.nextInt(6) == 0;

            if ("water".equals(fluid.getName())) {
                block = Blocks.flowing_water;
            } else if ("lava".equals(fluid.getName())) {
                block = Blocks.flowing_lava;
            }

            if (!"water".equals(fluid.getName()) || !worldObj.getBiomeGenForCoords(xCoord / 16, zCoord / 16).biomeName.toLowerCase().equals("hell")) {
                if (block == Blocks.flowing_water || block == Blocks.flowing_lava) {
                    worldObj.setBlock(xCoord, yCoord, zCoord, block, fullBucket ? 0 : (worldObj.rand.nextInt(6) + 1), 3);
                    worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, block, worldObj.rand.nextInt(30) + 10);
                } else if (block instanceof BlockFluidClassic) {
                    worldObj.setBlock(xCoord, yCoord, zCoord, block, fullBucket ? 0 : 1, 3);
                    worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, block, worldObj.rand.nextInt(30) + 10);
                    //block.updateTick(world    Obj, xCoord, yCoord, zCoord, worldObj.rand);
                } else if (block instanceof BlockFluidFinite && fullBucket) {
                    worldObj.setBlock(xCoord, yCoord, zCoord, block, 0, 3);
                    worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, block, worldObj.rand.nextInt(30) + 10);
                    //block.updateTick(worldObj, xCoord, yCoord, zCoord, worldObj.rand);
                }
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }

        }
    }


}

