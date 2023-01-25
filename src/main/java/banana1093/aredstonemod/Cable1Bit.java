package banana1093.aredstonemod;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import org.jetbrains.annotations.Nullable;

// A 1 bit cable, similar to redstone wire, but is instant and has no signal loss
// it can go upwards, downwards, and sideways
// it automatically connects to other cables next to it
// it doesn't connect to any redstone component but a repeater
// to power it you put a repeater facing into one end of the cable
// all other ends of the cable will instantly power up
// a repeater at the end of the cable facing away will get powered by the cable
// ex: repeater > cable > repeater
public class Cable1Bit extends Block {
    public static final EnumProperty<CableSide> UP;
    public static final EnumProperty<CableSide> DOWN;
    public static final EnumProperty<CableSide> NORTH;
    public static final EnumProperty<CableSide> SOUTH;
    public static final EnumProperty<CableSide> EAST;
    public static final EnumProperty<CableSide> WEST;

    public static final BooleanProperty POWERED;

    private static final VoxelShape NODE;
    private static final VoxelShape C_UP;
    private static final VoxelShape C_DOWN;
    private static final VoxelShape C_EAST;
    private static final VoxelShape C_WEST;
    private static final VoxelShape C_NORTH;
    private static final VoxelShape C_SOUTH;

    static {
        UP = EnumProperty.of("up", CableSide.class);
        DOWN = EnumProperty.of("down", CableSide.class);
        NORTH = EnumProperty.of("north", CableSide.class);
        SOUTH = EnumProperty.of("south", CableSide.class);
        EAST = EnumProperty.of("east", CableSide.class);
        WEST = EnumProperty.of("west", CableSide.class);

        NODE = Block.createCuboidShape(4.5, 4.5, 4.5, 11.5, 11.5, 11.5);
        C_DOWN = Block.createCuboidShape(4.5, 0, 4.5, 11.5, 5, 11.5);
        C_UP = Block.createCuboidShape(4.5, 11, 4.5, 11.5, 16, 11.5);
        C_EAST = Block.createCuboidShape(11, 4.5, 4.5, 16, 11.5, 11.5);
        C_WEST = Block.createCuboidShape(0, 4.5, 4.5, 5, 11.5, 11.5);
        C_NORTH = Block.createCuboidShape(4.5, 4.5, 0, 11.5, 11.5, 5);
        C_SOUTH = Block.createCuboidShape(4.5, 4.5, 11, 11.5, 11.5, 16);

        POWERED = BooleanProperty.of("powered");
    }

    public Cable1Bit(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(UP, CableSide.NONE)
                .with(DOWN, CableSide.NONE)
                .with(NORTH, CableSide.NONE)
                .with(SOUTH, CableSide.NONE)
                .with(EAST, CableSide.NONE)
                .with(WEST, CableSide.NONE)
                .with(POWERED, false));
    }

    public static EnumProperty<CableSide> getProperty(Direction facing) {
        return switch (facing) {
            case UP -> UP;
            case DOWN -> DOWN;
            case EAST -> EAST;
            case WEST -> WEST;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
        };
    }

    public static boolean getPowered(WorldView world, BlockPos pos, BlockState state) {
        // if any of the connected blocks are powered then powered is true
        for (Direction facing : Direction.values()) {
            BlockState targetState = world.getBlockState(pos.offset(facing));
            Block targetBlock = targetState.getBlock();
            if (targetBlock instanceof Cable1Bit) {
                if (state.get(getProperty(facing)) == CableSide.CABLE) {
                    if (targetState.get(POWERED)) {
                        return true;
                    }
                }
            } else if (targetBlock instanceof RepeaterBlock) {
                if (state.get(getProperty(facing)) == CableSide.REPEATER) {
                    if (targetState.get(RepeaterBlock.POWERED)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = NODE;
        if (state.get(UP) != CableSide.NONE)
            shape = VoxelShapes.combineAndSimplify(shape, C_UP, BooleanBiFunction.OR);
        if (state.get(DOWN) != CableSide.NONE)
            shape = VoxelShapes.combineAndSimplify(shape, C_DOWN, BooleanBiFunction.OR);
        if (state.get(NORTH) != CableSide.NONE)
            shape = VoxelShapes.combineAndSimplify(shape, C_NORTH, BooleanBiFunction.OR);
        if (state.get(EAST) != CableSide.NONE)
            shape = VoxelShapes.combineAndSimplify(shape, C_EAST, BooleanBiFunction.OR);
        if (state.get(SOUTH) != CableSide.NONE)
            shape = VoxelShapes.combineAndSimplify(shape, C_SOUTH, BooleanBiFunction.OR);
        if (state.get(WEST) != CableSide.NONE)
            shape = VoxelShapes.combineAndSimplify(shape, C_WEST, BooleanBiFunction.OR);

        return shape;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, POWERED);
    }

    public BlockState updateCable(BlockState state, WorldAccess world, BlockState neighborState, Block neighbor, Direction direction, BlockPos pos, BlockPos posFrom) {

        if (neighbor instanceof Cable1Bit) {
            return state.with(getProperty(direction), CableSide.CABLE);
        }
        else if (neighbor instanceof RepeaterBlock && direction != Direction.UP && direction != Direction.DOWN) {
            // if r is facing the cable, or facing away from the cable
            if (neighborState.get(Properties.HORIZONTAL_FACING) == direction || neighborState.get(Properties.HORIZONTAL_FACING) == direction.getOpposite()) {
                return state.with(getProperty(direction), CableSide.REPEATER);
            }
        }
        boolean powered = getPowered(world, pos, state);
        if (world.getBlockState(posFrom).getBlock() instanceof Cable1Bit) {
            world.setBlockState(posFrom, neighborState.with(POWERED, powered), 3); // 3 = notify neighbors
        }
        else if (neighbor instanceof RepeaterBlock && direction != Direction.UP && direction != Direction.DOWN && neighborState.get(Properties.HORIZONTAL_FACING) == direction) {
            world.setBlockState(posFrom, neighborState.with(RepeaterBlock.POWERED, powered), 3); // 3 = notify neighbors
        }
        return state.with(getProperty(direction), CableSide.NONE).with(POWERED, powered);

        // TODO: UNTESTED CODE
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {

        Block neighbor = newState.getBlock();
        return updateCable(state, world, newState, neighbor, direction, pos, posFrom);
    }


    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos());
        for (Direction facing : Direction.values()) {
            state = updateCable(state, ctx.getWorld(), ctx.getWorld().getBlockState(ctx.getBlockPos().offset(facing)), ctx.getWorld().getBlockState(ctx.getBlockPos().offset(facing)).getBlock(), facing, ctx.getBlockPos(), ctx.getBlockPos().add(facing.getVector()));
        }
        return state;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
