package com.almostreliable.energymeter.data;

import com.almostreliable.energymeter.ModConstants;

import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class EnergyMeterLang extends LanguageProvider {

    public static final LangEntry ENERGY_RATE = LangEntry.label("energy_rate", "Energy Rate");
    public static final LangEntry TOTAL_ENERGY = LangEntry.label("total_energy", "Total Energy");
    public static final LangEntry DISPLAY_MODE = LangEntry.label("display_mode", "Display Mode");
    public static final LangEntry TRANSFER_MODE = LangEntry.label("transfer_mode", "Transfer Mode");
    public static final LangEntry MEASURE_MODE = LangEntry.label("measure_mode", "Measure Mode");
    public static final LangEntry CONNECTION_STATUS = LangEntry.label("connection_status", "Connection Status");
    public static final LangEntry INTERVAL = LangEntry.label("interval", "Interval");
    public static final LangEntry ZERO_TOLERANCE = LangEntry.label("zero_tolerance", "Tolerance");

    EnergyMeterLang(PackOutput output) {
        super(output, ModConstants.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        for (LangEntry entry : LangEntry.ENTRIES) {
            add(entry.key, entry.value);
        }
    }

    public record LangEntry(String key, String value) implements Supplier<MutableComponent> {

        private static final Set<LangEntry> ENTRIES = new HashSet<>();

        public static LangEntry of(String prefix, String id, String value) {
            LangEntry entry = new LangEntry(String.format("%s.%s.%s", prefix, ModConstants.MOD_ID, id), value);
            ENTRIES.add(entry);
            return entry;
        }

        private static LangEntry label(String id, String value) {
            return of("label", id, value);
        }

        @Override
        public MutableComponent get() {
            return Component.translatable(key, value);
        }
    }
}
