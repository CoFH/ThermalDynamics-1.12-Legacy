package cofh.thermaldynamics.duct.entity;

import java.util.Random;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

public class ModelWrapper extends ModelBiped {
    ModelBiped base;
    
    public ModelWrapper(ModelBiped base) {
        this.base = base;

        this.textureWidth = base.textureWidth;
        this.textureHeight = base.textureHeight;

        if (base.bipedHead != null)
            bipedHead = base.bipedHead;
        if (base.bipedHeadwear != null)
            bipedHeadwear = base.bipedHeadwear;
        if (base.bipedBody != null)
            bipedBody = base.bipedBody;
        if (base.bipedRightArm != null)
            bipedRightArm = base.bipedRightArm;
        if (base.bipedLeftArm != null)
            bipedLeftArm = base.bipedLeftArm;
        if (base.bipedRightLeg != null)
            bipedRightLeg = base.bipedRightLeg;
        if (base.bipedLeftLeg != null)
            bipedLeftLeg = base.bipedLeftLeg;
        if (base.bipedEars != null)
            bipedEars = base.bipedEars;
        if (base.bipedCloak != null)
            bipedCloak = base.bipedCloak;

        heldItemLeft = base.heldItemLeft;

        heldItemRight = base.heldItemRight;
        isSneak = base.isSneak;
        isChild = base.isChild;
        isRiding = base.isRiding;
        onGround = base.onGround;

        aimedBow = base.aimedBow;
    }

    @Override
    public void render(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_) {
        GL11.glPushMatrix();


        RenderPlayerRiding.handleAnimations(this);
//        if (this.isChild)
//        {
//            float f6 = 2.0F;
//            GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
//            GL11.glTranslatef(0.0F, 16.0F * p_78088_7_, 0.0F);
//            this.bipedHead.render(p_78088_7_);
//            GL11.glPopMatrix();
//            GL11.glPushMatrix();
//            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
//            GL11.glTranslatef(0.0F, 24.0F * p_78088_7_, 0.0F);
//            this.bipedBody.render(p_78088_7_);
//            this.bipedRightArm.render(p_78088_7_);
//            this.bipedLeftArm.render(p_78088_7_);
//            this.bipedHeadwear.render(p_78088_7_);
//            this.bipedRightLeg.render(p_78088_7_);
//            this.bipedLeftLeg.render(p_78088_7_);
//
//        }
//        else
//        {
//            this.bipedHead.render(p_78088_7_);
//            this.bipedBody.render(p_78088_7_);
//            this.bipedRightArm.render(p_78088_7_);
//            this.bipedLeftArm.render(p_78088_7_);
//            this.bipedHeadwear.render(p_78088_7_);
//            this.bipedRightLeg.render(p_78088_7_);
//            this.bipedLeftLeg.render(p_78088_7_);
//        }
        GL11.glPopMatrix();
    }

    @Override
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
//        base.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, p_78087_7_);
    }

    @Override
    public void renderEars(float p_78110_1_) {
        base.renderEars(p_78110_1_);
    }

    @Override
    public void renderCloak(float p_78111_1_) {
        base.renderCloak(p_78111_1_);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase p_78086_1_, float p_78086_2_, float p_78086_3_, float p_78086_4_) {
//        base.setLivingAnimations(p_78086_1_, p_78086_2_, p_78086_3_, p_78086_4_);
    }

    @Override
    public ModelRenderer getRandomModelBox(Random p_85181_1_) {
        return base.getRandomModelBox(p_85181_1_);
    }

    @Override
    public TextureOffset getTextureOffset(String p_78084_1_) {
        return base.getTextureOffset(p_78084_1_);
    }
}
