package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.multiblock.IGridTile;
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
	HashMap<IGridTile, TObjectIntHashMap<IGridTile>> directions;
	HashMap<IGridTile, int[]> inputs = new HashMap<>();
	HashMap<IGridTile, int[]> outputs = new HashMap<>();

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

		HashMap<IGridTile, TObjectIntHashMap<IGridTile>> directions = this.directions;
		if (directions == null) {
			this.directions = directions = new HashMap<>();
		}

		processEntries(blocks, directions, false, inputs);
		processEntries(blocks, directions, true, outputs);

		for (TileEnergyDuctGlowing block : blocks) {
			block.updateFlux();
		}
	}

	protected void processEntries(Iterable<TileEnergyDuctGlowing> blocks, HashMap<IGridTile, TObjectIntHashMap<IGridTile>> directions, boolean output, HashMap<IGridTile, int[]> entrySet) {

		for (Iterator<Map.Entry<IGridTile, int[]>> iterator = entrySet.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry<IGridTile, int[]> entry = iterator.next();
			IGridTile key = entry.getKey();
			int[] value = entry.getValue();
			TObjectIntHashMap<IGridTile> facingMap = directions.computeIfAbsent(key, k -> buildDirectionTable(key));

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
		IGridTile neighborMultiBlock = block.neighborMultiBlocks[side];
		if (neighborMultiBlock instanceof TileEnergyDuctGlowing) {
			((TileEnergyDuctGlowing) neighborMultiBlock).addFlux(val, (byte) (side ^ 1), !output);
		}
	}

	public void noteReceivingEnergy(IGridTile block, byte side, int amount) {

		noteEnergy(block, side, amount, this.inputs);
	}

	public void noteExtractingEnergy(IGridTile block, byte side, int amount) {

		noteEnergy(block, side, amount, this.outputs);
	}

	protected void noteEnergy(IGridTile block, byte side, int amount, HashMap<IGridTile, int[]> map) {

		if (amount == 0) {
			return;
		}
		int[] sideData = map.computeIfAbsent(block, k -> new int[6]);
		sideData[side] += amount;
	}

	public TObjectIntHashMap<IGridTile> buildDirectionTable(IGridTile block) {

		TObjectIntHashMap<IGridTile> facingMap = new TObjectIntHashMap<>();
		TObjectIntHashMap<IGridTile> directionMap = new TObjectIntHashMap<>(10, 0.5F, -1);
		LinkedList<IGridTile> toProcess = new LinkedList<>();

		directionMap.put(block, 0);
		facingMap.put(block, (byte) 0);
		toProcess.add(block);
		IGridTile processing;
		while ((processing = toProcess.poll()) != null) {
			int bestDir = Integer.MAX_VALUE;
			int sideMask = 0;
			for (byte i = 0; i < 6; i++) {
				if (processing.isSideConnected(i)) {
					IGridTile connectedSide = processing.getConnectedSide(i);
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
