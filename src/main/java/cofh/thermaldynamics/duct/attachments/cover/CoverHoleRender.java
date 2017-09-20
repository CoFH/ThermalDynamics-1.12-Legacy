package cofh.thermaldynamics.duct.attachments.cover;

import codechicken.lib.colour.Colour;
import codechicken.lib.render.CCQuad;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.Vertex5;
import cofh.core.util.helpers.MathHelper;
import cofh.thermaldynamics.init.TDProps;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//TODO Minor rework of this system. Move away from Vertex8. Not a huge issue, just some minor performance improvements.
public class CoverHoleRender {

	public static final CoverTransformer hollowDuct = CoverHoleRender.hollowCover(0.3125f);
	public static final CoverTransformer hollowDuctLarge = CoverHoleRender.hollowCover(0.28125f);
	public static final CoverTransformer hollowDuctTile = CoverHoleRender.hollowCover(0.25f);

	public static final CoverTransformer hollowDuctCryo = CoverHoleRender.octaCover(0.5F - 0.375F, 0.1812F);
	public static final CoverTransformer hollowDuctTransport = CoverHoleRender.octaCover(0.5F * (1 - TDProps.largeInnerModelScaling), 0.1812F);

	public static List<CCQuad> holify(List<CCQuad> quads, int side, CoverTransformer transformer) {

		return quads.stream().map(Quad::new).flatMap(quad -> quad.sliceStretchBake(side, transformer).stream()).collect(Collectors.toList());
	}

	public static class Quad {

		protected CCQuad quad;

		public Quad(CCQuad quad) {

			this.quad = quad;
		}

		public List<CCQuad> sliceStretchBake(int side, CoverTransformer transformer) {

			float[][] uvTransform = getUVTransform(quad, side);

			List<CCQuad> quads = new ArrayList<>();
			if (uvTransform == null) {
				quads.add(bake());
				return quads;
			}

			for (ITransformer trans : transformer.getTransformers()) {
				Quad slice = slice(side, trans, uvTransform);
				if (slice.notEmpty()) {
					quads.add(slice.bake());
				}
			}
			return quads;
		}

		private boolean notEmpty() {

			Vector3 a = quad.vertices[0].vec;
			byte f = 0;
			boolean flagX = true, flagY = true, flagZ = true;
			for (int i = 1; i < 4; i++) {
				Vector3 b = quad.vertices[i].vec;
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

			CCQuad v = quad.copy();
			int s = side >> 1;

			for (int i = 0; i < 4; i++) {
				Vector3 vec = v.vertices[i].vec;

				float dx;
				float dy;
				if (s == 0) {
					dx = (float) vec.x;
					dy = (float) vec.z;
				} else if (s == 1) {
					dx = (float) vec.x;
					dy = (float) vec.y;
				} else {
					dx = (float) vec.z;
					dy = (float) vec.y;
				}

				if (transformer.shouldTransform(dx, dy)) {
					float dx2 = transformer.transformX(dx, dy);
					float dy2 = transformer.transformY(dx, dy);
					if (s == 0) {
						vec.x = dx2;
						vec.z = dy2;
					} else if (s == 1) {
						vec.x = dx2;
						vec.y = dy2;
					} else {
						vec.z = dx2;
						vec.y = dy2;
					}

					if (uvTransform != null) {
						float[] newTex = new float[8];
						for (int j = 0; j < 8; j++) {
							newTex[j] = uvTransform[0][j] + uvTransform[1][j] * dx2 + uvTransform[2][j] * dy2;
						}
						v.vertices[i].uv.u = newTex[0];
						v.vertices[i].uv.v = newTex[1];
						v.colours[i].set((int) MathHelper.clamp(newTex[2], 0, 255), (int) MathHelper.clamp(newTex[3], 0, 255), (int) MathHelper.clamp(newTex[4], 0, 255), (int) MathHelper.clamp(newTex[5], 0, 255));
						v.lightMaps[i] = (int) MathHelper.clamp(newTex[6], 0, 65535) | ((int) MathHelper.clamp(newTex[7], 0, 65535) << 16);
					}
				}
			}

			return new Quad(v);
		}

		public CCQuad bake() {
			return quad;
		}

		public float[][] getUVTransform(CCQuad quad, int side) {

			int s = side >> 1;
			float n = 0;
			float sx = 0, sy = 0, sxy = 0, sxx = 0, syy = 0;

			float XY[][] = new float[3][8];

			for (int v = 0; v < 4; v++) {
				Vertex5 vert = quad.vertices[v];
				Colour colour = quad.colours[v];
				n++;
				float dx;
				float dy;
				if (s == 0) {
					dx = (float) vert.vec.x;
					dy = (float) vert.vec.z;
				} else if (s == 1) {
					dx = (float) vert.vec.x;
					dy = (float) vert.vec.y;
				} else {
					dx = (float) vert.vec.z;
					dy = (float) vert.vec.y;
				}

				sx += dx;
				sy += dy;
				sxy += dx * dy;
				syy += dy * dy;
				sxx += dx * dx;

				//@formatter:off
				float[] tex = {
				        (float) vert.uv.u, (float) vert.uv.v,
                        colour.r & 0xFF, colour.g & 0xFF, colour.b & 0xFF, colour.a & 0xFF,
                        quad.lightMaps[v] & 0xFFFF, (quad.lightMaps[v] >>> 16) & 0xFFFF
				};
				//@formatter:on

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

			float[][] beta = new float[3][8];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 8; j++) {
					for (int k = 0; k < 3; k++) {
						beta[i][j] += XXI[i][k] * XY[k][j];
					}
				}
			}

			return beta;
		}
	}

	public static CoverTransformer hollowCover(float w) {

		//@formatter:off
		return new CoverTransformer(
				new TransformSquare(0, w, 0, 1),
				new TransformSquare(1 - w, 1, 0, 1),
				new TransformSquare(w, 1 - w, 0, w),
				new TransformSquare(w, 1 - w, 1 - w, 1)
		);
		//@formatter:on
	}

	public static CoverTransformer octaCover(float w, float k) {

		//@formatter:off
		return new CoverTransformer(
				new TransformSquare(0, w, 0, 1),
				new TransformSquare(1 - w, 1, 0, 1),
				new TransformSquare(w, 1 - w, 0, w),
				new TransformSquare(w, 1 - w, 1 - w, 1),
				new TriTransformer(w, w + 0.5F - k, false, false),
				new TriTransformer(w, w + 0.5F - k, false, true),
				new TriTransformer(w, w + 0.5F - k, true, false),
				new TriTransformer(w, w + 0.5F - k, true, true)
		);
		//@formatter:on
	}

	public static CoverTransformer octaCover(float k) {

		//@formatter:off
		return new CoverTransformer(
				new TriTransformer(0F, 0.5F - k, false, false),
				new TriTransformer(0F, 0.5F - k, false, true),
				new TriTransformer(0F, 0.5F - k, true, false),
				new TriTransformer(0F, 0.5F - k, true, true)
		);
		//@formatter:on
	}

	public static class CoverTransformer {

		private ITransformer[] transformers;

		public CoverTransformer(ITransformer... transformers) {
			this.transformers = transformers;
		}

		public ITransformer[] getTransformers() {
			return transformers;
		}
	}

	public interface ITransformer {

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
}

