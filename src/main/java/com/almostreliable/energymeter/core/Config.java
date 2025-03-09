package com.almostreliable.energymeter.core;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

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

        public final ModConfigSpec.IntValue defaultInterval;
        public final ModConfigSpec.IntValue maxWidth;
        public final ModConfigSpec.IntValue maxHeight;

        private CommonConfig(ModConfigSpec.Builder builder) {
            builder.push(Constants.METER_ID);
            defaultInterval = builder.comment("Default interval of ticks.").defineInRange("default_interval", 5, 1, 200);
            builder.pop();

            builder.push(Constants.MONITOR_ID);
            maxWidth = builder.comment("Max width of monitor.").defineInRange("max_width", 32, 1, 128);
            maxHeight = builder.comment("Max height of monitor.").defineInRange("max_height", 18, 1, 128);
            builder.pop();
        }
    }
}
