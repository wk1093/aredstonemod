package banana1093.aredstonemod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {

	// ##### BLOCKS #####
	public static Block CABLE_BLOCK;
	public static Block FAST_LAMP_BLOCK;
	public static Block MEMORY_GATE_BLOCK;

	// ##### BLOCK ENTITIES #####
	public static BlockEntityType<CableBlockEntity> CABLE_BLOCK_ENTITY;

	// ##### ITEMS #####
	public static Item HAND_ITEM;
	public static Item CABLE_BLOCK_ITEM;
	public static Item FAST_LAMP_ITEM;
	public static Item MEMORY_GATE_ITEM;

	@Override
	public void onInitialize() {
		// ##### GROUPS #####
		ItemGroup CMD_GROUP = FabricItemGroup.builder(new Identifier("aredstonemod", "cmd")).icon(() -> new ItemStack(Items.COMMAND_BLOCK)).displayName(Text.of("Command Items")).build();

		// ##### BLOCKS #####
		CABLE_BLOCK = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "cable"), new Cable(FabricBlockSettings.of(Material.METAL).breakInstantly().nonOpaque()));
		FAST_LAMP_BLOCK = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "fast_lamp"), new FastLamp(FabricBlockSettings.of(Material.REDSTONE_LAMP).strength(0.3F).sounds(BlockSoundGroup.GLASS)));
		MEMORY_GATE_BLOCK = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "memory_gate"), new MemoryGate(FabricBlockSettings.of(Material.METAL).breakInstantly().nonOpaque()));

		// ##### BLOCK ENTITIES #####
		CABLE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("aredstonemod", "cable_entity"), FabricBlockEntityTypeBuilder.create(CableBlockEntity::new, CABLE_BLOCK).build());

		// ##### ITEMS #####

		HAND_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "hand"), new Hand(new FabricItemSettings().maxCount(1)));
		CABLE_BLOCK_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "cable"), new BlockItem(CABLE_BLOCK, new FabricItemSettings()));
		FAST_LAMP_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "fast_lamp"), new BlockItem(FAST_LAMP_BLOCK, new FabricItemSettings()));
		MEMORY_GATE_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "memory_gate"), new BlockItem(MEMORY_GATE_BLOCK, new FabricItemSettings()));

		// ########################### CREATIVE MENUS ###########################
		ItemStack handStack = new ItemStack(HAND_ITEM);
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
			content.add(CABLE_BLOCK);
			content.add(FAST_LAMP_BLOCK);
			content.add(MEMORY_GATE_BLOCK);
		});
	}
}