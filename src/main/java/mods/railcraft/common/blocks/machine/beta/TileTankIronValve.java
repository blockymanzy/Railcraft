/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.machine.beta;

import mods.railcraft.common.blocks.machine.IComparatorValueProvider;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.fluids.FluidHelper;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.FakeTank;
import mods.railcraft.common.fluids.tanks.StandardTank;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TileTankIronValve extends TileTankBase implements IFluidHandler, IComparatorValueProvider {

    private static final Predicate<TileEntity> FLUID_OUTPUT_FILTER = tile -> {
        if (tile instanceof TileTankBase)
            return false;
        else if (tile instanceof IFluidHandler)
            return true;
        return false;
    };
    private static final EnumFacing[] FLUID_OUTPUTS = {EnumFacing.DOWN};
    private static final int FLOW_RATE = FluidHelper.BUCKET_VOLUME;
    private static final byte FILL_INCREMENT = 1;
    private final StandardTank fillTank = new StandardTank(20);
    private int previousComparatorValue;

    private boolean previousStructureValidity;

    public TileTankIronValve() {
        fillTank.setHidden(true);
        tankManager.add(fillTank);
    }

    private void setFilling(FluidStack resource) {
        boolean needsUpdate = fillTank.isEmpty();
        resource = resource.copy();
        resource.amount = 20;
        fillTank.fill(resource, true);

        if (needsUpdate)
            sendUpdateToClient();
    }

    private void decrementFilling() {
        if (!fillTank.isEmpty()) {
            fillTank.drain(FILL_INCREMENT, true);
            if (fillTank.isEmpty())
                sendUpdateToClient();
        }
    }

    public StandardTank getFillTank() {
        return fillTank;
    }

    @Override
    public EnumMachineBeta getMachineType() {
        return EnumMachineBeta.TANK_IRON_VALVE;
    }

    @Override
    public void update() {
        super.update();

        if (Game.isClient(worldObj))
            return;
        decrementFilling();

        if (isMaster) {
            TileEntity tileBelow = tileCache.getTileOnSide(EnumFacing.DOWN);

            TileTankIronValve valveBelow = null;
            if (tileBelow instanceof TileTankIronValve) {
                valveBelow = (TileTankIronValve) tileBelow;
                if (valveBelow.isStructureValid() && valveBelow.getPatternMarker() == 'T') {
                    //noinspection ConstantConditions
                    StandardTank tankBelow = valveBelow.getTankManager().get(0);
                    assert tankBelow != null;
                    FluidStack liquid = tankBelow.getFluid();
                    if (liquid != null && liquid.amount >= tankBelow.getCapacity() - FluidHelper.BUCKET_VOLUME) {
                        valveBelow = null;

                        FluidStack fillStack = liquid.copy();
                        fillStack.amount = FluidHelper.BUCKET_VOLUME - (tankBelow.getCapacity() - liquid.amount);
                        if (fillStack.amount > 0) {
                            int used = tank.fill(fillStack, false);
                            if (used > 0) {
                                fillStack = tankBelow.drain(used, true);
                                tank.fill(fillStack, true);
                            }
                        }
                    }
                } else
                    valveBelow = null;
            }

            if (valveBelow != null) {
                FluidStack available = tankManager.drain(0, FluidHelper.BUCKET_VOLUME, false);
                if (available != null && available.amount > 0) {
                    int used = valveBelow.fill(EnumFacing.UP, available, true);
                    tankManager.drain(0, used, true);
                }
            }
        }

        if (getPatternPosition().getY() - getPattern().getMasterOffset().getY() == 0) {
            TankManager tMan = getTankManager();
            if (tMan != null)
                tMan.outputLiquid(tileCache, FLUID_OUTPUT_FILTER, FLUID_OUTPUTS, 0, FLOW_RATE);
        }

        TileMultiBlock masterBlock = getMasterBlock();
        if (masterBlock instanceof TileTankBase) {
            TileTankBase masterTileTankBase = (TileTankBase) masterBlock;
            int compValue = masterTileTankBase.getComparatorValue();
            if (previousComparatorValue != compValue) {
                previousComparatorValue = compValue;
                getWorld().notifyNeighborsOfStateChange(getPos(), null);
            }
        }

        if (previousStructureValidity != isStructureValid())
            getWorld().notifyNeighborsOfStateChange(getPos(), null);
        previousStructureValidity = isStructureValid();
    }

//    @Override
//    public IIcon getIcon(int side) {
//        if (!isStructureValid() || getPattern() == null)
//            return getMachineType().getTexture(side);
//        EnumFacing s = EnumFacing.VALUES[side];
//        char markerSide = getPattern().getPatternMarkerChecked(MiscTools.getXOnSide(getPatternPositionX(), s), MiscTools.getYOnSide(getPatternPosition(), s), MiscTools.getZOnSide(getPatternPositionZ(), s));
//
//        if (!isMapPositionOtherBlock(markerSide)) {
//            if (side == EnumFacing.UP.ordinal() || side == EnumFacing.DOWN.ordinal())
//                return getMachineType().getTexture(6);
//            return getMachineType().getTexture(7);
//        }
//        return getMachineType().getTexture(side);
//    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (!canFill(from, null))
            return 0;
        if (resource == null || resource.amount <= 0) return 0;
        TankManager tMan = getTankManager();
        if (tMan == null)
            return 0;
        resource = resource.copy();
//        resource.amount = Math.min(resource.amount, FLOW_RATE);
        int filled = tMan.fill(0, resource, doFill);
        if (filled > 0 && doFill)
            setFilling(resource.copy());
        return filled;
    }

    @Nullable
    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        if (getPatternPosition().getY() - getPattern().getMasterOffset().getY() != 1)
            return null;
        TankManager tMan = getTankManager();
        if (tMan != null)
            //            maxDrain = Math.min(maxDrain, FLOW_RATE);
            return tMan.drain(0, maxDrain, doDrain);
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        if (resource == null)
            return null;
        TankManager tMan = getTankManager();
        //noinspection ConstantConditions
        if (tMan != null && tMan.get(0).getFluidType() == resource.getFluid())
            return drain(from, resource.amount, doDrain);
        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return getPatternPosition().getY() - getPattern().getMasterOffset().getY() > 0;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return getPatternPosition().getY() - getPattern().getMasterOffset().getY() <= 1;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing side) {
        TankManager tMan = getTankManager();
        if (tMan != null)
            return tMan.getTankInfo();
        return FakeTank.INFO;
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos pos) {
        TileMultiBlock masterBlock = getMasterBlock();
        if (masterBlock instanceof TileTankBase)
            return ((TileTankBase) masterBlock).getComparatorValue();
        return 0;
    }

}
