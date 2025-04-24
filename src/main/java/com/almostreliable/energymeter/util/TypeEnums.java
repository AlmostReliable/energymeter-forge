package com.almostreliable.energymeter.util;

public final class TypeEnums {

    private TypeEnums() {}

    /**
     * Enum to represent the type of the translation to identify its key inside the lang file.
     */
    public enum TranslateType {
        CONTAINER, LABEL, TOOLTIP, BLOCK_SIDE, IO_SETTING, NUMBER, STATUS, MODE, ACCURACY
    }

    /**
     * Enum to represent a setting for the different GUI buttons.
     */
    public enum Setting {
        NUMBER, MODE, ACCURACY
    }

    /**
     * Enum to represent the status of the meter.
     */
    public enum ConnectionStatus {
        DISCONNECTED, IDLE, SPLITTING, TRANSFERRING, CONSUMING
    }

    /**
     * Enum to represent the mode of the meter.
     */
    public enum TransferMode implements EnumExtension {

        SPLIT(true, true),
        TRANSFER(true, true),
        CONSUME(true, false);

        private final boolean requiresInput;
        private final boolean requiresOutput;

        TransferMode(boolean requiresInput, boolean requiresOutput) {
            this.requiresInput = requiresInput;
            this.requiresOutput = requiresOutput;
        }

        public boolean requiresInput() {
            return requiresInput;
        }

        public boolean requiresOutput() {
            return requiresOutput;
        }
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
