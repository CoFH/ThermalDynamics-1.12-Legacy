package cofh.thermaldynamics.duct.entity;

import cofh.CoFHCore;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.SoundHelper;
import cofh.lib.util.position.BlockPosition;
import cofh.repack.codechicken.lib.vec.Vector3;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.Route;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

public class EntityTransport extends Entity {

    public static final byte DATAWATCHER_DIRECTIONS = 16;
    public static final byte DATAWATCHER_PROGRESS = 17;
    public static final byte DATAWATCHER_POSX = 18;
    public static final byte DATAWATCHER_POSY = 19;
    public static final byte DATAWATCHER_POSZ = 20;
    public static final byte DATAWATCHER_STEP = 21;
    public static final byte DATAWATCHER_PAUSE = 22;

    public static final int PIPE_LENGTH = 100;
    public static final int PIPE_LENGTH2 = 50;

    public byte progress;
    public byte direction = 7;
    public byte oldDirection;
    public byte step = 1;
    public boolean reRoute = false;
    public byte pause = 0;


    public float originalWidth = 0;
    public float originalHeight = 0;
    public float originalYOffset = 0;
    public float originalEyeHeight = 0;
    public EntityLivingBase rider = null;

    Route myPath;
    BlockPosition pos;



    boolean initSound;
    public static final float DEFAULT_WIDTH = 0.05F;
    public static final float DEFAULT_HEIGHT = 0.05F;
    public static final float DEFAULT_YOFFSET = 0;

    @Override
    public boolean isEntityInvulnerable() {

        return true;
    }

    @Override
    public double getYOffset() {

        return super.getYOffset();
    }

    @Override
    public double getMountedYOffset() {

        Entity riddenByEntity = this.riddenByEntity;
        if (riddenByEntity == null) {
            return super.getMountedYOffset();
        } else {
            return -riddenByEntity.getYOffset();
        }
    }

    public EntityTransport(World p_i1582_1_) {

        super(p_i1582_1_);
        step = 0;
        this.height = 0.1F;
        this.width = 0.1F;
        this.noClip = true;
        this.isImmuneToFire = true;
    }

    public EntityTransport(TileTransportDuctBase origin, Route route, byte startDirection, byte step) {

        this(origin.world());

        this.step = step;
        pos = new BlockPosition(origin);
        myPath = route;

        progress = 0;
        this.direction = route.getNextDirection();
        this.oldDirection = startDirection;

        setPosition(0);
    }

    @Override
    public boolean shouldRiderSit() {

        return true;
    }

    public void start(EntityLivingBase passenger) {

        loadRider(passenger);
        worldObj.spawnEntityInWorld(this);
        passenger.mountEntity(this);
    }

