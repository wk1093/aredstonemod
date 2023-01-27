package banana1093.aredstonemod;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
public class Cable extends Block implements BlockEntityProvider {
    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

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



    public static final EnumProperty<CableSide> UP;
    public static final EnumProperty<CableSide> DOWN;
    public static final EnumProperty<CableSide> NORTH;
    public static final EnumProperty<CableSide> SOUTH;
    public static final EnumProperty<CableSide> EAST;
    public static final EnumProperty<CableSide> WEST;

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
    }

    public Cable(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(UP, CableSide.NONE).with(DOWN, CableSide.NONE)
                .with(NORTH, CableSide.NONE).with(SOUTH, CableSide.NONE).with(EAST, CableSide.NONE)
                .with(WEST, CableSide.NONE));
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
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }

    static boolean isPowerable(BlockState state) {
        // any block that can be powered by a redstone signal
        // buttons and pressure plates are not included because they are not powerable, they are just sources
        return state.getBlock() instanceof RedstoneWireBlock || state.getBlock() instanceof RedstoneLampBlock ||
               state.getBlock() instanceof AbstractRedstoneGateBlock || state.getBlock() instanceof ButtonBlock ||
               state.getBlock() instanceof PistonBlock;

    }

    public BlockState updateCable(BlockState state, World world, BlockPos pos) {
        boolean shouldBePowered = false;
        CableBlockEntity thisEntity = (CableBlockEntity)world.getBlockEntity(pos);
        assert thisEntity != null;
        for (Direction direction : Direction.values()) {
            BlockPos offset = pos.offset(direction);
            BlockState offsetState = world.getBlockState(offset);
            if (offsetState.getBlock() instanceof Cable && ((CableBlockEntity)Objects.requireNonNull(world.getBlockEntity(offset))).color == thisEntity.color) {
                CableBlockEntity offEntity = (CableBlockEntity)world.getBlockEntity(offset);
                assert offEntity != null;
                state = state.with(getProperty(direction), CableSide.CONNECTED);
                if (offEntity.power > 0) { // if neighbour is powered
                    if (offEntity.power < thisEntity.power || thisEntity.power == 0) { // if cable has better power than this one
                        if (offEntity.power+1 < CableBlockEntity.MAX_POWER) {
                            //state = state.with(POWER, offEntity.power + 1); // power this cable
                            thisEntity.power = offEntity.power+1;
                            shouldBePowered = true;
                        }
                    }
                }
            } else if ((offsetState.emitsRedstonePower() | isPowerable(offsetState)) && !(offsetState.getBlock() instanceof Cable)) {
                state = state.with(getProperty(direction), CableSide.CONNECTED);
                if (offsetState.getBlock() instanceof AbstractRedstoneGateBlock && offsetState.get(Properties.HORIZONTAL_FACING) != direction.getOpposite() && offsetState.get(Properties.HORIZONTAL_FACING) != direction) {
                    state = state.with(getProperty(direction), CableSide.NONE);
                }
                if (offsetState.getBlock() instanceof AbstractRedstoneGateBlock) {
                    int power = offsetState.getStrongRedstonePower(world, offset, direction);
                    if (power > 0) {
                        thisEntity.power = 1;
                        shouldBePowered = true;
                    }
                }
            } else {
                state = state.with(getProperty(direction), CableSide.NONE);
            }
        }
        if (!shouldBePowered) {
            thisEntity.power = 0;
        }

        for (Direction direction : Direction.values()) {
            if (state.get(getProperty(direction)) == CableSide.CONNECTED && thisEntity.power > 0) {
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
        CableBlockEntity thisEntity = (CableBlockEntity)world.getBlockEntity(pos);
        assert thisEntity != null;
        return thisEntity.power > 0 ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) { // for powering other redstone components
        return 0;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.getStackInHand(hand).getItem() == ExampleMod.CABLE_BLOCK_ITEM) {
            // use hit.side to get the side of the block that was clicked and set the cable to that side
            CableBlockEntity thisEntity = (CableBlockEntity)world.getBlockEntity(pos);
            assert thisEntity != null;
            world.setBlockState(pos.offset(hit.getSide()), ExampleMod.CABLE_BLOCK.getDefaultState());
            CableBlockEntity newEntity = (CableBlockEntity)world.getBlockEntity(pos.offset(hit.getSide()));
            assert newEntity != null;
            newEntity.color = thisEntity.color;
            world.updateNeighborsAlways(pos, this);
            return ActionResult.SUCCESS;
        } else if (player.getStackInHand(hand).getItem() instanceof DyeItem) {
            CableBlockEntity thisEntity = (CableBlockEntity)world.getBlockEntity(pos);
            assert thisEntity != null;
            thisEntity.color = CableColor.dye(((DyeItem)player.getStackInHand(hand).getItem()).getColor());
            world.updateNeighborsAlways(pos, this);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // remove block entity
        world.removeBlockEntity(pos);

        super.onBreak(world, pos, state, player);

    }



}
