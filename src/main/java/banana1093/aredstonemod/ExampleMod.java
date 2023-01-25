package banana1093.aredstonemod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	private static final ItemGroup CMD_GROUP = FabricItemGroup.builder(new Identifier("aredstonemod", "cmd"))
			.icon(() -> new ItemStack(Items.COMMAND_BLOCK))
			.displayName(Text.of("Command Items"))
			.build();

	@Override
	public void onInitialize() {
		// ########################### ITEMS & BLOCKS ###########################
		Item hand = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "hand"), new Hand(new FabricItemSettings().maxCount(1)));

		Block cable1b_block = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "cable1bit"), new Cable1Bit(FabricBlockSettings.of(Material.METAL).breakInstantly().nonOpaque()));
		Registry.register(Registries.ITEM, new Identifier("aredstonemod", "cable1bit"), new BlockItem(cable1b_block, new FabricItemSettings()));


		// ########################### CREATIVE MENUS ###########################
		ItemStack handStack = new ItemStack(hand);
		NbtCompound nbt = new NbtCompound();
		nbt.putString("command", "say hello world");
		handStack.setNbt(nbt);
		ItemGroupEvents.modifyEntriesEvent(CMD_GROUP).register((content) -> {
			content.add(Items.COMMAND_BLOCK);
			content.add(Items.CHAIN_COMMAND_BLOCK);
			content.add(Items.REPEATING_COMMAND_BLOCK);
			content.add(Items.COMMAND_BLOCK_MINECART);
			content.add(Items.STRUCTURE_BLOCK);
			content.add(Items.STRUCTURE_VOID);
			content.add(Items.JIGSAW);
			content.add(Items.BARRIER);
			content.add(handStack);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register((content) -> {
			content.add(cable1b_block);
		});
	}
}