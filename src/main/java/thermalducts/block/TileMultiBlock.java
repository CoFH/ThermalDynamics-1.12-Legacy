package thermalducts.block;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import cofh.api.tileentity.IPlacedTile;
import cofh.block.TileCoFHBase;
import cofh.network.CoFHPacket;
import cofh.network.ITilePacketHandler;
import cofh.render.hitbox.CustomHitBox;
import cofh.render.hitbox.ICustomHitBox;
import cofh.util.BlockHelper;
import cofh.util.ServerHelper;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.StringUtils;

import thermalducts.core.TickHandler;
import thermalducts.multiblock.IMultiBlock;
import thermalducts.multiblock.MultiBlockFormer;
import thermalducts.multiblock.MultiBlockGrid;

public class TileMultiBlock extends TileCoFHBase implements IMultiBlock, IPlacedTile, ITilePacketHandler, ICustomHitBox {

	static {
		GameRegistry.registerTileEntity(TileMultiBlock.class, "thermalducts.multiblock");
	}
	public boolean isValid = true;
	public boolean isNode = false;
	public MultiBlockGrid myGrid;
	public IMultiBlock neighborMultiBlocks[] = new IMultiBlock[ForgeDirection.VALID_DIRECTIONS.length];
	public NeighborTypes neighborTypes[] = { NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE, NeighborTypes.NONE };

	@Override
	public void setInvalidForForming() {

		isValid = false;
	}

	@Override
	public void setValidForForming() {

		isValid = true;
	}

	@Override
	public boolean isValidForForming() {

		return isValid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new MultiBlockGrid();
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		myGrid = newGrid;
	}

	@Override
	public MultiBlockGrid getGrid() {

		return myGrid;
	}

	@Override
	public IMultiBlock getConnectedSide(byte side) {

		return (IMultiBlock) BlockHelper.getAdjacentTileEntity(this, side);
	}

	@Override
	public boolean isSideConnected(byte side) {

		return BlockHelper.getAdjacentTileEntity(this, side) instanceof TileMultiBlock; // neighborTypes[side] == NeighborTypes.MULTIBLOCK; //
	}

	@Override
	public void setNotConnected(byte side) {

	}

	@Override
	public void tilePlaced() {

		onNeighborBlockChange();
		synchronized (TickHandler.INSTANCE.multiBlocksToCalculate) {
			TickHandler.INSTANCE.multiBlocksToCalculate.add(this);
		}
		System.out.println("tilePlaced");
	}

	@Override
	public void onNeighborBlockChange() {

		TileEntity theTile;
		boolean wasNode = isNode;
		isNode = false;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			theTile = BlockHelper.getAdjacentTileEntity(this, i);
			if (isConnectable(theTile, i)) {
				neighborMultiBlocks[i] = (IMultiBlock) theTile;
				neighborTypes[i] = NeighborTypes.MULTIBLOCK;
			} else if (isSignificantTile(theTile, i)) {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.TILE;
				isNode = true;
			} else {
				neighborMultiBlocks[i] = null;
				neighborTypes[i] = NeighborTypes.NONE;
			}
		}

		if (wasNode != isNode && myGrid != null) {
			myGrid.addBlock(this);
		}
		// worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	}

	@Override
	public void onNeighborTileChange(int tileX, int tileY, int tileZ) {

		int side = BlockHelper.determineAdjacentSide(this, tileX, tileY, tileZ);
		TileEntity theTile = worldObj.getTileEntity(tileX, tileY, tileZ);
		if (isConnectable(theTile, side)) {
			neighborMultiBlocks[side] = (IMultiBlock) theTile;
			neighborTypes[side] = NeighborTypes.MULTIBLOCK;
		} else if (isSignificantTile(theTile, side)) {
			neighborMultiBlocks[side] = null;
			neighborTypes[side] = NeighborTypes.TILE;
		} else {
			neighborMultiBlocks[side] = null;
			neighborTypes[side] = NeighborTypes.NONE;
		}
		boolean wasNode = isNode;
		checkIsNode();
		if (wasNode != isNode && myGrid != null) {
			myGrid.addBlock(this);
		}
	}

	private void checkIsNode() {

		isNode = false;
		for (byte i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			if (neighborTypes[i] == NeighborTypes.TILE) {
				isNode = true;
				return;
			}
		}
	}

