package cofh.hud;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CoFHHUD {

    public static List<IHUDModule> modules = new LinkedList<IHUDModule>();

    static boolean initialized = false;
    static int moduleID = 0;


    public static int registerHUDModule(IHUDModule module) {

        if (module != null) {
            if (!modules.contains(module)) {
                modules.add(module);
                module.setModuleID(moduleID++);
                if (!initialized) {
                    initialized = true;
                    FMLCommonHandler.instance().bus().register(HUDRenderHandler.instance);
                }
            }
            return 0;
        } else {
            if (!initialized) {
                initialized = true;
                FMLCommonHandler.instance().bus().register(HUDRenderHandler.instance);
            }
            return moduleID++;
        }
    }

}
