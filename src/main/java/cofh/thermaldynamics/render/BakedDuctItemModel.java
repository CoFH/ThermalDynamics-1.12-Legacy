package cofh.thermaldynamics.render;

import codechicken.lib.model.PerspectiveAwareModelProperties;
import codechicken.lib.model.bakery.ModelBakery;
import codechicken.lib.model.bakery.key.IItemStackKeyGenerator;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by covers1624 on 12/06/2017.
 * TODO Rewrite all of this in 1.11 with CCL's cleaner bakery pipe. Where none of this is needed.
 */
public class BakedDuctItemModel implements IBakedModel {

	public static BakedDuctItemModel INSTANCE = new BakedDuctItemModel();

	private static Cache<String, IBakedModel> modelCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		return new ArrayList<>();
	}

	@Override
	public boolean isAmbientOcclusion() {

		return true;
	}

	@Override
	public boolean isGui3d() {

		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {

		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return TextureUtils.getMissingSprite();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {

		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {

		return DuctOverrideList.INSTANCE;
	}

	private static class DuctOverrideList extends ItemOverrideList {

		private static DuctOverrideList INSTANCE = new DuctOverrideList();

		public DuctOverrideList() {

			super(ImmutableList.of());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {

			IItemStackKeyGenerator keyGen = ModelBakery.getKeyGenerator(stack.getItem());
			String key = keyGen.generateKey(stack);
			IBakedModel wrappedModel = modelCache.getIfPresent(key);
			if (wrappedModel == null) {
				IBakedModel bakeryModel = ModelBakery.generateItemModel(stack);
				PerspectiveAwareModelProperties properties = new PerspectiveAwareModelProperties(TransformUtils.DEFAULT_BLOCK, true, false);
				wrappedModel = new WrappedBakedModel(bakeryModel, properties);
				modelCache.put(key, wrappedModel);
			}
			return wrappedModel;
		}
	}

	private static class WrappedBakedModel implements IPerspectiveAwareModel {

		private IBakedModel wrappedModel;
		public PerspectiveAwareModelProperties perspectiveProperties;

		public WrappedBakedModel(IBakedModel model, PerspectiveAwareModelProperties perspectiveProperties) {

			this.wrappedModel = model;
			this.perspectiveProperties = perspectiveProperties;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

			return wrappedModel.getQuads(state, side, rand);
		}

		@Override
		public boolean isAmbientOcclusion() {

			return perspectiveProperties.getProperties().isAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {

			return perspectiveProperties.getProperties().isGui3d();
		}

		@Override
		public boolean isBuiltInRenderer() {

			return perspectiveProperties.getProperties().isBuiltInRenderer();
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {

			return TextureUtils.getMissingSprite();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {

			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {

			return ItemOverrideList.NONE;
		}

		@Override
		public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {

			return MapWrapper.handlePerspective(this, perspectiveProperties.getModelState(), cameraTransformType);
		}
	}

}
