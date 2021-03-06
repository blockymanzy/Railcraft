/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.carts;

import mods.railcraft.api.carts.locomotive.LocomotiveRenderType;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.items.ItemTicket;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.wrappers.InventoryMapper;
import mods.railcraft.common.util.sounds.RailcraftSoundEvents;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class EntityLocomotiveCreative extends EntityLocomotive implements ISidedInventory {

    private static final int SLOT_TICKET = 0;
    private static final int[] SLOTS = InvTools.buildSlotArray(0, 1);
    private final IInventory invTicket = new InventoryMapper(this, SLOT_TICKET, 2, false);

    @SuppressWarnings("unused")
    public EntityLocomotiveCreative(World world) {
        super(world);
    }

    @SuppressWarnings("unused")
    public EntityLocomotiveCreative(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public ICartType getCartType() {
        return RailcraftCarts.LOCO_CREATIVE;
    }

    @Override
    public SoundEvent getWhistle() {
        return RailcraftSoundEvents.ENTITY_LOCOMOTIVE_ELECTRIC_WHISTLE.getSoundEvent();
    }

    @Override
    protected void openGui(EntityPlayer player) {
        GuiHandler.openGui(EnumGui.LOCO_CREATIVE, player, worldObj, this);
    }

    @Override
    public void setMode(LocoMode mode) {
        if (mode == LocoMode.IDLE)
            mode = LocoMode.SHUTDOWN;
        super.setMode(mode);
    }

    @Override
    protected int getIdleFuelUse() {
        return 0;
    }

    @Override
    public int getMoreGoJuice() {
        return 100;
    }

    @Override
    public LocomotiveRenderType getRenderType() {
        return LocomotiveRenderType.ELECTRIC;
    }

    @Override
    protected ItemStack getCartItemBase() {
        return RailcraftCarts.LOCO_CREATIVE.getCartItem();
    }

    @Override
    public float getOptimalDistance(EntityMinecart cart) {
        return 0.92f;
    }

    @Override
    protected IInventory getTicketInventory() {
        return invTicket;
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side) {
        return slot == SLOT_TICKET;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        switch (slot) {
            case SLOT_TICKET:
                return ItemTicket.FILTER.apply(stack);
            default:
                return false;
        }
    }
}
