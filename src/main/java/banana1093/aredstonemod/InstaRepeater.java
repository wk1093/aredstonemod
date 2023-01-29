package banana1093.aredstonemod;

import net.minecraft.block.BlockState;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class InstaRepeater extends AbstractGate {
    public InstaRepeater(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState updateOutput(World world, BlockPos pos, BlockState state) {
        boolean powered = this.get(world, pos, state, state.get(FACING)) > 0;
        state = (BlockState)state.with(POWERED, powered);
        return state;
    }

    @Override
    public boolean dustConnects(BlockState state, Direction dir) {
        return dir == state.get(FACING) || dir == state.get(FACING).getOpposite();
    }
}
