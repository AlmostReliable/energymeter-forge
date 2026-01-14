package com.almostreliable.energymeter.util;

import java.util.function.Supplier;

public final class TypeEnums {

    private TypeEnums() {}

    public enum ConnectionStatus {
        DISCONNECTED, IDLE, SPLITTING, TRANSFERRING, CONSUMING
    }

    public enum TransferMode implements EnumExtension {

        SPLIT(true, true, ConnectionStatus.SPLITTING),
        TRANSFER(true, true, ConnectionStatus.TRANSFERRING),
        CONSUME(true, false, ConnectionStatus.CONSUMING);

        private final boolean requiresInput;
        private final boolean requiresOutput;
        public final ConnectionStatus activeStatus;

        TransferMode(boolean requiresInput, boolean requiresOutput, ConnectionStatus activeStatus) {
            this.requiresInput = requiresInput;
            this.requiresOutput = requiresOutput;
            this.activeStatus = activeStatus;
        }

        public boolean isCorrectlyConfigured(Supplier<Boolean> hasInput, Supplier<Boolean> hasOutput) {
            return (!requiresInput || hasInput.get()) && (!requiresOutput || hasOutput.get());
        }
    }

    public enum MeasureMode {
        EXACT, INTERVAL
    }
}
