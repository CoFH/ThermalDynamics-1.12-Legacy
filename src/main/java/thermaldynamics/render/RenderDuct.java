package thermaldynamics.render;

import cofh.core.block.BlockCoFHBase;
import cofh.core.render.IconRegistry;
import cofh.core.render.RenderUtils;
import cofh.lib.render.RenderHelper;
import cofh.repack.codechicken.lib.lighting.LightModel;
import cofh.repack.codechicken.lib.render.CCModel;
import cofh.repack.codechicken.lib.render.CCRenderState;
import cofh.repack.codechicken.lib.vec.Scale;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.repack.codechicken.lib.vec.Vector3;
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
import thermaldynamics.block.Attachment;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.core.TDProps;
import thermaldynamics.ducts.Ducts;
import thermalfoundation.block.BlockStorage;
import thermalfoundation.fluid.TFFluids;

public class RenderDuct implements ISimpleBlockRenderingHandler, IItemRenderer {

    public static final RenderDuct instance = new RenderDuct();


    static final int[] INV_CONNECTIONS = {BlockDuct.ConnectionTypes.DUCT.ordinal(), BlockDuct.ConnectionTypes.DUCT.ordinal(), 0, 0, 0, 0};
    static int[] connections = new int[6];

    static IIcon textureCenterLine;

    public static IIcon[] servoTexture = new IIcon[10];
    public static IIcon[] filterTexture = new IIcon[5];

    public static IIcon sideDucts;

    static CCModel[][] modelFluid = new CCModel[6][7];
    public static CCModel[][] modelConnection = new CCModel[3][6];
    static CCModel modelCenter;

    static CCModel[] modelLine = new CCModel[6];
    static CCModel modelLineCenter;


    static CCModel[] modelLargeDucts1 = new CCModel[64];
    static CCModel[] modelLargeDucts2 = new CCModel[64];

    static {
        TDProps.renderDuctId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(instance);


        generateFluidModels();
        generateModels();
    }

    public static CCModel[] opaqueTubes;
    public static CCModel[] transTubes;
    private static CCModel[] fluidTubes;


    public static void initialize() {
        generateFluidModels();
        generateModels();
        for (int i = 0; i < 10; i++)
            servoTexture[i] = IconRegistry.getIcon("ServoBase" + i);

        for (int i = 0; i < 5; i++)
            filterTexture[i] = IconRegistry.getIcon("FilterBase" + i);

        sideDucts = IconRegistry.getIcon("SideDucts");

        textureCenterLine = TFFluids.fluidSteam.getIcon();
    }

    private static void generateFluidModels() {

        for (int i = 1; i < 7; i++) {
            double d1 = 0.47 - 0.025 * i;
            double d2 = 0.53 + 0.025 * i;
            double d3 = 0.32 + 0.06 * i;
            double c1 = 0.32;
            double c2 = 0.68;
            double[][] boxes = new double[][]{{d1, 0, d1, d2, c1, d2}, {d1, d3, d1, d2, 1, d2}, {c1, c1, 0, c2, d3, c1}, {c1, c1, c2, c2, d3, 1},
                    {0, c1, c1, c1, d3, c2}, {c2, c1, c1, 1, d3, c2}, {c1, c1, c1, c2, d3, c2}};

            for (int s = 0; s < 7; s++) {
                modelFluid[i - 1][s] = CCModel.quadModel(24).generateBlock(0, boxes[s][0], boxes[s][1], boxes[s][2], boxes[s][3], boxes[s][4], boxes[s][5])
                        .computeNormals().computeLighting(LightModel.standardLightModel);
            }
        }
    }


