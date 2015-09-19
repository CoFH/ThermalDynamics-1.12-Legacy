package cofh.thermaldynamics.duct;

import net.minecraft.client.renderer.texture.IIconRegister;

public class DuctTransport extends Duct {

	public DuctTransport(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
			String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

		super(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency, frameTexture,
				frameFluidTexture, frameFluidTransparency);
	}

	@Override
	public void registerIcons(IIconRegister ir) {

		super.registerIcons(ir);
		frameType = 4;
	}
}
