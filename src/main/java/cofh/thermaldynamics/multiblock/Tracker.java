package cofh.thermaldynamics.multiblock;

import java.util.Arrays;

public class Tracker {

	private final static byte MEMORY = 101;
	private final static double AVG_MULTIPLIER = 1 / (MEMORY - 1.0);

	public final static short LIFESPAN = 3600;

	byte i = 0;

	short life = 0;

	final int[] mem = new int[MEMORY];
	final long[] memIn = new long[MEMORY];
	final long[] memOut = new long[MEMORY];

	public Tracker(int n) {

		Arrays.fill(mem, n);
	}

	public void newTick(int n) {

		i++;
		life++;
		if (i >= MEMORY) {
			i = 0;
		}

		mem[i] = n;
		memIn[i] = 0;
		memOut[i] = 0;
	}

	public void stuffIn(int n) {

		memIn[i] += n;
	}

	public void stuffOut(int n) {

		memOut[i] += n;
	}

	public double avgStuff() {

		double v = 0;
		for (int j = 0; j < mem.length; j++) {
			if (i == j) {
				continue;
			}
			int m = mem[j];
			v += m;
		}
		return v * AVG_MULTIPLIER;
	}

	public double avgStuffIn() {

		return getUnsignedLongAverage(memIn);
	}

	public double avgStuffOut() {

		return getUnsignedLongAverage(memOut);
	}

	public double getUnsignedLongAverage(long[] arr) {

		double v = 0;
		for (int j = 0; j < arr.length; j++) {
			if (j == i) {
				continue;
			}
			long m = arr[j];
			if (m < 0) {
				double dValue = m & 0x7fffffffffffffffL;
				dValue += 0x1.0p63;
				v += dValue;
			} else {
				v += m;
			}

		}
		return v * AVG_MULTIPLIER;
	}

}
