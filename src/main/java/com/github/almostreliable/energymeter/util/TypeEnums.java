package com.github.almostreliable.energymeter.util;

public final class TypeEnums {

    private TypeEnums() {}

    /**
     * Enum to represent the type of the translation to identify its key inside the lang file.
     */
    public enum TranslateType {
        CONTAINER, LABEL, TOOLTIP, BLOCK_SIDE, IO_SETTING, NUMBER, STATUS, MODE, ACCURACY
    }

    /**
     * Enum to represent the possible IO sides of a block.
     */
    public enum BlockSide {
        BOTTOM, TOP, FRONT, BACK, LEFT, RIGHT
    }

    /**
     * Enum to represent the different IO settings for the side configuration.
     */
    public enum IoSetting {
        OFF, IN, OUT
    }

    /**
     * Enum to represent a setting for the different GUI buttons.
     */
    public enum Setting {
        NUMBER, MODE, ACCURACY
    }

    /**
     * Enum to represent the number mode of the meter.
     */
    public enum DisplayMode {
        SHORT, LONG
    }

    /**
     * Enum to represent the status of the meter.
     */
    public enum Status {
        DISCONNECTED, CONNECTED, SPLITTING, TRANSFERRING, CONSUMING
    }

    /**
     * Enum to represent the mode of the meter.
     */
    public enum TransferMode {
        SPLIT, TRANSFER, CONSUME
    }

    /**
     * Enum to represent the calculation mode of the meter.
     */
    public enum MeasureMode {
        EXACT, INTERVAL
    }

    /**
     * Enum to represent the different types of text boxes.
     */
    public enum TextBox {
        INTERVAL, THRESHOLD
    }
}
