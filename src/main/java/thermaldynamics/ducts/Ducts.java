package thermaldynamics.ducts;


import net.minecraft.item.ItemStack;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.energy.TileEnergyDuct;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.fluid.TileFluidDuctFragile;
import thermaldynamics.ducts.item.TileItemDuct;

public enum Ducts {

    ENERGY_BASIC(0, "energyBasicDuct", Type.Energy, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileEnergyDuct(0);
        }
    }),

    ENERGY_HARDENED(1, "energyHardenedDuct", Type.Energy, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileEnergyDuct(1);
        }
    }),

    ENERGY_REINFORCED(2, "energyReinforcedDuct", Type.Energy, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileEnergyDuct(2);
        }
    }),

    FLUID_TRANS(3, "fluidWeakTransDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileFluidDuctFragile(0, false);
        }
    }),

    FLUID_OPAQUE(4, "fluidWeakOpaqueDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileFluidDuctFragile(0, true);
        }
    }),

    FLUID_HARDENED_TRANS(11, "fluidTransDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileFluidDuct(1, false);
        }
    }),

    FLUID_HARDENED_OPAQUE(12, "fluidOpaqueDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileFluidDuct(1, true);
        }
    }),

    ITEM_TRANS(5, "itemTransDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileItemDuct(0);
        }
    }),

    ITEM_OPAQUE(6, "itemOpaqueDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileItemDuct(1);
        }
    }),

    ITEM_FAST_TRANS(7, "itemFastTransDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileItemDuct(2);
        }
    }),

    ITEM_FAST_OPAQUE(8, "itemFastOpaqueDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileItemDuct(3);
        }
    }),

    ENERGY_REINFORCED_EMPTY(9, "energyEmptyReinforcedDuct", Type.Structural, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileStructuralDuct();
        }
    }),

    STRUCTURE(10, "structureDuct", Type.Structural, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity() {
            return new TileStructuralDuct();
        }
    }),

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
        for (Ducts d : values()) if (d.factory != null && n < d.id) n = d.id;
        ductList = new Ducts[n + 1];
        for (Ducts d : values()) {
            if (d.id >= 0) {
                if (ductList[d.id] != null)
                    throw new RuntimeException("Duplicate IDs");
                ductList[d.id] = d;
            }
        }
    }

    public static Ducts getDuct(int id) {
        if (isValid(id))
            return ductList[id];
        else
            return STRUCTURE;
    }

    public ItemStack itemStack = null;

    public final int id;
    public final String unlocalizedName;
    public final Type type;
    public final DuctFactory factory;

    private Ducts(int id, String name, Type type, DuctFactory factory) {
        this.id = id;
        this.type = type;
        this.unlocalizedName = name;
        this.factory = factory;
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

    public static abstract class DuctFactory {
        public abstract TileMultiBlock createTileEntity();
    }
}
