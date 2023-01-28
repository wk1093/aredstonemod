package banana1093.aredstonemod;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;

// active high d latch
// locations are relative to FACING
// NORTH: output
// SOUTH: data
// WEST: clock
// EAST: reset

///// RULES /////

// clock and reset should normally be pulses
// data has no effect on the output until the clock is pulsed
// when the clock is on, the output is the same as the data
// when the clock goes off it locks
// when the reset is pulsed, the output is set to 0
// when the reset is on, the output is locked to 0 (even if the clock is on) no matter what the data is





public class MemoryGate extends AbstractGate {
    protected MemoryGate(Settings settings) {
        super(settings);
    }

    @Override
    protected int getUpdateDelayInternal(BlockState state) {
        return 4; // 2 repeater ticks
    }

    @Override
    public BlockState updateOutput(World world, BlockPos pos, BlockState state) {
//        boolean data = world.getEmittedRedstonePower(pos.offset(state.get(FACING)), state.get(FACING)) > 0;
//        boolean clock = world.getEmittedRedstonePower(pos.offset(state.get(FACING).rotateYClockwise()), state.get(FACING).rotateYClockwise()) > 0;
//        boolean reset = world.getEmittedRedstonePower(pos.offset(state.get(FACING).rotateYCounterclockwise()), state.get(FACING).rotateYCounterclockwise()) > 0;
        boolean data = this.get(world, pos, state, state.get(FACING)) > 0;
        boolean clock = this.get(world, pos, state, state.get(FACING).rotateYClockwise()) > 0;
        boolean reset = this.get(world, pos, state, state.get(FACING).rotateYCounterclockwise()) > 0;
        if (reset) {
            state = (BlockState)state.with(POWERED, false);
        } else if (clock) {
            state = (BlockState)state.with(POWERED, data);
        }
        System.out.println("data: " + data + " clock: " + clock + " reset: " + reset + " output: " + state.get(POWERED));
        return state;
    }






}
