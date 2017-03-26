package cofh.thermaldynamics.multiblock;

import gnu.trove.iterator.TByteIterator;
import gnu.trove.list.linked.TByteLinkedList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Route<T extends IGridTileRoute<T, G>, G extends MultiBlockGrid<T>> implements Comparable<Route> {

	public TByteLinkedList pathDirections = new TByteLinkedList();

	public T endPoint;
	public int pathWeight = 0;
	public boolean routeFinished = false;
	public BlockPos dest;
	public static final byte[] tmpBuffer = new byte[256];

	public Route(T myParent) {

		endPoint = myParent;
	}

	public Route(Route<T,G> prevRoute, T newPlace, byte direction, boolean isFinished) {

		pathDirections = new TByteLinkedList(prevRoute.pathDirections);
		pathWeight = prevRoute.pathWeight + newPlace.getWeight();
		endPoint = newPlace;

		pathDirections.add(direction);
		routeFinished = isFinished;
	}

	// Used to set as a node
	public Route(Route<T,G> prevRoute, boolean endPath) {

		pathDirections = new TByteLinkedList(prevRoute.pathDirections);
		pathWeight = prevRoute.pathWeight;
		endPoint = prevRoute.endPoint;

		routeFinished = true;
	}

	public Route(Route<T,G> prevRoute) {

		pathDirections = new TByteLinkedList(prevRoute.pathDirections);
		pathWeight = prevRoute.pathWeight;
		endPoint = prevRoute.endPoint;

		routeFinished = prevRoute.routeFinished;
	}

	@Override
	public int compareTo(@Nonnull Route otherRoute) {

		if (this.pathWeight < otherRoute.pathWeight) {
			return -1;
		}
		if (this.pathWeight > otherRoute.pathWeight) {
			return 1;
		}

		if (this.pathDirections.size() < otherRoute.pathDirections.size()) {
			return -1;
		}

		if (this.pathDirections.size() > otherRoute.pathDirections.size()) {
			return 1;
		}

		return 0;
	}

	public Route copy() {

		return new Route<>(this);
	}

	public byte getNextDirection() {

		return pathDirections.removeAt(0);
	}

	public boolean hasNextDirection() {

		return pathDirections.size() > 0;
	}

	public int getCurrentDirection() {

		return pathDirections.get(0);
	}

	public int checkNextDirection() {

		return pathDirections.get(1);
	}

	public int getLastSide() {

		return pathDirections.size() > 0 ? pathDirections.get(pathDirections.size() - 1) : 0;
	}

	public Route(byte[] b) {

		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		byte[] array;

		try {
			if (bais.read() == 0) {
				array = new byte[bais.available()];
				bais.read(array);
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPInputStream zis = new GZIPInputStream(bais);
				int n;
				while ((n = zis.read(tmpBuffer)) >= 0) {
					baos.write(tmpBuffer, 0, n);
				}
				zis.close();
				array = baos.toByteArray();
			}

			for (byte b1 : array) {
				byte b2 = (byte) (b1 & 7);
				if (b2 < 6) {
					pathDirections.add(b2);
				}
				byte b3 = (byte) (b1 >> 3);
				if (b3 < 6) {
					pathDirections.add(b3);
				}
			}

		} catch (IOException ignore) {

		}
	}

	public byte[] toByteArray() {

		byte[] bytes = new byte[(pathDirections.size() + 1) / 2];
		int i = 0;

		TByteIterator iterator = pathDirections.iterator();
		while (iterator.hasNext()) {
			bytes[i] = iterator.next();
			if (iterator.hasNext()) {
				bytes[i] |= (iterator.next() << 3);
			} else {
				bytes[i] |= 48;
			}
			i++;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (bytes.length <= 20) {
				baos.write(0);
				baos.write(bytes);
			} else {
				baos.write(1);

				GZIPOutputStream zos = new GZIPOutputStream(baos);
				zos.write(bytes);
				zos.close();
			}
		} catch (IOException ignore) {

		}

		return baos.toByteArray();
	}
}
