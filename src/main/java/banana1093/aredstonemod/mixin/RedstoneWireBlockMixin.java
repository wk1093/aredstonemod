package banana1093.aredstonemod.mixin;

import banana1093.aredstonemod.AbstractGate;
import banana1093.aredstonemod.InstaRepeater;
import banana1093.aredstonemod.MemoryGate;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
	@Inject(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z",
			at = @At("HEAD"), cancellable = true, remap = true)
	private static void connectsTo_tcd_mixin(BlockState state, Direction dir, CallbackInfoReturnable<Boolean> e)
	{
		//check for block type
		if(!(state.getBlock() instanceof AbstractGate gate))
			return;

		//check for null direction
		if(dir == null)
		{
			e.setReturnValue(false);
			e.cancel();
			return;
		}

		e.setReturnValue(gate.dustConnects(state, dir));
		e.cancel();
	}
}