package com.almostreliable.energymeter.core;

import com.almostreliable.energymeter.network.ClientSyncPacket;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public interface Constants {

    // ids
    String METER_ID = "meter";
    String MONITOR_ID = "monitor";

    // serialization
    String SIDE_CONFIG_ID = "side_config";
    String TRANSFER_RATE_ID = "transfer_rate";
    String DISPLAY_MODE_ID = "display_mode";
    String TRANSFER_MODE_ID = "transfer_mode";
    String MEASURE_MODE_ID = "measure_mode";
    String MEASURE_INTERVAL_ID = "measure_interval";
    String ZERO_TOLERANCE_ID = "zero_tolerance";
    public static final String WIDTH_ID = "width";
    public static final String HEIGHT_ID = "height";

    // mod info
    String PIPEZ_ID = "pipez";
    String CCT_ID = "computercraft";
    // translation & nbt keys
    String STATUS_ID = "status";
    String IO_SIDE_ID = "io_side";
    String IO_MODE_ID = "io_mode";
    String IO_SCREEN_ID = "screen";

    /**
     * Decimal color values to ensure consistent color values.
     */
    interface UiColors {

        int WHITE = 15_790_320;
        int MINT = 65_442;
        int GRAY = 11_447_982;
        int GREEN = 65_328;
        int BLUE = 40_929;
        int YELLOW = 16_768_512;
        int ORANGE = 16_737_792;
        int PURPLE = 12_976_383;
        int PINK = 16_711_782;
        int ROSE = 16_711_920;
        int RED = 14_286_889;
    }

    /**
     * Flags to ensure only required data is synced in a {@link ClientSyncPacket}.
     */
    interface SyncFlags {

        int SIDE_CONFIG = 1;
        int TRANSFER_RATE = 1 << 1;
        int NUMBER_MODE = 1 << 2;
        int STATUS = 1 << 3;
        int MODE = 1 << 4;
        int ACCURACY = 1 << 5;
        int INTERVAL = 1 << 6;
        int THRESHOLD = 1 << 7;
    }
}
