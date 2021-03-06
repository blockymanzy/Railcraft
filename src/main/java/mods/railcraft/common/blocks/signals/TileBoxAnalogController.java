package mods.railcraft.common.blocks.signals;

import mods.railcraft.api.signals.IControllerTile;
import mods.railcraft.api.signals.SignalAspect;
import mods.railcraft.api.signals.SimpleSignalController;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.plugins.forge.PowerPlugin;
import mods.railcraft.common.util.misc.Game;
import mods.railcraft.common.util.network.IGuiReturnHandler;
import mods.railcraft.common.util.network.RailcraftInputStream;
import mods.railcraft.common.util.network.RailcraftOutputStream;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.Map;

public class TileBoxAnalogController extends TileBoxBase implements IControllerTile, IGuiReturnHandler {

    private final SimpleSignalController controller = new SimpleSignalController(getLocalizationTag(), this);
    private int strongestSignal;

    public EnumMap<SignalAspect, BitSet> aspects = new EnumMap<SignalAspect, BitSet>(SignalAspect.class);

    public TileBoxAnalogController() {
        for (SignalAspect aspect : SignalAspect.VALUES) {
            aspects.put(aspect, new BitSet());
        }
    }

    @Override
    public EnumSignal getSignalType() {
        return EnumSignal.BOX_ANALOG_CONTROLLER;
    }

    @Override
    public boolean blockActivated(EnumFacing side, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem) {
        if (player.isSneaking())
            return false;
        GuiHandler.openGui(EnumGui.BOX_ANALOG_CONTROLLER, player, worldObj, getPos());
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (Game.isClient(worldObj)) {
            controller.tickClient();
            return;
        }
        controller.tickServer();
        SignalAspect prevAspect = controller.getAspect();
        if (controller.isBeingPaired())
            controller.setAspect(SignalAspect.BLINK_YELLOW);
        else if (controller.isPaired())
            controller.setAspect(determineAspect());
        else
            controller.setAspect(SignalAspect.BLINK_RED);
        if (prevAspect != controller.getAspect())
            sendUpdateToClient();
    }

    @Override
    public void onNeighborBlockChange(IBlockState state, Block neighborBlock) {
        super.onNeighborBlockChange(state, neighborBlock);
        if (Game.isClient(getWorld()))
            return;
        int s = getPowerLevel();
        if (s != strongestSignal) {
            strongestSignal = s;
            sendUpdateToClient();
        }
    }

    private int getPowerLevel() {
        int p = 0, tmp;
        for (EnumFacing side : EnumFacing.VALUES) {
            if (side == EnumFacing.UP)
                continue;
            if (tileCache.getTileOnSide(side) instanceof TileBoxBase)
                continue;
            if ((tmp = PowerPlugin.getBlockPowerLevel(worldObj, getPos(), side)) > p)
                p = tmp;
            if ((tmp = PowerPlugin.getBlockPowerLevel(worldObj, getPos().down(), side)) > p)
                p = tmp;
        }
        return p;
    }

    private SignalAspect determineAspect() {
        SignalAspect aspect = SignalAspect.OFF;
        for (Map.Entry<SignalAspect, BitSet> entry : aspects.entrySet()) {
            SignalAspect current = entry.getKey();
            if (entry.getValue().get(strongestSignal))
                aspect = (aspect == SignalAspect.OFF) ? current : SignalAspect.mostRestrictive(aspect, current);
        }
        return aspect;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("strongestSignal", strongestSignal);

        for (Map.Entry<SignalAspect, BitSet> entry : aspects.entrySet()) {
            String n = entry.getKey().name();
            byte[] bytes = entry.getValue().toByteArray();
            data.setByteArray("aspect_" + n, bytes);
        }

        controller.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        strongestSignal = data.getInteger("strongestSignal");

        try {
            for (Map.Entry<SignalAspect, BitSet> entry : aspects.entrySet()) {
                String n = entry.getKey().name();
                byte[] bytes = data.getByteArray("aspect_" + n);
                BitSet bitSet = entry.getValue();
                bitSet.clear();
                bitSet.or(BitSet.valueOf(bytes));
            }
        } catch (Exception ignored) {
        }

        controller.readFromNBT(data);

        // Legacy Support Code - remove in the future
        for (Map.Entry<SignalAspect, BitSet> entry : aspects.entrySet()) {
            String n = entry.getKey().toString();
            boolean on = data.getBoolean("mode" + n);
            if (on) {
                int low = data.getInteger("low" + n);
                int high = data.getInteger("high" + n);
                entry.getValue().set(low, high);
            }
        }
    }

    @Override
    public void writePacketData(RailcraftOutputStream data) throws IOException {
        super.writePacketData(data);

        writeGuiData(data);

        controller.writePacketData(data);
    }

    @Override
    public void readPacketData(RailcraftInputStream data) throws IOException {
        super.readPacketData(data);

        readGuiData(data, null);

        controller.readPacketData(data);
        markBlockForUpdate();
    }

    @Override
    public void writeGuiData(RailcraftOutputStream data) throws IOException {
        for (Map.Entry<SignalAspect, BitSet> entry : aspects.entrySet()) {
            data.writeBitSet(entry.getValue());
        }
    }

    @Override
    public void readGuiData(RailcraftInputStream data, @Nullable EntityPlayer sender) throws IOException {
        for (Map.Entry<SignalAspect, BitSet> entry : aspects.entrySet()) {
            BitSet bitSet = entry.getValue();
            bitSet.clear();
            bitSet.or(data.readBitSet());
        }
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return false;
    }

    @Override
    public SignalAspect getBoxSignalAspect(EnumFacing side) {
        return controller.getAspect();
    }

    @Override
    public SimpleSignalController getController() {
        return controller;
    }

}
