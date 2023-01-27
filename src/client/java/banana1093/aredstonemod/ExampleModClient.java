package banana1093.aredstonemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			assert world != null;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof CableBlockEntity) {
				return ((CableBlockEntity) blockEntity).getRgb();
			}
			return 0xFFFFFF;
		}, ExampleMod.CABLE_BLOCK);
	}
}