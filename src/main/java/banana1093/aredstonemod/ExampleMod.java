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
	public static Block INSTA_REPEATER_BLOCK;

	// ##### BLOCK ENTITIES #####
	public static BlockEntityType<CableBlockEntity> CABLE_BLOCK_ENTITY;

	// ##### ITEMS #####
	public static Item CABLE_BLOCK_ITEM;
	public static Item FAST_LAMP_ITEM;
	public static Item MEMORY_GATE_ITEM;
	public static Item INSTA_REPEATER_ITEM;

	@Override
	public void onInitialize() {
		// ##### BLOCKS #####
		CABLE_BLOCK = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "cable"), new Cable(FabricBlockSettings.of(Material.METAL).breakInstantly().nonOpaque()));
		FAST_LAMP_BLOCK = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "fast_lamp"), new FastLamp(FabricBlockSettings.of(Material.REDSTONE_LAMP).strength(0.3F).sounds(BlockSoundGroup.GLASS)));
		MEMORY_GATE_BLOCK = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "memory_gate"), new MemoryGate(FabricBlockSettings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD).nonOpaque()));
		INSTA_REPEATER_BLOCK = Registry.register(Registries.BLOCK, new Identifier("aredstonemod", "insta_repeater"), new InstaRepeater(FabricBlockSettings.of(Material.DECORATION).breakInstantly().sounds(BlockSoundGroup.WOOD).nonOpaque()));
		// ##### BLOCK ENTITIES #####
		CABLE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("aredstonemod", "cable_entity"), FabricBlockEntityTypeBuilder.create(CableBlockEntity::new, CABLE_BLOCK).build());

		// ##### ITEMS #####

		CABLE_BLOCK_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "cable"), new BlockItem(CABLE_BLOCK, new FabricItemSettings()));
		FAST_LAMP_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "fast_lamp"), new BlockItem(FAST_LAMP_BLOCK, new FabricItemSettings()));
		MEMORY_GATE_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "memory_gate"), new BlockItem(MEMORY_GATE_BLOCK, new FabricItemSettings()));
		INSTA_REPEATER_ITEM = Registry.register(Registries.ITEM, new Identifier("aredstonemod", "insta_repeater"), new BlockItem(INSTA_REPEATER_BLOCK, new FabricItemSettings()));

		// ########################### CREATIVE MENUS ###########################

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register((content) -> {
			content.add(CABLE_BLOCK);
			content.add(FAST_LAMP_BLOCK);
			content.add(MEMORY_GATE_BLOCK);
			content.add(INSTA_REPEATER_BLOCK);
		});
	}
}