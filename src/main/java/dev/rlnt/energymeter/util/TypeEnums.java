package dev.rlnt.energymeter.util;

public class TypeEnums {

    private TypeEnums() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Defines the type of the translation to
     * identify its key inside the lang file.
     */
    public enum TRANSLATE_TYPE {
        CONTAINER,
        LABEL,
        TOOLTIP,
        BLOCK_SIDE,
        IO_SETTING,
        STATUS
    }

    /**
     * Defines the possible IO sides of a block.
     */
    public enum BLOCK_SIDE {
        BOTTOM,
        TOP,
        FRONT,
        BACK,
        LEFT,
        RIGHT
    }

    /**
     * Enum to represent the different IO settings for the side configuration.
     */
    public enum IO_SETTING {
        OFF,
        IN,
        OUT
    }

    /**
     * Enum to represent a setting for the different GUI buttons.
     */
    public enum SETTING {
        NUMBER,
        MODE
    }

    /**
     * Enum to represent the status of the meter.
     */
    public enum STATUS {
        DISCONNECTED,
        CONNECTED,
        TRANSFERRING,
        CONSUMING
    }

    /**
     * Enum to represent the mode of the meter.
     */
    public enum MODE {
        TRANSFER,
        CONSUMER
    }

    /**
     * Enum to represent the number mode of the meter.
     */
    public enum NUMBER_MODE {
        SHORT,
        LONG
    }
}
