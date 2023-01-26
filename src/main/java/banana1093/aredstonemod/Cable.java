package banana1093.aredstonemod;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

// Similar to redstone wire, but is instant and has no signal loss, it can go upwards, downwards, and sideways
// it uses an internal signal strength that goes up instead of down (like redstone dust)
// the internal signal strength is needed, otherwise it would be impossible to un-power, because it would become a loop with cables next to each other
// this way the signal travels up, and when it reaches the limit/end of cable it stops
// the signal strength/limit can be any integer
// it automatically connects to other cables next to it
// it connects to any redstone component
// it powers like a redstone wire
// the cable only connects to its same color
// the cable can be dyed

// POWER
// 0 = no power
// 1 = most powered
// 2 = less powered (powered by 1)
// 3 = less powered (powered by 2)
// ...
public class Cable extends Block {

    public enum CableSide implements StringIdentifiable { // mostly for visual purposes, but also helps with powering
        NONE("none"),
        CONNECTED("connected"),
        POWERED("powered");

        private final String name;

        CableSide(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    // tint color for cable
    public enum CableColor implements StringIdentifiable {
        WHITE("white"),
        ORANGE("orange"),
        MAGENTA("magenta"),
        LIGHT_BLUE("light_blue"),
        YELLOW("yellow"),
        LIME("lime"),
        PINK("pink"),
        GRAY("gray"),
        LIGHT_GRAY("light_gray"),
        CYAN("cyan"),
        PURPLE("purple"),
        BLUE("blue"),
        BROWN("brown"),
        GREEN("green"),
        RED("red"),
        BLACK("black");

        private final String name;

        CableColor(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }



    public static final EnumProperty<CableSide> UP;
    public static final EnumProperty<CableSide> DOWN;
    public static final EnumProperty<CableSide> NORTH;
    public static final EnumProperty<CableSide> SOUTH;
    public static final EnumProperty<CableSide> EAST;
    public static final EnumProperty<CableSide> WEST;

    public static final EnumProperty<CableColor> COLOR;

    public static final IntProperty POWER;

    private static final VoxelShape NODE;
    private static final VoxelShape C_UP;
    private static final VoxelShape C_DOWN;
    private static final VoxelShape C_EAST;
    private static final VoxelShape C_WEST;
    private static final VoxelShape C_NORTH;
    private static final VoxelShape C_SOUTH;

    private static final int MAX_POWER = 100;

    static {
        UP = EnumProperty.of("up", CableSide.class);
        DOWN = EnumProperty.of("down", CableSide.class);
        NORTH = EnumProperty.of("north", CableSide.class);
        SOUTH = EnumProperty.of("south", CableSide.class);
        EAST = EnumProperty.of("east", CableSide.class);
        WEST = EnumProperty.of("west", CableSide.class);

        COLOR = EnumProperty.of("color", CableColor.class); // default is white

        NODE = Block.createCuboidShape(4.5, 4.5, 4.5, 11.5, 11.5, 11.5);
        C_DOWN = Block.createCuboidShape(4.5, 0, 4.5, 11.5, 5, 11.5);
        C_UP = Block.createCuboidShape(4.5, 11, 4.5, 11.5, 16, 11.5);
        C_EAST = Block.createCuboidShape(11, 4.5, 4.5, 16, 11.5, 11.5);
        C_WEST = Block.createCuboidShape(0, 4.5, 4.5, 5, 11.5, 11.5);
        C_NORTH = Block.createCuboidShape(4.5, 4.5, 0, 11.5, 11.5, 5);
        C_SOUTH = Block.createCuboidShape(4.5, 4.5, 11, 11.5, 11.5, 16);

        POWER = IntProperty.of("power", 0, MAX_POWER);
    }

    public Cable(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(UP, CableSide.NONE).with(DOWN, CableSide.NONE)
                .with(NORTH, CableSide.NONE).with(SOUTH, CableSide.NONE).with(EAST, CableSide.NONE)
                .with(WEST, CableSide.NONE).with(COLOR, CableColor.WHITE).with(POWER, 0));
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
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, COLOR, POWER);
    }

    static boolean isPowerable(BlockState state) {
        // any block that can be powered by a redstone signal
        // buttons and pressure plates are not included because they are not powerable, they are just sources
        return state.getBlock() instanceof RedstoneWireBlock || state.getBlock() instanceof RedstoneLampBlock ||
               state.getBlock() instanceof AbstractRedstoneGateBlock || state.getBlock() instanceof ButtonBlock ||
               state.getBlock() instanceof PistonBlock;

    }

    private BlockState updateCable(BlockState state, World world, BlockPos pos) {
        boolean shouldBePowered = false;
        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            BlockState offsetState = world.getBlockState(offset);
            if (offsetState.getBlock() instanceof Cable
                    // && offsetState.get(COLOR) == state.get(COLOR)
            ) {
                state = state.with(getProperty(direction), CableSide.CONNECTED);
                if (offsetState.get(POWER) > 0) { // if neighbour is powered
                    if (offsetState.get(POWER) < state.get(POWER) || state.get(POWER) == 0) { // if cable has better power than this one
                        if (offsetState.get(POWER)+1 < MAX_POWER) {
                            state = state.with(POWER, offsetState.get(POWER) + 1); // power this cable
                            shouldBePowered = true;
                        }
                    }
                }
            } else if (offsetState.emitsRedstonePower() | isPowerable(offsetState)) {
                state = state.with(getProperty(direction), CableSide.CONNECTED);
                if (offsetState.getBlock() instanceof AbstractRedstoneGateBlock && offsetState.get(Properties.HORIZONTAL_FACING) != direction.getOpposite() && offsetState.get(Properties.HORIZONTAL_FACING) != direction) {
                    state = state.with(getProperty(direction), CableSide.NONE);
                }
                if (offsetState.getBlock() instanceof AbstractRedstoneGateBlock) {
                    int power = offsetState.getStrongRedstonePower(world, offset, direction);
                    if (power > 0) {
                        state = state.with(POWER, 1);
                        shouldBePowered = true;
                    }
                }
            } else {
                state = state.with(getProperty(direction), CableSide.NONE);
            }
        }
        if (!shouldBePowered) {
            state = state.with(POWER, 0);
        }

        for (Direction direction : Direction.values()) {
            if (state.get(getProperty(direction)) == CableSide.CONNECTED && state.get(POWER) > 0) {
                state = state.with(getProperty(direction), CableSide.POWERED);
            }
        }

        return state;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.setBlockState(pos, updateCable(state, world, pos));
        world.updateNeighborsAlways(pos, this);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            world.updateNeighborsAlways(pos, this);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        world.setBlockState(pos, updateCable(state, world, pos));
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, updateCable(state, world, pos));
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        world.setBlockState(pos, updateCable(state, world, pos));
    }





    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) { // for powering other redstone components
        return state.get(POWER) > 0 ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) { // for powering other redstone components
        return 0;
    }



}
