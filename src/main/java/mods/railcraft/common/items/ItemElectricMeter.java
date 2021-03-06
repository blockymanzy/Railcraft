/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.items;

import com.google.common.base.Optional;
import mods.railcraft.api.electricity.GridTools;
import mods.railcraft.api.electricity.IElectricGrid;
import mods.railcraft.api.electricity.IElectricMinecart;
import mods.railcraft.common.core.Railcraft;
import mods.railcraft.common.plugins.forge.ChatPlugin;
import mods.railcraft.common.plugins.forge.CraftingPlugin;
import mods.railcraft.common.plugins.forge.LootPlugin;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemElectricMeter extends ItemRailcraft implements IActivationBlockingItem {
    public ItemElectricMeter() {
        setMaxDamage(0);
        setMaxStackSize(1);
        setFull3D();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initializeDefinintion() {
        LootPlugin.addLoot(RailcraftItems.electricMeter, 1, 1, LootPlugin.Type.WORKSHOP);
    }

    @Override
    public void defineRecipes() {
        CraftingPlugin.addRecipe(new ItemStack(this),
                "T T",
                "BGB",
                " C ",
                'B', Blocks.STONE_BUTTON,
                'G', "paneGlassColorless",
                'C', "ingotCopper",
                'T', "ingotTin");
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();

        Entity entity = event.getTarget();

        ItemStack stack = event.getItemStack();
        if (stack != null && stack.getItem() instanceof ItemElectricMeter)
            player.swingArm(event.getHand());

        if (Game.isClient(player.worldObj))
            return;

        if (stack != null && stack.getItem() instanceof ItemElectricMeter)
            try {
                if (entity instanceof IElectricMinecart) {
                    IElectricMinecart cart = (IElectricMinecart) entity;
                    IElectricMinecart.ChargeHandler ch = cart.getChargeHandler();
                    if (ch != null) {
                        ChatPlugin.sendLocalizedChat(player, "railcraft.gui.electric.meter.charge", ch.getCharge(), ch.getDraw(), ch.getLosses());
                        event.setCanceled(true);
                    }
                }
            } catch (Throwable er) {
                Game.logErrorAPI(Railcraft.MOD_ID, er, IElectricMinecart.class);
                ChatPlugin.sendLocalizedChatFromServer(player, "chat.railcraft.api.error");
            }
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (Game.isClient(world))
            return EnumActionResult.PASS;
        EnumActionResult returnValue = EnumActionResult.PASS;
        try {
            Optional<IElectricGrid> gridObject = GridTools.getGridObjectAt(world, pos);
            if (gridObject.isPresent()) {
                IElectricGrid.ChargeHandler ch = gridObject.get().getChargeHandler();
                if (ch != null) {
                    ChatPlugin.sendLocalizedChat(player, "railcraft.gui.electric.meter.charge", ch.getCharge(), ch.getDraw(), ch.getLosses());
                    returnValue = EnumActionResult.SUCCESS;
                }
            }
        } catch (Throwable er) {
            Game.logErrorAPI(Railcraft.MOD_ID, er, IElectricGrid.class);
            ChatPlugin.sendLocalizedChatFromServer(player, "chat.railcraft.api.error");
        }
        return returnValue;
    }

}
