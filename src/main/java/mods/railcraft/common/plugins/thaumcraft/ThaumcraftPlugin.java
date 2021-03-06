/*******************************************************************************
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/
package mods.railcraft.common.plugins.thaumcraft;

import mods.railcraft.common.blocks.RailcraftBlocks;
import mods.railcraft.common.blocks.aesthetics.brick.BrickTheme;
import mods.railcraft.common.blocks.aesthetics.brick.BrickVariant;
import mods.railcraft.common.blocks.aesthetics.generic.EnumGeneric;
import mods.railcraft.common.blocks.detector.EnumDetector;
import mods.railcraft.common.blocks.machine.alpha.EnumMachineAlpha;
import mods.railcraft.common.blocks.machine.beta.EnumMachineBeta;
import mods.railcraft.common.blocks.ore.EnumOre;
import mods.railcraft.common.carts.ICartType;
import mods.railcraft.common.carts.RailcraftCarts;
import mods.railcraft.common.items.ItemDust;
import mods.railcraft.common.items.ItemGear.EnumGear;
import mods.railcraft.common.items.ItemRail.EnumRail;
import mods.railcraft.common.items.ItemTie.EnumTie;
import mods.railcraft.common.items.Metal;
import mods.railcraft.common.items.RailcraftItems;
import mods.railcraft.common.items.firestone.ItemFirestoneCracked;
import mods.railcraft.common.items.firestone.ItemFirestoneRefined;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import mods.railcraft.common.plugins.color.EnumColor;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.misc.ItemStackCache;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftMaterials;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchPage;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class ThaumcraftPlugin {
    public static final ItemStackCache ITEMS = new ItemStackCache("Thaumcraft", ItemsTC.class, ThaumcraftPlugin::isModInstalled, tag -> {
        try {
            return new ItemStack((Item) ItemsTC.class.getField(tag).get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (Game.IS_DEBUG)
                throw new RuntimeException(e);
            return null;
        }
    });
    public static final ItemStackCache BLOCKS = new ItemStackCache("Thaumcraft", BlocksTC.class, ThaumcraftPlugin::isModInstalled, tag -> {
        try {
            return new ItemStack((Block) BlocksTC.class.getField(tag).get(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            if (Game.IS_DEBUG)
                throw new RuntimeException(e);
            return null;
        }
    });
    public static final String RESEARCH_CATEGORY = "RAILCRAFT";
    private static Map<String, ResearchPage> researchPages = new HashMap<String, ResearchPage>();
    private static Boolean modLoaded;

    public static void setupResearch() {
        ResearchCategories.registerCategory(RESEARCH_CATEGORY, null, new ResourceLocation("railcraft", "textures/items/tool.crowbar.magic.png"), new ResourceLocation("thaumcraft", "textures/gui/gui_researchback.png"));
    }

    private static ResearchPage createResearchPage(String key, int pageNum) {
        return new ResearchPage(LocalizationPlugin.translate(String.format("thaumcraft.research.%s.page.%d", key, pageNum)).replace("\n", "<BR>").replace("---", "<LINE>").replace("{img}", "<IMG>").replace("{/img}", "</IMG>"));
    }

    public static ResearchPage getResearchPage(String researchTag) {
        ResearchPage page = researchPages.get(researchTag);
        if (page == null) {
            page = createResearchPage(researchTag, 1);
            researchPages.put(researchTag, page);
        }
        return page;
    }

    public static void registerAspects() {
        try {
            AspectList anchorAspects = new AspectList().add(Aspect.ELDRITCH, 4).add(Aspect.ORDER, 4).add(Aspect.AURA, 2).add(Aspect.DESIRE, 2);
            AspectList steamAspects = new AspectList().add(Aspect.WATER, 3).add(Aspect.MECHANISM, 2).add(Aspect.FIRE, 3);
            AspectList tankAspects = new AspectList().add(Aspect.VOID, 4).add(Aspect.WATER, 4);

            addBrickAspects(BrickTheme.ABYSSAL, Aspect.DARKNESS);
            addBrickAspects(BrickTheme.BLEACHEDBONE, Aspect.DEATH);
            addBrickAspects(BrickTheme.BLOODSTAINED, Aspect.LIFE);
            addBrickAspects(BrickTheme.FROSTBOUND, Aspect.COLD);
            addBrickAspects(BrickTheme.INFERNAL, 2, Aspect.FIRE, Aspect.SOUL);
            addBrickAspects(BrickTheme.NETHER, Aspect.FIRE);
            addBrickAspects(BrickTheme.QUARRIED, Aspect.LIGHT);
            addBrickAspects(BrickTheme.SANDY, Aspect.EARTH);

            addItemAspect(EnumGeneric.STONE_ABYSSAL.getStack(), new AspectList().add(Aspect.EARTH, 4).add(Aspect.DARKNESS, 2));
            addItemAspect(EnumGeneric.STONE_QUARRIED.getStack(), new AspectList().add(Aspect.EARTH, 4).add(Aspect.LIGHT, 2));
            addItemAspect(EnumGeneric.CRUSHED_OBSIDIAN.getStack(), copyAspects(Blocks.OBSIDIAN).remove(Aspect.EARTH, 1).add(Aspect.ENTROPY, 1).add(Aspect.ELDRITCH, 1));
            addItemAspect(EnumGeneric.BLOCK_CONCRETE.getStack(), new AspectList().add(Aspect.EARTH, 3).add(Aspect.METAL, 1));
            addItemAspect(EnumGeneric.BLOCK_CREOSOTE.getStack(), new AspectList().add(Aspect.PLANT, 3).add(Aspect.ORDER, 1));

            addItemAspect(EnumOre.DARK_DIAMOND.getItem(), copyAspects(Blocks.DIAMOND_ORE).add(Aspect.DARKNESS, 1));
            addItemAspect(EnumOre.DARK_EMERALD.getItem(), copyAspects(Blocks.EMERALD_ORE).add(Aspect.DARKNESS, 1));
            addItemAspect(EnumOre.DARK_LAPIS.getItem(), copyAspects(Blocks.LAPIS_ORE).add(Aspect.DARKNESS, 1));

            addItemAspect(EnumOre.SULFUR.getItem(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.FIRE, 3));
            addItemAspect(EnumOre.SALTPETER.getItem(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.AIR, 3));
            addItemAspect(EnumOre.FIRESTONE.getItem(), new AspectList().add(Aspect.EARTH, 1).add(Aspect.FIRE, 6).add(Aspect.ENTROPY, 1));

            addItemAspect(RailcraftItems.dust.getStack(ItemDust.EnumDust.SULFUR), new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.FIRE, 3));
            addItemAspect(RailcraftItems.dust.getStack(ItemDust.EnumDust.SALTPETER), new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.AIR, 3));
            addItemAspect(RailcraftItems.dust.getStack(ItemDust.EnumDust.CHARCOAL), new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.FIRE, 2));
            addItemAspect(RailcraftItems.dust.getStack(ItemDust.EnumDust.OBSIDIAN), copyAspects(Blocks.OBSIDIAN).remove(Aspect.EARTH, 2).add(Aspect.ENTROPY, 1));

            addItemAspect(RailcraftItems.rebar.getStack(), new AspectList().add(Aspect.METAL, 1));
            addItemAspect(RailcraftItems.rail.getStack(1, EnumRail.STANDARD), new AspectList().add(Aspect.METAL, 1));
            addItemAspect(RailcraftItems.rail.getStack(1, EnumRail.REINFORCED), new AspectList().add(Aspect.METAL, 1).add(Aspect.ORDER, 1));
            addItemAspect(RailcraftItems.rail.getStack(1, EnumRail.WOOD), new AspectList().add(Aspect.PLANT, 1));
            addItemAspect(RailcraftItems.rail.getStack(1, EnumRail.ADVANCED), new AspectList().add(Aspect.METAL, 1).add(Aspect.MECHANISM, 1));
            addItemAspect(RailcraftItems.rail.getStack(1, EnumRail.SPEED), new AspectList().add(Aspect.METAL, 1).add(Aspect.MOTION, 1));
            addItemAspect(RailcraftItems.tie.getStack(1, EnumTie.WOOD), new AspectList().add(Aspect.PLANT, 3).add(Aspect.ORDER, 1));
            addItemAspect(RailcraftItems.tie.getStack(1, EnumTie.STONE), new AspectList().add(Aspect.EARTH, 3).add(Aspect.METAL, 1));

            addItemAspect(RailcraftItems.coke.getStack(), new AspectList().add(Aspect.FIRE, 4).add(Aspect.ENERGY, 4));

            addItemAspect(RailcraftItems.plate.getStack(1, Metal.IRON), new AspectList().add(Aspect.METAL, 4));
            addItemAspect(RailcraftItems.plate.getStack(1, Metal.STEEL), new AspectList().add(Aspect.METAL, 3).add(Aspect.ORDER, 1));
            addItemAspect(RailcraftItems.plate.getStack(1, Metal.TIN), new AspectList().add(Aspect.METAL, 3));

            addItemAspect(RailcraftItems.gear.getStack(EnumGear.GOLD_PLATE), new AspectList().add(Aspect.METAL, 4).add(Aspect.ORDER, 1).add(Aspect.MECHANISM, 2).add(Aspect.DESIRE, 2));
            addItemAspect(RailcraftItems.gear.getStack(EnumGear.BUSHING), new AspectList().add(Aspect.METAL, 1).add(Aspect.ORDER, 1).add(Aspect.MECHANISM, 1));
            addItemAspect(RailcraftItems.gear.getStack(EnumGear.IRON), new AspectList().add(Aspect.METAL, 4).add(Aspect.ORDER, 1).add(Aspect.MECHANISM, 4));
            addItemAspect(RailcraftItems.gear.getStack(EnumGear.STEEL), new AspectList().add(Aspect.METAL, 8).add(Aspect.ORDER, 2).add(Aspect.MECHANISM, 4));

            addItemAspect(EnumMachineAlpha.ROLLING_MACHINE.getItem(), new AspectList().add(Aspect.CRAFT, 6).add(Aspect.MECHANISM, 2));
            addItemAspect(EnumMachineAlpha.ROCK_CRUSHER.getItem(), new AspectList().add(Aspect.CRAFT, 2).add(Aspect.MECHANISM, 4).add(Aspect.EARTH, 4).add(Aspect.EARTH, 4));
            addItemAspect(EnumMachineAlpha.FEED_STATION.getItem(), new AspectList().add(Aspect.PLANT, 4).add(Aspect.BEAST, 4).add(Aspect.MECHANISM, 2));
            addItemAspect(EnumMachineAlpha.SMOKER.getItem(), new AspectList().add(Aspect.FIRE, 4).add(Aspect.AIR, 4).add(Aspect.MECHANISM, 2));
            addItemAspect(EnumMachineAlpha.SMOKER.getItem(), new AspectList().add(Aspect.FIRE, 4).add(Aspect.AIR, 4).add(Aspect.MECHANISM, 2));

            addItemAspect(EnumMachineAlpha.STEAM_TRAP_MANUAL.getItem(), steamAspects.copy().add(Aspect.TRAP, 4));
            addItemAspect(EnumMachineAlpha.STEAM_TRAP_AUTO.getItem(), steamAspects.copy().add(Aspect.TRAP, 4).add(Aspect.MIND, 2));
            addItemAspect(EnumMachineAlpha.STEAM_OVEN.getItem(), steamAspects.copy().add(Aspect.FIRE, 3));
            addItemAspect(EnumMachineAlpha.TURBINE.getItem(), steamAspects.copy().add(Aspect.ENERGY, 4));

            addItemAspect(EnumMachineAlpha.ANCHOR_ADMIN.getItem(), anchorAspects);
            addItemAspect(EnumMachineAlpha.ANCHOR_PERSONAL.getItem(), anchorAspects);
            addItemAspect(EnumMachineAlpha.ANCHOR_WORLD.getItem(), anchorAspects);
            addItemAspect(EnumMachineBeta.SENTINEL.getItem(), anchorAspects);

            addItemAspect(EnumMachineBeta.BOILER_FIREBOX_SOLID.getItem(), steamAspects.copy().add(Aspect.ENERGY, 2));
            addItemAspect(EnumMachineBeta.BOILER_FIREBOX_FLUID.getItem(), steamAspects.copy().add(Aspect.ENERGY, 2));
            addItemAspect(EnumMachineBeta.BOILER_TANK_LOW_PRESSURE.getItem(), steamAspects.copy().add(Aspect.ENERGY, 2));
            addItemAspect(EnumMachineBeta.BOILER_TANK_HIGH_PRESSURE.getItem(), steamAspects.copy().add(Aspect.ENERGY, 2));
            addItemAspect(EnumMachineBeta.ENGINE_STEAM_HOBBY.getItem(), steamAspects.copy().add(Aspect.ENERGY, 4));
            addItemAspect(EnumMachineBeta.ENGINE_STEAM_LOW.getItem(), steamAspects.copy().add(Aspect.ENERGY, 4));
            addItemAspect(EnumMachineBeta.ENGINE_STEAM_HIGH.getItem(), steamAspects.copy().add(Aspect.ENERGY, 4));

            addItemAspect(EnumMachineAlpha.TANK_WATER.getItem(), tankAspects.copy().add(Aspect.PLANT, 2).add(Aspect.WATER, 2));

            AspectList ironTankAspects = tankAspects.copy().add(Aspect.METAL, 2);
            addItemAspect(EnumMachineBeta.TANK_IRON_GAUGE.getItem(), ironTankAspects);
            addItemAspect(EnumMachineBeta.TANK_IRON_VALVE.getItem(), ironTankAspects);
            addItemAspect(EnumMachineBeta.TANK_IRON_WALL.getItem(), ironTankAspects);
            addItemAspect(EnumMachineBeta.TANK_STEEL_GAUGE.getItem(), ironTankAspects);
            addItemAspect(EnumMachineBeta.TANK_STEEL_VALVE.getItem(), ironTankAspects);
            addItemAspect(EnumMachineBeta.TANK_STEEL_WALL.getItem(), ironTankAspects);

            AspectList detectorAspects = new AspectList().add(Aspect.SENSES, 4).add(Aspect.MECHANISM, 4);
            for (EnumDetector detector : EnumDetector.VALUES) {
                addItemAspect(detector.getItem(), detectorAspects);
            }

            AspectList glassAspects = new AspectList().add(Aspect.CRYSTAL, 4).add(Aspect.METAL, 1);
            for (EnumColor color : EnumColor.VALUES) {
                addItemAspect(RailcraftBlocks.glass.getStack(color), glassAspects);
            }

            addItemAspect(RailcraftBlocks.anvilSteel.getStack(), new AspectList().add(Aspect.CRAFT, 4).add(Aspect.EXCHANGE, 2).add(Aspect.METAL, 4).add(Aspect.ORDER, 2));

            addCartAspect(RailcraftCarts.LOCO_STEAM_SOLID, steamAspects.copy().add(Aspect.MOTION, 2).add(Aspect.MECHANISM, 4));
            addCartAspect(RailcraftCarts.LOCO_ELECTRIC, new AspectList().add(Aspect.FLUX, 6).add(Aspect.MECHANISM, 6).add(Aspect.MOTION, 2));
            addCartAspect(RailcraftCarts.PUMPKIN, new AspectList(new ItemStack(Blocks.PUMPKIN)).add(Aspect.MOTION, 2).add(Aspect.ENTROPY, 6));
            addCartAspect(RailcraftCarts.ANCHOR, anchorAspects.copy().add(Aspect.MOTION, 2));
            addCartAspect(RailcraftCarts.ANCHOR_ADMIN, anchorAspects.copy().add(Aspect.MOTION, 2));
            addCartAspect(RailcraftCarts.ANCHOR_PERSONAL, anchorAspects.copy().add(Aspect.MOTION, 2));

            addItemAspect(RailcraftItems.goggles.getStack(), new AspectList().add(Aspect.AURA, 4).add(Aspect.SENSES, 4));

            addItemAspect(RailcraftItems.firestoneRaw.getStack(), new AspectList().add(Aspect.FIRE, 6).add(Aspect.CRYSTAL, 2).add(Aspect.ENTROPY, 4));
            addItemAspect(RailcraftItems.firestoneCut.getStack(), new AspectList().add(Aspect.FIRE, 6).add(Aspect.CRYSTAL, 2).add(Aspect.ENTROPY, 2).add(Aspect.ORDER, 2));
            addItemAspect(ItemFirestoneRefined.getItemCharged(), new AspectList().add(Aspect.FIRE, 6).add(Aspect.CRYSTAL, 2).add(Aspect.ORDER, 4));
            addItemAspect(ItemFirestoneCracked.getItemCharged(), new AspectList().add(Aspect.FIRE, 6).add(Aspect.CRYSTAL, 2).add(Aspect.ENTROPY, 4));
        } catch (Throwable error) {
            Game.logErrorAPI("Thaumcraft", error, ThaumcraftApi.class);
        }
    }

    public static AspectList copyAspects(Block block) {
        return new AspectList(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE));
    }

    private static void addItemAspect(@Nullable ItemStack stack, AspectList aspects) {
        if (stack == null)
            return;
        ThaumcraftApi.registerObjectTag(stack, aspects);
    }

    private static void addItemAspect(@Nullable Block block, AspectList aspects) {
        if (block == null)
            return;
        ThaumcraftApi.registerObjectTag(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE), aspects);
    }

    private static void addItemAspectComplex(ItemStack stack, AspectList aspects) {
        ThaumcraftApi.registerComplexObjectTag(stack, aspects);
    }

    private static void addCartAspect(ICartType cart, AspectList aspects) {
        addItemAspect(cart.getCartItem(), aspects);
        ThaumcraftApi.registerEntityTag(cart.getTag(), aspects);
    }

    private static void addBrickAspects(BrickTheme brick, Aspect baseAspect) {
        addBrickAspects(brick, 4, baseAspect);
    }

    private static void addBrickAspects(BrickTheme brick, int baseAmount, Aspect... baseAspects) {
        Block block = brick.getBlock();
        if (block == null)
            return;
        AspectList aspects = new AspectList().add(Aspect.EARTH, 4);
        for (Aspect a : baseAspects) {
            aspects.add(a, baseAmount);
        }
        ThaumcraftApi.registerObjectTag(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE), aspects);
        ThaumcraftApi.registerObjectTag(brick.getStack(BrickVariant.COBBLE), aspects.copy().remove(Aspect.EARTH, 2).add(Aspect.ENTROPY, 2));
        ThaumcraftApi.registerObjectTag(brick.getStack(BrickVariant.BLOCK), aspects.copy().remove(Aspect.EARTH, 2).add(Aspect.ORDER, 2));
        ThaumcraftApi.registerObjectTag(brick.getStack(BrickVariant.ETCHED), aspects.copy().remove(Aspect.EARTH, 2).add(Aspect.DESIRE, 2));
    }

    public static ToolMaterial getThaumiumToolMaterial() {
        try {
            return ThaumcraftMaterials.TOOLMAT_THAUMIUM;
        } catch (Throwable error) {
            Game.logErrorAPI("Thaumcraft", error, ThaumcraftApi.class);
        }
        return ToolMaterial.IRON;
    }

    public static ToolMaterial getVoidmetalToolMaterial() {
        try {
            return ThaumcraftMaterials.TOOLMAT_VOID;
        } catch (Throwable error) {
            Game.logErrorAPI("Thaumcraft", error, ThaumcraftApi.class);
        }
        return ToolMaterial.IRON;
    }

    public static boolean isModInstalled() {
        if (modLoaded == null)
            modLoaded = Loader.isModLoaded("Thaumcraft");
        return modLoaded;
    }
}
