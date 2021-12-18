package com.github.almostreliable.energymeter.util;

public final class TypeEnums {

    private TypeEnums() {}

    /**
     * Enum to represent the type of the translation to identify its key inside the lang file.
     */
    public enum TRANSLATE_TYPE {
        CONTAINER, LABEL, TOOLTIP, BLOCK_SIDE, IO_SETTING, NUMBER, STATUS, MODE, ACCURACY
    }

    /**
     * Enum to represent the possible IO sides of a block.
     */
    public enum BLOCK_SIDE {
        BOTTOM, TOP, FRONT, BACK, LEFT, RIGHT
    }

    /**
     * Enum to represent the different IO settings for the side configuration.
     */
    public enum IO_SETTING {
        OFF, IN, OUT
    }

    /**
     * Enum to represent a setting for the different GUI buttons.
     */
    public enum SETTING {
        NUMBER, MODE, ACCURACY
    }

    /**
     * Enum to represent the number mode of the meter.
     */
    public enum NUMBER_MODE {
        SHORT, LONG
    }

    /**
     * Enum to represent the status of the meter.
     */
    public enum STATUS {
        DISCONNECTED, CONNECTED, TRANSFERRING, CONSUMING
    }

    /**
     * Enum to represent the mode of the meter.
     */
    public enum MODE {
        TRANSFER, CONSUMER
    }

    /**
     * Enum to represent the calculation mode of the meter.
     */
    public enum ACCURACY {
        EXACT, INTERVAL
    }

    /**
     * Enum to represent the different types of text boxes.
     */
    public enum TEXT_BOX {
        INTERVAL, THRESHOLD
    }
}