    public void loadRider(EntityLivingBase passenger) {
        this.rider = passenger;
        this.originalWidth = passenger.width;
        this.originalHeight = passenger.height;
        this.originalYOffset = passenger.yOffset;
        if(rider instanceof EntityPlayer)
            originalEyeHeight = ((EntityPlayer) rider).eyeHeight;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isInvisibleToPlayer(EntityPlayer p_98034_1_) {
        return true;
    }

    @Override
    public void onUpdate() {
        if (riddenByEntity == null || riddenByEntity.isDead) {
            setDead();
            return;
        }

        if(rider == null){
            if(!(riddenByEntity instanceof EntityLivingBase)) {
                riddenByEntity.mountEntity(null);
                setDead();
                return;
            }

            loadRider((EntityLivingBase) riddenByEntity);
        }else{
            updateRider(rider);
        }


        boolean wasPause = pause > 0;

        if (worldObj.isRemote) {
            if (!initSound) {
                initSound = true;
                SoundHelper.playSound(getSound());
            }
            if (this.dataWatcher.hasChanges()) {
                this.dataWatcher.func_111144_e();
                loadWatcherData();
            }
        }

        if (direction == 7 || pos == null) {
            return;
        }

        TileEntity tile = worldObj.getTileEntity(pos.x, pos.y, pos.z);

        if (tile == null || !(tile instanceof TileTransportDuctBase)) {
            if (worldObj.isRemote) {
                pos = null;
            } else {
                dropPassenger();
            }
            return;
        }

        TileTransportDuctBase homeTile = ((TileTransportDuctBase) tile);

        if (pause > 0) {
            pause--;
            if (!worldObj.isRemote)
                updateWatcherData();
            else {
                setPosition(0);

                if (riddenByEntity == CoFHCore.proxy.getClientPlayer())
                    if (pause == 0) {
                        CoFHCore.proxy.addIndexedChatMessage(null, -515781222);
                    } else
                        CoFHCore.proxy.addIndexedChatMessage(
                                new ChatComponentText("Charging - " + (TileTransportDuctCrossover.PAUSE_LEVEL - pause) + " / " + TileTransportDuctCrossover.PAUSE_LEVEL)
                                , -515781222);

                for (int i = 0; i < 10; i++) {
                    worldObj.spawnParticle("portal", pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, MathHelper.RANDOM.nextGaussian()* 0.5, MathHelper.RANDOM.nextGaussian() * 0.5, MathHelper.RANDOM.nextGaussian() * 0.5);
                }
            }

            return;
        }

        if (!worldObj.isRemote) {
            homeTile.advanceEntity(this);
            updateWatcherData();
        } else {
            if (wasPause && riddenByEntity == CoFHCore.proxy.getClientPlayer())
                CoFHCore.proxy.addIndexedChatMessage(null, -515781222);
            homeTile.advanceEntityClient(this);
        }

        setPosition(0);

        if (riddenByEntity != null && !riddenByEntity.isDead) {
            updateRiderPosition();
        }
    }

    public void updateRider(EntityLivingBase rider) {
        rider.width = DEFAULT_WIDTH;
        rider.height = DEFAULT_HEIGHT;
        rider.yOffset = DEFAULT_YOFFSET;
        if (rider instanceof EntityPlayer)
            ((EntityPlayer) rider).eyeHeight = 0;
        rider.setPosition(rider.posX, rider.posY, rider.posZ);
    }

    @Override
    public void setDead() {
        if(rider != null && !rider.isDead){
            rider.height = originalHeight;
            rider.width = originalWidth;
            rider.yOffset = originalYOffset;
            if (rider instanceof EntityPlayer)
                ((EntityPlayer) rider).eyeHeight = originalEyeHeight;

            rider.setPosition(rider.posX, rider.posY, rider.posZ);
        }
        super.setDead();
    }

    public boolean trySimpleAdvance() {
        BlockPosition p = pos.copy().step(direction);

        TileEntity tileEntity = worldObj.getTileEntity(p.x, p.y, p.z);
        if (!(tileEntity instanceof TileTransportDuctBase)) {
            pos = null;
            return false;
        }
        TileTDBase.NeighborTypes[] neighbours = ((TileTransportDuctBase) tileEntity).neighborTypes;
        if (neighbours[direction ^ 1] != TileTDBase.NeighborTypes.MULTIBLOCK) {
            pos = null;
            return false;
        }

        pos = p;
        oldDirection = direction;
        progress %= PIPE_LENGTH;
        return true;
    }

    @SideOnly(Side.CLIENT)
    public ISound getSound() {
        return new SoundWoosh(this);
    }

    @Override
    public void onEntityUpdate() {

        // super.onEntityUpdate();

    }

    public void setPosition(double frame) {

        if (pos == null) {
            return;
        }

        if (pause > 0) {
            Vector3 newPos = getPos(frame);
            setPosition(newPos.x, newPos.y, newPos.z);
            lastTickPosX = prevPosX = posX;
            lastTickPosY = prevPosY = posY;
            lastTickPosZ = prevPosZ = posZ;
            motionX = motionY = motionZ = 0;
            return;
        }

        Vector3 oldPos = getPos(frame - 1);
        lastTickPosX = prevPosX = oldPos.x;
        lastTickPosY = prevPosY = oldPos.y;
        lastTickPosZ = prevPosZ = oldPos.z;

        Vector3 newPos = getPos(frame);
        setPosition(newPos.x, newPos.y, newPos.z);

        motionX = newPos.x - oldPos.x;
        motionY = newPos.y - oldPos.y;
        motionZ = newPos.z - oldPos.z;

        updateRiderPosition();
    }

    public void dropPassenger() {

        if (!worldObj.isRemote) {
            moveToSafePosition();
            riddenByEntity.mountEntity(null);
            setDead();
        }
    }

    public void moveToSafePosition() {

        if (direction >= 0 && direction < 6) {
            setPosition(pos.x + Facing.offsetsXForSide[direction] + 0.5, pos.y + Facing.offsetsYForSide[direction], pos.z + Facing.offsetsZForSide[direction]
                    + 0.5);
        }
    }

    @Override
    protected boolean canTriggerWalking() {

        return false;
    }

    public void advanceTile(TileTransportDuctBaseRoute homeTile) {

        if (homeTile.neighborTypes[direction] == TileTDBase.NeighborTypes.MULTIBLOCK
                && homeTile.connectionTypes[direction] == TileTDBase.ConnectionTypes.NORMAL) {
            TileTransportDuctBase newHome = (TileTransportDuctBase) homeTile.getPhysicalConnectedSide(direction);
            if (newHome != null && newHome.neighborTypes[direction ^ 1] == TileTDBase.NeighborTypes.MULTIBLOCK) {
                pos = new BlockPosition(newHome);

                if (myPath.hasNextDirection()) {
                    oldDirection = direction;
                    direction = myPath.getNextDirection();
                } else {
                    reRoute = true;
                }
            } else
                reRoute = true;
        } else if (homeTile.neighborTypes[direction] == TileTDBase.NeighborTypes.OUTPUT && homeTile.connectionTypes[direction].allowTransfer) {
            dropPassenger();
        } else {
            bouncePassenger(homeTile);
        }
    }

    public void bouncePassenger(TileTransportDuctBaseRoute homeTile) {

        myPath = homeTile.getRoute(this, direction, step);

        if (myPath == null) {
            dropPassenger();
        } else {
            oldDirection = direction;
            direction = myPath.getNextDirection();
            reRoute = false;
        }
    }

    @Override
    protected void entityInit() {

        this.dataWatcher.addObject(DATAWATCHER_DIRECTIONS, (byte) 0);
        this.dataWatcher.addObject(DATAWATCHER_PROGRESS, (byte) 0);
        this.dataWatcher.addObject(DATAWATCHER_POSX, 0);
        this.dataWatcher.addObject(DATAWATCHER_POSY, 0);
        this.dataWatcher.addObject(DATAWATCHER_POSZ, 0);
        this.dataWatcher.addObject(DATAWATCHER_STEP, (byte) 1);
        this.dataWatcher.addObject(DATAWATCHER_PAUSE, (byte) 0);
    }

    public void updateWatcherData() {

        byte p_75692_2_ = (byte) (direction | (oldDirection << 3));
        this.dataWatcher.updateObject(DATAWATCHER_DIRECTIONS, p_75692_2_);
        this.dataWatcher.updateObject(DATAWATCHER_PROGRESS, progress);
        this.dataWatcher.updateObject(DATAWATCHER_POSX, pos.x);
        this.dataWatcher.updateObject(DATAWATCHER_POSY, pos.y);
        this.dataWatcher.updateObject(DATAWATCHER_POSZ, pos.z);
        this.dataWatcher.updateObject(DATAWATCHER_STEP, step);
        this.dataWatcher.updateObject(DATAWATCHER_PAUSE, pause);

    }

    public void loadWatcherData() {

        byte b = this.dataWatcher.getWatchableObjectByte(DATAWATCHER_DIRECTIONS);
        direction = (byte) (b & 7);
        oldDirection = (byte) (b >> 3);
        progress = this.dataWatcher.getWatchableObjectByte(DATAWATCHER_PROGRESS);
        pos = new BlockPosition(this.dataWatcher.getWatchableObjectInt(DATAWATCHER_POSX), this.dataWatcher.getWatchableObjectInt(DATAWATCHER_POSY),
                this.dataWatcher.getWatchableObjectInt(DATAWATCHER_POSZ));
        step = this.dataWatcher.getWatchableObjectByte(DATAWATCHER_STEP);
        pause = this.dataWatcher.getWatchableObjectByte(DATAWATCHER_PAUSE);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {

        if (tag.hasKey("route", 7)) {
            myPath = new Route(tag.getByteArray("route"));
        }

        pos = new BlockPosition(tag.getInteger("posx"), tag.getInteger("posy"), tag.getInteger("posz"));

        progress = tag.getByte("progress");
        direction = tag.getByte("direction");
        oldDirection = tag.getByte("oldDirection");
        step = tag.getByte("step");
        reRoute = tag.getBoolean("reRoute");

        originalWidth = tag.getFloat("originalWidth");
        originalHeight = tag.getFloat("originalHeight");
        originalYOffset = tag.getFloat("originalYOffset");
        originalEyeHeight = tag.getFloat("originalEyeHeight");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {

        if (myPath != null) {
            tag.setByteArray("route", myPath.toByteArray());
        }

        tag.setInteger("posx", pos.x);
        tag.setInteger("posy", pos.y);
        tag.setInteger("posz", pos.z);

        tag.setByte("progress", progress);
        tag.setByte("direction", direction);
        tag.setByte("oldDirection", oldDirection);
        tag.setByte("step", step);
        tag.setBoolean("reRoute", reRoute);

        tag.setFloat("originalWidth", originalWidth);
        tag.setFloat("originalHeight", originalHeight);
        tag.setFloat("originalYOffset", originalYOffset);
        tag.setFloat("originalEyeHeight", originalEyeHeight);
    }

    public Vector3 getPos(double framePos) {

        return getPos(progress, framePos);
    }

    public Vector3 getPos(byte progress, double framePos) {

        double v = (progress + step * framePos) / (PIPE_LENGTH) - 0.5;
        int dir = v < 0 ? oldDirection : direction;

        Vector3 vec = Vector3.center.copy();
        vec.add(v * Facing.offsetsXForSide[dir], v * Facing.offsetsYForSide[dir], v * Facing.offsetsZForSide[dir]);
        vec.add(pos.x, pos.y, pos.z);

        return vec;
    }

    @Override
    public boolean handleWaterMovement() {

        return false;
    }

    @Override
    public boolean canBeCollidedWith() {

        return false;
    }

    @Override
    public boolean handleLavaMovement() {

        return false;
    }

    @Override
    public void moveEntity(double p_70091_1_, double p_70091_3_, double p_70091_5_) {

        setPosition(0);
    }

    @Override
    public void addVelocity(double p_70024_1_, double p_70024_3_, double p_70024_5_) {

    }

    @Override
    public boolean isPushedByWater() {

        return false;
    }

    @Override
    public boolean canBePushed() {

        return false;
    }

    @Override
    public boolean isInRangeToRenderDist(double p_70112_1_) {
        return p_70112_1_ < 4096;
    }

}
