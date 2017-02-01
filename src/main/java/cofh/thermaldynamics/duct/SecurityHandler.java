package cofh.thermaldynamics.duct;

import cofh.api.tileentity.ISecurable;
import cofh.core.init.CoreProps;
import cofh.lib.util.helpers.SecurityHelper;
import cofh.lib.util.helpers.StringHelper;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SecurityHandler implements ISecurable {

	protected GameProfile owner = CoreProps.DEFAULT_OWNER;
	protected AccessMode access = AccessMode.PUBLIC;

	@Override
	public boolean setAccess(AccessMode access) {

		this.access = access;
		return true;
	}

	@Override
	public AccessMode getAccess() {

		return access;
	}

	@Override
	public boolean setOwnerName(String name) {

		if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) {
			return false;
		}
		if (Strings.isNullOrEmpty(name) || CoreProps.DEFAULT_OWNER.getName().equalsIgnoreCase(name)) {
			return false;
		}
		String uuid = PreYggdrasilConverter.convertMobOwnerIfNeeded(FMLCommonHandler.instance().getMinecraftServerInstance(), name);
		if (Strings.isNullOrEmpty(uuid)) {
			return false;
		}
		return setOwner(new GameProfile(UUID.fromString(uuid), name));
	}

	@Override
	public boolean setOwner(GameProfile profile) {

		if (SecurityHelper.isDefaultUUID(owner.getId())) {
			owner = profile;
			if (!SecurityHelper.isDefaultUUID(owner.getId())) {
				if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
					new Thread("CoFH User Loader") {

						@Override
						public void run() {

							owner = SecurityHelper.getProfile(owner.getId(), owner.getName());
						}
					}.start();
				}

				return true;
			}
		}
		return false;
	}

	@Override
	public GameProfile getOwner() {

		return owner;
	}

	@Override
	public boolean canPlayerAccess(EntityPlayer player) {

		return false;
	}

	@Override
	public String getOwnerName() {

		String name = owner.getName();
		if (name == null) {
			return StringHelper.localize("info.cofh.anotherplayer");
		}
		return name;
	}
}
