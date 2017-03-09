package cofh.thermaldynamics.render;

import codechicken.lib.lighting.LightModel;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.block.ICCBlockRenderer;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.lib.render.RenderHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermalfoundation.init.TFFluids;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

public class RenderDuct implements ICCBlockRenderer, IItemRenderer, IPerspectiveAwareModel {

	public static final RenderDuct instance = new RenderDuct();

	static final int[] INV_CONNECTIONS = { BlockDuct.ConnectionTypes.DUCT.ordinal(), BlockDuct.ConnectionTypes.DUCT.ordinal(), 0, 0, 0, 0 };
	//	static int[] connections = new int[6];

	static TextureAtlasSprite textureCenterLine;

	static CCModel[][] modelFluid = new CCModel[6][7];
	public static CCModel[][] modelConnection = new CCModel[3][6];
	static CCModel modelCenter;

	static CCModel[] modelLine = new CCModel[6];
	static CCModel modelLineCenter;

	static CCModel[] modelFrameConnection = new CCModel[64];
	static CCModel[] modelFrame = new CCModel[64];

	static CCModel[] modelTransportConnection = new CCModel[64];
	static CCModel[] modelTransport = new CCModel[64];

	static {
		generateModels();
		generateFluidModels();
	}

	public static CCModel[] modelOpaqueTubes;
	public static CCModel[] modelTransTubes;
	private static CCModel[] modelFluidTubes;
	private static CCModel[] modelLargeTubes;

	public static void initialize() {

		textureCenterLine = TextureUtils.getTexture(TFFluids.fluidSteam.getStill());
	}

	private static void generateFluidModels() {

		for (int i = 1; i < 7; i++) {
			double d1 = 0.47 - 0.025 * i;
			double d2 = 0.53 + 0.025 * i;
			double d3 = 0.32 + 0.06 * i;
			double c1 = 0.32;
			double c2 = 0.68;
			double[][] boxes = new double[][] { { d1, 0, d1, d2, c1, d2 }, { d1, d3, d1, d2, 1, d2 }, { c1, c1, 0, c2, d3, c1 }, { c1, c1, c2, c2, d3, 1 }, { 0, c1, c1, c1, d3, c2 }, { c2, c1, c1, 1, d3, c2 }, { c1, c1, c1, c2, d3, c2 } };

			for (int s = 0; s < 7; s++) {
				modelFluid[i - 1][s] = CCModel.quadModel(24).generateBlock(0, boxes[s][0], boxes[s][1], boxes[s][2], boxes[s][3], boxes[s][4], boxes[s][5]).computeNormals();
			}
		}
	}

