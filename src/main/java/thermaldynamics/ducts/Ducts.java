package thermaldynamics.ducts;


import net.minecraft.item.ItemStack;

public enum Ducts {

    ENERGY_BASIC(0, "energyBasicDuct", Type.Energy),
    ENERGY_HARDENED(1, "energyHardenedDuct", Type.Energy),
    ENERGY_REINFORCED(2, "energyReinforcedDuct", Type.Energy),
    FLUID_TRANS(3, "fluidTransDuct", Type.Fluid),
    FLUID_OPAQUE(4, "fluidOpaqueDuct", Type.Fluid),
    ITEM_TRANS(5, "itemTransDuct", Type.Item),
    ITEM_OPAQUE(6, "itemOpaqueDuct", Type.Item),
    ITEM_FAST_TRANS(7, "itemFastTransDuct", Type.Item),
    ITEM_FAST_OPAQUE(8, "itemFastOpaqueDuct", Type.Item),
    ENERGY_REINFORCED_EMPTY(9, "energyEmptyReinforcedDuct", Type.Structural),
    STRUCTURE(10, "energyEmptyReinforcedDuct", Type.Structural),

//    ENERGY_SUPERCONDUCTOR(9, "energySuperConductorDuct", Type.Energy),
//    FLUID_WEAK_TRANS(10, "fluidWeakTransDuct", Type.Fluid),
//    FLUID_WEAK_OPAQUE(11, "fluidWeakOpaqueDuct", Type.Fluid),
//    FLUID_POWER_TRANS(12, "fluidPoweredTransDuct", Type.Fluid),
//    FLUID_POWER_OPAQUE(13, "fluidPoweredOpaqueDuct", Type.Fluid),
//    ITEM_ENDER_TRANS(14, "itemEnderTransDuct", Type.Item),
//    ITEM_ENDER_OPAQUE(15, "itemEnderOpaqueDuct", Type.Item),
    ;

    public final static Ducts[] ductList;

    static {
        int n = 0;
        for (Ducts d : values()) if (n < d.id) n = d.id;
        ductList = new Ducts[n + 1];
        for (Ducts d : values()) {
            if (d.id >= 0) {
                ductList[d.id] = d;
            }
        }
    }

    public ItemStack itemStack = null;

    public final int id;
    public final String unlocalizedName;
    public final Type type;

    private Ducts(int id, String name, Type type) {
        this.id = id;
        this.type = type;
        this.unlocalizedName = name;
    }

    public static boolean isValid(int id) {
        return id < ductList.length && ductList[id] != null;
    }

    public static Ducts getType(int id) {
        return ductList[id];
    }

    public static enum Type {
        Item, Fluid, Energy, Players, Structural
    }


}
