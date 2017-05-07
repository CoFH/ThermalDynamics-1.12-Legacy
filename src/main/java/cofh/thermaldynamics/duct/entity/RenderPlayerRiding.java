package cofh.thermaldynamics.duct.entity;

import cofh.core.render.ShaderHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.IDuctHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;

public class RenderPlayerRiding extends RenderPlayerAlt {

	static EntityTransport transport;

	public RenderPlayerRiding(RenderManager renderManager) {

		super(renderManager);

		List<LayerRenderer<?>> layersToRemove = new ArrayList<>();
		for (LayerRenderer<?> layer : layerRenderers) {
			if (layer instanceof LayerBipedArmor || layer instanceof LayerDeadmau5Head || layer instanceof LayerCustomHead) {
				continue;
			}
			layersToRemove.add(layer);
		}

		for (LayerRenderer<?> layer : layersToRemove) {
			removeLayer(layer);
		}
	}

	@Override
	protected void rotateCorpse(AbstractClientPlayer entityLiving, float p_77043_2_, float p_77043_3_, float partialTicks) {

		if (transport == null) {
			return;
		}

		entityLiving.prevRotationYawHead = 0F;
		entityLiving.rotationYawHead = 0F;
		entityLiving.prevRotationPitch = -90F;
		entityLiving.rotationPitch = -90F;

		int d = transport.direction;
		int od = transport.oldDirection;
		float stepTime = (transport.progress + (transport.pause > 0 ? 0 : transport.step) * ShaderHelper.midGameTick) / (EntityTransport.PIPE_LENGTH);
		float yaw = 0, pitch;

		switch (d) {
			case 0:
				pitch = 180;
				break;
			case 1:
				pitch = 0;
				break;
			case 2:
				yaw = 0;
				pitch = 270;
				break;
			case 3:
				yaw = 180;
				pitch = 270;
				break;
			case 4:
				yaw = 90;
				pitch = 270;
				break;
			case 5:
				yaw = 270;
				pitch = 270;
				break;
			default:
				return;
		}

		double scale = 0.85;
		GlStateManager.scale(scale, scale, scale);

		if (d != od && d != (od ^ 1)) {
			float prevPitch, prevYaw = 0;
			switch (od) {
				case 0:
					prevPitch = 180;
					break;
				case 1:
					prevPitch = 0;
					break;
				case 2:
					prevYaw = 0;
					prevPitch = 270;
					break;
				case 3:
					prevYaw = 180;
					prevPitch = 270;
					break;
				case 4:
					prevYaw = 90;
					prevPitch = 270;
					break;
				case 5:
					prevYaw = 270;
					prevPitch = 270;
					break;
				default:
					return;
			}

			if (d < 2) {
				yaw = prevYaw;
			} else if (od < 2) {
				prevYaw = yaw;
			}

			float v = MathHelper.clamp((stepTime - 0.25F) / (1.0F - 0.25F), 0, 1);

			if (Math.abs(prevYaw - yaw) > Math.abs(prevYaw - yaw - 360)) {
				yaw += 360;
			}

			if (Math.abs(prevYaw - yaw) > Math.abs(prevYaw - yaw + 360)) {
				yaw -= 360;
			}

			yaw = yaw * v + prevYaw * (1 - v);
			pitch = pitch * v + prevPitch * (1 - v);
		}

		GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);

		GlStateManager.translate(0, -1F, 0);

		if (od != d) {
			GlStateManager.translate(0, -0.3F, 0);
		} else if (stepTime < 0.5F) {
			if (transport.pos != null) {
				TileEntity tile = transport.worldObj.getTileEntity(transport.pos.offset(EnumFacing.VALUES[d].getOpposite()));

				if (tile instanceof IDuctHolder) {
					DuctUnitTransportBase base = ((IDuctHolder) tile).getDuct(DuctToken.TRANSPORT);
					if (base != null) {
						if (base.getRenderConnectionType(d ^ 1).renderDuct()) {
							GlStateManager.translate(0, -0.3F * (1 - stepTime * 2), 0);
						}
					}
				} else {
					GlStateManager.translate(0, -0.3F * (1 - stepTime * 2), 0);
				}
			}
		}

	}

	@Override
	protected boolean canRenderName(AbstractClientPlayer entity) {

		return transport != null && (transport.getPassengers().isEmpty() || transport.getPassengers().get(0) != Minecraft.getMinecraft().thePlayer);
	}
}
