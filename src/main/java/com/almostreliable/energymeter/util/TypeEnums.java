package com.almostreliable.energymeter.util;

public final class TypeEnums {

    private TypeEnums() {}

    public enum ConnectionStatus {
        DISCONNECTED, IDLE, SPLITTING, TRANSFERRING, CONSUMING
    }

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

    public enum MeasureMode {
        EXACT, INTERVAL
    }
}
