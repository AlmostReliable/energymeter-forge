package com.github.almostreliable.energymeter.network;

import com.github.almostreliable.energymeter.meter.MeterContainer;
import com.github.almostreliable.energymeter.meter.MeterTile;
import com.github.almostreliable.energymeter.util.TypeEnums;
import com.github.almostreliable.energymeter.util.TypeEnums.SETTING;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SettingUpdatePacket {

    private SETTING setting;

    public SettingUpdatePacket(SETTING setting) {
        this.setting = setting;
    }

    private SettingUpdatePacket() {}

    static SettingUpdatePacket decode(PacketBuffer buffer) {
        SettingUpdatePacket packet = new SettingUpdatePacket();
        packet.setting = SETTING.values()[buffer.readInt()];
        return packet;
    }

    static void handle(SettingUpdatePacket packet, Supplier<? extends Context> context) {
        ServerPlayerEntity player = context.get().getSender();
        context.get().enqueueWork(() -> handlePacket(packet, player));
        context.get().setPacketHandled(true);
    }

    private static void handlePacket(SettingUpdatePacket packet, @Nullable ServerPlayerEntity player) {
        if (player != null && player.containerMenu instanceof MeterContainer) {
            MeterTile tile = ((MeterContainer) player.containerMenu).getTile();
            World level = tile.getLevel();
            if (level == null || !level.isLoaded(tile.getBlockPos())) return;
            tile.updateSetting(packet.setting);
            tile.setChanged();
        }
    }

    void encode(PacketBuffer buffer) {
        buffer.writeInt(setting.ordinal());
    }
}
