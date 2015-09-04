package cofh.thermaldynamics.duct.attachments.cover;

import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.core.TDProps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.Tessellator;

public class CoverHoleRender {

	public static final ITransformer[] hollowDuct = CoverHoleRender.hollowCover(0.3125f);
	public static final ITransformer[] hollowDuctLarge = CoverHoleRender.hollowCover(0.28125f);
	public static final ITransformer[] hollowDuctTile = CoverHoleRender.hollowCover(0.25f);

	public static final ITransformer[] hollowDuctCryo = CoverHoleRender.octaCover(0.5F - 0.375F, 0.1812F);
	public static final ITransformer[] hollowDuctTransport = CoverHoleRender.octaCover(0.5F * (1 - TDProps.largeInnerModelScaling), 0.1812F);


	public static class Quad {
		Vertex8[] verts;
		public Quad(Vertex8[] verts) {
			this.verts = verts;
		}


		public void sliceStretchDraw(int x, int y, int z, int side, ITransformer[] transformers) {
			float[][] uvTransform = getUVTransform(verts, side, x, y, z);

			if (uvTransform == null) {
				draw();
				return;
			}

			for (ITransformer transformer : transformers) {
				Quad slice = slice(x, y, z, side, transformer, uvTransform);
				if (slice.notEmpty())
					slice.draw();
			}
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
				if (f > 1) return true;
			}

			return false;
		}

		public Quad slice(int x, int y, int z, int side, ITransformer transformer, float[][] uvTransform) {
			Vertex8[] v = new Vertex8[this.verts.length];
			int s = side >> 1;

			for (int i = 0; i < verts.length; i++) {
				Vertex8 copy = verts[i].copy();

				float dx;
				float dy;
				if (s == 0) {
					dx = copy.x - x;
					dy = copy.z - z;
				} else if (s == 1) {
					dx = copy.x - x;
					dy = copy.y - y;
				} else {
					dx = copy.z - z;
					dy = copy.y - y;
				}

				if (transformer.shouldTransform(dx, dy)) {
					float dx2 = transformer.transformX(dx, dy);
					float dy2 = transformer.transformY(dx, dy);
					if (s == 0) {
						copy.x = x + dx2;
						copy.z = z + dy2;
					} else if (s == 1) {
						copy.x = x + dx2;
						copy.y = y + dy2;
					} else {
						copy.z = z + dx2;
						copy.y = y + dy2;
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

			return new Quad(v);
		}

		public void draw() {
			for (Vertex8 vertex : verts) {
				vertex.draw();
			}
		}

		public float[][] getUVTransform(Vertex8[] quads, int side, int x, int y, int z) {
			int s = side >> 1;
			float n = 0;
			float sx = 0, sy = 0, sxy = 0, sxx = 0, syy = 0;

			float XY[][] = new float[3][Vertex8.TEX_NUM];

			for (Vertex8 vertex : quads) {
				n++;
				float dx;
				float dy;
				if (s == 0) {
					dx = vertex.x - x;
					dy = vertex.z - z;
				} else if (s == 1) {
					dx = vertex.x - x;
					dy = vertex.y - y;
				} else {
					dx = vertex.z - z;
					dy = vertex.y - y;
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
			} else
				determinant = 1 / determinant;

			float cy_xy = (sxy * sy - syy * sx) * determinant;
			float cx_xy = (sxy * sx - sxx * sy) * determinant;
			float cx_y = (sx * sy - sxy * n) * determinant;
			float XXI[][] = {
					{v * determinant, cy_xy, cx_xy},
					{cy_xy, (syy * n - sy * sy) * determinant, cx_y},
					{cx_xy, cx_y, (sxx * n - sx * sx) * determinant}
			};

			float[][] beta = new float[3][Vertex8.TEX_NUM];
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < Vertex8.TEX_NUM; j++) {
					for (int k = 0; k < 3; k++)
						beta[i][j] += XXI[i][k] * XY[k][j];
				}

			return beta;
		}
	}

	public static void holify(int startIndex, int x, int y, int z, int side, ITransformer[] transformers) {
		List<Quad> tessQuads = loadFromTessellator(startIndex, true);
		for (Quad tessQuad : tessQuads) {
			tessQuad.sliceStretchDraw(x, y, z, side, transformers);
		}
	}

	public static List<Quad> loadFromTessellator(int startIndex, boolean pop) {
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

				verts[k2] = new Vertex8(
						Float.intBitsToFloat(rb[i]) - dx,
						Float.intBitsToFloat(rb[i + 1]) - dy,
						Float.intBitsToFloat(rb[i + 2]) - dz,
						Float.intBitsToFloat(rb[i + 3]),
						Float.intBitsToFloat(rb[i + 4]),
						rb[i + 5],
						rb[i + 6],
						rb[i + 7]);
			}

			list.add(new Quad(verts));
		}

		if (pop) {
			tess.rawBufferIndex = startIndex;
			tess.vertexCount = startIndex >> 3;
		}

		return list;
	}