	private static void generateModels() {

		modelCenter = CCModel.quadModel(48).generateBox(0, -3, -3, -3, 6, 6, 6, 0, 0, 32, 32, 16);

		modelConnection[0][1] = CCModel.quadModel(48).generateBlock(0, (new Cuboid6(0.3125, 0.6875, 0.3125, 0.6875, 1, 0.6875)).expand(-1.0D / 1024.0D));
		modelConnection[1][1] = CCModel.quadModel(24).generateBox(0, -4, 4, -4, 8, 4 - RenderHelper.RENDER_OFFSET, 8, 0, 0, 32, 32, 16).computeNormals();
		modelConnection[2][1] = CCModel.quadModel(24).generateBox(0, -4, 4, -4, 8, 4 - RenderHelper.RENDER_OFFSET, 8, 0, 16, 32, 32, 16).computeNormals();

		double h = 0.4;
		modelLineCenter = CCModel.quadModel(24).generateBlock(0, h, h, h, 1 - h, 1 - h, 1 - h).computeNormals();
		modelLine[1] = CCModel.quadModel(16).generateBlock(0, h, 1 - h, h, 1 - h, 1.0, 1 - h, 3).computeNormals();
		CCModel.generateSidedModels(modelLine, 1, Vector3.center);

		modelOpaqueTubes = ModelHelper.StandardTubes.genModels(0.1875F, true);
		modelTransTubes = ModelHelper.StandardTubes.genModels(0.1875F, false);
		modelFluidTubes = ModelHelper.StandardTubes.genModels(0.1875F * TDProps.smallInnerModelScaling, false, false);
		modelLargeTubes = ModelHelper.StandardTubes.genModels(0.21875f, true);

		modelFrameConnection = (new ModelHelper.OctagonalTubeGen(0.375, 0.1812, true)).generateModels();
		modelFrame = (new ModelHelper.OctagonalTubeGen(0.375 * TDProps.largeInnerModelScaling, 0.1812, false)).generateModels();

		modelTransportConnection = (new ModelHelper.OctagonalTubeGen(0.5 * TDProps.largeInnerModelScaling, 0.1812, true)).generateModels();
		modelTransport = (new ModelHelper.OctagonalTubeGen(0.5 * TDProps.largeInnerModelScaling * TDProps.largeInnerModelScaling, 0.1812, false)).generateModels();

		CCModel.generateBackface(modelCenter, 0, modelCenter, 24, 24);
		CCModel.generateBackface(modelConnection[0][1], 0, modelConnection[0][1], 24, 24);
		modelConnection[0][1].apply(new Translation(-0.5, -0.5, -0.5));

		for (CCModel[] aModelConnection1 : modelConnection) {
			CCModel.generateSidedModels(aModelConnection1, 1, Vector3.zero);
		}
		Scale[] mirrors = new Scale[] { new Scale(1, -1, 1), new Scale(1, 1, -1), new Scale(-1, 1, 1) };
		for (CCModel[] sideModels : modelConnection) {
			for (int s = 2; s < 6; s += 2) {
				sideModels[s] = sideModels[0].sidedCopy(0, s, Vector3.zero);
			}
			for (int s = 1; s < 6; s += 2) {
				sideModels[s] = sideModels[s - 1].backfacedCopy().apply(mirrors[s / 2]);
			}
		}

		modelCenter.computeNormals().computeLighting(LightModel.standardLightModel).shrinkUVs(RenderHelper.RENDER_OFFSET);

		for (CCModel[] aModelConnection : modelConnection) {
			for (CCModel anAModelConnection : aModelConnection) {
				anAModelConnection.computeNormals().computeLighting(LightModel.standardLightModel).shrinkUVs(RenderHelper.RENDER_OFFSET);
			}
		}

	}

	public boolean renderBase(CCRenderState ccrs, boolean invRender, int renderType, int[] connection, double x, double y, double z, TextureAtlasSprite iconBaseTexture) {

		x += 0.5;
		y += 0.5;
		z += 0.5;

		Translation trans = new Translation(x, y, z);

		int c = 0;
		Duct ductType = TDDucts.ductList.get(renderType);
		for (int s = 0; s < 6; s++) {
			if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct()) {
				IconTransformation icon;

				if (BlockDuct.ConnectionTypes.values()[connection[s]] == BlockDuct.ConnectionTypes.STRUCTURE) {
					icon = RenderUtils.getIconTransformation(TDDucts.structure.iconBaseTexture);
					modelConnection[0][s].render(ccrs, 8, 24, trans, icon);
					modelConnection[0][s].render(ccrs, 32, 48, trans, icon);
					if (ductType.iconConnectionTexture != null) {
						modelConnection[1][s].render(ccrs, trans, RenderUtils.getIconTransformation(ductType.iconConnectionTexture));
					}
				} else {
					c = c | (1 << s);
					if (invRender && iconBaseTexture != null) {
						icon = RenderUtils.getIconTransformation(TDDucts.structureInvis.iconBaseTexture);
						modelConnection[0][s].render(ccrs, 4, 8, trans, icon);
					}
					if (connection[s] == BlockDuct.ConnectionTypes.TILECONNECTION.ordinal() && ductType.iconConnectionTexture != null) {
						modelConnection[1][s].render(ccrs, trans, RenderUtils.getIconTransformation(ductType.iconConnectionTexture));
					}
				}
			}
		}

