package cofh.thermaldynamics.duct.entity;

import cofh.core.render.ShaderHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.block.TileTDBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Facing;
import org.lwjgl.opengl.GL11;

public class RenderPlayerRiding extends RenderPlayer {
    static EntityTransport transport;

    @Override
    public void setRenderPassModel(ModelBase p_77042_1_) {
        if (p_77042_1_ instanceof ModelBiped)
            this.renderPassModel = new ModelWrapper((ModelBiped) p_77042_1_);
        else
            this.renderPassModel = null;
    }

    @Override
    protected int shouldRenderPass(AbstractClientPlayer p_77032_1_, int p_77032_2_, float p_77032_3_) {
        int i = super.shouldRenderPass(p_77032_1_, p_77032_2_, p_77032_3_);
        if (this.renderPassModel == null)
            i = -1;
        return i;
    }

    @Override
    protected void renderModel(EntityLivingBase p_77036_1_, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float p_77036_7_) {
        GL11.glPushMatrix();
        this.bindEntityTexture(p_77036_1_);
        renderBiped(p_77036_7_, this.modelBipedMain);
        GL11.glPopMatrix();
    }

    @Override
    protected boolean func_110813_b(EntityLivingBase p_110813_1_) {
        return transport != null && transport.riddenByEntity != Minecraft.getMinecraft().thePlayer;
    }

    public static void handleAnimations(ModelBiped modelBiped) {
        if (transport == null) return;

        double scale = 0.85;
        GL11.glScaled(scale, scale, scale);


        int d = transport.direction;
        int od = transport.oldDirection;

        float stepTime = (transport.progress + (transport.pause > 0 ? 0 : transport.step) * ShaderHelper.midGameTick) / (EntityTransport.PIPE_LENGTH);

        float  yaw = 0, pitch = 0;

        switch (d) {
            case 0:
                pitch = 180;
                break;
            case 1:
                pitch = 0;
                break;
            case 2:
                yaw = 180;
                pitch = 90;
                break;
            case 3:
                yaw = 0;
                pitch = 90;
                break;
            case 4:
                yaw = 90;
                pitch = 90;
                break;
            case 5:
                yaw = 270;
                pitch = 90;
                break;
            default:
                return;
        }

        modelBiped.bipedLeftLeg.rotateAngleX =
                modelBiped.bipedLeftLeg.rotateAngleZ =
                        modelBiped.bipedRightLeg.rotateAngleX =
                                modelBiped.bipedRightLeg.rotateAngleZ = 0;

        if (d != od && d != (od ^ 1)) {
            float  prevPitch = 0, prevYaw = 0;
            switch (od) {
                case 0:
                    prevPitch = 180;
                    break;
                case 1:
                    prevPitch = 0;
                    break;
                case 2:
                    prevYaw = 180;
                    prevPitch = 90;
                    break;
                case 3:
                    prevYaw = 0;
                    prevPitch = 90;
                    break;
                case 4:
                    prevYaw = 90;
                    prevPitch = 90;
                    break;
                case 5:
                    prevYaw = 270;
                    prevPitch = 90;
                    break;
                default:
                    return;
            }

            if (d < 2) {
                yaw = prevYaw;
            } else if (od < 2) {
                prevYaw = yaw;
            }


            float v = MathHelper.clampF((stepTime - 0.25F) / (1.0F - 0.25F), 0, 1);

            if (Math.abs(prevYaw - yaw) > Math.abs(prevYaw - yaw - 360))
                yaw += 360;

            if (Math.abs(prevYaw - yaw) > Math.abs(prevYaw - yaw + 360))
                yaw -= 360;

            yaw = yaw * v + prevYaw * (1 - v);
            pitch = pitch * v + prevPitch * (1 - v);

            v = MathHelper.clampF(v, 0, 1);
            float angle = (v) * (1 - v) * 4 * 60 / 180.0F * (float) Math.PI;

            if (d == 0 || od == 1) {
                modelBiped.bipedLeftLeg.rotateAngleX = modelBiped.bipedRightLeg.rotateAngleX = -angle;
            } else if (d == 1 || od == 0) {
                modelBiped.bipedLeftLeg.rotateAngleX = modelBiped.bipedRightLeg.rotateAngleX = angle;
            } else {
                int q;
                if (d == 2 || d == 3) {
                    q = (od == 4) == (d == 2) ? 1 : -1;
                } else {
                    q = (od == 2) == (d == 4) ? -1 : 1;
                }

                angle *= q;

                modelBiped.bipedLeftLeg.rotateAngleZ = modelBiped.bipedRightLeg.rotateAngleZ = angle;
                if(angle < 0)
                    modelBiped.bipedRightLeg.rotationPointZ *= 0.7F;
                else
                    modelBiped.bipedLeftLeg.rotationPointZ *= 0.7F;
            }
        }

        GL11.glRotatef(yaw, 0, 1, 0);
        GL11.glRotatef(pitch, 1, 0, 0);


        modelBiped.bipedHead.rotateAngleY = (float) 0 / (180F / (float) Math.PI);
        modelBiped.bipedHead.rotateAngleX = (float) -90 / (180F / (float) Math.PI);
        modelBiped.bipedHeadwear.rotateAngleY = modelBiped.bipedHead.rotateAngleY;
        modelBiped.bipedHeadwear.rotateAngleX = modelBiped.bipedHead.rotateAngleX;

        modelBiped.bipedRightArm.rotateAngleZ = 0.0F;
        modelBiped.bipedLeftArm.rotateAngleZ = 0.0F;
        modelBiped.bipedRightLeg.rotateAngleY = 0.0F;
        modelBiped.bipedLeftLeg.rotateAngleY = 0.0F;
        modelBiped.bipedRightArm.rotateAngleX = 0.0F;
        modelBiped.bipedLeftArm.rotateAngleX = 0.0F;


        modelBiped.bipedBody.rotateAngleX = 0.0F;

        modelBiped.bipedRightLeg.rotationPointZ = 0.1F;
        modelBiped.bipedLeftLeg.rotationPointZ = 0.1F;
        modelBiped.bipedRightLeg.rotationPointY = 12.0F;
        modelBiped.bipedLeftLeg.rotationPointY = 12.0F;


        float renderScale = 1/16.0f;

        modelBiped.bipedHead.render(renderScale);
        modelBiped.bipedBody.render(renderScale);
        modelBiped.bipedRightArm.render(renderScale);
        modelBiped.bipedLeftArm.render(renderScale);
        modelBiped.bipedHeadwear.render(renderScale);


        if(od != d) {
            GL11.glTranslatef(0, -0.3F, 0);
        }else if (stepTime < 0.5F){
            if(transport.pos != null){
                TileEntity tile = transport.worldObj.getTileEntity(
                        transport.pos.x + Facing.offsetsXForSide[d ^ 1],
                        transport.pos.y + Facing.offsetsYForSide[d ^ 1],
                        transport.pos.z + Facing.offsetsZForSide[d ^ 1]);
                if (tile instanceof TileTransportDuctBase) {
                    if(((TileTransportDuctBase) tile).neighborTypes[d ^ 1] == TileTDBase.NeighborTypes.NONE){
                        GL11.glTranslatef(0, -0.3F * (1 - stepTime * 2 ), 0);
                    }
                }else
                    GL11.glTranslatef(0, -0.3F * (1 - stepTime * 2 ), 0);
            }
        }
        modelBiped.bipedRightLeg.render(renderScale);
        modelBiped.bipedLeftLeg.render(renderScale);

    }

    private void renderBiped(float p_77036_7_, ModelBiped modelBiped) {
        handleAnimations(modelBiped);


    }
}
