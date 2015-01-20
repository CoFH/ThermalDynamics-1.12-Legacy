package thermaldynamics.ducts;


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
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
import thermaldynamics.render.TextureOverlay;
import thermaldynamics.render.TextureTransparent;

public enum Ducts {

    ENERGY_BASIC(0, false, 1, 0, "energyBasicDuct", Type.Energy, DuctFactory.energy, "lead", "thermaldynamics:duct/energy/ConnectionEnergy00", Constants.redstone_block, 255, null, null, 255),

    ENERGY_HARDENED(1, false, 1, 1, "energyHardenedDuct", Type.Energy, DuctFactory.energy, "invar", "thermaldynamics:duct/energy/ConnectionEnergy10", Constants.redstone_block, 255, null, null, 0),

    ENERGY_REINFORCED(2, false, 1, 2, "energyReinforcedDuct", Type.Energy, DuctFactory.energy, "electrum", "thermaldynamics:duct/energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, null, null, 0),

    FLUID_TRANS(3, false, 1, 0, "fluidWeakDuct", Type.Fluid, DuctFactory.fluid_fragile, "copper", "thermaldynamics:duct/fluid/ConnectionFluid00", null, 0, null, null, 0),

    FLUID_OPAQUE(4, true, 1, 0, "fluidWeakDuct", Type.Fluid, DuctFactory.fluid_fragile, "copper", "thermaldynamics:duct/fluid/ConnectionFluid00", null, 0, null, null, 0),

    FLUID_HARDENED_TRANS(11, false, 1, 1, "fluidDuct", Type.Fluid, DuctFactory.fluid, "invar", "thermaldynamics:duct/fluid/ConnectionFluid10", null, 0, null, null, 0),

    FLUID_HARDENED_OPAQUE(12, true, 1, 1, "fluidDuct", Type.Fluid, DuctFactory.fluid, "invar", "thermaldynamics:duct/fluid/ConnectionFluid11", null, 0, null, null, 0),


    ENERGY_REINFORCED_EMPTY(9, false, 1, -1, "energyEmptyReinforcedDuct", Type.Structural, DuctFactory.structural, "electrum", "thermaldynamics:duct/energy/ConnectionEnergy20", null, 0, null, null, 0),

    STRUCTURE(10, true, 1, -1, "structureDuct", Type.Structural, DuctFactory.structural, "support", null, null, 0, null, null, 0),

    ENERGY_SUPERCONDUCTOR(13, false, 1, 3, "energySuperconductorDuct", Type.Energy, DuctFactory.energy_super, "electrum", "thermaldynamics:duct/energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, "thermaldynamics:duct/overduct/OverDuctElectrum", "thermalfoundation:fluid/Fluid_Cryotheum_Still", 72),

    ENERGY_SUPERCONDUCTOR_EMPTY(14, false, 1, -1, "energySuperconductorEmptyDuct", Type.Energy, DuctFactory.structural, "electrum", "thermaldynamics:duct/energy/ConnectionEnergy20", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, "thermaldynamics:duct/overduct/OverDuctElectrum", null, 0),


    ITEM_TRANS(5, false, 1, 0, "itemDuct", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_OPAQUE(6, true, 1, 0, "itemDuct", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_FAST_TRANS(7, false, 1, 1, "itemDuctFast", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0),

    ITEM_FAST_OPAQUE(8, true, 1, 1, "itemDuctFast", Type.Item, DuctFactory.item, "tin_1", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_ENDERIUM_TRANS(15, false, 0, 2, "itemDuctEnder", Type.Item, DuctFactory.item_ender, "enderium", "thermaldynamics:duct/item/ConnectionItem20", null, 48, null, null, 0),

    ITEM_ENDERIUM_OPAQUE(16, true, 0, 2, "itemDuctEnder", Type.Item, DuctFactory.item_ender, "enderium", "thermaldynamics:duct/item/ConnectionItem20", null, 48, null, null, 0),

    ITEM_REDSTONE_TRANS(17, false, 1, 3, "itemDuctRedstone", Type.Item, DuctFactory.item_redstone, "tin", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0),

    ITEM_REDSTONE_OPAQUE(18, true, 1, 3, "itemDuctRedstone", Type.Item, DuctFactory.item_redstone, "tin_2", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    STRUCTURE_TRANS(19, false, 1, -1, "structureDuct", Type.Structural, DuctFactory.structural, "support", null, null, 0, null, null, 0),


    ITEM_TRANS_DENSE(20, false, 1000, 0, "itemDuct", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_OPAQUE_DENSE(21, true, 1000, 0, "itemDuct", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_FAST_TRANS_DENSE(22, false, 1000, 1, "itemDuctFast", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0),

    ITEM_FAST_OPAQUE_DENSE(23, true, 1000, 1, "itemDuctFast", Type.Item, DuctFactory.item, "tin_1", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_ENDERIUM_TRANS_DENSE(24, false, 1000, 2, "itemDuctEnder", Type.Item, DuctFactory.item_ender, "enderium", "thermaldynamics:duct/item/ConnectionItem20", null, 48, null, null, 0),

    ITEM_ENDERIUM_OPAQUE_DENSE(25, true, 1000, 2, "itemDuctEnder", Type.Item, DuctFactory.item_ender, "enderium", "thermaldynamics:duct/item/ConnectionItem20", null, 48, null, null, 0),

    ITEM_REDSTONE_TRANS_DENSE(26, false, 1000, 3, "itemDuctRedstone", Type.Item, DuctFactory.item_redstone, "tin", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0),

    ITEM_REDSTONE_OPAQUE_DENSE(27, true, 1000, 3, "itemDuctRedstone", Type.Item, DuctFactory.item_redstone, "tin_2", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),


    ITEM_TRANS_VACUUM(28, false, -1000, 0, "itemDuct", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_OPAQUE_VACUUM(29, true, -1000, 0, "itemDuct", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_FAST_TRANS_VACUUM(30, false, -1000, 1, "itemDuctFast", Type.Item, DuctFactory.item, "tin", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0),

    ITEM_FAST_OPAQUE_VACUUM(31, true, -1000, 1, "itemDuctFast", Type.Item, DuctFactory.item, "tin_1", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),

    ITEM_ENDERIUM_TRANS_VACUUM(32, false, -1000, 2, "itemDuctEnder", Type.Item, DuctFactory.item_ender, "enderium", "thermaldynamics:duct/item/ConnectionItem20", null, 48, null, null, 0),

    ITEM_ENDERIUM_OPAQUE_VACUUM(33, true, -1000, 2, "itemDuctEnder", Type.Item, DuctFactory.item_ender, "enderium", "thermaldynamics:duct/item/ConnectionItem20", null, 48, null, null, 0),

    ITEM_REDSTONE_TRANS_VACUUM(34, false, -1000, 3, "itemDuctRedstone", Type.Item, DuctFactory.item_redstone, "tin", "thermaldynamics:duct/item/ConnectionItem00", "thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0),

    ITEM_REDSTONE_OPAQUE_VACUUM(35, true, -1000, 3, "itemDuctRedstone", Type.Item, DuctFactory.item_redstone, "tin_2", "thermaldynamics:duct/item/ConnectionItem00", null, 0, null, null, 0),;

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
    public final int pathWeight;
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


    private Ducts(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture, String connectionTexture, String fluidTexture, int fluidTransparency, String overDuct, String overDuct2, int overDuct2Trans) {
        this.id = id;
        this.pathWeight = pathWeight;
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
        //iconBaseTexture = ir.registerIcon(baseTexture);

        iconBaseTexture = TextureOverlay.generateTexture(ir, baseTexture
                , opaque ? null : "trans",
                pathWeight == 1000 ? "dense" : pathWeight == -1000 ? "vacuum" : null
        );

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

        public static DuctFactory item = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileItemDuct();
            }
        };

        public static DuctFactory item_ender = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileItemDuctEnder();
            }
        };

        public static DuctFactory item_redstone = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileItemDuctRedstone();
            }
        };

        public static DuctFactory energy = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileEnergyDuct();
            }
        };

        public static DuctFactory energy_super = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileEnergyDuctSuperConductor();
            }
        };

        public static DuctFactory fluid = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileFluidDuct();
            }
        };

        public static DuctFactory fluid_fragile = new DuctFactory() {
            @Override
            public TileMultiBlock createTileEntity(Ducts duct, World worldObj) {
                return new TileFluidDuctFragile();
            }
        };


        public abstract TileMultiBlock createTileEntity(Ducts duct, World worldObj);
    }

    private static class Constants {
        public static final String redstone_block = "minecraft:redstone_block";

    }
}