		if (iconBaseTexture != null) {
			IconTransformation icon = RenderUtils.getIconTransformation(iconBaseTexture);
			(ductType.opaque ? modelOpaqueTubes[c] : modelTransTubes[c]).render(ccrs, trans, icon);
		}

		if (ductType.iconFluidTexture != null && ductType.fluidTransparency == (byte) 255) {
			modelFluidTubes[c].render(ccrs, x, y, z, RenderUtils.getIconTransformation(ductType.iconFluidTexture));
		}

		if (ductType.frameType == 1) {
			renderSideTubes(ccrs, 0, connection, x - 0.5, y - 0.5, z - 0.5, TDTextures.SIDE_DUCTS);
		} else if (ductType.frameType == 2 && ductType.iconFrameTexture != null) {
			c = 0;
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
					if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
						modelFrameConnection[64 + s].render(ccrs, x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameBandTexture));
						modelFrame[70 + s].render(ccrs, x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
					}
				}
			}
			if (modelFrameConnection[c].verts.length != 0) {
				modelFrameConnection[c].render(ccrs, x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
			}
		} else if (ductType.frameType == 3 && ductType.iconFrameTexture != null) {
			modelLargeTubes[c].render(ccrs, x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
		} else if (ductType.frameType == 4 && ductType.iconFrameTexture != null) {
			c = 0;
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
					if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
						modelTransportConnection[64 + s].render(ccrs, x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameBandTexture));
					}
				}
			}
			if (modelTransportConnection[c].verts.length != 0) {
				modelTransportConnection[c].render(ccrs, x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
			}
		}
		return true;
	}

	public boolean renderSideTubes(CCRenderState ccrs, int pass, int[] connections, double x, double y, double z, TextureAtlasSprite icon) {

		CCModel[] models = pass == 0 ? ModelHelper.SideTubeGen.standardTubes : ModelHelper.SideTubeGen.standardTubesInner;
		int c = 0;
		for (int i = 0; i < 6; i++) {
			if (BlockDuct.ConnectionTypes.values()[connections[i]].renderDuct() && connections[i] != BlockDuct.ConnectionTypes.CLEANDUCT.ordinal()) {
				c = c | (1 << i);

				if (connections[i] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
					models[64 + i].render(ccrs, x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(icon));
				}
			}
		}

		if (models[c].verts.length == 0) {
			return false;
		}

		models[c].render(ccrs, x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(icon));
		return true;
	}

	public boolean renderWorldExtra(CCRenderState ccrs, boolean invRender, int renderType, int[] connection, double x, double y, double z) {

		Duct ductType = TDDucts.ductList.get(renderType);
		TextureAtlasSprite texture = ductType.iconFluidTexture;

		boolean flag = false;

		if (texture != null && ductType.fluidTransparency != (byte) 255) {
			int c = 0;

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
				}
			}
			modelFluidTubes[c].render(ccrs, x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(texture));

			flag = true;
		}

		if (ductType.frameType == 1 && ductType.iconFrameFluidTexture != null) {
			flag = renderSideTubes(ccrs, 1, connection, x, y, z, ductType.iconFrameFluidTexture) || flag;
		}

		if (ductType.frameType == 2 && ductType.iconFrameFluidTexture != null) {
			int c = 0;
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);

					if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
						modelFrame[70 + s].render(ccrs, x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(ductType.iconFrameFluidTexture));
					}
				}
			}

			if (modelFrame[c].verts.length != 0) {
				modelFrame[c].render(ccrs, x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(ductType.iconFrameFluidTexture));
				flag = true;
			}
		}

		if (ductType.frameType == 4 && ductType.iconFrameFluidTexture != null) {
			int c = 0;
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
				}
			}

			if (modelTransport[c].verts.length != 0) {
				modelTransport[c].render(ccrs, x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(ductType.iconFrameFluidTexture));
				flag = true;
			}
		}

		return flag;
	}

	public void renderFluid(CCRenderState ccrs, FluidStack stack, int[] connection, int level, double x, double y, double z) {

		if (stack == null || stack.amount <= 0 || level <= 0) {
			return;
		}
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		Fluid fluid = stack.getFluid();

		ccrs.setFluidColour(stack);
		RenderHelper.bindTexture(RenderHelper.MC_BLOCK_SHEET);
		TextureAtlasSprite fluidTex = RenderHelper.getFluidTexture(stack);

		if (fluid.isGaseous(stack)) {
			ccrs.alphaOverride = 32 + 32 * level;
			level = 6;
		}
		if (level < 6) {
			CCModel[] models = modelFluid[level - 1];

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					models[s].render(ccrs, x, y, z, RenderUtils.getIconTransformation(fluidTex));
				}
			}
			models[6].render(ccrs, x, y, z, RenderUtils.getIconTransformation(fluidTex));
		} else {
			int c = 0;

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
				}
			}
			modelFluidTubes[c].render(ccrs, x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(fluidTex));
		}
		ccrs.draw();
	}

	public int[] getDuctConnections(TileDuctBase tile) {

		int[] connections = new int[6];
		for (int i = 0; i < 6; i++) {
			connections[i] = tile.getRenderConnectionType(i).ordinal();
		}
		return connections;
	}

	@Override
	public void handleRenderBlockDamage(IBlockAccess world, BlockPos pos, IBlockState state, TextureAtlasSprite sprite, VertexBuffer buffer) {

	}

	@Override
	public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, VertexBuffer buffer) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		CCRenderState ccrs = CCRenderState.instance();
		ccrs.bind(buffer);
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileDuctBase)) {
			return false;
		}
		TileDuctBase theTile = (TileDuctBase) tile;

		ccrs.preRenderWorld(world, pos);
		int[] connections = getDuctConnections(theTile);

		boolean flag = false;
		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		for (Attachment attachment : theTile.attachments) {
			if (attachment != null) {
				flag = attachment.render(world, layer, ccrs) || flag;
			}
		}
		for (Cover cover : theTile.covers) {
			if (cover != null) {
				flag = cover.render(world, layer, ccrs) || flag;
			}
		}
		int renderType = TDDucts.getDuct(((BlockDuct) state.getBlock()).offset + state.getBlock().getMetaFromState(state)).id;

		if (layer == BlockRenderLayer.CUTOUT) {
			renderBase(ccrs, false, renderType, connections, x, y, z, theTile.getBaseIcon());
			flag = true;
		} else if (layer == BlockRenderLayer.TRANSLUCENT) {
			flag = renderWorldExtra(ccrs, false, renderType, connections, x, y, z) || flag;
		}

		return flag;
	}

	@Override
	public void renderBrightness(IBlockState state, float brightness) {

	}

	@Override
	public void registerTextures(TextureMap map) {

	}

	@Override
	public void renderItem(ItemStack item) {

		Block blockFromItem = Block.getBlockFromItem(item.getItem());

		Duct duct = TDDucts.getDuct(((BlockDuct) blockFromItem).offset + item.getItemDamage());
		int metadata = duct.id;

		GlStateManager.pushMatrix();

		RenderHelper.setBlockTextureSheet();
		CCRenderState ccrs = CCRenderState.instance();
		ccrs.reset();

		ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		renderBase(ccrs, true, metadata, INV_CONNECTIONS, 0, 0, 0, duct.getBaseTexture(item));
		ccrs.draw();

		ccrs.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		renderWorldExtra(ccrs, true, metadata, INV_CONNECTIONS, 0, 0 - RenderHelper.RENDER_OFFSET, 0);
		ccrs.draw();

		GlStateManager.popMatrix();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		return new ArrayList<>();
	}

	@Override
	public boolean isAmbientOcclusion() {

		return false;
	}

	@Override
	public boolean isGui3d() {

		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {

		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return null;
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

		return MapWrapper.handlePerspective(this, TransformUtils.DEFAULT_BLOCK.getTransforms(), cameraTransformType);
	}
}
