package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.debughelper.DebugHelper;
import cofh.thermaldynamics.duct.Duct.Type;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import cofh.thermaldynamics.duct.entity.TileTransportDuctCrossover;
import cofh.thermaldynamics.duct.entity.TileTransportDuctLongRange;
import cofh.thermaldynamics.duct.light.DuctLight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;

public class TDDucts {

    private TDDucts() {

    }

    public static ArrayList<Duct> ductList = new ArrayList<Duct>();
    public static ArrayList<Duct> ductListSorted = null;

    static Duct addDuct(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
                        String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

        Duct newDuct = new Duct(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency,
                frameTexture, frameFluidTexture, frameFluidTransparency);

        return registerDuct(newDuct);
    }

    static DuctItem addDuctItem(int id, boolean opaque, int pathWeight, int type, String name, Type ductType, DuctFactory factory, String baseTexture,
                                String connectionTexture, String fluidTexture, int fluidTransparency, String frameTexture, String frameFluidTexture, int frameFluidTransparency) {

        DuctItem newDuct = new DuctItem(id, opaque, pathWeight, type, name, ductType, factory, baseTexture, connectionTexture, fluidTexture, fluidTransparency,
                frameTexture, frameFluidTexture, frameFluidTransparency);

        return registerDuct(newDuct);
    }

    static <T extends Duct> T registerDuct(T newDuct) {

        int id = newDuct.id;
        while (id >= ductList.size()) {
            ductList.add(null);
        }
        Duct oldDuct = ductList.set(id, newDuct);
        if (oldDuct != null) {
            ThermalDynamics.log.info("Replacing " + oldDuct.unlocalizedName + " with " + newDuct.unlocalizedName);
        }
        return newDuct;
    }

    public static Duct getDuct(int id) {

        if (isValid(id)) {
            return ductList.get(id);
        } else {
            return structure;
        }
    }

    public static List<Duct> getSortedDucts() {

        if (ductListSorted == null) {
            ductListSorted = new ArrayList<Duct>();
            for (Duct duct : ductList) {
                if (duct != null) {
                    ductListSorted.add(duct);
                }
            }
            Collections.sort(ductListSorted, new Comparator<Duct>() {

                @Override
                public int compare(Duct o1, Duct o2) {

                    int i = o1.ductType.compareTo(o2.ductType);
                    if (i == 0) {
                        i = o1.compareTo(o2);
                    }
                    return i;
                }
            });
        }
        return ductListSorted;
    }

    public static boolean isValid(int id) {

        return id < ductList.size() && ductList.get(id) != null;
    }

    public static Duct getType(int id) {

        return ductList.get(id) != null ? ductList.get(id) : structure;
    }

    public static boolean addDucts() {

        addEnergyDucts();
        addFluidDucts();
        addItemDucts();
        addSupportDucts();

        if (DebugHelper.debug) {
            addIndevDucts();
        }
        return true;
    }

    private static void addIndevDucts() {

        registerDuct(new Duct(4 * 16, false, 1, 4, "entityTransport", Type.ENTITY, new DuctFactory() {

            @Override
            public TileTDBase createTileEntity(Duct duct, World worldObj) {

                return new TileTransportDuct();
            }
        }, null, null, null, 255, "electrum", "thermaldynamics:duct/base/greenGlass",
                96
        ) {
            @Override
            public void registerIcons(IIconRegister ir) {
                super.registerIcons(ir);
                frameType = 4;
            }
        });


        registerDuct(new Duct(4 * 16 + 1, false, 1, 4, "entityTransportLongRange", Type.ENTITY, new DuctFactory() {

            @Override
            public TileTDBase createTileEntity(Duct duct, World worldObj) {

                return new TileTransportDuctLongRange();
            }
        }, null, null, null, 255, "copper", "thermaldynamics:duct/base/greenGlass",
                80
        ) {
            @Override
            public void registerIcons(IIconRegister ir) {
                super.registerIcons(ir);
                frameType = 4;
            }
        });

        registerDuct(new Duct(4 * 16 + 2, false, 1, 4, "entityTransportAcceleration", Type.ENTITY, new DuctFactory() {

            @Override
            public TileTDBase createTileEntity(Duct duct, World worldObj) {

                return new TileTransportDuctCrossover();
            }
        }, null, null, null, 255, "enderium", "thermaldynamics:duct/base/greenGlass",
                128
        ) {
            @Override
            public void registerIcons(IIconRegister ir) {
                super.registerIcons(ir);
                frameType = 4;
            }
        });


    }