	/*
	 * Should return true if theTile is an instance of this multiblock.
	 * 
	 * This must also be an instance of IMultiBlock
	 */
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileMultiBlock;
	}

	/*
	 * Should return true if theTile is significant to this multiblock
	 * 
	 * IE: Inventory's to ItemDuct's
	 */
	public boolean isSignificantTile(TileEntity theTile, int side) {

		return false;
	}

	@Override
	public String getName() {

		return "tile.thermalducts.multiblock.name";
	}

	@Override
	public int getType() {

		return 0;
	}

	@Override
	public void tickMultiBlock() {

		onNeighborBlockChange();
		formGrid();
	}

	public void formGrid() {

		if (myGrid == null && ServerHelper.isServerWorld(worldObj)) {
			new MultiBlockFormer().formGrid(this);
			// DEBUG CODE
			System.out.println("Grid Formed: " + (myGrid != null ? myGrid.nodeSet.size() + myGrid.idleSet.size() : "Failed"));
		}
	}

	@Override
	public void tickPass(int pass) {

	}

	@Override
	public boolean isNode() {

		return isNode;
	}

	@Override
	public void readFromNBT(NBTTagCompound p_145839_1_) {

		super.readFromNBT(p_145839_1_);
		synchronized (TickHandler.INSTANCE.multiBlocksToCalculate) {
			TickHandler.INSTANCE.multiBlocksToCalculate.add(this);
		}
	}

	public static enum NeighborTypes {
		NONE, MULTIBLOCK, TILE
	}

	public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

		double minX, minY, minZ;
		double maxX, maxY, maxZ;
		minX = minY = minZ = 0.3;
		maxX = maxY = maxZ = 0.7;

		Vector3 pos = new Vector3(xCoord, yCoord, zCoord);
		for (int i = 0; i < 6; i++) {
			if (neighborTypes[i] == NeighborTypes.TILE) {
				cuboids.add(new IndexedCuboid6(i, subSelection[i].copy().add(pos)));
			}
		}
		if (neighborTypes[0] == NeighborTypes.MULTIBLOCK) {
			minY = 0;
		}
		if (neighborTypes[1] == NeighborTypes.MULTIBLOCK) {
			maxY = 1;
		}
		if (neighborTypes[2] == NeighborTypes.MULTIBLOCK) {
			minZ = 0;
		}
		if (neighborTypes[3] == NeighborTypes.MULTIBLOCK) {
			maxZ = 1;
		}
		if (neighborTypes[4] == NeighborTypes.MULTIBLOCK) {
			minX = 0;
		}
		if (neighborTypes[5] == NeighborTypes.MULTIBLOCK) {
			maxX = 1;
		}
		cuboids.add(new IndexedCuboid6(6, new Cuboid6(minX, minY, minZ, maxX, maxY, maxZ).add(pos)));

		// if (neighborTypes[0] == NeighborTypes.MULTIBLOCK) {
		// minY = 0;
		// }
		// if (neighborTypes[1] == NeighborTypes.MULTIBLOCK) {
		// maxY = 1;
		// }
		// if (neighborTypes[2] == NeighborTypes.MULTIBLOCK) {
		// minZ = 0;
		// }
		// if (neighborTypes[3] == NeighborTypes.MULTIBLOCK) {
		// maxZ = 1;
		// }
		// if (neighborTypes[4] == NeighborTypes.MULTIBLOCK) {
		// minX = 0;
		// }
		// if (neighborTypes[5] == NeighborTypes.MULTIBLOCK) {
		// maxX = 1;
		// }
		// cuboids.add(new IndexedCuboid6(6, new Cuboid6(minX, minY, minZ, maxX, maxY, maxZ).add(pos)));
	}

	@Override
	public boolean shouldRenderCustomHitBox(int subHit) {

		return subHit == 6;
	}

	@Override
	public CustomHitBox getCustomHitBox(int subHit) {

		CustomHitBox hb = new CustomHitBox(.4, .4, .4, xCoord + .3, yCoord + .3, zCoord + .3);

		for (int i = 0; i < neighborTypes.length; i++) {
			if (neighborTypes[i] == NeighborTypes.MULTIBLOCK) {
				hb.drawSide(i, true);
				hb.setSideLength(i, .3);
			} else if (neighborTypes[i] == NeighborTypes.TILE) {
				hb.drawSide(i, true);
				hb.setSideLength(i, .04);
			}
		}

		return hb;
	}

	public static Cuboid6[] subSelection = new Cuboid6[6];

	static {

		double min = 0.25;
		double max = 0.75;
		double min2 = 0.2;
		double max2 = 0.8;

		subSelection[0] = new Cuboid6(min2, 0.0, min2, max2, min, max2);
		subSelection[1] = new Cuboid6(min2, 1.0 - min, min2, max2, 1.0, max2);
		subSelection[2] = new Cuboid6(min2, min2, 0.0, max2, max2, min);
		subSelection[3] = new Cuboid6(min2, min2, 1.0 - min, max2, max2, 1.0);
		subSelection[4] = new Cuboid6(0.0, min2, min2, min, max2, max2);
		subSelection[5] = new Cuboid6(1.0 - min, min2, min2, 1.0, max2, max2);
	}

	public void doDebug(EntityPlayer thePlayer) {

		thePlayer.addChatMessage(new ChatComponentText("Neighbors: " + StringUtils.join(neighborTypes, ",")));
		thePlayer.addChatMessage(new ChatComponentText("isNode: " + isNode));
		thePlayer.addChatMessage(new ChatComponentText("Grid Nodes: " + myGrid.nodeSet.size()));
	}

	/* NETWORK METHODS */
	@Override
	public CoFHPacket getPacket() {

		CoFHPacket payload = super.getPacket();

		for (byte i = 0; i < neighborTypes.length; i++) {
			payload.addByte(neighborTypes[i].ordinal());
		}
		// payload.addBool(isNode);

		return payload;
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(CoFHPacket payload, boolean isServer) {

		if (ServerHelper.isClientWorld(worldObj)) {
			for (byte i = 0; i < neighborTypes.length; i++) {
				neighborTypes[i] = NeighborTypes.values()[payload.getByte()];
			}
		}
	}

}
