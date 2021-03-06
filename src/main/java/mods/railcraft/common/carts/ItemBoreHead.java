/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.carts;

import mods.railcraft.api.carts.bore.IBoreHead;
import mods.railcraft.common.core.IRailcraftObject;
import mods.railcraft.common.plugins.forge.CreativePlugin;
import net.minecraft.item.Item;

public abstract class ItemBoreHead extends Item implements IBoreHead, IRailcraftObject {

    protected ItemBoreHead() {
        maxStackSize = 1;
        setCreativeTab(CreativePlugin.RAILCRAFT_TAB);
    }
}