    static void addEnergyDucts() {

        energyBasic = addDuct(OFFSET_ENERGY + 0, false, 1, 0, "energyBasic", Type.ENERGY, DuctFactory.energy, "lead", "lead", Duct.REDSTONE_BLOCK, 255, null,
                null, 0);

        energyHardened = addDuct(OFFSET_ENERGY + 1, false, 1, 1, "energyHardened", Type.ENERGY, DuctFactory.energy, "invar", "invar", Duct.REDSTONE_BLOCK, 255,
                null, null, 0);

        energyReinforced = addDuct(OFFSET_ENERGY + 2, false, 1, 2, "energyReinforced", Type.ENERGY, DuctFactory.energy, "electrum", "electrum",
                "thermalfoundation:fluid/Fluid_Redstone_Still", 128, null, null, 0);

        energyReinforcedEmpty = addDuct(OFFSET_ENERGY + 3, false, 1, -1, "energyReinforcedEmpty", Type.CRAFTING, DuctFactory.structural, "electrum",
                "electrum", null, 0, null, null, 0);

        energyResonant = addDuct(OFFSET_ENERGY + 4, false, 1, 3, "energyResonant", Type.ENERGY, DuctFactory.energy, "enderium", "enderium",
                "thermalfoundation:fluid/Fluid_Redstone_Still", 128, null, null, 0);
        energyResonantEmpty = addDuct(OFFSET_ENERGY + 5, false, 1, -1, "energyResonantEmpty", Type.CRAFTING, DuctFactory.structural, "enderium", "enderium",
                null, 0, null, null, 0);

        energySuperCond = addDuct(OFFSET_ENERGY + 6, false, 1, 4, "energySuperconductor", Type.ENERGY, DuctFactory.energy_super, "electrum", "electrum",
                "thermalfoundation:fluid/Fluid_Redstone_Still", 255, "electrum", "thermalfoundation:fluid/Fluid_Cryotheum_Still", 96);
        energySuperCondEmpty = addDuct(OFFSET_ENERGY + 7, false, 1, -1, "energySuperconductorEmpty", Type.CRAFTING, DuctFactory.structural, "electrum",
                "electrum", "thermalfoundation:fluid/Fluid_Redstone_Still", 192, "electrum", null, 0);

        energyReinforced.setRarity(1);
        energyReinforcedEmpty.setRarity(1);
        energyResonant.setRarity(2);
        energyResonantEmpty.setRarity(2);
        energySuperCond.setRarity(2);
        energySuperCondEmpty.setRarity(2);
    }

    static void addFluidDucts() {

        fluidBasic = addDuct(OFFSET_FLUID + 0, false, 1, 0, "fluidBasic", Type.FLUID, DuctFactory.fluid_fragile, "copper", "copper", null, 0, null, null, 0);
        fluidBasicOpaque = addDuct(OFFSET_FLUID + 1, true, 1, 0, "fluidBasic", Type.FLUID, DuctFactory.fluid_fragile, "copper", "copper", null, 0, null, null,
                0);

        fluidHardened = addDuct(OFFSET_FLUID + 2, false, 1, 1, "fluidHardened", Type.FLUID, DuctFactory.fluid, "invar", "invar", null, 0, null, null, 0);
        fluidHardenedOpaque = addDuct(OFFSET_FLUID + 3, true, 1, 1, "fluidHardened", Type.FLUID, DuctFactory.fluid, "invar", "invar", null, 0, null, null, 0);

        fluidFlux = addDuct(OFFSET_FLUID + 4, false, 1, 2, "fluidFlux", Type.FLUID, DuctFactory.fluid_flux, "fluxElectrum", "fluxElectrum", null, 0, null,
                null, 0);
        fluidFluxOpaque = addDuct(OFFSET_FLUID + 5, true, 1, 2, "fluidFlux", Type.FLUID, DuctFactory.fluid_flux, "fluxElectrum", "fluxElectrum", null, 0, null,
                null, 0);

        fluidSuper = addDuct(OFFSET_FLUID + 6, false, 1, 3, "fluidSuper", Type.FLUID, DuctFactory.fluid_super, "invar", "invar", null, 0, "bronze_large", null,
                0);

        fluidSuperOpaque = addDuct(OFFSET_FLUID + 7, true, 1, 3, "fluidSuper", Type.FLUID, DuctFactory.fluid_super, "invar", "invar", null, 0, "bronze_large",
                null, 0);

        fluidHardened.setRarity(1);
        fluidHardenedOpaque.setRarity(1);

        fluidFlux.setRarity(1);
        fluidFluxOpaque.setRarity(1);

        fluidSuper.setRarity(2);
        fluidSuperOpaque.setRarity(2);
    }

