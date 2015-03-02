package cofh.thermaldynamics.multiblock;

import cofh.repack.codechicken.lib.vec.BlockCoord;
import gnu.trove.list.linked.TByteLinkedList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Route implements Comparable<Route> {
    public TByteLinkedList pathDirections = new TByteLinkedList();
    public int pathPos = 0;
    public IMultiBlockRoute endPoint;
    public int pathWeight = 0;
    public boolean routeFinished = false;
    public BlockCoord dest;

    public Route(IMultiBlockRoute myParent) {


        endPoint = myParent;
    }

    @SuppressWarnings("unchecked")
    public Route(Route prevRoute, IMultiBlockRoute newPlace, byte direction, boolean isFinished) {

        pathDirections = new TByteLinkedList( prevRoute.pathDirections);
        pathWeight = prevRoute.pathWeight + newPlace.getWeight();
        endPoint = newPlace;

        pathDirections.add(direction);
        routeFinished = isFinished;
    }

    // Used to set as a node
    @SuppressWarnings("unchecked")
    public Route(Route prevRoute, boolean endPath) {

        pathDirections = new TByteLinkedList( prevRoute.pathDirections);
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;

        routeFinished = true;
    }

    @SuppressWarnings("unchecked")
    public Route(Route prevRoute) {

        pathDirections = new TByteLinkedList( prevRoute.pathDirections);
        pathWeight = prevRoute.pathWeight;
        endPoint = prevRoute.endPoint;

        routeFinished = prevRoute.routeFinished;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Route otherRoute) {

        if (this.pathWeight < otherRoute.pathWeight) {
            return -1;
        }
        if (this.pathWeight > otherRoute.pathWeight) {
            return 1;
        }

        if(this.pathDirections.size() < otherRoute.pathDirections.size())
            return  -1;

        if(this.pathDirections.size() > otherRoute.pathDirections.size())
            return  1;

        return 0;
    }

    public Route copy() {

        return new Route(this);
    }

    public byte getNextDirection() {

        return pathDirections.get(pathPos++);
    }

    public boolean hasNextDirection() {

        return pathDirections.size() > pathPos;
    }

    public int getCurrentDirection() {

        return pathDirections.get(pathPos);
    }

    public int checkNextDirection() {

        return pathDirections.get(pathPos + 1);
    }

    public int getLastSide() {

        return pathDirections.size() > 0 ? pathDirections.get(pathDirections.size() - 1) : 0;
    }

    public Route (byte[] b){
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        byte[] array;

        try {
            if (bais.read() == 0) {
                array = new byte[bais.available()];
                bais.read(array);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPInputStream zis = new GZIPInputStream(bais);
                byte[] tmpBuffer = new byte[256];
                int n;
                while ((n = zis.read(tmpBuffer)) >= 0)
                    baos.write(tmpBuffer, 0, n);
                zis.close();
                array = baos.toByteArray();
            }

            for (byte b1 : array) {
                byte b2 = (byte) (b1 & 7);
                if(b2 < 6) {
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
        int src = pathPos;
        int len = pathDirections.size() - src;
        byte[] b = pathDirections.toArray(src, len);

        byte[] bytes = new byte[(b.length + 1) / 2];
        for (int i = 0; (i * 2) < b.length; i += 1) {
            bytes[i] = b[i * 2];
            if (i * 2 + 1 == b.length)
                bytes[i] |= (6 << 3);
            else
                bytes[i] |= b[i * 2 + 1] << 3;
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
