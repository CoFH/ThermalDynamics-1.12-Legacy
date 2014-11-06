package thermaldynamics.render;

import cofh.repack.codechicken.lib.render.BlockRenderer;
import cofh.repack.codechicken.lib.render.CCModel;
import cofh.repack.codechicken.lib.render.Vertex5;
import cofh.repack.codechicken.lib.render.uv.UV;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Rotation;
import cofh.repack.codechicken.lib.vec.Transformation;
import cofh.repack.codechicken.lib.vec.Vector3;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class ModelHelper {
    public static CCModel expandModel(CCModel model) {
        return expandModel(model, new Cuboid6(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), 1);
    }

    public static CCModel expandModel(CCModel model, Cuboid6 bounds, double size) {
        CCModel newModel = CCModel.newModel(model.vp == 4 ? 7 : 3, model.verts.length);
        newModel.verts = model.verts.clone();

        for (Vertex5 v : newModel.verts) {
            v.vec.multiply(size);
            if (v.vec.x < bounds.min.x) v.vec.x = bounds.min.x;
            if (v.vec.y < bounds.min.y) v.vec.y = bounds.min.y;
            if (v.vec.z < bounds.min.z) v.vec.z = bounds.min.z;
            if (v.vec.x > bounds.max.x) v.vec.x = bounds.max.x;
            if (v.vec.y > bounds.max.y) v.vec.y = bounds.max.y;
            if (v.vec.z > bounds.max.z) v.vec.z = bounds.max.z;
        }

        return newModel.computeNormals();
    }

    static Vector3[] axes = {
            new Vector3(0, -1, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, 0, -1),
            new Vector3(0, 0, 1),
            new Vector3(-1, 0, 0),
            new Vector3(1, 0, 0),
    };

    static int[] sideMasks = {3, 3, 12, 12, 48, 48};

    public static LinkedList<Vertex5> addSideFaces(Cuboid6 bounds, int sideMask) {
        LinkedList<Vertex5> vecs = new LinkedList<Vertex5>();

        for (int side = 0; side < sideMask; side++) {
            if ((sideMask & (1 << side)) == 0)
                vecs.addAll(addSideFace(bounds, side));
        }
        return vecs;
    }

    public static LinkedList<Vertex5> addSideFace(Cuboid6 bounds, int side) {
        LinkedList<Vertex5> vecs = new LinkedList<Vertex5>();
        face.loadCuboidFace(bounds.copy().add(Vector3.center), side);
        for (Vertex5 v : face.getVertices())
            vecs.add(new Vertex5(v.vec.copy().sub(Vector3.center), v.uv.copy()));

        return vecs;
    }

    static BlockRenderer.BlockFace face = new BlockRenderer.BlockFace();

    public static LinkedList<Vertex5> apply(LinkedList<Vertex5> vecs, Transformation transformation) {
        LinkedList<Vertex5> t = new LinkedList<Vertex5>();
        for (Vertex5 v : vecs) {
            t.add(v.copy().apply(transformation));
        }
        return t;
    }

    // very slow method that combines squares
    public static LinkedList<Vertex5> simplifyModel(LinkedList<Vertex5> in) {
        LinkedList<Face> faces = new LinkedList<Face>();
        Iterator<Vertex5> iter = in.iterator();
        while (iter.hasNext()) {
            Face f = Face.loadFromIterator(iter);

            for (Iterator<Face> iterator = faces.iterator(); iterator.hasNext(); ) {
                Face g = iterator.next();
                if (f.attemptToCombine(g)) {
                    iterator.remove();
                }
            }

            faces.add(f);
        }

        LinkedList<Vertex5> out = new LinkedList<Vertex5>();
        for (Face f : faces) {
            Collections.addAll(out, f.verts);
        }

        return out;
    }


    public static class Face {

        public static Face loadFromIterator(Iterator<Vertex5> iter) {
            Face f = new Face(new Vertex5[4]);
            for (int i = 0; i < 4; i++) {
                f.verts[i] = iter.next();
            }

            return f;
        }

        public Vertex5[] verts;

        public Vertex5 vec(int s) {
            return verts[s & 3];
        }

        public void setVec(int s, Vertex5 newVec) {
            verts[s & 3] = newVec;
        }

        public Face(Vertex5... v) {
            assert v.length == 4;
            this.verts = v;
        }

        public boolean isPolygon() {
            for (int i = 0; i < 4; i++) {
                if (vec(i).vec.equalsT(vec(i + 1).vec))
                    return true;
            }
            return false;
        }

        public Face reverse() {
            verts = new Vertex5[]{verts[3], verts[2], verts[1], verts[0]};
            return this;
        }

        public boolean attemptToCombine(Face other) {
            if (isPolygon() || other.isPolygon())
                return false;

            if (attemptToCombineUnflipped(other)) return true;
            reverse();
            if (attemptToCombineUnflipped(other)) return true;
            reverse();
            other.reverse();
            if (attemptToCombineUnflipped(other)) return true;
            reverse();
            if (attemptToCombineUnflipped(other)) return true;
            reverse();
            other.reverse();
            return false;
        }

        public boolean equalVert(Vertex5 a, Vertex5 b) {
            return a.vec.equalsT(b.vec) && a.uv.equals(b.uv);
        }

        public boolean attemptToCombineUnflipped(Face other) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (equalVert(vec(i), vec(j)) && equalVert(vec(i + 1), vec(j - 1))) {
                        Vector3 l1 = (vec(i - 1).vec.copy().subtract(vec(i).vec)).normalize();
                        Vector3 l2 = (vec(i + 2).vec.copy().subtract(vec(i + 1).vec)).normalize();

                        Vector3 l3 = (other.vec(j).vec.copy().subtract(other.vec(j + 1).vec)).normalize();
                        Vector3 l4 = (other.vec(j - 1).vec.copy().subtract(other.vec(j - 2).vec)).normalize();

                        if (l1.equalsT(l3) && l2.equalsT(l4)) {
                            setVec(i, other.vec(j + 1));
                            setVec(i + 1, other.vec(j - 2));
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    public static class OctagonalTubeGen {
        double size;
        double innerSize;
        static final double outerWidth = 0.5;

        boolean frameOnly = false;


        Vector3[] octoFace;


        public OctagonalTubeGen(double size, boolean framesOnly) {
            this(size, size * 0.414, framesOnly);
        }

        public static int getBestSide(Vector3 vector3) {
            int s = 0;
            double m = 0;
            for (int i = 2; i < 6; i++) {
                if (Math.abs(vector3.getSide(i)) > m) {
                    m = Math.abs(vector3.getSide(i));
                    s = i;
                }
            }
            return s;
        }

        public static Vertex5 toVertex5(Vector3 vector3) {
            return toVertex5(vector3, getBestSide(vector3));
        }

        public static Vertex5 toVertex5(Vector3 vector3, int side) {
            UV uv;
            if (side == 0 || side == 1)
                uv = new UV(0.5 + vector3.x, 0.5 + vector3.z);
            else if (side == 2 || side == 3)
                uv = new UV(0.5 + vector3.x, 0.5 + vector3.y);
            else if (side == 4 || side == 5)
                uv = new UV(0.5 + vector3.z, 0.5 + vector3.y);
            else
                uv = new UV(0.5, 0.5);

            return new Vertex5(vector3, uv);
        }

        public OctagonalTubeGen(double s, double t, boolean framesOnly) {
            this.size = s;
            this.innerSize = t;
            this.frameOnly = framesOnly;

            octoFace = new Vector3[8];

            octoFace[0] = new Vector3(-s, -0.5, -t);
            octoFace[1] = new Vector3(-t, -0.5, -s);
            octoFace[2] = new Vector3(t, -0.5, -s);
            octoFace[3] = new Vector3(s, -0.5, -t);
            octoFace[4] = new Vector3(s, -0.5, t);
            octoFace[5] = new Vector3(t, -0.5, s);
            octoFace[6] = new Vector3(-t, -0.5, s);
            octoFace[7] = new Vector3(-s, -0.5, t);


        }


        public CCModel[] generateModels() {
            CCModel[] models = new CCModel[64];
            for (int i = 0; i < 64; i++) {
                LinkedList<Vertex5> v = generateIntersections(i);
                v = simplifyModel(v);
                int n = v.size();
                models[i] = CCModel.newModel(7, n * 2);

                for (int j = 0; j < n; j++) {
                    Vertex5 nv = v.get(j);
                    models[i].verts[j] = nv;
                }

                CCModel.generateBackface(models[i], 0, models[i], n, n);

                models[i].computeNormals();
            }
            return models;
        }


        public LinkedList<Vertex5> generateIntersections(int connections) {
            LinkedList<Vertex5> v = new LinkedList<Vertex5>();

            LinkedList<Vertex5> center = addSideFace(new Cuboid6(-innerSize, -size, -innerSize, innerSize, size, innerSize), 0);
            LinkedList<Vertex5> arm = new LinkedList<Vertex5>();

            for (int k = 0; k < 8; k++) {
                if (frameOnly && (k % 2 == 0))
                    continue;
                arm.add(toVertex5(octoFace[k].copy()));
                arm.add(toVertex5(octoFace[k].copy().setSide(0, -size)));
                arm.add(toVertex5(octoFace[(k + 1) % 8].copy().setSide(0, -size)));
                arm.add(toVertex5(octoFace[(k + 1) % 8].copy()));
            }


            for (int i = 0; i < 6; i++) {
                if ((connections & (1 << i)) != 0) {

                    v.addAll(apply(arm, Rotation.sideRotations[i]));
                } else {
                    v.addAll(apply(center, Rotation.sideRotations[i]));
                }
            }

            for (int i = 0; i < 6; i++) {
                for (int j = (i + 1); j < 6; j++) {
                    if ((i ^ 1) == j)
                        continue;

                    boolean a = (connections & (1 << i)) != 0;
                    boolean b = (connections & (1 << j)) != 0;

                    Vector3 v1 = axes[i].copy();
                    Vector3 v2 = axes[j].copy();
                    Vector3 v3 = v1.copy().crossProduct(v2);

                    if (!a && !b) {
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(innerSize)).add(v3.copy().multiply(innerSize)), i));
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(innerSize)).add(v3.copy().multiply(-innerSize)), i));
                        v.add(toVertex5(v1.copy().multiply(innerSize).add(v2.copy().multiply(size)).add(v3.copy().multiply(-innerSize)), i));
                        v.add(toVertex5(v1.copy().multiply(innerSize).add(v2.copy().multiply(size)).add(v3.copy().multiply(innerSize)), i));
                    } else if (!a && b) {
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(innerSize)).add(v3.copy().multiply(innerSize)), i));
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(innerSize)).add(v3.copy().multiply(-innerSize)), i));
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(size)).add(v3.copy().multiply(-innerSize)), i));
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(size)).add(v3.copy().multiply(innerSize)), i));
                    } else if (a && !b) {
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(size)).add(v3.copy().multiply(innerSize)), j));
                        v.add(toVertex5(v1.copy().multiply(size).add(v2.copy().multiply(size)).add(v3.copy().multiply(-innerSize)), j));
                        v.add(toVertex5(v1.copy().multiply(innerSize).add(v2.copy().multiply(size)).add(v3.copy().multiply(-innerSize)), j));
                        v.add(toVertex5(v1.copy().multiply(innerSize).add(v2.copy().multiply(size)).add(v3.copy().multiply(innerSize)), j));
                    }
                }
            }

            if (!frameOnly) {

                for (int i = 0; i < 2; i++) {
                    for (int j = 2; j < 4; j++) {
                        for (int k = 4; k < 6; k++) {
                            boolean up = (connections & (1 << i)) != 0;
                            boolean right = (connections & (1 << j)) != 0;
                            boolean left = (connections & (1 << k)) != 0;

                            int s = (up ? 1 : 0) + (right ? 1 : 0) + (left ? 1 : 0);

                            Vector3 v1 = axes[i];
                            Vector3 v2 = axes[j];
                            Vector3 v3 = axes[k];

                            if (s == 3) {
                                Vector3 a1 = v1.copy().multiply(size).add(v2.copy().multiply(size).add(v3.copy().multiply(innerSize)));
                                Vector3 a2 = v1.copy().multiply(size).add(v2.copy().multiply(innerSize).add(v3.copy().multiply(size)));
                                Vector3 a3 = v1.copy().multiply(innerSize).add(v2.copy().multiply(size).add(v3.copy().multiply(size)));

                                v.add(toVertex5(a1, i));
                                v.add(toVertex5(a1, i));
                                v.add(toVertex5(a3, i));
                                v.add(toVertex5(a2, i));

                            } else if (s == 0) {
                                Vector3 a1 = v1.copy().multiply(size).add(v2.copy().multiply(innerSize).add(v3.copy().multiply(innerSize)));
                                Vector3 a2 = v1.copy().multiply(innerSize).add(v2.copy().multiply(innerSize).add(v3.copy().multiply(size)));
                                Vector3 a3 = v1.copy().multiply(innerSize).add(v2.copy().multiply(size).add(v3.copy().multiply(innerSize)));

                                v.add(toVertex5(a1, 0));
                                v.add(toVertex5(a1, 0));
                                v.add(toVertex5(a3, 0));
                                v.add(toVertex5(a2, 0));
                            } else if (s == 1) {
                                Vector3 a1;
                                Vector3 a2;
                                Vector3 a3;
                                if (up) {
                                    a1 = v1;
                                    a2 = v2;
                                    a3 = v3;
                                } else if (right) {
                                    a1 = v2;
                                    a2 = v1;
                                    a3 = v3;
                                } else {
                                    a1 = v3;
                                    a2 = v1;
                                    a3 = v2;
                                }

                                v.add(toVertex5(a1.copy().multiply(innerSize).add(a2.copy().multiply(size)).add(a3.copy().multiply(innerSize)), 0));
                                v.add(toVertex5(a1.copy().multiply(size).add(a2.copy().multiply(size).add(a3.copy().multiply(innerSize))), 0));
                                v.add(toVertex5(a1.copy().multiply(size).add(a2.copy().multiply(innerSize)).add(a3.copy().multiply(size)), 0));
                                v.add(toVertex5(a1.copy().multiply(innerSize).add(a2.copy().multiply(innerSize)).add(a3.copy().multiply(size)), 0));
                            }

                            if (s == 2) {
                                int dir;
                                Vector3 a1;
                                Vector3 a2;
                                Vector3 a3;
                                if (!up) {
                                    dir = i;
                                    a1 = v1;
                                    a2 = v2;
                                    a3 = v3;
                                } else if (!right) {
                                    dir = j;
                                    a1 = v2;
                                    a2 = v1;
                                    a3 = v3;
                                } else {
                                    dir = k;
                                    a1 = v3;
                                    a2 = v1;
                                    a3 = v2;
                                }


                                v.add(toVertex5(a1.copy().multiply(size).add(a2.copy().multiply(innerSize)).add(a3.copy().multiply(innerSize)), dir));
                                v.add(toVertex5(a1.copy().multiply(size).add(a2.copy().multiply(size)).add(a3.copy().multiply(innerSize)), dir));
                                v.add(toVertex5(a1.copy().multiply(innerSize).add(a2.copy().multiply(size)).add(a3.copy().multiply(size)), dir));
                                v.add(toVertex5(a1.copy().multiply(size).add(a2.copy().multiply(innerSize)).add(a3.copy().multiply(size)), dir));


                            }
                        }
                    }
                }
            }

            return v;
        }


    }
}
