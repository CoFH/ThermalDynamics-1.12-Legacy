package cofh.thermaldynamics.duct.attachments.cover;

import codechicken.lib.model.bakery.CCQuad;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.Vertex5;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.init.TDProps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoverHoleRender {

	public static final ITransformer[] hollowDuct = CoverHoleRender.hollowCover(0.3125f);
	public static final ITransformer[] hollowDuctLarge = CoverHoleRender.hollowCover(0.28125f);
	public static final ITransformer[] hollowDuctTile = CoverHoleRender.hollowCover(0.25f);

	public static final ITransformer[] hollowDuctCryo = CoverHoleRender.octaCover(0.5F - 0.375F, 0.1812F);
	public static final ITransformer[] hollowDuctTransport = CoverHoleRender.octaCover(0.5F * (1 - TDProps.largeInnerModelScaling), 0.1812F);

	public static class Quad {

		Vertex8[] verts;
		CCQuad originalQuad;

		public Quad(Vertex8[] verts, CCQuad quad) {

			originalQuad = quad;
			this.verts = verts;
		}

		public List<CCQuad> sliceStretchDraw(int side, ITransformer[] transformers) {

			float[][] uvTransform = getUVTransform(verts, side);

			List<CCQuad> quads = new ArrayList<CCQuad>();
			if (uvTransform == null) {
				quads.add(draw());
				return quads;
			}

			for (ITransformer transformer : transformers) {
				Quad slice = slice(side, transformer, uvTransform);
				if (slice.notEmpty()) {
					quads.add(slice.draw());
				}
			}
			return quads;
		}

		private boolean notEmpty() {

			Vertex8 a = verts[0];
			byte f = 0;
			boolean flagX = true, flagY = true, flagZ = true;
			for (int i = 1; i < 4; i++) {
				Vertex8 b = verts[i];
				if (flagX && Math.abs(a.x - b.x) > 1e-4F) {
					flagX = false;
					f++;
				}
				if (flagY && Math.abs(a.y - b.y) > 1e-4F) {
					flagY = false;
					f++;
				}
				if (flagZ && Math.abs(a.z - b.z) > 1e-4F) {
					flagZ = false;
					f++;
				}
				if (f > 1) {
					return true;
				}
			}

			return false;
		}

		public Quad slice(int side, ITransformer transformer, float[][] uvTransform) {

			Vertex8[] v = new Vertex8[this.verts.length];
			int s = side >> 1;

			for (int i = 0; i < verts.length; i++) {
				Vertex8 copy = verts[i].copy();

				float dx;
				float dy;
				if (s == 0) {
					dx = copy.x;
					dy = copy.z;
				} else if (s == 1) {
					dx = copy.x;
					dy = copy.y;
				} else {
					dx = copy.z;
					dy = copy.y;
				}

				if (transformer.shouldTransform(dx, dy)) {
					float dx2 = transformer.transformX(dx, dy);
					float dy2 = transformer.transformY(dx, dy);
					if (s == 0) {
						copy.x = dx2;
						copy.z = dy2;
					} else if (s == 1) {
						copy.x = dx2;
						copy.y = dy2;
					} else {
						copy.z = dx2;
						copy.y = dy2;
					}

					if (uvTransform != null) {
						float[] newTex = new float[Vertex8.TEX_NUM];
						for (int j = 0; j < Vertex8.TEX_NUM; j++) {
							newTex[j] = uvTransform[0][j] + uvTransform[1][j] * dx2 + uvTransform[2][j] * dy2;
						}

						copy.reloadTex(newTex);
					}
				}

				v[i] = copy;
			}

			return new Quad(v, originalQuad.copy());
		}

		public CCQuad draw() {

			CCQuad quad = originalQuad.copy();
			for (int i = 0; i < verts.length; i++) {
				Vertex8 vertex = verts[i];
				vertex.draw(quad, i);
			}
			return quad;
		}

		public float[][] getUVTransform(Vertex8[] quads, int side) {

			int s = side >> 1;
			float n = 0;
			float sx = 0, sy = 0, sxy = 0, sxx = 0, syy = 0;

			float XY[][] = new float[3][Vertex8.TEX_NUM];

			for (Vertex8 vertex : quads) {
				n++;
				float dx;
				float dy;
				if (s == 0) {
					dx = vertex.x;
					dy = vertex.z;
				} else if (s == 1) {
					dx = vertex.x;
					dy = vertex.y;
				} else {
					dx = vertex.z;
					dy = vertex.y;
				}

				sx += dx;
				sy += dy;
				sxy += dx * dy;
				syy += dy * dy;
				sxx += dx * dx;

				float[] tex = vertex.buildTex();

				for (int j = 0; j < tex.length; j++) {
					XY[0][j] += tex[j];
					XY[1][j] += tex[j] * dx;
					XY[2][j] += tex[j] * dy;
				}
			}

			float v = sxx * syy - sxy * sxy;
			float determinant = (n * v) - (sxx * sy * sy + syy * sx * sx) + (2 * (sxy * sx * sy));

			if (Math.abs(determinant) <= 1e-4F) {
				return null;
			} else {
				determinant = 1 / determinant;
			}

			float cy_xy = (sxy * sy - syy * sx) * determinant;
			float cx_xy = (sxy * sx - sxx * sy) * determinant;
			float cx_y = (sx * sy - sxy * n) * determinant;
			float XXI[][] = { { v * determinant, cy_xy, cx_xy }, { cy_xy, (syy * n - sy * sy) * determinant, cx_y }, { cx_xy, cx_y, (sxx * n - sx * sx) * determinant } };

			float[][] beta = new float[3][Vertex8.TEX_NUM];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < Vertex8.TEX_NUM; j++) {
					for (int k = 0; k < 3; k++) {
						beta[i][j] += XXI[i][k] * XY[k][j];
					}
				}
			}

			return beta;
		}

		@Override
		public String toString() {

			return String.format("Quad{%s}", Arrays.toString(verts));
		}
	}

	public static List<CCQuad> holify(List<CCQuad> quads, int side, ITransformer[] transformers) {

		List<Quad> tessQuads = loadFromQuads(quads);
		List<CCQuad> transformedQuads = new ArrayList<CCQuad>();

		for (Quad tessQuad : tessQuads) {
			transformedQuads.addAll(tessQuad.sliceStretchDraw(side, transformers));
		}

		return transformedQuads;
	}

	public static List<Quad> loadFromQuads(List<CCQuad> ccQuads) {

		List<Quad> quads = new ArrayList<Quad>();

		for (CCQuad quad : ccQuads) {

			Vertex8[] verts = new Vertex8[4];

			for (int v = 0; v < 4; v++) {
				Vertex5 vert = quad.vertices[v];
				//verts[v].x = (float) vert.vec.x;
				//verts[v].y = (float) vert.vec.y;
				//verts[v].z = (float) vert.vec.z;
				//verts[v].u = (float) vert.uv.u;
				//verts[v].v = (float) vert.uv.v;
				verts[v] = new Vertex8((float) vert.vec.x, (float) vert.vec.y, (float) vert.vec.z, (float) vert.uv.u, (float) vert.uv.v, quad.colours[v].rgba(), quad.normals[v].copy(), quad.lightMaps[v]);
			}
			quads.add(new Quad(verts, quad.copy()));

		}
		return quads;
	}

	/*public static List<Quad> loadFromTessellator(int startIndex, boolean pop) {

		Tessellator tess = Tessellator.instance;
		int endIndex = tess.rawBufferIndex;

		int[] rb = tess.rawBuffer;

		float dx = (float) tess.xOffset;
		float dy = (float) tess.yOffset;
		float dz = (float) tess.zOffset;

		if (startIndex == endIndex) {
			return Collections.emptyList();
		}

		List<Quad> list = new ArrayList<Quad>((endIndex - startIndex) >> 2);

		for (int k = startIndex; k < endIndex; k += 32) {
			Vertex8[] verts = new Vertex8[4];
			for (int k2 = 0; k2 < 4; k2++) {
				int i = k + k2 * 8;

				verts[k2] = new Vertex8(Float.intBitsToFloat(rb[i]) - dx, Float.intBitsToFloat(rb[i + 1]) - dy, Float.intBitsToFloat(rb[i + 2]) - dz,
						Float.intBitsToFloat(rb[i + 3]), Float.intBitsToFloat(rb[i + 4]), rb[i + 5], rb[i + 6], rb[i + 7]);
			}

			list.add(new Quad(verts));
		}

		if (pop) {
			tess.rawBufferIndex = startIndex;
			tess.vertexCount = startIndex >> 3;
		}

		return list;
	}*/

	public static ITransformer[] hollowCover(float w) {

		return new ITransformer[] { new TransformSquare(0, w, 0, 1), new TransformSquare(1 - w, 1, 0, 1), new TransformSquare(w, 1 - w, 0, w), new TransformSquare(w, 1 - w, 1 - w, 1) };
	}

	public static ITransformer[] octaCover(float w, float k) {

		return new ITransformer[] { new TransformSquare(0, w, 0, 1), new TransformSquare(1 - w, 1, 0, 1), new TransformSquare(w, 1 - w, 0, w), new TransformSquare(w, 1 - w, 1 - w, 1), new TriTransformer(w, w + 0.5F - k, false, false), new TriTransformer(w, w + 0.5F - k, false, true), new TriTransformer(w, w + 0.5F - k, true, false), new TriTransformer(w, w + 0.5F - k, true, true), };
	}

	public static ITransformer[] octaCover(float k) {

		return new ITransformer[] { new TriTransformer(0F, 0.5F - k, false, false), new TriTransformer(0F, 0.5F - k, false, true), new TriTransformer(0F, 0.5F - k, true, false), new TriTransformer(0F, 0.5F - k, true, true), };
	}

	public static interface ITransformer {

		boolean shouldTransform(float dx, float dy);

		float transformX(float dx, float dy);

		float transformY(float dx, float dy);
	}

	public static class TransformSquare implements ITransformer {

		float x0, x1, y0, y1;

		public TransformSquare(float x0, float x1, float y0, float y1) {

			this.x0 = x0;
			this.x1 = x1;
			this.y0 = y0;
			this.y1 = y1;
		}

		@Override
		public boolean shouldTransform(float dx, float dy) {

			return dx < x0 || dx > x1 || dy < y0 || dy > y1;
		}

		@Override
		public float transformX(float dx, float dy) {

			return MathHelper.clamp(dx, x0, x1);
		}

		@Override
		public float transformY(float dx, float dy) {

			return MathHelper.clamp(dy, y0, y1);
		}

		@Override
		public String toString() {

			return "TransformSquare{" + "x0=" + x0 + ", x1=" + x1 + ", y0=" + y0 + ", y1=" + y1 + '}';
		}
	}

	public static class TriTransformer implements ITransformer {

		@Override
		public String toString() {

			return "TriTransformer{" + "m=" + m + ", k=" + k + ", flipX=" + flipX + ", flipY=" + flipY + '}';
		}

		float m, k;
		boolean flipX, flipY;

		public TriTransformer(float m, float k, boolean flipX, boolean flipY) {

			this.m = m;
			this.k = k;
			this.flipX = flipX;
			this.flipY = flipY;
		}

		@Override
		public boolean shouldTransform(float dx, float dy) {

			if (flipX) {
				dx = 1 - dx;
			}
			if (flipY) {
				dy = 1 - dy;
			}

			return dx < m || dy < m || (dx + dy) > k;
		}

		@Override
		public float transformX(float dx, float dy) {

			if (flipX) {
				dx = 1 - dx;
			}

			if (dx < m) {
				return flipX ? 1 - m : m;
			}

			if (flipY) {
				dy = 1 - dy;
			}
			if (dy < m) {
				float d = MathHelper.clamp(dx, m, k - m);
				return flipX ? 1 - d : d;
			} else {
				float d = k * dx / (dx + dy);
				return flipX ? 1 - d : d;
			}
		}

		@Override
		public float transformY(float dx, float dy) {

			if (flipY) {
				dy = 1 - dy;
			}

			if (dy < m) {
				return flipY ? 1 - m : m;
			}

			if (flipX) {
				dx = 1 - dx;
			}
			if (dx < m) {
				float d = MathHelper.clamp(dy, m, k - m);
				return flipY ? 1 - d : d;
			} else {
				float d = k * dy / (dy + dx);
				return flipY ? 1 - d : d;
			}
		}
	}

	@Deprecated
	public static class Vertex8 {

		public final static int TEX_NUM = 8;
		float x, y, z;
		float u, v;
		int color;
		Vector3 normal;
		int brightness;

		@Override
		public String toString() {

			return String.format("V8{{%s,%s,%s},{%s,%s},c=%d,n=%s,b=%d}", x, y, z, u, v, color, normal.toString(), brightness);
		}

		public Vertex8(float x, float y, float z, float u, float v, int color, Vector3 normal, int brightness) {

			this.x = x;
			this.y = y;
			this.z = z;
			this.u = u;
			this.v = v;
			this.color = color;
			this.normal = normal;
			this.brightness = brightness;
		}

		public Vertex8 copy() {

			return new Vertex8(x, y, z, u, v, color, normal, brightness);
		}

		public void draw(CCQuad quad, int i) {

			quad.vertices[i].vec.set(x, y, z);
			quad.vertices[i].uv.set(u, v);
			quad.colours[i].set(color);
			quad.normals[i].set(normal);
			quad.lightMaps[i] = brightness;
		}

		public float[] buildTex() {

			return new float[] { u, v, (color >> 24) & 0xFF, (color >> 16) & 0xFF, (color >> 8) & 0xFF, (color) & 0xFF, brightness & 0xFFFF, (brightness >>> 16) & 0xFFFF };
		}

		public void reloadTex(float[] tex) {

			u = tex[0];
			v = tex[1];
			color = ((int) MathHelper.clamp(tex[2], 0, 255) << 24) | ((int) MathHelper.clamp(tex[3], 0, 255) << 16) | ((int) MathHelper.clamp(tex[4], 0, 255) << 8) | ((int) MathHelper.clamp(tex[5], 0, 255));

			brightness = ((int) MathHelper.clamp(tex[6], 0, 65535)) | ((int) MathHelper.clamp(tex[7], 0, 65535) << 16);
		}
	}

}