	public static ITransformer[] hollowCover(float w) {
		return new ITransformer[]{
				new TransformSquare(0, w, 0, 1),
				new TransformSquare(1 - w, 1, 0, 1),
				new TransformSquare(w, 1 - w, 0, w),
				new TransformSquare(w, 1 - w, 1 - w, 1)
		};
	}

	public static ITransformer[] octaCover(float w, float k) {
		return new ITransformer[]{
				new TransformSquare(0, w, 0, 1),
				new TransformSquare(1 - w, 1, 0, 1),
				new TransformSquare(w, 1 - w, 0, w),
				new TransformSquare(w, 1 - w, 1 - w, 1),
				new TriTransformer(w, w + 0.5F - k, false, false),
				new TriTransformer(w, w + 0.5F - k, false, true),
				new TriTransformer(w, w + 0.5F - k, true, false),
				new TriTransformer(w, w + 0.5F - k, true, true),
		};
	}

	public static ITransformer[] octaCover(float k) {
		return new ITransformer[]{
				new TriTransformer(0F, 0.5F - k, false, false),
				new TriTransformer(0F, 0.5F - k, false, true),
				new TriTransformer(0F, 0.5F - k, true, false),
				new TriTransformer(0F, 0.5F - k, true, true),
		};
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
			return MathHelper.clampF(dx, x0, x1);
		}

		@Override
		public float transformY(float dx, float dy) {
			return MathHelper.clampF(dy, y0, y1);
		}
	}

	public static class TriTransformer implements ITransformer {

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
			if (flipX) dx = 1 - dx;
			if (flipY) dy = 1 - dy;

			return dx < m || dy < m || (dx + dy) > k;
		}

		@Override
		public float transformX(float dx, float dy) {
			if (flipX) dx = 1 - dx;

			if (dx < m)
				return flipX ? 1 - m : m;

			if (flipY) dy = 1 - dy;
			if (dy < m) {
				float d = MathHelper.clampF(dx, m, k - m);
				return flipX ? 1 - d : d;
			} else {
				float d = k * dx / (dx + dy);
				return flipX ? 1 - d : d;
			}
		}

		@Override
		public float transformY(float dx, float dy) {
			if (flipY) dy = 1 - dy;

			if (dy < m) {
				return flipY ? 1 - m : m;
			}

			if (flipX) dx = 1 - dx;
			if (dx < m) {
				float d = MathHelper.clampF(dy, m, k - m);
				return flipY ? 1 - d : d;
			} else {
				float d = k * dy / (dy + dx);
				return flipY ? 1 - d : d;
			}
		}
	}

	public static class Vertex8 {
		public final static int TEX_NUM = 8;
		float x, y, z;
		float u, v;
		int color;
		int normal;
		int brightness;

		public Vertex8(float x, float y, float z, float u, float v, int color, int normal, int brightness) {
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

		public void draw() {
			Tessellator tess = Tessellator.instance;
			int index = tess.rawBufferIndex;
			tess.addVertex(this.x, this.y, this.z); // to grow the rawBuffer if needed
			int[] buffer = tess.rawBuffer;
			buffer[index + 3] = Float.floatToRawIntBits(this.u);
			buffer[index + 4] = Float.floatToRawIntBits(this.v);
			buffer[index + 5] = this.color;
			buffer[index + 6] = this.normal;
			buffer[index + 7] = this.brightness;
		}

		public float[] buildTex() {
			return new float[]{
					u, v,
					(color >> 24) & 0xFF,
					(color >> 16) & 0xFF,
					(color >> 8) & 0xFF,
					(color) & 0xFF,
					brightness & 0xFFFF, (brightness >>> 16) & 0xFFFF
			};
		}

		public void reloadTex(float[] tex) {
			u = tex[0];
			v = tex[1];
			color = ((int) MathHelper.clampF(tex[2], 0, 255) << 24) |
					((int) MathHelper.clampF(tex[3], 0, 255) << 16) |
					((int) MathHelper.clampF(tex[4], 0, 255) << 8) |
					((int) MathHelper.clampF(tex[5], 0, 255));

			brightness = ((int) MathHelper.clampF(tex[6], 0, 65535)) | ((int) MathHelper.clampF(tex[7], 0, 65535) << 16);
		}
	}

}
