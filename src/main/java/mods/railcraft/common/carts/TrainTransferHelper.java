/*
 * Copyright (c) CovertJaguar, 2015 http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.carts;

import mods.railcraft.api.carts.IFluidCart;
import mods.railcraft.api.carts.IItemCart;
import mods.railcraft.api.carts.ITrainTransferHelper;
import mods.railcraft.api.core.IStackFilter;
import mods.railcraft.common.fluids.FluidHelper;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.wrappers.IInventoryObject;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Utility class for simplifying moving items and fluids through a train.
 *
 * Created by CovertJaguar on 5/9/2015.
 */
public class TrainTransferHelper implements mods.railcraft.api.carts.ITrainTransferHelper {
    public static final ITrainTransferHelper INSTANCE = new TrainTransferHelper();
    private static final int NUM_SLOTS = 8;
    private static final int TANK_CAPACITY = 8 * FluidHelper.BUCKET_VOLUME;

    private TrainTransferHelper() {
    }

    /**
     * Offers an item stack to linked carts or drops it if no one wants it.
     */
    @Override
    public void offerOrDropItem(EntityMinecart cart, ItemStack stack) {
        stack = pushStack(cart, stack);

        if (stack != null && stack.stackSize > 0)
            cart.entityDropItem(stack, 1);
    }

