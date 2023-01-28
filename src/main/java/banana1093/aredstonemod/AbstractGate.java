package banana1093.aredstonemod;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.TickPriority;

public abstract class AbstractGate extends AbstractRedstoneGateBlock {
    protected AbstractGate(Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(POWERED, false));
    }

    @Override
    protected void appendProperties(net.minecraft.state.StateManager.Builder<net.minecraft.block.Block, net.minecraft.block.BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    public abstract BlockState updateOutput(World world, BlockPos pos, BlockState state);


    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = super.getPlacementState(ctx);
        assert blockState != null;
        return this.updateOutput(ctx.getWorld(), ctx.getBlockPos(), blockState);
    }
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return this.updateOutput((World) world, pos, state);
    }

    // Get Output Level
    @Override
    protected int getOutputLevel(BlockView world, BlockPos pos, BlockState state) {
        state = this.updateOutput((World) world, pos, state);
        return (Boolean)state.get(POWERED) ? 15 : 0;
    }

    // update powered
    @Override
    protected void updatePowered(World world, BlockPos pos, BlockState state) {
        boolean b1 = (Boolean)state.get(POWERED);
        state = this.updateOutput(world, pos, state);
        boolean b2 = (Boolean)state.get(POWERED);
        if (b1 != b2 && !world.getBlockTickScheduler().isTicking(pos, this)) {
            TickPriority tickPriority = TickPriority.HIGH;
            if (this.isTargetNotAligned(world, pos, state)) {
                tickPriority = TickPriority.EXTREMELY_HIGH;
            } else if (b2) {
                tickPriority = TickPriority.VERY_HIGH;
            }

            world.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), tickPriority);
        }
    }

    // scheduled tick
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean b1 = (Boolean)state.get(POWERED);
        state = this.updateOutput(world, pos, state);
        boolean b2 = (Boolean)state.get(POWERED);
        if (b1 != b2) {
            world.scheduleBlockTick(pos, this, this.getUpdateDelayInternal(state), TickPriority.VERY_HIGH);
        }
        world.setBlockState(pos, state, 2); // 2 = Block.NOTIFY_LISTENERS
    }

    public int get(World world, BlockPos pos, BlockState state, Direction dir) {
        return world.getEmittedRedstonePower(pos.offset(dir), dir);
    }

}
