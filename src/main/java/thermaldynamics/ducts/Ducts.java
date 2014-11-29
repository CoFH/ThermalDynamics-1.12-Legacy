package thermaldynamics.ducts;


import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.energy.TileEnergyDuct;
import thermaldynamics.ducts.energy.TileEnergyDuctSuperConductor;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.fluid.TileFluidDuctFragile;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TileItemDuctEnder;
import thermaldynamics.ducts.item.TileItemDuctRedstone;
import thermaldynamics.render.TextureTransparent;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public enum Ducts {

    ENERGY_BASIC(0, true, 0, "energyBasicDuct", Type.Energy, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileEnergyDuct();
        }
    }, "thermaldynamics:duct/energy/DuctEnergy00", "thermaldynamics:duct/energy/ConnectionEnergy00", "thermaldynamics:duct/energy/redstone_noise", 255, null, null, 255),

    ENERGY_HARDENED(1, true, 1, "energyHardenedDuct", Type.Energy, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileEnergyDuct();
        }
    }, "thermaldynamics:duct/energy/DuctEnergy10", "thermaldynamics:duct/energy/ConnectionEnergy10", "thermaldynamics:duct/energy/redstone_noise", 255, null, null, 0),

    ENERGY_REINFORCED(2, true, 2, "energyReinforcedDuct", Type.Energy, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileEnergyDuct();
        }
    }, "thermaldynamics:duct/energy/DuctEnergy20", "thermaldynamics:duct/energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, null, null, 0),

    FLUID_TRANS(3, false, 0, "fluidWeakTransDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileFluidDuctFragile();
        }
    }, "thermaldynamics:duct/fluid/DuctFluid00", "thermaldynamics:duct/fluid/ConnectionFluid00", null, 0, null, null, 0),

    FLUID_OPAQUE(4, true, 0, "fluidWeakOpaqueDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileFluidDuctFragile();
        }
    }, "thermaldynamics:duct/fluid/DuctFluid01", "thermaldynamics:duct/fluid/ConnectionFluid00", null, 0, null, null, 0),

    FLUID_HARDENED_TRANS(11, false, 1, "fluidTransDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileFluidDuct();
        }
    }, "thermaldynamics:duct/fluid/DuctFluid10", "thermaldynamics:duct/fluid/ConnectionFluid10", null, 0, null, null, 0),

    FLUID_HARDENED_OPAQUE(12, true, 1, "fluidOpaqueDuct", Type.Fluid, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileFluidDuct();
        }
    }, "thermaldynamics:duct/fluid/DuctFluid11", "thermaldynamics:duct/fluid/ConnectionFluid11", null, 0, null, null, 0),

    ITEM_TRANS(5, false, 0, "itemTransDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileItemDuct();
        }
    }, "thermaldynamics:duct/item/DuctItem00", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_OPAQUE(6, true, 0, "itemOpaqueDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileItemDuct();
        }
    }, "thermaldynamics:duct/item/DuctItem01", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_FAST_TRANS(7, false, 1, "itemFastTransDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileItemDuct();
        }
    }, "thermaldynamics:duct/item/DuctItem00", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0),

    ITEM_FAST_OPAQUE(8, true, 1, "itemFastOpaqueDuct", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileItemDuct();
        }
    }, "thermaldynamics:duct/item/DuctItem11", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ENERGY_REINFORCED_EMPTY(9, true, -1, "energyEmptyReinforcedDuct", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/energy/DuctEnergy20", "thermaldynamics:duct/energy/ConnectionEnergy20", null, 0, null, null, 0),

    STRUCTURE(10, true, -1, "structureDuct", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/structure", null, null, 0, null, null, 0),

    ENERGY_SUPERCONDUCTOR(13, true, 3, "energySuperconductorDuct", Type.Energy,
            new DuctFactory() {
                @Override
                public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                    return new TileEnergyDuctSuperConductor();
                }
            }
            , "thermaldynamics:duct/energy/DuctEnergy20", "thermaldynamics:duct/energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192,
            "thermaldynamics:duct/overduct/OverDuctElectrum", "thermalfoundation:fluid/Fluid_Cryotheum_Still", 72
    ),

    ENERGY_SUPERCONDUCTOR_EMPTY(14, true, -1, "energySuperconductorEmptyDuct", Type.Energy, DuctFactory.structural
            , "thermaldynamics:duct/energy/DuctEnergy20", "thermaldynamics:duct/energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192,
            "thermaldynamics:duct/overduct/OverDuctElectrum", null, 0
    ),

    ENDERIUM(15, false, 2, "test2", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileItemDuctEnder();
        }
    }, "thermaldynamics:duct/test/DuctEnderium0", null,
            //"thermalfoundation:fluid/Fluid_Ender_Still"
            null
            , 48, null, null, 0
    ),


    ENDERIUM_OPAQUE(16, true, 2, "itemOpaqueDuctEnder", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileItemDuctEnder();
        }
    }, "thermaldynamics:duct/test/DuctEnderium1", null,
