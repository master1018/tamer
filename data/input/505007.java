public class LocaleData {
    public Integer firstDayOfWeek;
    public Integer minimalDaysInFirstWeek;
    public String[] amPm;
    public String[] eras;
    public String[] longMonthNames;
    public String[] shortMonthNames;
    public String[] longWeekdayNames;
    public String[] shortWeekdayNames;
    public String fullTimeFormat;
    public String longTimeFormat;
    public String mediumTimeFormat;
    public String shortTimeFormat;
    public String fullDateFormat;
    public String longDateFormat;
    public String mediumDateFormat;
    public String shortDateFormat;
    public String decimalPatternChars;
    public String infinity;
    public String NaN;
    public String currencySymbol;
    public String internationalCurrencySymbol;
    public String numberPattern;
    public String integerPattern;
    public String currencyPattern;
    public String percentPattern;
    @Override public String toString() {
        return "LocaleData[" +
                "firstDayOfWeek=" + firstDayOfWeek + "," +
                "minimalDaysInFirstWeek=" + minimalDaysInFirstWeek + "," +
                "amPm=" + amPm + "," +
                "eras=" + eras + "," +
                "longMonthNames=" + longMonthNames + "," +
                "shortMonthNames=" + shortMonthNames + "," +
                "longWeekdayNames=" + longWeekdayNames + "," +
                "shortWeekdayNames=" + shortWeekdayNames + "," +
                "fullTimeFormat=" + fullTimeFormat + "," +
                "longTimeFormat=" + longTimeFormat + "," +
                "mediumTimeFormat=" + mediumTimeFormat + "," +
                "shortTimeFormat=" + shortTimeFormat + "," +
                "fullDateFormat=" + fullDateFormat + "," +
                "longDateFormat=" + longDateFormat + "," +
                "mediumDateFormat=" + mediumDateFormat + "," +
                "shortDateFormat=" + shortDateFormat + "," +
                "decimalPatternChars=" + decimalPatternChars + "," +
                "infinity=" + infinity + "," +
                "NaN=" + NaN + "," +
                "currencySymbol=" + currencySymbol + "," +
                "internationalCurrencySymbol=" + internationalCurrencySymbol + "," +
                "numberPattern=" + numberPattern + "," +
                "integerPattern=" + integerPattern + "," +
                "currencyPattern=" + currencyPattern + "," +
                "percentPattern=" + percentPattern + "]";
    }
    public void overrideWithDataFrom(LocaleData overrides) {
        if (overrides.firstDayOfWeek != null) {
            firstDayOfWeek = overrides.firstDayOfWeek;
        }
        if (overrides.minimalDaysInFirstWeek != null) {
            minimalDaysInFirstWeek = overrides.minimalDaysInFirstWeek;
        }
        if (overrides.amPm != null) {
            amPm = overrides.amPm;
        }
        if (overrides.eras != null) {
            eras = overrides.eras;
        }
        if (overrides.longMonthNames != null) {
            longMonthNames = overrides.longMonthNames;
        }
        if (overrides.shortMonthNames != null) {
            shortMonthNames = overrides.shortMonthNames;
        }
        if (overrides.longWeekdayNames != null) {
            longWeekdayNames = overrides.longWeekdayNames;
        }
        if (overrides.shortWeekdayNames != null) {
            shortWeekdayNames = overrides.shortWeekdayNames;
        }
        if (overrides.fullTimeFormat != null) {
            fullTimeFormat = overrides.fullTimeFormat;
        }
        if (overrides.longTimeFormat != null) {
            longTimeFormat = overrides.longTimeFormat;
        }
        if (overrides.mediumTimeFormat != null) {
            mediumTimeFormat = overrides.mediumTimeFormat;
        }
        if (overrides.shortTimeFormat != null) {
            shortTimeFormat = overrides.shortTimeFormat;
        }
        if (overrides.fullDateFormat != null) {
            fullDateFormat = overrides.fullDateFormat;
        }
        if (overrides.longDateFormat != null) {
            longDateFormat = overrides.longDateFormat;
        }
        if (overrides.mediumDateFormat != null) {
            mediumDateFormat = overrides.mediumDateFormat;
        }
        if (overrides.shortDateFormat != null) {
            shortDateFormat = overrides.shortDateFormat;
        }
        if (overrides.decimalPatternChars != null) {
            decimalPatternChars = overrides.decimalPatternChars;
        }
        if (overrides.NaN != null) {
            NaN = overrides.NaN;
        }
        if (overrides.infinity != null) {
            infinity = overrides.infinity;
        }
        if (overrides.currencySymbol != null) {
            currencySymbol = overrides.currencySymbol;
        }
        if (overrides.internationalCurrencySymbol != null) {
            internationalCurrencySymbol = overrides.internationalCurrencySymbol;
        }
        if (overrides.numberPattern != null) {
            numberPattern = overrides.numberPattern;
        }
        if (overrides.integerPattern != null) {
            integerPattern = overrides.integerPattern;
        }
        if (overrides.currencyPattern != null) {
            currencyPattern = overrides.currencyPattern;
        }
        if (overrides.percentPattern != null) {
            percentPattern = overrides.percentPattern;
        }
    }
    public String getDateFormat(int style) {
        switch (style) {
        case DateFormat.SHORT:
            return shortDateFormat;
        case DateFormat.MEDIUM:
            return mediumDateFormat;
        case DateFormat.LONG:
            return longDateFormat;
        case DateFormat.FULL:
            return fullDateFormat;
        }
        throw new AssertionError();
    }
    public String getTimeFormat(int style) {
        switch (style) {
        case DateFormat.SHORT:
            return shortTimeFormat;
        case DateFormat.MEDIUM:
            return mediumTimeFormat;
        case DateFormat.LONG:
            return longTimeFormat;
        case DateFormat.FULL:
            return fullTimeFormat;
        }
        throw new AssertionError();
    }
}
