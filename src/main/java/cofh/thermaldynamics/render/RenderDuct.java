package cofh.thermaldynamics.render;

import cofh.core.block.BlockCoFHBase;
import cofh.core.render.IconRegistry;
import cofh.core.render.RenderUtils;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.lighting.LightModel;
import cofh.repack.codechicken.lib.render.CCModel;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Scale;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.repack.codechicken.lib.vec.Vector3;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.core.TDProps;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermalfoundation.fluid.TFFluids;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class RenderDuct implements ISimpleBlockRenderingHandler, IItemRenderer {

	public static final RenderDuct instance = new RenderDuct();

	static final int[] INV_CONNECTIONS = { BlockDuct.ConnectionTypes.DUCT.ordinal(), BlockDuct.ConnectionTypes.DUCT.ordinal(), 0, 0, 0, 0 };
	static int[] connections = new int[6];

	static IIcon textureCenterLine;

	public static IIcon coverBase;
	public static IIcon signalTexture;
	public static IIcon[] servoTexture = new IIcon[10];
	public static IIcon[] retrieverTexture = new IIcon[10];
	public static IIcon[] filterTexture = new IIcon[5];

	public static IIcon sideDucts;

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
		TDProps.renderDuctId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(instance);

		generateModels();
		generateFluidModels();
	}

	public static CCModel[] modelOpaqueTubes;
	public static CCModel[] modelTransTubes;
	private static CCModel[] modelFluidTubes;
	private static CCModel[] modelLargeTubes;

	public static void initialize() {

		generateFluidModels();
		generateModels();
		for (int i = 0; i < 10; i++) {
			servoTexture[i] = IconRegistry.getIcon("ServoBase" + i);
			retrieverTexture[i] = IconRegistry.getIcon("RetrieverBase" + i);
		}
		for (int i = 0; i < 5; i++) {
			filterTexture[i] = IconRegistry.getIcon("FilterBase" + i);
		}
		coverBase = IconRegistry.getIcon("CoverBase");
		sideDucts = IconRegistry.getIcon("SideDucts");
		signalTexture = IconRegistry.getIcon("Signaller");

		textureCenterLine = TFFluids.fluidSteam.getIcon();
	}

	private static void generateFluidModels() {

		for (int i = 1; i < 7; i++) {
			double d1 = 0.47 - 0.025 * i;
			double d2 = 0.53 + 0.025 * i;
			double d3 = 0.32 + 0.06 * i;
			double c1 = 0.32;
			double c2 = 0.68;
			double[][] boxes = new double[][] { { d1, 0, d1, d2, c1, d2 }, { d1, d3, d1, d2, 1, d2 }, { c1, c1, 0, c2, d3, c1 }, { c1, c1, c2, c2, d3, 1 },
					{ 0, c1, c1, c1, d3, c2 }, { c2, c1, c1, 1, d3, c2 }, { c1, c1, c1, c2, d3, c2 } };

			for (int s = 0; s < 7; s++) {
				modelFluid[i - 1][s] = CCModel.quadModel(24).generateBlock(0, boxes[s][0], boxes[s][1], boxes[s][2], boxes[s][3], boxes[s][4], boxes[s][5])
						.computeNormals();
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
		modelConnection[0][1].apply(RenderUtils.getRenderVector(-0.5, -0.5, -0.5).translation());

		for (int i = 0; i < modelConnection.length; i++) {
			CCModel.generateSidedModels(modelConnection[i], 1, Vector3.zero);
		}
		Scale[] mirrors = new Scale[] { new Scale(1, -1, 1), new Scale(1, 1, -1), new Scale(-1, 1, 1) };
		for (int i = 0; i < modelConnection.length; i++) {
			CCModel[] sideModels = modelConnection[i];
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

	public boolean renderBase(boolean invRender, int renderType, int[] connection, double x, double y, double z, IIcon iconBaseTexture) {

		x += 0.5;
		y += 0.5;
		z += 0.5;

		Translation trans = RenderUtils.getRenderVector(x, y, z).translation();

		int c = 0;
		Duct ductType = TDDucts.ductList.get(renderType);
		for (int s = 0; s < 6; s++) {
			if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct()) {
				RenderUtils.ScaledIconTransformation icon;

				if (BlockDuct.ConnectionTypes.values()[connection[s]] == BlockDuct.ConnectionTypes.STRUCTURE) {
					icon = RenderUtils.getIconTransformation(TDDucts.structure.iconBaseTexture);
					modelConnection[0][s].render(8, 24, trans, icon);
					modelConnection[0][s].render(32, 48, trans, icon);
					if (ductType.iconConnectionTexture != null) {
						modelConnection[1][s].render(trans, RenderUtils.getIconTransformation(ductType.iconConnectionTexture));
					}
				} else {
					c = c | (1 << s);
					if (invRender) {
						icon = RenderUtils.getIconTransformation(TDDucts.structureInvis.iconBaseTexture);
						modelConnection[0][s].render(4, 8, trans, icon);
					}
					if (connection[s] == BlockDuct.ConnectionTypes.TILECONNECTION.ordinal() && ductType.iconConnectionTexture != null) {
						modelConnection[1][s].render(trans, RenderUtils.getIconTransformation(ductType.iconConnectionTexture));
					}
				}
			}
		}

		if (iconBaseTexture != null) {
			RenderUtils.ScaledIconTransformation icon = RenderUtils.getIconTransformation(iconBaseTexture);
			(ductType.opaque ? modelOpaqueTubes[c] : modelTransTubes[c]).render(trans, icon);
		}

		if (ductType.iconFluidTexture != null && ductType.fluidTransparency == (byte) 255) {
			modelFluidTubes[c].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFluidTexture));
		}

		if (ductType.frameType == 1) {
			renderSideTubes(0, connection, x - 0.5, y - 0.5, z - 0.5, sideDucts);
		} else if (ductType.frameType == 2 && ductType.iconFrameTexture != null) {
			c = 0;
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
					if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
						modelFrameConnection[64 + s].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameBandTexture));
						modelFrame[70 + s].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
					}
				}
			}
			if (modelFrameConnection[c].verts.length != 0) {
				modelFrameConnection[c].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
			}
		} else if (ductType.frameType == 3 && ductType.iconFrameTexture != null) {
			modelLargeTubes[c].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
		} else if (ductType.frameType == 4 && ductType.iconFrameTexture != null) {
            c = 0;
            for (int s = 0; s < 6; s++) {
                if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
                    c = c | (1 << s);
                    if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
                        modelTransportConnection[64 + s].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameBandTexture));
