package dev.rlnt.energymeter.core;

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
}