    public static void generateModels() {

        modelCenter = CCModel.quadModel(48).generateBox(0, -3, -3, -3, 6, 6, 6, 0, 0, 32, 32, 16);
        modelConnection[0][1] = CCModel.quadModel(48).generateBox(0, -3, 3, -3, 6, 5, 6, 0, 16, 32, 32, 16);
        modelConnection[1][1] = CCModel.quadModel(24).generateBox(0, -4, 4, -4, 8, 4, 8, 0, 0, 32, 32, 16).computeNormals();
        modelConnection[2][1] = CCModel.quadModel(24).generateBox(0, -4, 4, -4, 8, 4, 8, 0, 16, 32, 32, 16).computeNormals();

        double h = 0.4;
        modelLineCenter = CCModel.quadModel(24).generateBlock(0, h, h, h, 1 - h, 1 - h, 1 - h).computeNormals();
        modelLine[1] = CCModel.quadModel(16).generateBlock(0, h, 1 - h, h, 1 - h, 1.0, 1 - h, 3).computeNormals();
        CCModel.generateSidedModels(modelLine, 1, Vector3.center);

        opaqueTubes = ModelHelper.StandardTubes.genModels(0.1875F, true);
        transTubes = ModelHelper.StandardTubes.genModels(0.1875F, false);
        fluidTubes = ModelHelper.StandardTubes.genModels(0.1875F * 0.99F, false);

        modelLargeDucts1 = (new ModelHelper.OctagonalTubeGen(0.4375, true)).generateModels();
        modelLargeDucts2 = (new ModelHelper.OctagonalTubeGen(0.4375 * 0.99, false)).generateModels();

        CCModel.generateBackface(modelCenter, 0, modelCenter, 24, 24);
        CCModel.generateBackface(modelConnection[0][1], 0, modelConnection[0][1], 24, 24);

        for (int i = 0; i < modelConnection.length; i++) {
            CCModel.generateSidedModels(modelConnection[i], 1, Vector3.zero);
        }
        Scale[] mirrors = new Scale[]{new Scale(1, -1, 1), new Scale(1, 1, -1), new Scale(-1, 1, 1)};
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

    public boolean renderFrame(boolean invRender, int renderType, int[] connection, double x, double y, double z) {

        x += 0.5;
        y += 0.5;
        z += 0.5;

        Translation trans = RenderUtils.getRenderVector(x, y, z).translation();


        for (int s = 0; s < 6; s++) {
            if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct()) {
                RenderUtils.ScaledIconTransformation icon;

                if (BlockDuct.ConnectionTypes.values()[connection[s]] == BlockDuct.ConnectionTypes.STRUCTURE) {
                    icon = RenderUtils.getIconTransformation(Ducts.STRUCTURE.iconBaseTexture);
                    modelConnection[0][s].render(0, 4, trans, icon);
                    modelConnection[0][s].render(8, 24, trans, icon);
                    modelConnection[0][s].render(24, 28, trans, icon);
                    modelConnection[0][s].render(32, 48, trans, icon);
                } else {
                    icon = RenderUtils.getIconTransformation(Ducts.values()[renderType].iconBaseTexture);
                    if (!invRender) {
                        modelConnection[0][s].render(0, 4, trans, icon);
                        modelConnection[0][s].render(8, 24, trans, icon);
                        modelConnection[0][s].render(24, 28, trans, icon);
                        modelConnection[0][s].render(32, 48, trans, icon);
                    } else {
                        modelConnection[0][s].render(trans, icon);
                    }

                    if (connection[s] == BlockDuct.ConnectionTypes.TILECONNECTION.ordinal() && Ducts.values()[renderType].iconConnectionTexture != null) {
                        modelConnection[1][s].render(trans, RenderUtils.getIconTransformation(Ducts.values()[renderType].iconConnectionTexture));
                    }
                }
            } else {
                modelCenter.render(s * 4, s * 4 + 4, trans, RenderUtils.getIconTransformation(Ducts.values()[renderType].iconBaseTexture));
                modelCenter.render(24 + s * 4, 28 + s * 4, trans, RenderUtils.getIconTransformation(Ducts.values()[renderType].iconBaseTexture));
            }
        }


        if (Ducts.values()[renderType].iconOverDuctTexture != null) {
            int c = 0;
            for (int s = 0; s < 6; s++) {
                if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
                    c = c | (1 << s);

                    if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
                        modelLargeDucts1[64 + s].render(x, y, z, RenderUtils.getIconTransformation(BlockStorage.TEXTURES[7]));
//                    }
//                    if (invRender) {
                        modelLargeDucts2[70 + s].render(x, y, z, RenderUtils.getIconTransformation(Ducts.values()[renderType].iconOverDuctTexture));
                    }
                }
            }

