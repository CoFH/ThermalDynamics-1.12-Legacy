package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.IMultiBlock;
import com.google.common.collect.Iterables;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class EnergyGridGlowing extends EnergyGrid<TileEnergyDuctGlowing> {

	@Nullable
	HashMap<IMultiBlock, TObjectIntHashMap<IMultiBlock>> directions;
	HashMap<IMultiBlock, int[]> inputs = new HashMap<>();
	HashMap<IMultiBlock, int[]> outputs = new HashMap<>();

	public EnergyGridGlowing(World world, int type) {

		super(world, type);
	}

	@Override
	public void onMinorGridChange() {

		super.onMinorGridChange();
		directions = null;
	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		directions = null;
	}

	@Override
	public void tickGrid() {

		super.tickGrid();
		if ((worldGrid.worldObj.getTotalWorldTime() % 2) == 0) {
			processDirections();
		}
	}

	public void processDirections() {

		Iterable<TileEnergyDuctGlowing> blocks = Iterables.concat(nodeSet, idleSet);

		for (TileEnergyDuctGlowing block : blocks) {
			block.resetFlux();
		}

		HashMap<IMultiBlock, TObjectIntHashMap<IMultiBlock>> directions = this.directions;
		if (directions == null) {
			this.directions = directions = new HashMap<>();
		}

		processEntries(blocks, directions, false, inputs);
		processEntries(blocks, directions, true, outputs);

		for (TileEnergyDuctGlowing block : blocks) {
			block.updateFlux();
		}
	}

	protected void processEntries(Iterable<TileEnergyDuctGlowing> blocks, HashMap<IMultiBlock, TObjectIntHashMap<IMultiBlock>> directions, boolean output, HashMap<IMultiBlock, int[]> entrySet) {

		for (Iterator<Map.Entry<IMultiBlock, int[]>> iterator = entrySet.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<IMultiBlock, int[]> entry = iterator.next();
			IMultiBlock key = entry.getKey();
			int[] value = entry.getValue();
			TObjectIntHashMap<IMultiBlock> facingMap = directions.computeIfAbsent(key, k -> buildDirectionTable(key));

			boolean nonEmpty = false;
			for (byte side = 0; side < 6; side++) {
				int val = value[side];
				if (val > 0) {
					nonEmpty = true;
					for (TileEnergyDuctGlowing block : blocks) {
						int s;
						if (block == key) {
							s = 1 << side;
							sendFlux(block, val, side, output);
						} else {
							s = facingMap.get(block);
							int n = 0;
							for (byte i = 0; i < 6; i++) {
								if ((s & (1 << i)) != 0) {
									n++;
								}
							}
							float amount = ((float) val) / n;
							if (n > 0) {
								for (byte i = 0; i < 6; i++) {
									if ((s & (1 << i)) != 0) {
										sendFlux(block, amount, i, output);
									}
								}
							}

						}
					}
					value[side] = 0;
				}
			}
			if (!nonEmpty) {
				iterator.remove();
			}
		}
	}

	private void sendFlux(TileEnergyDuctGlowing block, float val, byte side, boolean output) {

		block.addFlux(val, side, output);
		IMultiBlock neighborMultiBlock = block.neighborMultiBlocks[side];
		if (neighborMultiBlock instanceof TileEnergyDuctGlowing) {
			((TileEnergyDuctGlowing) neighborMultiBlock).addFlux(val, (byte) (side ^ 1), !output);
		}
	}

	public void noteReceivingEnergy(IMultiBlock block, byte side, int amount) {

		noteEnergy(block, side, amount, this.inputs);
	}

	public void noteExtractingEnergy(IMultiBlock block, byte side, int amount) {

		noteEnergy(block, side, amount, this.outputs);
	}

	protected void noteEnergy(IMultiBlock block, byte side, int amount, HashMap<IMultiBlock, int[]> map) {

		if (amount == 0) {
			return;
		}
		int[] sideData = map.computeIfAbsent(block, k -> new int[6]);
		sideData[side] += amount;
	}

	public TObjectIntHashMap<IMultiBlock> buildDirectionTable(IMultiBlock block) {

		TObjectIntHashMap<IMultiBlock> facingMap = new TObjectIntHashMap<>();
		TObjectIntHashMap<IMultiBlock> directionMap = new TObjectIntHashMap<>(10, 0.5F, -1);
		LinkedList<IMultiBlock> toProcess = new LinkedList<>();

		directionMap.put(block, 0);
		facingMap.put(block, (byte) 0);
		toProcess.add(block);
		IMultiBlock processing;
		while ((processing = toProcess.poll()) != null) {
			int bestDir = Integer.MAX_VALUE;
			int sideMask = 0;
			for (byte i = 0; i < 6; i++) {
				if (processing.isSideConnected(i)) {
					IMultiBlock connectedSide = processing.getConnectedSide(i);
					if (connectedSide == null || connectedSide.getGrid() != this) {
						continue;
					}
					int dir = directionMap.get(connectedSide);
					if (dir == -1) {
						toProcess.add(connectedSide);
					} else {
						if (dir < bestDir) {
							bestDir = dir;
							sideMask = (1 << i);
						} else if (dir == bestDir) {
							sideMask |= (1 << i);
						}
					}
				}
			}

			directionMap.put(processing, bestDir);
			facingMap.put(processing, sideMask);
		}

		return facingMap;
	}
}
