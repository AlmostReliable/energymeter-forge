package com.almostreliable.energymeter.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class NumberFormatter {

    private static final DecimalFormat DECIMAL_FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
    private static final String[] UNIT_PREFIXES = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};

    static {
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.DOWN);
        DECIMAL_FORMAT.setMinimumFractionDigits(1);
        DECIMAL_FORMAT.setMaximumFractionDigits(3);
    }

    private NumberFormatter() {}

    public static String formatEnergyRate(double energy) {
        return formatTotalEnergy(energy) + "/t";
    }

    public static String formatTotalEnergy(double energy) {
        int index = 0;
        while (energy >= 1_000 && index < UNIT_PREFIXES.length - 1) {
            energy /= 1_000;
            index++;
        }

        return DECIMAL_FORMAT.format(energy) + " " + UNIT_PREFIXES[index] + "FE";
    }
}
