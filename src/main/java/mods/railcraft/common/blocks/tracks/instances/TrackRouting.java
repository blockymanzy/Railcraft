/*******************************************************************************
 * Copyright (c) CovertJaguar, 2011-2016
 * http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 ******************************************************************************/
package mods.railcraft.common.blocks.tracks.instances;

import com.mojang.authlib.GameProfile;
import mods.railcraft.api.carts.IRoutableCart;
import mods.railcraft.api.core.items.IToolCrowbar;
import mods.railcraft.api.tracks.IRoutingTrack;
import mods.railcraft.api.tracks.ITrackPowered;
import mods.railcraft.common.blocks.tracks.EnumTrack;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.items.ItemTicket;
import mods.railcraft.common.items.RailcraftItems;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.inventory.StandaloneInventory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrackRouting extends TrackSecured implements ITrackPowered, IRoutingTrack {

    private final StandaloneInventory inv = new StandaloneInventory(1);
    private boolean powered;

    @Override
    public EnumTrack getTrackType() {
        return EnumTrack.ROUTING;
    }

    public IInventory getInventory() {
        return inv;
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        state = super.getActualState(state);
        state = state.withProperty(POWERED, isPowered());
        return state;
    }

    @Override
    public boolean blockActivated(EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem) {
        if (heldItem != null && heldItem.getItem() instanceof IToolCrowbar) {
            IToolCrowbar crowbar = (IToolCrowbar) heldItem.getItem();
            if (crowbar.canWhack(player, hand, heldItem, getPos())) {
                GuiHandler.openGui(EnumGui.TRACK_ROUTING, player, theWorldAsserted(), getPos().getX(), getPos().getY(), getPos().getZ());
                crowbar.onWhack(player, hand, heldItem, getPos());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMinecartPass(EntityMinecart cart) {
        if (!isPowered())
            return;
        if (inv.getStackInSlot(0) == null)
            return;
        ItemStack stack = inv.getStackInSlot(0);
        if (cart instanceof IRoutableCart && stack != null)
            ((IRoutableCart) cart).setDestination(stack);
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("powered", powered);
        inv.writeToNBT("inv", data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        powered = data.getBoolean("powered");
        inv.readFromNBT("inv", data);
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        data.writeBoolean(powered);
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        boolean p = data.readBoolean();
        if (p != powered) {
            powered = p;
            markBlockNeedsUpdate();
        }
    }

    @Override
    public boolean setTicket(String dest, String title, GameProfile owner) {
        ItemStack ticket = RailcraftItems.ticket.getStack();
        return ItemTicket.setTicketData(ticket, dest, title, owner);
    }

    @Override
    public void clearTicket() {
        inv.setInventorySlotContents(0, null);
    }

    @Override
    public void onBlockRemoved() {
        super.onBlockRemoved();
        InvTools.dropInventory(inv, getTile().getWorld(), getPos());
    }

}
