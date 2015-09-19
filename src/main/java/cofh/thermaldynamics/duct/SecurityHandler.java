package cofh.thermaldynamics.duct;

import cofh.api.tileentity.ISecurable;
import cofh.core.CoFHProps;
import cofh.lib.util.helpers.SecurityHelper;
import cofh.lib.util.helpers.StringHelper;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PreYggdrasilConverter;

public class SecurityHandler implements ISecurable {

	protected GameProfile owner = CoFHProps.DEFAULT_OWNER;
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

		if (MinecraftServer.getServer() == null) {
			return false;
		}
		if (Strings.isNullOrEmpty(name) || CoFHProps.DEFAULT_OWNER.getName().equalsIgnoreCase(name)) {
			return false;
		}
		String uuid = PreYggdrasilConverter.func_152719_a(name);
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
				if (MinecraftServer.getServer() != null) {
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
