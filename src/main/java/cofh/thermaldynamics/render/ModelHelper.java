package cofh.thermaldynamics.render;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCModel;
import codechicken.lib.vec.*;
import codechicken.lib.vec.uv.UV;
import cofh.core.util.helpers.MathHelper;
import cofh.core.util.helpers.RenderHelper;
import cofh.thermaldynamics.init.TDProps;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class ModelHelper {

	public static class StandardTubes {

		public final boolean opaque;
		public final float width;

		final static int[][] orthogs = { { 2, 3, 4, 5 }, { 2, 3, 4, 5 }, { 0, 1, 4, 5 }, { 0, 1, 4, 5 }, { 0, 1, 2, 3 }, { 0, 1, 2, 3 }, };

		Cuboid6 center;
		Cuboid6[] ductWCenter = new Cuboid6[6];
		Cuboid6[] duct = new Cuboid6[6];
		Cuboid6[] ductFullLength = new Cuboid6[6];

		public static Cuboid6[] rotateCuboids(Cuboid6 downCube) {

			Cuboid6[] cuboid6s = new Cuboid6[6];
			for (int i = 0; i < 6; i++) {
				cuboid6s[i] = downCube.copy().apply(Rotation.sideRotations[i]);
			}
			return cuboid6s;
		}

		public static CCModel[] genModels(float w, boolean opaque) {

			return genModels(w, opaque, true);
		}

		public static CCModel[] genModels(float w, boolean opaque, boolean lighting) {

			StandardTubes tubes = new StandardTubes(w, opaque);
			CCModel[] models = new CCModel[64];
			for (int i = 0; i < 64; i++) {
				LinkedList<Vertex5> model = tubes.createModel(i);

				int n = model.size();
				models[i] = CCModel.newModel(7, n * 2);

				for (int j = 0; j < n; j++) {
					models[i].verts[j] = model.get(j);
				}
				CCModel.generateBackface(models[i], 0, models[i], n, n);

				finalizeModel(models[i], lighting);
			}
			return models;
		}

		public StandardTubes(int i) {

			this.width = 0.36f;
			this.opaque = false;
			double d1 = 0.47 - 0.025 * i;
			double d2 = 0.53 + 0.025 * i;
			double d3 = 0.32 + 0.06 * i;
			double c1 = 0.32;
			double c2 = 0.68;
			double[][] boxes = new double[][] { { d1, 0, d1, d2, c1, d2 }, { d1, d3, d1, d2, 1, d2 }, { c1, c1, 0, c2, d3, c1 }, { c1, c1, c2, c2, d3, 1 }, { 0, c1, c1, c1, d3, c2 }, { c2, c1, c1, 1, d3, c2 } };

			center = new Cuboid6(c1, c1, c1, c2, d3, c2);

			duct = new Cuboid6[6];
			for (int s = 0; s < duct.length; s++) {
				duct[s] = new Cuboid6(boxes[s][0], boxes[s][1], boxes[s][2], boxes[s][3], boxes[s][4], boxes[s][5]);
			}

		}

		public StandardTubes(float w, boolean opaque) {

			this.width = w;
			center = new Cuboid6(-w, -w, -w, w, w, w);
			duct = rotateCuboids(new Cuboid6(-w, -0.5, -w, w, -w, w));
			ductWCenter = rotateCuboids(new Cuboid6(-w, -0.5, -w, w, w, w));
			ductFullLength = rotateCuboids(new Cuboid6(-w, -0.5, -w, w, 0.5, w));
			this.opaque = opaque;
		}

		public LinkedList<Vertex5> createModel(int cMask) {

			LinkedList<Vertex5> verts = new LinkedList<>();

			for (int side = 0; side < 6; side++) {
				if (!opaque && MathHelper.isBitSet(cMask, side)) {
					for (int i : orthogs[side]) {
						if (MathHelper.isBitSet(cMask, i)) {
							addSideFace(verts, duct[i], side);
						}
					}
				} else {
					int singlePipeIndex = -1, doublePipeIndex = -1;
					for (int i : orthogs[side]) {
						if (MathHelper.isBitSet(cMask, i) && ductWCenter[i] != null) {
							singlePipeIndex = i;
							if (MathHelper.isBitSet(cMask, i ^ 1) && ductFullLength[i] != null && ductFullLength[i ^ 1] != null) {
								doublePipeIndex = i;
								break;
							}
						}
					}
					if (doublePipeIndex != -1) {
						for (int i : orthogs[side]) {
							if (i == doublePipeIndex) {
								addSideFace(verts, ductFullLength[i], side);
							} else if (i != (doublePipeIndex ^ 1) && MathHelper.isBitSet(cMask, i)) {
								addSideFace(verts, duct[i], side);
							}
						}
					} else if (singlePipeIndex != -1) {
						for (int i : orthogs[side]) {
							if (i == singlePipeIndex) {
								addSideFace(verts, ductWCenter[i], side);
							} else if (MathHelper.isBitSet(cMask, i)) {
								addSideFace(verts, duct[i], side);
							}
						}
					} else {
						if (!MathHelper.isBitSet(cMask, side)) {
							addSideFace(verts, center, side);
						}

						for (int i : orthogs[side]) {
							if (MathHelper.isBitSet(cMask, i)) {
								addSideFace(verts, duct[i], side);
							}
						}
					}
				}
			}
			return verts;
		}
	}

	public static void finalizeModel(CCModel model1) {

		finalizeModel(model1, true);
	}

	public static void finalizeModel(CCModel model1, boolean lighting) {

		if (lighting) {
			model1.shrinkUVs(RenderHelper.RENDER_OFFSET).computeNormals().computeLighting(LightModel.standardLightModel);
		} else {
			model1.shrinkUVs(RenderHelper.RENDER_OFFSET).computeNormals();
		}
	}

	public static CCModel expandModel(CCModel model, double size) {

		return expandModel(model, new Cuboid6(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), size);
	}

	public static CCModel expandModel(CCModel model, Cuboid6 bounds, double size) {

		CCModel newModel = CCModel.newModel(model.vp == 4 ? 7 : 3, model.verts.length);
		newModel.verts = model.verts.clone();

		for (Vertex5 v : newModel.verts) {
			v.vec.multiply(size);
			if (v.vec.x < bounds.min.x) {
				v.vec.x = bounds.min.x;
			}
			if (v.vec.y < bounds.min.y) {
				v.vec.y = bounds.min.y;
			}
			if (v.vec.z < bounds.min.z) {
				v.vec.z = bounds.min.z;
			}
			if (v.vec.x > bounds.max.x) {
				v.vec.x = bounds.max.x;
			}
			if (v.vec.y > bounds.max.y) {
				v.vec.y = bounds.max.y;
			}
			if (v.vec.z > bounds.max.z) {
				v.vec.z = bounds.max.z;
			}
		}

		return newModel.computeNormals();
	}

	static Vector3[] axes = { new Vector3(0, -1, 0), new Vector3(0, 1, 0), new Vector3(0, 0, -1), new Vector3(0, 0, 1), new Vector3(-1, 0, 0), new Vector3(1, 0, 0), };

	static int[] sideMasks = { 3, 3, 12, 12, 48, 48 };

	public static void addSideFaces(LinkedList<Vertex5> vecs, Cuboid6 bounds, int sideMask) {

		for (int s = 0; s < 6; s++) {
			if ((sideMask & (1 << s)) == 0) {
				addSideFace(vecs, bounds, s);
			}
		}
	}

	public static LinkedList<Vertex5> addSideFace(LinkedList<Vertex5> vecs, Cuboid6 bounds, int side) {

		face.loadCuboidFace(bounds.copy().add(Vector3.center), side);
		for (Vertex5 v : face.getVertices()) {
			vecs.add(new Vertex5(v.vec.copy().subtract(Vector3.center), v.uv.copy()));
		}
		return vecs;
	}

	static BlockRenderer.BlockFace face = new BlockRenderer.BlockFace();

	public static LinkedList<Vertex5> apply(LinkedList<Vertex5> vecs, Transformation transformation) {

		LinkedList<Vertex5> t = new LinkedList<>();
		for (Vertex5 v : vecs) {
			t.add(v.copy().apply(transformation));
		}
		return t;
	}

	// very slow method that combines squares
	public static LinkedList<Vertex5> simplifyModel(LinkedList<Vertex5> in) {

		LinkedList<Face> faces = new LinkedList<>();
		Iterator<Vertex5> iter = in.iterator();
		while (iter.hasNext()) {
			Face f = Face.loadFromIterator(iter);

			faces.removeIf(f::attemptToCombine);
			faces.add(f);
		}

		LinkedList<Vertex5> out = new LinkedList<>();
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
				if (vec(i).vec.equalsT(vec(i + 1).vec)) {
					return true;
				}
			}
			return false;
		}

		public Face reverse() {

			verts = new Vertex5[] { verts[3], verts[2], verts[1], verts[0] };
			return this;
		}

		public boolean attemptToCombine(Face other) {

			if (isPolygon() || other.isPolygon()) {
				return false;
			}

			if (attemptToCombineUnflipped(other)) {
				return true;
			}
			reverse();
			if (attemptToCombineUnflipped(other)) {
				return true;
			}
			reverse();
			other.reverse();
			if (attemptToCombineUnflipped(other)) {
				return true;
			}
			reverse();
			if (attemptToCombineUnflipped(other)) {
				return true;
			}
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
			if (side == 0 || side == 1) {
				uv = new UV(0.5 + vector3.x, 0.5 + vector3.z);
			} else if (side == 2 || side == 3) {
				uv = new UV(0.5 + vector3.x, 0.5 + vector3.y);
			} else if (side == 4 || side == 5) {
				uv = new UV(0.5 + vector3.z, 0.5 + vector3.y);
			} else {
				uv = new UV(0.5, 0.5);
			}
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

		public CCModel generateSideFace() {

			CCModel model = CCModel.newModel(7, 24);

			model.verts[0] = toVertex5(octoFace[0].copy(), 0);
			model.verts[1] = toVertex5(octoFace[1].copy(), 0);
			model.verts[2] = toVertex5(octoFace[2].copy(), 0);
			model.verts[3] = toVertex5(octoFace[3].copy(), 0);

			model.verts[4] = toVertex5(octoFace[4].copy(), 0);
			model.verts[5] = toVertex5(octoFace[5].copy(), 0);
			model.verts[6] = toVertex5(octoFace[6].copy(), 0);
			model.verts[7] = toVertex5(octoFace[7].copy(), 0);

			model.verts[8] = toVertex5(octoFace[0].copy(), 0);
			model.verts[9] = toVertex5(octoFace[3].copy(), 0);
			model.verts[10] = toVertex5(octoFace[4].copy(), 0);
			model.verts[11] = toVertex5(octoFace[7].copy(), 0);

			for (int i = 0; i < 12; i++) {
				model.verts[i].vec.y = -0.5 * (frameOnly ? 0.75 : 0.99);
			}
			CCModel.generateBackface(model, 0, model, 12, 12);
			return model;
		}

		public CCModel generateConnection() {

			CCModel model = CCModel.newModel(7, 64);
			double v = 0.375 * TDProps.largeInnerModelScaling;
			double o = 1.01;

			for (int k = 0; k < 8; k++) {
				model.verts[k * 4] = new Vertex5(octoFace[k].copy().multiply(o, 1, o), 0.5 - innerSize, 0);
				model.verts[k * 4 + 1] = new Vertex5(octoFace[k].copy().multiply(o, 1, o).setSide(0, -v), 0.5 - innerSize, 0.5 - v);
				model.verts[k * 4 + 2] = new Vertex5(octoFace[(k + 1) % 8].copy().multiply(o, 1, o).setSide(0, -v), 0.5 + innerSize, 0.5 - v);
				model.verts[k * 4 + 3] = new Vertex5(octoFace[(k + 1) % 8].copy().multiply(o, 1, o), 0.5 + innerSize, 0);
			}
			CCModel.generateBackface(model, 0, model, 32, 32);
			return model;
		}

		public CCModel[] generateModels() {

			CCModel[] models = new CCModel[64 + 12];
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
				finalizeModel(models[i]);
			}
			models[64] = generateConnection();
			for (int s = 0; s < 6; s++) {
				if (s != 0) {
					models[64 + s] = models[64].sidedCopy(0, s, Vector3.zero);
				}
				finalizeModel(models[64 + s]);
			}
			models[70] = generateSideFace();
			for (int s = 0; s < 6; s++) {
				if (s != 0) {
					models[70 + s] = models[70].sidedCopy(0, s, Vector3.zero);
				}
				finalizeModel(models[70 + s]);
			}
			return models;
		}

		public LinkedList<Vertex5> generateIntersections(int connections) {

			LinkedList<Vertex5> v = new LinkedList<>();

			LinkedList<Vertex5> center = addSideFace(new LinkedList<>(), new Cuboid6(-innerSize, -size, -innerSize, innerSize, size, innerSize), 0);
			LinkedList<Vertex5> arm = new LinkedList<>();

			for (int k = 0; k < 8; k++) {
				if (frameOnly && (k % 2 == 0)) {
					continue;
				}
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
					if ((i ^ 1) == j) {
						continue;
					}
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
								v.add(toVertex5(a3, i));
								v.add(toVertex5(a2, i));
								v.add(toVertex5(a1, i));

							} else if (s == 0) {
								Vector3 a1 = v1.copy().multiply(size).add(v2.copy().multiply(innerSize).add(v3.copy().multiply(innerSize)));
								Vector3 a2 = v1.copy().multiply(innerSize).add(v2.copy().multiply(innerSize).add(v3.copy().multiply(size)));
								Vector3 a3 = v1.copy().multiply(innerSize).add(v2.copy().multiply(size).add(v3.copy().multiply(innerSize)));

								v.add(toVertex5(a1, 0));
								v.add(toVertex5(a3, 0));
								v.add(toVertex5(a2, 0));
								v.add(toVertex5(a1, 0));
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
							} else if (s == 2) {
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

	static int[][] orthogonals = { { 6, 6, 4, 5, 2, 3 }, { 6, 6, 4, 5, 2, 3 }, { 4, 5, 6, 6, 0, 1 }, { 5, 4, 6, 6, 1, 0 }, { 2, 3, 0, 1, 6, 6 }, { 3, 2, 1, 0, 6, 6 }, };

	static int[][] edgePairs = { { 0, 2 }, { 0, 3 }, { 0, 4 }, { 0, 5 }, { 1, 2 }, { 1, 3 }, { 1, 4 }, { 1, 5 }, { 2, 4 }, { 2, 5 }, { 3, 4 }, { 3, 5 }, };

	static int[][] cornerTriplets = { { 0, 2, 4 }, { 0, 2, 5 }, { 0, 3, 4 }, { 0, 3, 5 }, { 1, 2, 4 }, { 1, 2, 5 }, { 1, 3, 4 }, { 1, 3, 5 }, };

	static int[][] orthogAxes = { { 2, 4 }, { 2, 4 }, { 0, 4 }, { 0, 4 }, { 0, 2 }, { 0, 2 }, };

	public static class SideTubeGen {

		public double s;
		public double s2;
		public double h = 1;

		public static CCModel[] standardTubes = new SideTubeGen(0.1875 + 1e-4).generateModels();
		public static CCModel[] standardTubesInner = new SideTubeGen(0.1875 + 1e-4).contract(0.999).generateModels();

		public SideTubeGen(double s) {

			this(s, s + 0.09375);
		}

		public SideTubeGen(double s, double s2) {

			this.s = s;
			this.s2 = s2;
		}

		public SideTubeGen contract(double h) {

			this.h = h;
			return this;
		}

		public Cuboid6 newCube(Vector3 min, Vector3 max) {

			double temp;
			if (min.x > max.x) {
				temp = min.x;
				min.x = max.x;
				max.x = temp;
			}
			if (min.y > max.y) {
				temp = min.y;
				min.y = max.y;
				max.y = temp;
			}
			if (min.z > max.z) {
				temp = min.z;
				min.z = max.z;
				max.z = temp;
			}
			if (h < 1) {
				Vector3 mid = min.copy().add(max).multiply(0.5);

				min.x = min.x <= -0.5 || min.x >= 0.5 ? min.x : (min.x - mid.x) * h + mid.x;
				min.y = min.y <= -0.5 || min.y >= 0.5 ? min.y : (min.y - mid.y) * h + mid.y;
				min.z = min.z <= -0.5 || min.z >= 0.5 ? min.z : (min.z - mid.z) * h + mid.z;
				max.x = max.x <= -0.5 || max.x >= 0.5 ? max.x : (max.x - mid.x) * h + mid.x;
				max.y = max.y <= -0.5 || max.y >= 0.5 ? max.y : (max.y - mid.y) * h + mid.y;
				max.z = max.z <= -0.5 || max.z >= 0.5 ? max.z : (max.z - mid.z) * h + mid.z;
			}
			return new Cuboid6(min, max);
		}

		private LinkedList<Vertex5> generateConnections(int i) {

			LinkedList<Vertex5> vecs = new LinkedList<>();
			Cuboid6 cube;
			Vector3 a = axes[i];
			Vector3 b = axes[orthogAxes[i][0]];
			Vector3 c = axes[orthogAxes[i][1]];

			for (int x = -1; x <= 1; x += 2) {
				for (int y = -1; y <= 1; y += 2) {
					cube = newCube(a.copy().multiply(s2).add(b.copy().multiply(s * x)).add(c.copy().multiply(s * y)), a.copy().multiply(h / 2).add(b.copy().multiply(s2 * x)).add(c.copy().multiply(s2 * y)));
					addSideFaces(vecs, cube, (1 << i) ^ (63));
				}
			}
			for (int j = 0; j < 6; j++) {
				if (i != j && (i ^ 1) != j) {
					a = axes[i];
					b = axes[j];
					int orthog = orthogonals[i][j];
					c = axes[orthog];

					cube = newCube(a.copy().multiply(h / 2 - (s2 - s)).add(b.copy().multiply(s)).add(c.copy().multiply(s)), a.copy().multiply(h / 2).add(b.copy().multiply(s2)).add(c.copy().multiply(-s)));

					addSideFaces(vecs, cube, (1 << orthog) | (1 << (orthog ^ 1)));
				}
			}
			return vecs;
		}

		private LinkedList<Vertex5> generateIntersections(int connections) {

			LinkedList<Vertex5> vecs = new LinkedList<>();
			Vector3 a, b, c;
			Cuboid6 cube;

			boolean cullSides = false;

			for (int i = 0; i < 6; i++) {
				if ((connections & (1 << i)) != 0) {
					a = axes[i];
					b = axes[orthogAxes[i][0]];
					c = axes[orthogAxes[i][1]];

					for (int x = -1; x <= 1; x += 2) {
						for (int y = -1; y <= 1; y += 2) {
							cube = newCube(a.copy().multiply(s2).add(b.copy().multiply(s * x)).add(c.copy().multiply(s * y)), a.copy().multiply(h / 2).add(b.copy().multiply(s2 * x)).add(c.copy().multiply(s2 * y)));
							addSideFaces(vecs, cube, (1 << i) | (1 << (i ^ 1)));
						}
					}
				}
			}

			for (int[] pair : edgePairs) {
				if (cullSides || ((connections & (1 << pair[0])) != 0) == ((connections & (1 << pair[1])) != 0)) {
					a = axes[pair[0]];
					b = axes[pair[1]];
					int orthog = orthogonals[pair[0]][pair[1]];
					c = axes[orthog];
					cube = newCube(a.copy().multiply(s).add(b.copy().multiply(s)).add(c.copy().multiply(s)), a.copy().multiply(s2).add(b.copy().multiply(s2)).add(c.copy().multiply(-s)));

					addSideFaces(vecs, cube, (1 << orthog) | (1 << (orthog ^ 1)));
				}
			}

			for (int[] cr : cornerTriplets) {
				a = axes[cr[0]];
				b = axes[cr[1]];
				c = axes[cr[2]];
				cube = newCube(a.copy().multiply(s).add(b.copy().multiply(s)).add(c.copy().multiply(s)), a.copy().multiply(s2).add(b.copy().multiply(s2)).add(c.copy().multiply(s2)));

				int m = ((1 << cr[0]) & connections) | ((1 << cr[1]) & connections) | ((1 << cr[2]) & connections);

				if (cullSides || ((connections & (1 << cr[1])) != 0) == ((connections & (1 << cr[2])) != 0)) {
					m = m | (1 << (cr[0] ^ 1));
				}
				if (cullSides || ((connections & (1 << cr[0])) != 0) == ((connections & (1 << cr[2])) != 0)) {
					m = m | (1 << (cr[1] ^ 1));
				}
				if (cullSides || ((connections & (1 << cr[0])) != 0) == ((connections & (1 << cr[1])) != 0)) {
					m = m | (1 << (cr[2] ^ 1));
				}

				addSideFaces(vecs, cube, m);
			}
			return vecs;
		}

		public CCModel[] generateModels() {

			CCModel[] models = new CCModel[70];
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

				finalizeModel(models[i]);
			}

			for (int i = 0; i < 6; i++) {
				LinkedList<Vertex5> v = generateConnections(i);
				v = simplifyModel(v);
				int n = v.size();
				models[64 + i] = CCModel.newModel(7, n);

				for (int j = 0; j < n; j++) {
					Vertex5 nv = v.get(j);
					models[64 + i].verts[j] = nv;
				}

				finalizeModel(models[64 + i]);
			}
			return models;
		}
	}

}