            if (modelLargeDucts1[c].verts.length != 0) {
                modelLargeDucts1[c].render(x, y, z, RenderUtils.getIconTransformation(Ducts.values()[renderType].iconOverDuctTexture));
            }
        }

        return true;
    }

    boolean debug = true;

    public boolean renderSideTubes(int pass, int[] connections, double x, double y, double z) {
        CCModel[] models = pass == 0 ? ModelHelper.SideTubeGen.standardTubes : ModelHelper.SideTubeGen.standardTubesInner;
        int c = 0;
        for (int i = 0; i < 6; i++)
            if (connections[i] == BlockDuct.ConnectionTypes.DUCT.ordinal()) {
                c = c | (1 << i);
            }

        if (models[c].verts.length == 0)
            return false;

        models[c].render(x + 0.5, y + 0.5, z + 0.5,
                RenderUtils.getIconTransformation(pass == 0 ? RenderDuct.sideDucts : TFFluids.fluidRedstone.getIcon())
        );
        return true;
    }


    public boolean renderWorldExtra(boolean invRender, TileMultiBlock tile, int renderType, int[] connection, double x, double y, double z) {
        Tessellator.instance.setColorOpaque_F(1, 1, 1);
        IIcon texture = Ducts.values()[renderType].iconFluidTexture;

        boolean flag = false;

        if (texture != null) {
            CCModel[] models = modelFluid[5];

            for (int s = 0; s < 6; s++) {
                if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
                    models[s].render(x, y, z, RenderUtils.getIconTransformation(texture));
                }
            }
            models[6].render(x, y, z, RenderUtils.getIconTransformation(texture));

            flag = true;
        }

        if (Ducts.values()[renderType].iconOverDuctInternalTexture != null) {
            int c = 0;
            for (int s = 0; s < 6; s++) {
                if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct() && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()) {
                    c = c | (1 << s);

                    if (invRender || connection[s] != BlockDuct.ConnectionTypes.DUCT.ordinal()) {
                        modelLargeDucts2[70 + s].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(Ducts.values()[renderType].iconOverDuctInternalTexture));
                    }
                }
            }

            if (modelLargeDucts2[c].verts.length != 0) {
                modelLargeDucts2[c].render(x + 0.5, y + 0.5, z + 0.5, RenderUtils.getIconTransformation(Ducts.values()[renderType].iconOverDuctInternalTexture));
                flag = true;
            }
        }

        return flag;
    }

    public void renderFluid(FluidStack stack, int[] connection, int level, double x, double y, double z) {

        if (stack == null || stack.amount <= 0 || level <= 0 || stack.fluidID <= 0) {
            return;
        }
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        CCRenderState.startDrawing();
        Fluid fluid = stack.getFluid();

        RenderUtils.setFluidRenderColor(stack);
        RenderHelper.bindTexture(RenderHelper.MC_BLOCK_SHEET);
        IIcon fluidTex = RenderHelper.getFluidTexture(stack);

        if (fluid.isGaseous(stack)) {
            CCRenderState.setColour(RenderUtils.getFluidRenderColor(stack) << 8 | 32 + 36 * level);
            level = 6;
        }
        CCModel[] models = modelFluid[level - 1];

        for (int s = 0; s < 6; s++) {
            if (BlockDuct.ConnectionTypes.values()[connection[s]].renderDuct()
                    && connection[s] != BlockDuct.ConnectionTypes.STRUCTURE.ordinal()
                    ) {
                models[s].render(x, y, z, RenderUtils.getIconTransformation(fluidTex));
            }
        }
        models[6].render(x, y, z, RenderUtils.getIconTransformation(fluidTex));
        CCRenderState.draw();
    }

    public void getDuctConnections(TileMultiBlock tile) {
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
        if (!(tile instanceof TileMultiBlock)) {
            return false;
        }
        TileMultiBlock theTile = (TileMultiBlock) tile;

        RenderUtils.preWorldRender(world, x, y, z);
        getDuctConnections(theTile);

        boolean flag = false;


        for (Attachment attachment : theTile.attachments) {
            if (attachment != null) {
                flag = attachment.render(BlockCoFHBase.renderPass, renderer) || flag;
            }
        }

        int renderType = Ducts.getDuct(((BlockDuct) block).offset + world.getBlockMetadata(x, y, z)).ordinal();


        if (BlockCoFHBase.renderPass == 0) {
            renderFrame(false, renderType, connections, x, y, z);
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

        int metadata = Ducts.getDuct(((BlockDuct) blockFromItem).offset + item.getItemDamage()).ordinal();
        boolean renderExtra = false;

        GL11.glPushMatrix();
        double offset = -0.5;
        if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            offset = 0;
        } else if (type == ItemRenderType.ENTITY) {
            GL11.glScaled(0.5, 0.5, 0.5);
        }
        RenderHelper.setBlockTextureSheet();
        RenderUtils.preItemRender();

        CCRenderState.startDrawing();
        instance.renderFrame(true, metadata, INV_CONNECTIONS, offset, offset, offset);
        CCRenderState.draw();

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);


        CCRenderState.startDrawing();
        renderWorldExtra(true, null, metadata, INV_CONNECTIONS, offset, offset - RenderHelper.RENDER_OFFSET, offset);
        CCRenderState.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);


        CCRenderState.useNormals = false;
        RenderHelper.setItemTextureSheet();
        RenderUtils.postItemRender();

        GL11.glPopMatrix();
    }

}
