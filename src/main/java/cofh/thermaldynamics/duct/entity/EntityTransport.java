package cofh.thermaldynamics.duct.entity;

import cofh.CoFHCore;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.SoundHelper;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.IDuctHolder;
import cofh.thermaldynamics.multiblock.Route;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityTransport extends Entity {

	private static final DataParameter<Byte> DIRECTIONS = EntityDataManager.createKey(EntityTransport.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> PROGRESS = EntityDataManager.createKey(EntityTransport.class, DataSerializers.BYTE);
	private static final DataParameter<Integer> POSX = EntityDataManager.createKey(EntityTransport.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> POSY = EntityDataManager.createKey(EntityTransport.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> POSZ = EntityDataManager.createKey(EntityTransport.class, DataSerializers.VARINT);
	private static final DataParameter<Byte> STEP = EntityDataManager.createKey(EntityTransport.class, DataSerializers.BYTE);
	private static final DataParameter<Byte> PAUSE = EntityDataManager.createKey(EntityTransport.class, DataSerializers.BYTE);

	public static final int DUCT_LENGTH = 100;
	public static final int DUCT_LENGTH2 = 50;

	public byte progress = 0;
	public byte direction = 7;
	public byte oldDirection;
	public byte step = 1;
	public boolean reRoute = false;
	public byte pause = 0;

	public float originalWidth = 0;
	public float originalHeight = 0;
	public double originalYOffset = 0;
	public float originalEyeHeight = 0;
	public Entity rider = null;

	Route myPath;
	BlockPos pos;

	boolean initSound;
	public static final float DEFAULT_WIDTH = 0.25F;
	public static final float DEFAULT_HEIGHT = 0.25F;

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {

		return true;
	}

	@Override
	public double getYOffset() {

		return super.getYOffset();
	}

	@Override
	public double getMountedYOffset() {

		Entity riddenByEntity = this.rider;
		if (riddenByEntity == null) {
			return super.getMountedYOffset();
		} else {
			return -riddenByEntity.getYOffset();
		}
	}

	public EntityTransport(World world) {

		super(world);

		step = 0;
		this.height = 0F;
		this.width = 0F;
		this.noClip = true;
		this.isImmuneToFire = true;
	}

	public EntityTransport(DuctUnitTransportBase origin, Route route, byte startDirection, byte step) {

		this(origin.world());

		this.step = step;
		pos = new BlockPos(origin.pos());
		myPath = route;

		this.direction = route.getNextDirection();
		this.oldDirection = startDirection;

		setPosition(0);
	}

	@Override
	public boolean shouldRiderSit() {

		return true;
	}

	public void start(Entity passenger) {

		passenger.startRiding(this);
		loadRider(passenger);
		worldObj.spawnEntityInWorld(this);
	}

	@Override
	protected void addPassenger(Entity passenger) {

		super.addPassenger(passenger);

		if (rider == null && passenger instanceof EntityPlayer) {
			loadRider(passenger);
		}
	}

	public void loadRider(Entity passenger) {

		this.rider = passenger;
		this.originalWidth = passenger.width;
		this.originalHeight = passenger.height;
		this.originalYOffset = passenger.getYOffset();

		if (rider instanceof EntityPlayer) {
			originalEyeHeight = ((EntityPlayer) rider).eyeHeight;
		}
	}

	@Override
	public boolean isInvisible() {

		return true;
	}

	@Override
	public boolean isInvisibleToPlayer(EntityPlayer player) {

		return true;
	}

	@Override
	public void onUpdate() {

		if (!worldObj.isRemote || rider != null) {
			if (!isBeingRidden() || getPassengers().get(0).isDead) {
				setDead();
				return;
			}
		} else if (!isBeingRidden()) {
			return;
		}
		if (rider == null) {
			if (!(getPassengers().get(0) instanceof EntityLivingBase)) {
				getPassengers().get(0).dismountRidingEntity();
				setDead();
				return;
			}
			loadRider(getPassengers().get(0));
		} else {
			updateRider(rider);
		}
		boolean wasPause = pause > 0;

		if (worldObj.isRemote) {
			if (!initSound) {
				initSound = true;
				SoundHelper.playSound(getSound());
			}
			if (this.dataManager.isDirty()) {
				this.dataManager.setClean();
				loadDataParameters();
			}
		}
		if (direction == 7 || pos == null) {
			return;
		}
		TileEntity tile = worldObj.getTileEntity(pos);

		DuctUnitTransportBase homeTile;

		if (tile == null || !(tile instanceof IDuctHolder) || (homeTile = ((IDuctHolder) tile).getDuct(DuctToken.TRANSPORT)) == null) {
			if (worldObj.isRemote) {
				pos = null;
			} else {
				dropPassenger();
			}
			return;
		}
		if (pause > 0) {
			pause--;
			if (!worldObj.isRemote) {
				updateDataParameters();
			} else {
				setPosition(0);

				if (!getPassengers().isEmpty() && getPassengers().get(0) == CoFHCore.proxy.getClientPlayer()) {
					if (pause == 0) {
						CoFHCore.proxy.addIndexedChatMessage(null, -515781222);
					} else {
						CoFHCore.proxy.addIndexedChatMessage(new TextComponentString("Charging - " + (DuctUnitTransportLinking.CHARGE_TIME - pause) + " / " + DuctUnitTransportLinking.CHARGE_TIME), -515781222);
					}
				}
				for (int i = 0; i < 10; i++) {
					worldObj.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, MathHelper.RANDOM.nextGaussian() * 0.5, MathHelper.RANDOM.nextGaussian() * 0.5, MathHelper.RANDOM.nextGaussian() * 0.5);
				}
			}
			return;
		}
		if (!worldObj.isRemote) {
			homeTile.advanceEntity(this);
			updateDataParameters();
		} else {
			if (wasPause && !getPassengers().isEmpty() && getPassengers().get(0) == CoFHCore.proxy.getClientPlayer()) {
				CoFHCore.proxy.addIndexedChatMessage(null, -515781222);
			}
			homeTile.advanceEntityClient(this);
		}
		setPosition(0);

		if (isBeingRidden() && !getPassengers().get(0).isDead) {
			updatePassenger(getPassengers().get(0));
		}
	}

	public void updateRider(Entity rider) {

		rider.width = DEFAULT_WIDTH;
		rider.height = DEFAULT_HEIGHT;

		if (rider instanceof EntityPlayer) {
			((EntityPlayer) rider).eyeHeight = 0.35F;
		}
		rider.setPosition(rider.posX, rider.posY, rider.posZ);
	}

	@Override
	public void setDead() {

		if (rider != null && !rider.isDead) {
			rider.height = originalHeight;
			rider.width = originalWidth;

			if (rider instanceof EntityPlayer) {
				((EntityPlayer) rider).eyeHeight = originalEyeHeight;
			}
			rider.setPosition(rider.posX, rider.posY, rider.posZ);
		}
		super.setDead();
	}

	public boolean trySimpleAdvance() {

		BlockPos p = pos.offset(EnumFacing.VALUES[direction]);

		TileEntity tileEntity = worldObj.getTileEntity(p);
		DuctUnitTransportBase transportBase = IDuctHolder.getTokenFromTile(tileEntity, DuctToken.TRANSPORT);

		if (transportBase == null) {
			pos = null;
			return false;
		}
		if (transportBase.ductCache[direction ^ 1] == null) {
			pos = null;
			return false;
		}
		pos = p;
		oldDirection = direction;
		progress %= DUCT_LENGTH;
		return true;
	}

	@SideOnly (Side.CLIENT)
	public ISound getSound() {

		return new SoundWoosh(this);
	}

	@Override
	public void onEntityUpdate() {

	}

	public void setPosition(double frame) {

		if (pos == null) {
			return;
		}
		if (pause > 0) {
			Vec3d newPos = getPos(frame);
			setPosition(newPos.xCoord, newPos.yCoord, newPos.zCoord);
			lastTickPosX = prevPosX = posX;
			lastTickPosY = prevPosY = posY;
			lastTickPosZ = prevPosZ = posZ;
			motionX = motionY = motionZ = 0;
			return;
		}
		Vec3d oldPos = getPos(frame - 1);
		lastTickPosX = prevPosX = oldPos.xCoord;
		lastTickPosY = prevPosY = oldPos.yCoord;
		lastTickPosZ = prevPosZ = oldPos.zCoord;

		Vec3d newPos = getPos(frame);
		setPosition(newPos.xCoord, newPos.yCoord, newPos.zCoord);

		motionX = newPos.xCoord - oldPos.xCoord;
		motionY = newPos.yCoord - oldPos.yCoord;
		motionZ = newPos.zCoord - oldPos.zCoord;

		if (!getPassengers().isEmpty()) {
			updatePassenger(getPassengers().get(0));
		}
	}

	public void dropPassenger() {

		if (!worldObj.isRemote) {
			rider.dismountRidingEntity();

			if (direction >= 0 && direction < 6) {
				Vec3i vec = EnumFacing.VALUES[direction].getDirectionVec();
				double x = pos.getX() + vec.getX() + 0.5;
				double y = pos.getY() + vec.getY();
				double z = pos.getZ() + vec.getZ() + 0.5;

				if (direction == 0) {
					y = Math.floor(pos.getY() - originalHeight);
				}
				rider.setPosition(x, y, z);

				if (rider instanceof EntityPlayerMP) {
					float yaw, pitch;
					switch (direction) {
						case 0:
							yaw = rider.rotationYaw;
							pitch = 0;
							break;
						case 1:
							yaw = rider.rotationYaw;
							pitch = 0;
							break;
						case 2:
							yaw = 180;
							pitch = 0;
							break;
						case 3:
							yaw = 0;
							pitch = 0;
							break;
						case 4:
							yaw = 90;
							pitch = 0;
							break;
						case 5:
							yaw = 270;
							pitch = 0;
							break;
						default:
							return;
					}
					((EntityPlayerMP) rider).connection.setPlayerLocation(x, y, z, yaw, pitch);
				}
			}
			setDead();
		}
	}

	@Override
	public boolean canTriggerWalking() {

		return false;
	}

	public void advanceTile(DuctUnitTransportBase homeTile) {

		if (homeTile.ductCache[direction] != null) {
			DuctUnitTransportBase newHome = (DuctUnitTransportBase) homeTile.getPhysicalConnectedSide(direction);
			if (newHome != null && newHome.ductCache[direction ^ 1] != null) {
				pos = new BlockPos(newHome.pos());

				if (myPath.hasNextDirection()) {
					oldDirection = direction;
					direction = myPath.getNextDirection();
				} else {
					reRoute = true;
				}
			} else {
				reRoute = true;
			}
		} else if (homeTile.parent.getConnectionType(direction) == ConnectionType.FORCED) {
			dropPassenger();
		} else {
			bouncePassenger(homeTile);
		}
	}

	public void bouncePassenger(DuctUnitTransportBase homeTile) {

		if (homeTile.getGrid() == null) {
			return;
		}
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

		this.dataManager.register(DIRECTIONS, (byte) 0);
		this.dataManager.register(PROGRESS, (byte) 0);
		this.dataManager.register(POSX, 0);
		this.dataManager.register(POSY, 0);
		this.dataManager.register(POSZ, 0);
		this.dataManager.register(STEP, (byte) 1);
		this.dataManager.register(PAUSE, (byte) 0);
	}

	public void updateDataParameters() {

		byte dir = (byte) (direction | (oldDirection << 3));
		this.dataManager.set(DIRECTIONS, dir);
		this.dataManager.set(PROGRESS, progress);
		this.dataManager.set(POSX, pos.getX());
		this.dataManager.set(POSY, pos.getY());
		this.dataManager.set(POSZ, pos.getZ());
		this.dataManager.set(STEP, step);
		this.dataManager.set(PAUSE, pause);
	}

	public void loadDataParameters() {

		byte b = this.dataManager.get(DIRECTIONS);
		direction = (byte) (b & 7);
		oldDirection = (byte) (b >> 3);
		progress = this.dataManager.get(PROGRESS);
		pos = new BlockPos(this.dataManager.get(POSX), this.dataManager.get(POSY), this.dataManager.get(POSZ));
		step = this.dataManager.get(STEP);
		pause = this.dataManager.get(PAUSE);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {

		if (tag.hasKey("route", 7)) {
			myPath = new Route(tag.getByteArray("route"));
		}
		pos = new BlockPos(tag.getInteger("posx"), tag.getInteger("posy"), tag.getInteger("posz"));

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
		tag.setInteger("posx", pos.getX());
		tag.setInteger("posy", pos.getY());
		tag.setInteger("posz", pos.getZ());

		tag.setByte("progress", progress);
		tag.setByte("direction", direction);
		tag.setByte("oldDirection", oldDirection);
		tag.setByte("step", step);
		tag.setBoolean("reRoute", reRoute);

		tag.setFloat("originalWidth", originalWidth);
		tag.setFloat("originalHeight", originalHeight);
		//		tag.setFloat("originalYOffset", originalYOffset);
		tag.setFloat("originalEyeHeight", originalEyeHeight);
	}

	public Vec3d getPos(double framePos) {

		// TODO: This is a stopgap to prevent camera jerk. It does lock players into the center of the duct, however.
		//		double v = ((double) progress + step * framePos) / (DUCT_LENGTH) - 0.5;
		//		int dir = v < 0 ? oldDirection : direction;
		//		Vec3i vec = EnumFacing.VALUES[dir].getDirectionVec();

		return new Vec3d(0.5D + pos.getX(), 0.5D + pos.getY(), 0.5D + pos.getZ()); //.addVector(vec.getX() * v, vec.getY() * v, vec.getZ() * v);
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
	public boolean isInLava() {

		return false;
	}

	@Override
	public void moveEntity(double x, double y, double z) {

		setPosition(0);
	}

	@Override
	public void addVelocity(double x, double y, double z) {

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
	public boolean isInRangeToRenderDist(double distance) {

		return distance < 4096;
	}

	public void teleport(DuctUnitTransport dest) {

		if (this.worldObj.isRemote || this.isDead || rider == null || rider.isDead) {
			return;
		}
		int curDim = this.dimension;
		int destDim = dest.world().provider.getDimension();

		if (destDim != curDim) {
			MinecraftServer minecraftserver = this.worldObj.getMinecraftServer();

			WorldServer currentWorld = minecraftserver.worldServerForDimension(curDim);
			WorldServer destinationWorld = minecraftserver.worldServerForDimension(destDim);

			rider.dismountRidingEntity();

			transferNormalEntity(curDim, destDim, currentWorld, destinationWorld, this);

			if (rider instanceof EntityPlayerMP) {
				transferPlayer(destDim, rider);
			} else {
				transferNormalEntity(curDim, destDim, currentWorld, destinationWorld, rider);
			}

			rider.dismountRidingEntity();

			currentWorld.resetUpdateEntityTick();
			destinationWorld.resetUpdateEntityTick();
		}
		pos = new BlockPos(dest.pos());

		if (myPath.hasNextDirection()) {
			oldDirection = direction;
			direction = myPath.getNextDirection();
		} else {
			reRoute = true;
		}
	}

	public void transferPlayer(int destDim, Entity entity) {

		entity.changeDimension(destDim);
	}

	public void transferNormalEntity(int curDim, int destDim, WorldServer currentWorld, WorldServer destinationWorld, Entity entity) {

		entity.changeDimension(destDim);
/*
    TODO verify that this works for normal entity
        entity.worldObj.removeEntity(entity);
        this.worldObj.getMinecraftServer().getConfigurationManager().transferEntityToWorld(entity, curDim, currentWorld, destinationWorld);
        destinationWorld.spawnEntityInWorld(entity);
        entity.dimension = destDim;
*/
	}

}
