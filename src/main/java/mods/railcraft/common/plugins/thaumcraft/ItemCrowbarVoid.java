/*
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * or licensed for use by CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.plugins.thaumcraft;

import mods.railcraft.common.core.RailcraftConfig;
import mods.railcraft.common.items.ItemCrowbar;
import mods.railcraft.common.items.ItemMaterials;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.items.IRepairable;
import thaumcraft.api.items.IWarpingGear;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;

public class ItemCrowbarVoid extends ItemCrowbar implements IRepairable, IWarpingGear {
    public static final String ITEM_TAG = "railcraft.tool.crowbar.void";
    public static final String RESEARCH_TAG = "RC_Crowbar_Void";
    public static ItemCrowbarVoid item;

    public ItemCrowbarVoid() {
        super(ItemMaterials.Material.VOID, ThaumcraftPlugin.getVoidmetalToolMaterial());
        setUnlocalizedName(ITEM_TAG);
    }

    public static void registerItem() {
        if (item == null && RailcraftConfig.isItemEnabled(ITEM_TAG)) {
            item = new ItemCrowbarVoid();
            RailcraftRegistry.register(item);
            item.initializeDefinintion();
        }
    }

    public static void registerResearch() {
        if (item == null)
            return;
        try {
            IArcaneRecipe recipe = ThaumcraftApi.addArcaneCraftingRecipe(RESEARCH_TAG, new ItemStack(item),
                    new AspectList().add(Aspect.ENTROPY, 50),
                    " RI",
                    "RIR",
                    "IR ",
                    'I', ThaumcraftPlugin.ITEMS.get("ingots", 1),
                    'R', "dyeRed");

            AspectList aspects = new AspectList();
            aspects.add(Aspect.TOOL, 2).add(Aspect.MECHANISM, 4).add(Aspect.METAL, 2);

            ResearchItemRC voidCrowbar = new ResearchItemRC(RESEARCH_TAG, ThaumcraftPlugin.RESEARCH_CATEGORY, aspects, 0, 1, 3, new ItemStack(item));
            voidCrowbar.setPages(ThaumcraftPlugin.getResearchPage(RESEARCH_TAG), new ResearchPage(recipe))
                    .setParents(ItemCrowbarMagic.RESEARCH_TAG).setParentsHidden("VOIDMETAL")
                    .registerResearchItem();
        } catch (Throwable error) {
            Game.logErrorAPI("Thaumcraft", error, ResearchItem.class);
        }
    }

    public static ItemStack getItem() {
        if (item == null)
            return null;
        return new ItemStack(item);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        super.onUpdate(stack, world, entity, itemSlot, isSelected);

        if (stack.isItemDamaged() && entity != null && entity.ticksExisted % 20 == 0 && entity instanceof EntityLivingBase) {
            stack.damageItem(-1, (EntityLivingBase) entity);
        }
    }

    @Override
    public int getWarp(ItemStack itemstack, EntityPlayer player) {
        return 1;
    }
}
