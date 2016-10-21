//package cofh.thermaldynamics.duct.entity;
//
//import com.mojang.authlib.GameProfile;
//
//import net.minecraft.block.Block;
//import net.minecraft.client.entity.AbstractClientPlayer;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.client.model.ModelBiped;
//import net.minecraft.client.renderer.RenderBlocks;
//import net.minecraft.client.renderer.entity.RenderBiped;
//import net.minecraft.client.renderer.entity.RendererLivingEntity;
//import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.init.Items;
//import net.minecraft.item.EnumAction;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemArmor;
//import net.minecraft.item.ItemBlock;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.nbt.NBTUtil;
//import net.minecraft.scoreboard.Score;
//import net.minecraft.scoreboard.ScoreObjective;
//import net.minecraft.scoreboard.Scoreboard;
//import net.minecraft.util.MathHelper;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.StringUtils;
//
//import org.lwjgl.opengl.GL11;
//
//public class RenderPlayerAlt extends RendererLivingEntity {
//
//	public ModelBiped modelBipedMain;
//	public ModelBiped modelArmorChestplate;
//	public ModelBiped modelArmor;
//
//	public RenderPlayerAlt() {
//
//		super(new ModelBiped(0.0F), 0.5F);
//		this.modelBipedMain = (ModelBiped) this.mainModel;
//		this.modelArmorChestplate = new ModelBiped(1.0F);
//		this.modelArmor = new ModelBiped(0.5F);
//	}
//
//	public void doRender(AbstractClientPlayer player, double x, double p_76986_4_, double z, float p_76986_8_, float p_76986_9_) {
//
//		GL11.glColor3f(1.0F, 1.0F, 1.0F);
//		ItemStack itemstack = player.inventory.getCurrentItem();
//		this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = itemstack != null ? 1 : 0;
//
//		if (itemstack != null && player.getItemInUseCount() > 0) {
//			EnumAction enumaction = itemstack.getItemUseAction();
//
//			if (enumaction == EnumAction.block) {
//				this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 3;
//			} else if (enumaction == EnumAction.bow) {
//				this.modelArmorChestplate.aimedBow = this.modelArmor.aimedBow = this.modelBipedMain.aimedBow = true;
//			}
//		}
//
//		this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = player.isSneaking();
//		double y2 = p_76986_4_ - player.yOffset;
//
//		if (player.isSneaking() && !(player instanceof EntityPlayerSP)) {
//			y2 -= 0.125D;
//		}
//
//		super.doRender(player, x, y2, z, p_76986_8_, p_76986_9_);
//		this.modelArmorChestplate.aimedBow = this.modelArmor.aimedBow = this.modelBipedMain.aimedBow = false;
//		this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = false;
//		this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 0;
//	}
//
//	@Override
//	protected void func_96449_a(EntityLivingBase player, double x, double y, double z, String p_96449_8_, float p_96449_9_, double dist) {
//
//		if (dist < 100.0D) {
//			Scoreboard scoreboard = ((AbstractClientPlayer) player).getWorldScoreboard();
//			ScoreObjective scoreobjective = scoreboard.func_96539_a(2);
//
//			if (scoreobjective != null) {
//				Score score = scoreboard.func_96529_a(player.getCommandSenderName(), scoreobjective);
//
//				if (player.isPlayerSleeping()) {
//					this.func_147906_a(player, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y - 1.5D, z, 64);
//				} else {
//					this.func_147906_a(player, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
//				}
//
//				y += this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * p_96449_9_;
//			}
//		}
//
//		super.func_96449_a(player, x, y, z, p_96449_8_, p_96449_9_, dist);
//	}
//
//	@Override
//	protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_) {
//
//		float f1 = 0.9375F;
//		GL11.glScalef(f1, f1, f1);
//	}
//
//	@Override
//	protected void func_82408_c(EntityLivingBase p_82408_1_, int p_82408_2_, float p_82408_3_) {
//
//		ItemStack itemstack = ((AbstractClientPlayer) p_82408_1_).inventory.armorItemInSlot(3 - p_82408_2_);
//
//		if (itemstack != null) {
//			Item item = itemstack.getItem();
//
//			if (item instanceof ItemArmor) {
//				this.bindTexture(RenderBiped.getArmorResource(p_82408_1_, itemstack, p_82408_2_, "overlay"));
//				GL11.glColor3f(1.0F, 1.0F, 1.0F);
//			}
//		}
//	}
//
//	@Override
//	protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_) {
//
//		ItemStack itemstack = ((AbstractClientPlayer) p_77032_1_).inventory.armorItemInSlot(3 - p_77032_2_);
//
//		if (itemstack != null) {
//			Item item = itemstack.getItem();
//
//			if (item instanceof ItemArmor) {
//				ItemArmor itemarmor = (ItemArmor) item;
//				this.bindTexture(RenderBiped.getArmorResource(p_77032_1_, itemstack, p_77032_2_, null));
//				ModelBiped modelbiped = p_77032_2_ == 2 ? this.modelArmor : this.modelArmorChestplate;
//				modelbiped.bipedHead.showModel = p_77032_2_ == 0;
//				modelbiped.bipedHeadwear.showModel = p_77032_2_ == 0;
//				modelbiped.bipedBody.showModel = p_77032_2_ == 1 || p_77032_2_ == 2;
//				modelbiped.bipedRightArm.showModel = p_77032_2_ == 1;
//				modelbiped.bipedLeftArm.showModel = p_77032_2_ == 1;
//				modelbiped.bipedRightLeg.showModel = p_77032_2_ == 2 || p_77032_2_ == 3;
//				modelbiped.bipedLeftLeg.showModel = p_77032_2_ == 2 || p_77032_2_ == 3;
//				modelbiped = net.minecraftforge.client.ForgeHooksClient.getArmorModel(p_77032_1_, itemstack, p_77032_2_, modelbiped);
//				this.setRenderPassModel(modelbiped);
//				modelbiped.onGround = this.mainModel.onGround;
//				modelbiped.isRiding = this.mainModel.isRiding;
//				modelbiped.isChild = this.mainModel.isChild;
//
//				//Move outside if to allow for more then just CLOTH
//				int j = itemarmor.getColor(itemstack);
//				if (j != -1) {
//					float f1 = (j >> 16 & 255) / 255.0F;
//					float f2 = (j >> 8 & 255) / 255.0F;
//					float f3 = (j & 255) / 255.0F;
//					GL11.glColor3f(f1, f2, f3);
//
//					if (itemstack.isItemEnchanted()) {
//						return 31;
//					}
//
//					return 16;
//				}
//
//				GL11.glColor3f(1.0F, 1.0F, 1.0F);
//
//				if (itemstack.isItemEnchanted()) {
//					return 15;
//				}
//
//				return 1;
//			}
//		}
//
//		return -1;
//	}
//
//	@Override
//	protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_) {
//
//		GL11.glColor3f(1.0F, 1.0F, 1.0F);
//		super.renderArrowsStuckInEntity(p_77029_1_, p_77029_2_);
//		AbstractClientPlayer player = (AbstractClientPlayer) p_77029_1_;
//		ItemStack itemstack = player.inventory.armorItemInSlot(3);
//
//		if (itemstack != null) {
//			GlStateManager.pushMatrix();
//			this.modelBipedMain.bipedHead.postRender(0.0625F);
//			float f1;
//
//			if (itemstack.getItem() instanceof ItemBlock) {
//				net.minecraftforge.client.IItemRenderer customRenderer = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(itemstack,
//						net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
//				boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED,
//						itemstack, net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D));
//
//				if (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack.getItem()).getRenderType())) {
//					f1 = 0.625F;
//					GL11.glTranslatef(0.0F, -0.25F, 0.0F);
//					GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
//					GL11.glScalef(f1, -f1, -f1);
//				}
//
//				this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack, 0);
//			} else if (itemstack.getItem() == Items.skull) {
//				f1 = 1.0625F;
//				GL11.glScalef(f1, -f1, -f1);
//				GameProfile gameprofile = null;
//
//				if (itemstack.hasTagCompound()) {
//					NBTTagCompound nbttagcompound = itemstack.getTagCompound();
//
//					if (nbttagcompound.hasKey("SkullOwner", 10)) {
//						gameprofile = NBTUtil.func_152459_a(nbttagcompound.getCompoundTag("SkullOwner"));
//					} else if (nbttagcompound.hasKey("SkullOwner", 8) && !StringUtils.isNullOrEmpty(nbttagcompound.getString("SkullOwner"))) {
//						gameprofile = new GameProfile(null, nbttagcompound.getString("SkullOwner"));
//					}
//				}
//
//				TileEntitySkullRenderer.field_147536_b.func_152674_a(-0.5F, 0.0F, -0.5F, 1, 180.0F, itemstack.getItemDamage(), gameprofile);
//			}
//
//			GlStateManager.popMatrix();
//		}
//
//		float f2;
//
//		if (p_77029_1_.getCommandSenderName().equals("deadmau5") && player.func_152123_o()) {
//			this.bindTexture(player.getLocationSkin());
//
//			for (int j = 0; j < 2; ++j) {
//				float f9 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * p_77029_2_
//						- (player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * p_77029_2_);
//				float f10 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * p_77029_2_;
//				GlStateManager.pushMatrix();
//				GL11.glRotatef(f9, 0.0F, 1.0F, 0.0F);
//				GL11.glRotatef(f10, 1.0F, 0.0F, 0.0F);
//				GL11.glTranslatef(0.375F * (j * 2 - 1), 0.0F, 0.0F);
//				GL11.glTranslatef(0.0F, -0.375F, 0.0F);
//				GL11.glRotatef(-f10, 1.0F, 0.0F, 0.0F);
//				GL11.glRotatef(-f9, 0.0F, 1.0F, 0.0F);
//				f2 = 1.3333334F;
//				GL11.glScalef(f2, f2, f2);
//				this.modelBipedMain.renderEars(0.0625F);
//				GlStateManager.popMatrix();
//			}
//		}
//
//		boolean flag = player.func_152122_n();
//		float f4;
//
//		if (flag && !p_77029_1_.isInvisible() && !player.getHideCape()) {
//			this.bindTexture(player.getLocationCape());
//			GlStateManager.pushMatrix();
//			GL11.glTranslatef(0.0F, 0.0F, 0.125F);
//			double d3 = player.field_71091_bM + (player.field_71094_bP - player.field_71091_bM) * p_77029_2_
//					- (player.prevPosX + (player.posX - player.prevPosX) * p_77029_2_);
//			double d4 = player.field_71096_bN + (player.field_71095_bQ - player.field_71096_bN) * p_77029_2_
//					- (player.prevPosY + (player.posY - player.prevPosY) * p_77029_2_);
//			double d0 = player.field_71097_bO + (player.field_71085_bR - player.field_71097_bO) * p_77029_2_
//					- (player.prevPosZ + (player.posZ - player.prevPosZ) * p_77029_2_);
//			f4 = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * p_77029_2_;
//			double d1 = MathHelper.sin(f4 * (float) Math.PI / 180.0F);
//			double d2 = (-MathHelper.cos(f4 * (float) Math.PI / 180.0F));
//			float f5 = (float) d4 * 10.0F;
//
//			if (f5 < -6.0F) {
//				f5 = -6.0F;
//			}
//
//			if (f5 > 32.0F) {
//				f5 = 32.0F;
//			}
//
//			float f6 = (float) (d3 * d1 + d0 * d2) * 100.0F;
//			float f7 = (float) (d3 * d2 - d0 * d1) * 100.0F;
//
//			if (f6 < 0.0F) {
//				f6 = 0.0F;
//			}
//
//			float f8 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * p_77029_2_;
//			f5 += MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * p_77029_2_) * 6.0F)
//					* 32.0F * f8;
//
//			if (p_77029_1_.isSneaking()) {
//				f5 += 25.0F;
//			}
//
//			GL11.glRotatef(6.0F + f6 / 2.0F + f5, 1.0F, 0.0F, 0.0F);
//			GL11.glRotatef(f7 / 2.0F, 0.0F, 0.0F, 1.0F);
//			GL11.glRotatef(-f7 / 2.0F, 0.0F, 1.0F, 0.0F);
//			GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
//			this.modelBipedMain.renderCloak(0.0625F);
//			GlStateManager.popMatrix();
//		}
//
//		ItemStack itemstack1 = player.inventory.getCurrentItem();
//
//		if (itemstack1 != null) {
//			GlStateManager.pushMatrix();
//			this.modelBipedMain.bipedRightArm.postRender(0.0625F);
//			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);
//
//			if (player.fishEntity != null) {
//				itemstack1 = new ItemStack(Items.stick);
//			}
//
//			EnumAction enumaction = null;
//
//			if (player.getItemInUseCount() > 0) {
//				enumaction = itemstack1.getItemUseAction();
//			}
//
//			net.minecraftforge.client.IItemRenderer customRenderer = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(itemstack1,
//					net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
//			boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED,
//					itemstack1, net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D));
//
//			if (is3D || itemstack1.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack1.getItem()).getRenderType())) {
//				f2 = 0.5F;
//				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
//				f2 *= 0.75F;
//				GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
//				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
//				GL11.glScalef(-f2, -f2, f2);
//			} else if (itemstack1.getItem() == Items.bow) {
//				f2 = 0.625F;
//				GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
//				GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
//				GL11.glScalef(f2, -f2, f2);
//				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
//				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
//			} else if (itemstack1.getItem().isFull3D()) {
//				f2 = 0.625F;
//
//				if (itemstack1.getItem().shouldRotateAroundWhenRendering()) {
//					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
//					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
//				}
//
//				if (player.getItemInUseCount() > 0 && enumaction == EnumAction.block) {
//					GL11.glTranslatef(0.05F, 0.0F, -0.1F);
//					GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
//					GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
//					GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
//				}
//
//				GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
//				GL11.glScalef(f2, -f2, f2);
//				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
//				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
//			} else {
//				f2 = 0.375F;
//				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
//				GL11.glScalef(f2, f2, f2);
//				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
//				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
//				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
//			}
//
//			float f3;
//			int k;
//			float f12;
//
//			if (itemstack1.getItem().requiresMultipleRenderPasses()) {
//				for (k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k) {
//					int i = itemstack1.getItem().getColorFromItemStack(itemstack1, k);
//					f12 = (i >> 16 & 255) / 255.0F;
//					f3 = (i >> 8 & 255) / 255.0F;
//					f4 = (i & 255) / 255.0F;
//					GlStateManager.color(f12, f3, f4, 1.0F);
//					this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack1, k);
//				}
//			} else {
//				k = itemstack1.getItem().getColorFromItemStack(itemstack1, 0);
//				float f11 = (k >> 16 & 255) / 255.0F;
//				f12 = (k >> 8 & 255) / 255.0F;
//				f3 = (k & 255) / 255.0F;
//				GlStateManager.color(f11, f12, f3, 1.0F);
//				this.renderManager.itemRenderer.renderItem(p_77029_1_, itemstack1, 0);
//			}
//
//			GlStateManager.popMatrix();
//		}
//	}
//
//	@Override
//	protected void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_) {
//
//		if (p_77043_1_.isEntityAlive() && p_77043_1_.isPlayerSleeping()) {
//			GL11.glRotatef(((AbstractClientPlayer) p_77043_1_).getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
//			GL11.glRotatef(this.getDeathMaxRotation(p_77043_1_), 0.0F, 0.0F, 1.0F);
//			GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
//		} else {
//			super.rotateCorpse(p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
//		}
//	}
//
//	@Override
//	protected void renderLivingAt(EntityLivingBase p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_) {
//
//		if (p_77039_1_.isEntityAlive() && p_77039_1_.isPlayerSleeping()) {
//			super.renderLivingAt(p_77039_1_, p_77039_2_ + ((AbstractClientPlayer) p_77039_1_).field_71079_bU, p_77039_4_
//					+ ((AbstractClientPlayer) p_77039_1_).field_71082_cx, p_77039_6_ + ((AbstractClientPlayer) p_77039_1_).field_71089_bV);
//		} else {
//			super.renderLivingAt(p_77039_1_, p_77039_2_, p_77039_4_, p_77039_6_);
//		}
//	}
//
//	@Override
//	public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
//
//		this.doRender((AbstractClientPlayer) p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
//	}
//
//	@Override
//	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
//
//		return ((AbstractClientPlayer) p_110775_1_).getLocationSkin();
//	}
//
//	@Override
//	public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {
//
//		this.doRender((AbstractClientPlayer) p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
//	}
//}
