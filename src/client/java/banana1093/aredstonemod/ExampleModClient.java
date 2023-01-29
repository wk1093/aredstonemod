package banana1093.aredstonemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			assert world != null;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof CableBlockEntity) {
				System.out.println(((CableBlockEntity) blockEntity).getRgb());
				return ((CableBlockEntity) blockEntity).getRgb();
			}
			return 0xFFFFFF;
		}, ExampleMod.CABLE_BLOCK);
		BlockRenderLayerMap.INSTANCE.putBlock(ExampleMod.MEMORY_GATE_BLOCK, RenderLayer.getCutout());
		System.out.println("Client initialized");
	}
}