    // ***************************************************************************************************************************
    // Items
    // ***************************************************************************************************************************
    @Override
    public ItemStack pushStack(EntityMinecart requester, ItemStack stack) {
        Iterable<EntityMinecart> carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_A);
        stack = _pushStack(requester, carts, stack);
        if (stack == null)
            return null;
        if (LinkageManager.instance().hasLink(requester, LinkageManager.LinkType.LINK_B)) {
            carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_B);
            stack = _pushStack(requester, carts, stack);
        }
        return stack;
    }

    @Nullable
    private ItemStack _pushStack(EntityMinecart requester, Iterable<EntityMinecart> carts, ItemStack stack) {
        for (EntityMinecart cart : carts) {
            IInventoryObject inv = InvTools.getInventory(cart);
            if (inv != null && canAcceptPushedItem(requester, cart, stack))
                stack = InvTools.moveItemStack(stack, inv);
            if (stack == null || !canPassItemRequests(cart))
                break;
        }
        return stack;
    }

    @Override
    public ItemStack pullStack(EntityMinecart requester, IStackFilter filter) {
        Iterable<EntityMinecart> carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_A);
        ItemStack stack = _pullStack(requester, carts, filter);
        if (stack != null)
            return stack;
        carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_B);
        return _pullStack(requester, carts, filter);
    }

    @Nullable
    private ItemStack _pullStack(EntityMinecart requester, Iterable<EntityMinecart> carts, IStackFilter filter) {
        for (EntityMinecart cart : carts) {
            IInventoryObject inv = InvTools.getInventory(cart);
            if (inv != null) {
                Set<ItemStack> items = InvTools.findMatchingItems(inv, filter);
                for (ItemStack stack : items) {
                    if (stack != null && canProvidePulledItem(requester, cart, stack)) {
                        ItemStack removed = InvTools.removeOneItem(inv, stack);
                        if (removed != null)
                            return removed;
                    }
                }
            }
            if (!canPassItemRequests(cart))
                break;
        }
        return null;
    }

    private boolean canAcceptPushedItem(EntityMinecart requester, EntityMinecart cart, ItemStack stack) {
        return !(cart instanceof IItemCart) || ((IItemCart) cart).canAcceptPushedItem(requester, stack);
    }

    private boolean canProvidePulledItem(EntityMinecart requester, EntityMinecart cart, ItemStack stack) {
        return !(cart instanceof IItemCart) || ((IItemCart) cart).canProvidePulledItem(requester, stack);
    }

    private boolean canPassItemRequests(EntityMinecart cart) {
        if (cart instanceof IItemCart)
            return ((IItemCart) cart).canPassItemRequests();
        IInventoryObject inv = InvTools.getInventory(cart);
        return inv != null && inv.getNumSlots() >= NUM_SLOTS;
    }

    // ***************************************************************************************************************************
    // Fluids
    // ***************************************************************************************************************************
    @Override
    public FluidStack pushFluid(EntityMinecart requester, FluidStack fluidStack) {
        Iterable<EntityMinecart> carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_A);
        fluidStack = _pushFluid(requester, carts, fluidStack);
        if (fluidStack == null)
            return null;
        if (LinkageManager.instance().hasLink(requester, LinkageManager.LinkType.LINK_B)) {
            carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_B);
            fluidStack = _pushFluid(requester, carts, fluidStack);
        }
        return fluidStack;
    }

    @Nullable
    private FluidStack _pushFluid(EntityMinecart requester, Iterable<EntityMinecart> carts, FluidStack fluidStack) {
        for (EntityMinecart cart : carts) {
            if (canAcceptPushedFluid(requester, cart, fluidStack.getFluid())) {
                fluidStack.amount -= ((IFluidHandler) cart).fill(EnumFacing.UP, fluidStack, true);
            }
            if (fluidStack.amount <= 0 || !canPassFluidRequests(cart, fluidStack.getFluid()))
                break;
        }
        if (fluidStack.amount <= 0)
            return null;
        return fluidStack;
    }

    @Override
    public FluidStack pullFluid(EntityMinecart requester, FluidStack fluidStack) {
        Iterable<EntityMinecart> carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_A);
        FluidStack pulled = _pullFluid(requester, carts, fluidStack);
        if (pulled != null)
            return pulled;
        carts = LinkageManager.instance().linkIterator(requester, LinkageManager.LinkType.LINK_B);
        return _pullFluid(requester, carts, fluidStack);
    }

    @Nullable
    private FluidStack _pullFluid(EntityMinecart requester, Iterable<EntityMinecart> carts, FluidStack fluidStack) {
        for (EntityMinecart cart : carts) {
            if (canProvidePulledFluid(requester, cart, fluidStack.getFluid())) {
                IFluidHandler fluidHandler = (IFluidHandler) cart;
                if (fluidHandler.canDrain(EnumFacing.DOWN, fluidStack.getFluid())) {
                    FluidStack drained = fluidHandler.drain(EnumFacing.DOWN, fluidStack, true);
                    if (drained != null)
                        return drained;
                }
            }

            if (!canPassFluidRequests(cart, fluidStack.getFluid()))
                break;
        }
        return null;
    }

    private boolean canAcceptPushedFluid(EntityMinecart requester, EntityMinecart cart, Fluid fluid) {
        if (!(cart instanceof IFluidHandler))
            return false;
        if (cart instanceof IFluidCart)
            return ((IFluidCart) cart).canAcceptPushedFluid(requester, fluid);
        return ((IFluidHandler) cart).canFill(EnumFacing.UP, fluid);
    }

    private boolean canProvidePulledFluid(EntityMinecart requester, EntityMinecart cart, Fluid fluid) {
        if (!(cart instanceof IFluidHandler))
            return false;
        if (cart instanceof IFluidCart)
            return ((IFluidCart) cart).canProvidePulledFluid(requester, fluid);
        return ((IFluidHandler) cart).canDrain(EnumFacing.DOWN, fluid);
    }

    private boolean canPassFluidRequests(EntityMinecart cart, Fluid fluid) {
        if (cart instanceof IFluidCart)
            return ((IFluidCart) cart).canPassFluidRequests(fluid);
        if (cart instanceof IFluidHandler) {
            if (hasMatchingTank((IFluidHandler) cart, fluid))
                return true;
        }
        return false;
    }

    private boolean hasMatchingTank(IFluidHandler handler, Fluid fluid) {
        FluidTankInfo[] tankInfo = handler.getTankInfo(EnumFacing.UP);
        for (FluidTankInfo info : tankInfo) {
            if (info.capacity >= TANK_CAPACITY) {
                if (info.fluid == null || info.fluid.amount == 0 || info.fluid.getFluid() == fluid)
                    return true;
            }
        }
        return false;
    }
}
