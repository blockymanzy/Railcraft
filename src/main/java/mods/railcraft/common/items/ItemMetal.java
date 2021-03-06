/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/
package mods.railcraft.common.items;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;
import mods.railcraft.common.core.IVariantEnum;
import mods.railcraft.common.plugins.forestry.ForestryPlugin;
import mods.railcraft.common.plugins.forge.RailcraftRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;

import static mods.railcraft.common.items.Metal.Form;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class ItemMetal extends ItemRailcraftSubtyped {
    private final BiMap<Integer, Metal> metalBiMap;
    private final IVariantEnum[] variantArray;
    private final Form form;
    private final boolean registerOreDict;
    private final boolean registerMinerBackpack;

    protected ItemMetal(Form form, boolean registerOreDict, boolean registerMinerBackpack, BiMap<Integer, Metal> variants) {
        super(Metal.class);
        this.form = form;
        this.registerOreDict = registerOreDict;
        this.registerMinerBackpack = registerMinerBackpack;
        this.metalBiMap = Maps.unmodifiableBiMap(variants);
        variantArray = new IVariantEnum[metalBiMap.size()];
        for (int i = 0; i < variants.size(); i++) {
            variantArray[i] = variants.get(i);
        }
    }

    @Nullable
    @Override
    public IVariantEnum[] getVariants() {
        return variantArray;
    }

    public final BiMap<Integer, Metal> getMetalBiMap() {
        return metalBiMap;
    }

    @Override
    public void initializeDefinintion() {
        for (Integer meta : getMetalBiMap().keySet()) {
            ItemStack stack = new ItemStack(this, 1, meta);
            RailcraftRegistry.register(stack);
        }
        if (registerOreDict)
            for (Metal m : getMetalBiMap().values()) {
                OreDictionary.registerOre(m.getOreTag(form), m.getStack(form));
            }
        if (registerMinerBackpack)
            for (Integer meta : getMetalBiMap().keySet()) {
                ItemStack stack = new ItemStack(this, 1, meta);
                ForestryPlugin.addBackpackItem("forestry.miner", stack);
            }
    }

    @Nullable
    @Override
    public ItemStack getStack(int qty, @Nullable IVariantEnum variant) {
        checkVariant(variant);
        Integer meta = metalBiMap.inverse().get((Metal) variant);
        if (meta == null)
            meta = 0;
        return new ItemStack(this, qty, meta);
    }

    @Override
    public String getOreTag(@Nullable IVariantEnum variant) {
        checkVariant(variant);
        return ((Metal) variant).getOreTag(form);
    }

}
