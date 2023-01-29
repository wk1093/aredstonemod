package banana1093.aredstonemod;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.DyeItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CableBlockEntity extends BlockEntity {

    public CableColor color = CableColor.WHITE;
    public int power = 0;
    public static final int MAX_POWER = 15;


    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ExampleMod.CABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        // Save the current value of the number to the nbt
        nbt.putInt("power", power);
        nbt.putString("color", color.toString());

        super.writeNbt(nbt);
    }



    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        power = nbt.getInt("number");
        color = CableColor.valueOf(nbt.getString("color").toUpperCase());
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    int getRgb() {
        return color.getRgb();
    }



}
