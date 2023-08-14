public class DateFormatProviderImpl extends DateFormatProvider {
    static Locale[] avail = {
        Locale.JAPAN,
        new Locale("ja", "JP", "osaka"),
        new Locale("ja", "JP", "kyoto"),
        new Locale("yy")};
    static String[] datePattern = {
        "yyyy'\u5e74'M'\u6708'd'\u65e5'", 
        "yyyy/MMM/dd", 
        "yyyy/MM/dd", 
        "yy/MM/dd" 
    };
    static String[] timePattern = {
        "H'\u6642'mm'\u5206'ss'\u79d2' z", 
        "H:mm:ss z", 
        "H:mm:ss", 
        "H:mm" 
    };
    static String[] dialect = {
        "\u3067\u3059\u3002",
        "\u3084\u3002",
        "\u3069\u3059\u3002",
        "\u308f\u3044\u308f\u3044"
    };
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public DateFormat getDateInstance(int style, Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                return new FooDateFormat(datePattern[style]+dialect[i], locale);
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public DateFormat getTimeInstance(int style, Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                return new FooDateFormat(timePattern[style]+dialect[i], locale);
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                return new FooDateFormat(
                    datePattern[dateStyle]+" "+timePattern[timeStyle]+dialect[i], locale);
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
}
