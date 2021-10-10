package dev.rlnt.energymeter.core;

import dev.rlnt.energymeter.network.ClientSyncPacket;

public class Constants {

    // mod info
    public static final String MOD_ID = "energymeter";
    public static final String PIPEZ_ID = "pipez";
    // ids
    public static final String METER_ID = "meter";
    // utility keys
    public static final String IO_STATE_ID = "io";
    public static final String NETWORK_ID = "network";
    // translation & nbt keys
    public static final String SIDE_CONFIG_ID = "side_config";
    public static final String TRANSFER_RATE_ID = "transfer_rate";
    public static final String STATUS_ID = "status";
    public static final String NUMBER_MODE_ID = "number_mode";
    public static final String MODE_ID = "mode";
    public static final String IO_SIDE_ID = "io_side";
    public static final String IO_MODE_ID = "io_mode";
    public static final String IO_SCREEN_ID = "screen";

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Flags to esnure only required data is synced in a {@link ClientSyncPacket}.
     */
    public static class SyncFlags {

        public static final int SIDE_CONFIG = (1);
        public static final int TRANSFER_RATE = (1 << 1);
        public static final int STATUS = (1 << 2);
        public static final int NUMBER_MODE = (1 << 3);
        public static final int MODE = (1 << 4);

        private SyncFlags() {}
    }
}