    static void addItemDucts() {

        itemBasic = addDuctItem(OFFSET_ITEM + 0, false, 1, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);
        itemBasicOpaque = addDuctItem(OFFSET_ITEM + 1, true, 1, 0, "itemBasic", Type.ITEM, DuctFactory.item, "tin", "tin", null, 0, null, null, 0);

        itemFast = addDuctItem(OFFSET_ITEM + 2, false, 1, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin", "tin",
                "thermalfoundation:fluid/Fluid_Glowstone_Still", 128, null, null, 0);
        itemFastOpaque = addDuctItem(OFFSET_ITEM + 3, true, 1, 1, "itemFast", Type.ITEM, DuctFactory.item, "tin_1", "tin", null, 0, null, null, 0);

        itemEnder = addDuctItem(OFFSET_ITEM + 4, false, 0, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48, null, null, 0);
        itemEnderOpaque = addDuctItem(OFFSET_ITEM + 5, true, 0, 2, "itemEnder", Type.ITEM, DuctFactory.item_ender, "enderium", "enderium", null, 48, null,
                null, 0);

        itemEnergy = addDuctItem(OFFSET_ITEM + 6, false, 1, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin", "tin",
                "thermalfoundation:fluid/Fluid_Redstone_Still", 48, null, null, 0);

        itemEnergyOpaque = addDuctItem(OFFSET_ITEM + 7, true, 1, 3, "itemFlux", Type.ITEM, DuctFactory.item_flux, "tin_2", "tin", null, 0, null, null, 0);

        itemFast.setRarity(1);
        itemFastOpaque.setRarity(1);

        itemEnder.setRarity(2);
        itemEnderOpaque.setRarity(2);

        itemEnergy.setRarity(1);
        itemEnergyOpaque.setRarity(1);
    }

    static void addSupportDucts() {

        structure = addDuct(OFFSET_STRUCTURE + 0, true, 1, -1, "structure", Type.STRUCTURAL, DuctFactory.structural, "support", null, null, 0, null, null, 0);

        lightDuct = registerDuct(new DuctLight(OFFSET_STRUCTURE + 1, 0, "structureGlow", Type.STRUCTURAL, DuctFactory.glow, "lumium", "lumium", null, 0));
    }

    public static int OFFSET_ENERGY = 0 * 16;
    public static int OFFSET_FLUID = 1 * 16;
    public static int OFFSET_ITEM = 2 * 16;
    public static int OFFSET_STRUCTURE = 3 * 16;

    /* ENERGY */
    public static Duct energyBasic;

    public static Duct energyHardened;

    public static Duct energyReinforced;
    public static Duct energyReinforcedEmpty;

    public static Duct energyResonant;
    public static Duct energyResonantEmpty;

    public static Duct energySuperCond;
    public static Duct energySuperCondEmpty;

    /* FLUID */
    public static Duct fluidBasic;
    public static Duct fluidBasicOpaque;

    public static Duct fluidHardened;
    public static Duct fluidHardenedOpaque;

    public static Duct fluidFlux;
    public static Duct fluidFluxOpaque;

    public static Duct fluidSuper;
    public static Duct fluidSuperOpaque;

    /* ITEM */
    public static DuctItem itemBasic;
    public static DuctItem itemBasicOpaque;

    public static DuctItem itemFast;
    public static DuctItem itemFastOpaque;

    public static DuctItem itemEnder;
    public static DuctItem itemEnderOpaque;

    public static DuctItem itemEnergy;
    public static DuctItem itemEnergyOpaque;

    /* STRUCTURE */
    public static Duct structure;

    public static DuctLight lightDuct;

    /* HELPERS - NOT REAL */
    public static Duct structureInvis = new Duct(-1, false, 1, -1, "structure", Type.STRUCTURAL, DuctFactory.structural, "support", null, null, 0, null, null,
            0);

    public static Duct placeholder = new Duct(-1, false, 1, -1, "structure", Type.STRUCTURAL, DuctFactory.structural, "support", null, null, 0, null, null, 0);

}
