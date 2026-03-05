package com.almostreliable.energymeter.core;

import com.almostreliable.energymeter.block.entity.MeterBlockEntity;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class Config {

    public static final CommonConfig COMMON;
    private static final ModConfigSpec COMMON_SPEC;

    static {
        var commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();
    }

    private Config() {}

    public static void init(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static final class CommonConfig {

        public final IntValue defaultInterval;
        public final BooleanValue allowMeterConnections;
        public final IntValue maxWidth;
        public final IntValue maxHeight;

        private CommonConfig(ModConfigSpec.Builder builder) {
            builder.push(Constants.METER_ID);
            defaultInterval = builder.comment("Default values for the meter interval and zero tolerance.")
                .comment("These values can be changed in the GUI for each meter individually.")
                .defineInRange("default_interval", MeterBlockEntity.DEFAULT_INTERVAL, 5, 200);
            allowMeterConnections = builder.comment("Whether or not meters can connect to each other.")
                .comment(
                    "Keep in mind that allowing meter connections can cause performance issues and allow players to abuse them as infinite cables.")
                .define("allow_meter_connections", false);
            builder.pop();

            builder.push(Constants.MONITOR_ID);
            maxWidth = builder.comment("Max width of monitor.").defineInRange("max_width", 32, 1, 128);
            maxHeight = builder.comment("Max height of monitor.").defineInRange("max_height", 18, 1, 128);
            builder.pop();
        }
    }
}
