package banana1093.aredstonemod;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FastLamp extends Block {
    public static final BooleanProperty LIT;


    static {
        LIT = RedstoneTorchBlock.LIT;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(LIT, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            boolean bl = (Boolean)state.get(LIT);
            if (bl != world.isReceivingRedstonePower(pos)) {
                if (bl) {
                    if ((Boolean)state.get(LIT) && !world.isReceivingRedstonePower(pos)) {
                        world.setBlockState(pos, (BlockState)state.cycle(LIT), 2);
                    }
                } else {
                    world.setBlockState(pos, (BlockState)state.cycle(LIT), 2);
                }
            }

        }
    }
    public FastLamp(final AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)this.getDefaultState().with(LIT, false));
    }
}