//            "thermalfoundation:fluid/Fluid_Ender_Still"
            null, 48, null, null, 0
    ),

    ITEM_TRANS_REDSTONE(17, false, 0, "itemTransDuctRedstone", Type.Item, new DuctFactory() {
        @Override
        public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
            return new TileItemDuctRedstone();
        }
    }, "thermaldynamics:duct/item/DuctItem00", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Redstone_Still", 128, null, null, 0),



    //BRONZE(13, "test0", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctBronze0", null, "thermalfoundation:fluid/Fluid_Pyrotheum_Still", 238),
    //COPPER(14, "test1", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctCopper0", null, "thermalfoundation:fluid/Fluid_Coal_Still", 185, null, null, 0),

//    LUMINIUM(16, "test3", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctLumium0", null, "thermalfoundation:fluid/Fluid_Cryotheum_Still", 180, null, null, 0),
//    NICKEL(17, "test4", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctNickel0", null, "thermalexpansion:light/Illuminator_Effect", 160, null, null, 0),
//    PLATINUM(18, "test5", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctPlatinum0", null, "thermalfoundation:fluid/Fluid_Ender_Still", 188, null, null, 0),
//    SIGNALUM(19, "test6", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctSignalum0", null, "thermalfoundation:fluid/Fluid_Redstone_Still", 255, null, null, 0),
//    SILVER(20, "test7", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctSilver0", null, "thermalfoundation:fluid/Fluid_Steam_Still", 128, null, null, 0),
//
//    BRONZE1(21, "test0", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctBronze1", null, null, 0, null, null, 0),
//    COPPER1(22, "test1", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctCopper1", null, null, 0, null, null, 0),
//    ENDERIUM1(23, "test2", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctEnderium1", null, null, 0, null, null, 0),
//    LUMINIUM1(24, "test3", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctLumium1", null, null, 0, null, null, 0),
//    NICKEL1(25, "test4", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctNickel1", null, null, 0, null, null, 0),
//    PLATINUM1(26, "test5", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctPlatinum1", null, null, 0, null, null, 0),
//    SIGNALUM1(27, "test6", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctSignalum1", null, null, 0, null, null, 0),
//    SILVER1(28, "test7", Type.Structural, DuctFactory.structural, "thermaldynamics:duct/test/DuctSilver1", null, null, 0, null, null, 0),

//    ENERGY_SUPERCONDUCTOR(9, "energySuperConductorDuct", Type.Energy),
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
                    throw new RuntimeException("Duplicate IDs. Tema you moron.");
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

    public IIcon iconBaseTexture;
    public IIcon iconConnectionTexture;
    public IIcon iconFluidTexture;
    public IIcon iconOverDuctTexture;
    public IIcon iconOverDuctInternalTexture;

    public final int id;
    public final String unlocalizedName;
    public final Type ductType;
    public final DuctFactory factory;
    public final String baseTexture;
    public final String connectionTexture;
    public final String fluidTexture;
    public final byte fluidTransparency;
    public final String overDuct;
    public final String overDuct2;
    public final byte overDuct2Trans;
    public final boolean opaque;
    public final int type;

    private Ducts(int id, boolean opaque, int type, String name, Type ductType, DuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency, String overDuct, String overDuct2, int overDuct2Trans) {
        this.id = id;
        this.ductType = ductType;
        this.opaque = opaque;
        this.type = type;
        this.unlocalizedName = name;
        this.factory = factory;
        this.baseTexture = baseTexture;
        this.connectionTexture = connectionTexture;
        this.fluidTexture = fluidTexture;
        this.fluidTransparency = (byte) fluidTransparency;
        this.overDuct = overDuct;
        this.overDuct2 = overDuct2;
        this.overDuct2Trans = (byte) overDuct2Trans;
    }


    public static boolean isValid(int id) {
        return id < ductList.length && ductList[id] != null;
    }

    public static Ducts getType(int id) {
        return ductList[id];
    }

    public static LinkedList<Ducts> ductsSorted = null;

    public static List<Ducts> getSortedDucts() {
        if (ductsSorted == null) {
            ductsSorted = new LinkedList<Ducts>();
            for (Ducts duct : ductList)
                if (duct != null)
                    ductsSorted.add(duct);

            Collections.sort(ductsSorted, new Comparator<Ducts>() {
                @Override
                public int compare(Ducts o1, Ducts o2) {
                    int i = o1.ductType.compareTo(o2.ductType);
                    if (i == 0) i = o1.compareTo(o2);
                    return i;
                }
            });
        }
        return ductsSorted;
    }

    public void registerIcons(IIconRegister ir) {
        iconBaseTexture = ir.registerIcon(baseTexture);
        if (connectionTexture != null)
            iconConnectionTexture = ir.registerIcon(connectionTexture);
        if (fluidTexture != null)
            iconFluidTexture = TextureTransparent.registerTransparentIcon(ir, fluidTexture, fluidTransparency);
        if (overDuct != null)
            iconOverDuctTexture = ir.registerIcon(overDuct);
        if (overDuct2 != null)
            iconOverDuctInternalTexture = TextureTransparent.registerTransparentIcon(ir, overDuct2, overDuct2Trans);
    }

    public boolean isLargeTube() {
        return overDuct != null | overDuct2 != null;
    }


    public static enum Type {
        Item, Fluid, Energy, Players, Structural
    }


    public static abstract class DuctFactory {
        public static DuctFactory structural = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileStructuralDuct();
            }
        };

        public abstract TileMultiBlock createTileEntity(Ducts duct, World worldObj);
    }
}
