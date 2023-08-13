public class CurrencyNameProviderImpl extends CurrencyNameProvider {
    static Locale[] avail = {new Locale("ja", "JP", "osaka"),
        new Locale("ja", "JP", "kyoto"),
        Locale.JAPAN,
        new Locale("xx")};
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public String getSymbol(String c, Locale locale) {
        if (!Utils.supportsLocale(Arrays.asList(avail), locale)) {
            throw new IllegalArgumentException("locale is not supported: "+locale);
        }
        if (c.equals("JPY")) {
            if (Utils.supportsLocale(avail[0], locale)) {
                return "\u5186\u3084\u3002";
            } else if (Utils.supportsLocale(avail[1], locale)) {
                return "\u5186\u3069\u3059\u3002";
            } else if (Utils.supportsLocale(avail[2], locale)) {
                return "\u5186\u3067\u3059\u3002";
            } else if (Utils.supportsLocale(avail[3], locale)) {
                return "\u5186\u3070\u3064\u3070\u3064\u3002";
            }
        }
        return null;
    }
    @Override
    public String getDisplayName(String c, Locale locale) {
        if (!Utils.supportsLocale(Arrays.asList(avail), locale)) {
            throw new IllegalArgumentException("locale is not supported: "+locale);
        }
        if (c.equals("JPY")) {
            if (Utils.supportsLocale(avail[0], locale)) {
                return "\u65e5\u672c\u5186\u3084\u3002";
            } else if (Utils.supportsLocale(avail[1], locale)) {
                return "\u65e5\u672c\u5186\u3069\u3059\u3002";
            } else if (Utils.supportsLocale(avail[2], locale)) {
                return "\u65e5\u672c\u5186\u3067\u3059\u3002";
            } else if (Utils.supportsLocale(avail[3], locale)) {
                return "\u65e5\u672c\u5186\u3070\u3064\u3070\u3064\u3002";
            }
        }
        return null;
    }
}
