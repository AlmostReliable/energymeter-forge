package com.almostreliable.energymeter.network.packet;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;
import com.almostreliable.energymeter.util.Utils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class EnergyRateUpdatePacket implements CustomPacketPayload {

    public static final Type<EnergyRateUpdatePacket> TYPE = new Type<>(Utils.getRL("energy_rate_update"));
    public static final StreamCodec<FriendlyByteBuf, EnergyRateUpdatePacket> STREAM_CODEC = CustomPacketPayload.codec(
        EnergyRateUpdatePacket::encode,
        EnergyRateUpdatePacket::decode
    );

    private final BlockPos pos;
    private final double energyRate;

    public EnergyRateUpdatePacket(BlockPos pos, double energyRate) {
        this.pos = pos;
        this.energyRate = energyRate;
    }

    private static void encode(EnergyRateUpdatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeDouble(packet.energyRate);
    }

    private static EnergyRateUpdatePacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        double energyRate = buffer.readDouble();
        return new EnergyRateUpdatePacket(pos, energyRate);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EnergyRateUpdatePacket payload, IPayloadContext context) {
        // noinspection resource
        Level level = context.player().level();
        if (level.getBlockEntity(payload.pos) instanceof MeterBlockEntity entity) {
            entity.setEnergyRate(payload.energyRate);
        }
    }
}
