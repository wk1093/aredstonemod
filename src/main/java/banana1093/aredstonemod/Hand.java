package banana1093.aredstonemod;

import com.mojang.brigadier.ParseResults;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

// hand is an item that can run a command when right-clicked, the command is specified in the item's nbt
public class Hand extends Item {

    public Hand(Settings settings) {
        super(settings);
    }

    // right click:
    // if the item has a command in its nbt, run the command
    // if the item has no command in its nbt, do nothing
    // if the item has a command in its nbt, but the command is invalid, do nothing
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, net.minecraft.util.Hand hand) {
        // get the item stack
        ItemStack stack = user.getStackInHand(hand);
        // get the command from the item's nbt
        String command = stack.getOrCreateNbt().getString("command");
        // if the command is not empty, run the command
        if (!command.isEmpty()) {
            // run the command
            MinecraftServer ms = user.getServer();
            if (ms == null) {
                ms = world.getServer();
            }
            if (ms == null) {
                return TypedActionResult.pass(stack);
            }

            CommandManager cm = ms.getCommandManager();
            ParseResults<ServerCommandSource> parseResults = cm.getDispatcher().parse(command, user.getCommandSource());
            cm.execute(parseResults, command);
        }
        // return the item stack
        return TypedActionResult.success(stack);
    }

    // make it so the item has a glint if it has a command in its nbt
    @Override
    public boolean hasGlint(ItemStack stack) {
        return !stack.getOrCreateNbt().getString("command").isEmpty();
    }

    // make it so when the item is given, it's name is the command
    @Override
    public Text getName(ItemStack stack) {
        return Text.of("");
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String command = stack.getOrCreateNbt().getString("command");
        if (command.isEmpty()) {
            tooltip.add(Text.of("None"));
        } else {
            tooltip.add(Text.of("/" + command));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        // if no command, remove item from inventory
        if (stack.getOrCreateNbt().getString("command").isEmpty()) {
            if (entity instanceof PlayerEntity) {
                ((PlayerEntity) entity).getInventory().removeStack(slot);
            }
        }
    }
}
