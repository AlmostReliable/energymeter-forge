package com.almostreliable.energymeter.data;

import com.almostreliable.energymeter.ModConstants;
import com.almostreliable.energymeter.block.component.IoConfig;
import com.almostreliable.energymeter.client.screen.MeterScreen;
import com.almostreliable.energymeter.client.screen.widget.BlockSideButton;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.common.data.LanguageProvider;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public final class EnergyMeterLang extends LanguageProvider {

    // @formatter:off

    // labels
    public static final LangEntry ENERGY_RATE = LangEntry.label("energy_rate", "Energy Rate");
    public static final LangEntry TOTAL_ENERGY = LangEntry.label("total_energy", "Total Energy");
    public static final LangEntry DISPLAY_MODE = LangEntry.label("display_mode", "Display Mode");
    public static final LangEntry TRANSFER_MODE = LangEntry.label("transfer_mode", "Transfer Mode");
    public static final LangEntry MEASURE_MODE = LangEntry.label("measure_mode", "Measure Mode");
    public static final LangEntry CONNECTION_STATUS = LangEntry.label("connection_status", "Connection Status");
    public static final LangEntry INTERVAL = LangEntry.label("interval", "Interval");
    public static final LangEntry ZERO_TOLERANCE = LangEntry.label("zero_tolerance", "Tolerance");
    public static final LangEntry TRANSFER_LIMIT = LangEntry.label("transfer_limit", "Transfer Limit");

    // tooltips
    public static final LangEntry CURRENT_SETTING = LangEntry.tooltip("current_setting", "Current Setting");
    public static final LangEntry DIRECTION = LangEntry.tooltip("direction", "Direction");
    public static final LangEntry OUTPUT_PRIORITY = LangEntry.tooltip("output_priority", "Output Priority");
    public static final LangEntry CYCLE_NEXT_SETTING = LangEntry.tooltip("cycle_next_setting", "Next setting");
    public static final LangEntry CYCLE_PREVIOUS_SETTING = LangEntry.tooltip("cycle_previous_setting", "Previous setting");
    public static final LangEntry SELECT_SETTING = LangEntry.tooltip("select_setting", "Select setting");
    public static final LangEntry RESET_SETTING = LangEntry.tooltip("reset_setting", "Reset setting");
    public static final LangEntry RESET_ALL_SETTINGS = LangEntry.tooltip("reset_all_settings", "Reset all settings");

    // controls
    public static final LangEntry LMB = LangEntry.control("lmb", "Click");
    public static final LangEntry RMB = LangEntry.control("rmb", "Right-Click");
    public static final LangEntry SHIFT = LangEntry.control("shift", "Shift");

    // enums
    public static final Map<BlockSideButton.BlockSide, LangEntry> BLOCK_SIDES = LangEntry.enumValues(BlockSideButton.BlockSide.values());
    public static final Map<Direction, LangEntry> DIRECTIONS = LangEntry.enumValues(Direction.values());
    public static final Map<IoConfig.IoSetting, LangEntry> IO_SETTINGS = LangEntry.enumValues(IoConfig.IoSetting.values());
    public static final Map<MeterScreen.TabType, LangEntry> TAB_TYPE = LangEntry.enumValues(MeterScreen.TabType.values());

    // @formatter:on

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

        private static LangEntry tooltip(String id, String value) {
            return of("tooltip", id, value);
        }

        private static LangEntry control(String id, String value) {
            return of("control", id, value);
        }

        @SafeVarargs
        private static <T extends Enum<?>> Map<T, LangEntry> enumValues(T... enumValues) {
            Map<T, LangEntry> enumEntries = new HashMap<>();

            String prefix = enumValues[0].getClass().getSimpleName().toLowerCase(Locale.ROOT);
            for (T enumValue : enumValues) {
                String id = enumValue.name().toLowerCase(Locale.ROOT);
                String value = StringUtils.capitalize(id);
                enumEntries.put(enumValue, of(prefix, id, value));
            }

            return enumEntries;
        }

        @Override
        public MutableComponent get() {
            return Component.translatable(key, value);
        }
    }
}
