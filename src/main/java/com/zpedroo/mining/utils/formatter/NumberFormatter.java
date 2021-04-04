package com.zpedroo.mining.utils.formatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberFormatter {

    public static String fixDecimal(double number) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        return formatter.format(number);
    }

    public static String formatNumber(double number) {
        if (number < 1000.0) {
            return fixNumber(number);
        }
        if (number < 1000000.0) {
            return fixNumber(number / 1000.0) + "k";
        }
        if (number < 1.0E9) {
            return fixNumber(number / 1000000.0) + "M";
        }
        if (number < 1.0E12) {
            return fixNumber(number / 1.0E9) + "B";
        }
        if (number < 1.0E15) {
            return fixNumber(number / 1.0E12) + "T";
        }
        if (number < 1.0E18) {
            return fixNumber(number / 1.0E15) + "Q";
        }
        if (number < 1.0E21) {
            return fixNumber(number / 1.0E18) + "QQ";
        }
        if (number < 1.0E24) {
            return fixNumber(number / 1.0E21) + "S";
        }
        if (number < 1.0E27) {
            return fixNumber(number / 1.0E24) + "SS";
        }
        if (number < 1.0E30) {
            return fixNumber(number / 1.0E27) + "O";
        }
        if (number < 1.0E33) {
            return fixNumber(number / 1.0E30) + "N";
        }
        if (number < 1.0E36) {
            return fixNumber(number / 1.0E33) + "D";
        }
        return String.valueOf(number);
    }

    private static String fixNumber(double number) {
        NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format.format(number);
    }
}