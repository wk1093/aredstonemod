package banana1093.aredstonemod;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;
import org.apache.commons.lang3.builder.CompareToBuilder;

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

// when the player clicks the block, it toggles the memory state

public class MemoryGate extends AbstractGate {
    protected MemoryGate(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState updateOutput(World world, BlockPos pos, BlockState state) {
        boolean data = this.get(world, pos, state, state.get(FACING)) > 0;
        boolean clock = this.get(world, pos, state, state.get(FACING).rotateYClockwise()) > 0;
        boolean reset = this.get(world, pos, state, state.get(FACING).rotateYCounterclockwise()) > 0;
        if (reset) {
            state = (BlockState)state.with(POWERED, false);
        } else if (clock) {
            state = (BlockState)state.with(POWERED, data);
        }
        return state;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        state = (BlockState)state.cycle(POWERED);
        world.setBlockState(pos, state, 3); // 3 = notify neighbors
        world.updateNeighborsAlways(pos, this); // update this
        world.updateNeighborsAlways(pos.offset(state.get(FACING)), this); // update output
        return ActionResult.SUCCESS;
    }
}