//                        modelTransport[70 + s].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
                    }
                }
            }
            if (modelTransportConnection[c].verts.length != 0) {
                modelTransportConnection[c].render(x, y, z, RenderUtils.getIconTransformation(ductType.iconFrameTexture));
            }
        }
		return true;
	}

	public boolean renderSideTubes(int pass, int[] connections, double x, double y, double z, IIcon icon) {

		CCModel[] models = pass == 0 ? ModelHelper.SideTubeGen.standardTubes : ModelHelper.SideTubeGen.standardTubesInner;
		int c = 0;
		for (int i = 0; i < 6; i++) {
			if (BlockDuct.ConnectionTypes.values()[connections[i]].renderDuct() && connections[i] != BlockDuct.ConnectionTypes.CLEANDUCT.ordinal()) {
				c = c | (1 << i);

				if (connections[i] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
					models[64 + i].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(icon));
				}
			}
		}

		if (models[c].verts.length == 0) {
			return false;
		}

		models[c].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(icon));
		return true;
	}

	public boolean renderWorldExtra(boolean invRender, TileTDBase tile, int renderType, int[] connection, double x, double y, double z) {

		Tessellator.instance.setColorOpaque_F(1, 1, 1);
		Duct ductType = TDDucts.ductList.get(renderType);
		IIcon texture = ductType.iconFluidTexture;

		boolean flag = false;

		if (texture != null && ductType.fluidTransparency != (byte) 255) {
			int c = 0;

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
				}
			}
			modelFluidTubes[c].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(texture));

			flag = true;
		}

		if (ductType.frameType == 1 && ductType.iconFrameFluidTexture != null) {
			flag = renderSideTubes(1, connection, x, y, z, ductType.iconFrameFluidTexture) || flag;
		}

		if (ductType.frameType == 2 && ductType.iconFrameFluidTexture != null) {
			int c = 0;
			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);

					if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
						modelFrame[70 + s].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(ductType.iconFrameFluidTexture));
					}
				}
			}

			if (modelFrame[c].verts.length != 0) {
				modelFrame[c].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(ductType.iconFrameFluidTexture));
				flag = true;
			}
		}

        if (ductType.frameType == 4 && ductType.iconFrameFluidTexture != null) {
            int c = 0;
            for (int s = 0; s < 6; s++) {
                if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
                    c = c | (1 << s);

//                    if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
//                        modelTransport[70 + s].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(ductType.iconFrameFluidTexture));
//                    }
                }
            }

            if (modelTransport[c].verts.length != 0) {
                modelTransport[c].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(ductType.iconFrameFluidTexture));
                flag = true;
            }
        }

		return flag;
	}

	public void renderFluid(FluidStack stack, int[] connection, int level, double x, double y, double z) {

		if (stack == null || stack.amount <= 0 || level <= 0) {
			return;
		}
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		CCRenderState.startDrawing();
		Fluid fluid = stack.getFluid();

		RenderUtils.setFluidRenderColor(stack);
		RenderHelper.bindTexture(RenderHelper.MC_BLOCK_SHEET);
		IIcon fluidTex = RenderHelper.getFluidTexture(stack);

		if (fluid.isGaseous(stack)) {
			CCRenderState.alphaOverride = 32 + 32 * level;
			level = 6;
		}
		if (level < 6) {
			CCModel[] models = modelFluid[level - 1];

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					models[s].render(x, y, z, RenderUtils.getIconTransformation(fluidTex));
				}
			}
			models[6].render(x, y, z, RenderUtils.getIconTransformation(fluidTex));
		} else {
			int c = 0;

			for (int s = 0; s < 6; s++) {
				if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
					c = c | (1 << s);
				}
			}
			modelFluidTubes[c].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(fluidTex));
		}
		CCRenderState.draw();
	}

	public void getDuctConnections(TileTDBase tile) {

		for (int i = 0; i < 6; i++) {
			connections[i] = tile.getConnectionType(i).ordinal();
		}
	}

	/* ISimpleBlockRenderingHandler */
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {

		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof TileTDBase)) {
			return false;
		}
		TileTDBase theTile = (TileTDBase) tile;

		RenderUtils.preWorldRender(world, x, y, z);
		getDuctConnections(theTile);

		boolean flag = false;

		for (Attachment attachment : theTile.attachments) {
			if (attachment != null) {
				flag = attachment.render(BlockCoFHBase.renderPass, renderer) || flag;
			}
		}
		for (Cover cover : theTile.covers) {
			if (cover != null) {
				flag = cover.render(BlockCoFHBase.renderPass, renderer) || flag;
			}
		}
		int renderType = TDDucts.getDuct(((BlockDuct) block).offset + world.getBlockMetadata(x, y, z)).id;

		if (BlockCoFHBase.renderPass == 0) {
			renderBase(false, renderType, connections, x, y, z, theTile.getBaseIcon());
			flag = true;
		} else {
			flag = renderWorldExtra(false, theTile, renderType, connections, x, y, z) || flag;
		}

		flag = theTile.renderAdditional(renderType, connections, BlockCoFHBase.renderPass) || flag;

		return flag;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {

		return true;
	}

	@Override
	public int getRenderId() {

		return TDProps.renderDuctId;
	}

	/* IItemRenderer */
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {

		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {

		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {

		Block blockFromItem = Block.getBlockFromItem(item.getItem());

		Duct duct = TDDucts.getDuct(((BlockDuct) blockFromItem).offset + item.getItemDamage());
		int metadata = duct.id;
		boolean renderExtra = false;

		GL11.glPushMatrix();
		double offset = -0.5;
		if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			offset = 0;
		}
		RenderHelper.setBlockTextureSheet();
		RenderUtils.preItemRender();

		// GL11.glDepthMask(true);
		CCRenderState.startDrawing();
		renderBase(true, metadata, INV_CONNECTIONS, offset, offset, offset, duct.getBaseTexture(item));
		CCRenderState.draw();

		// GL11.glDepthMask(false);
		CCRenderState.startDrawing();
		renderWorldExtra(true, null, metadata, INV_CONNECTIONS, offset, offset - RenderHelper.RENDER_OFFSET, offset);
		CCRenderState.draw();

		// GL11.glDepthMask(true);

		CCRenderState.useNormals = false;
		RenderHelper.setItemTextureSheet();

		RenderUtils.postItemRender();
		GL11.glPopMatrix();
	}

}
