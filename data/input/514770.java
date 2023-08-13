public final class Currency implements Serializable {
    private static final long serialVersionUID = -158308464356906721L;
    private static final Hashtable<String, Currency> codesToCurrencies = new Hashtable<String, Currency>();
    private static final Hashtable<Locale, Currency> localesToCurrencies = new Hashtable<Locale, Currency>();
    private final String currencyCode;
    private transient int defaultFractionDigits;
    private Currency(String currencyCode) {
        this.currencyCode = currencyCode;
        if (currencyCode.equals("XXX")) {
            this.defaultFractionDigits = -1;
            return;
        }
        this.defaultFractionDigits = Resources.getCurrencyFractionDigitsNative(currencyCode);
        if (defaultFractionDigits < 0) {
            throw new IllegalArgumentException(Msg.getString("K0322", currencyCode));
        }
    }
    public static Currency getInstance(String currencyCode) {
        Currency currency = codesToCurrencies.get(currencyCode);
        if (currency == null) {
            currency = new Currency(currencyCode);
            codesToCurrencies.put(currencyCode, currency);
        }
        return currency;
    }
    public static Currency getInstance(Locale locale) {
        Currency currency = localesToCurrencies.get(locale);
        if (currency != null) {
            return currency;
        }
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if (variant.length() > 0 && (variant.equals("EURO") || variant.equals("HK") ||
                variant.equals("PREEURO"))) {
            country = country + "_" + variant;
        }
        String currencyCode = Resources.getCurrencyCodeNative(country);
        if (currencyCode == null) {
            throw new IllegalArgumentException(Msg.getString("K0323", locale.toString()));
        } else if (currencyCode.equals("None")) {
            return null;
        }
        Currency result = getInstance(currencyCode);
        localesToCurrencies.put(locale, result);
        return result;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }
    public String getSymbol() {
        return getSymbol(Locale.getDefault());
    }
    public String getSymbol(Locale locale) {
        if (locale.getCountry().length() == 0) {
            return currencyCode;
        }
        LocaleData localeData = Resources.getLocaleData(locale);
        if (localeData.internationalCurrencySymbol.equals(currencyCode)) {
            return localeData.currencySymbol;
        }
        String symbol = Resources.getCurrencySymbolNative(locale.toString(), currencyCode);
        return symbol != null ? symbol : currencyCode;
    }
    public int getDefaultFractionDigits() {
        return defaultFractionDigits;
    }
    @Override
    public String toString() {
        return currencyCode;
    }
    private Object readResolve() {
        return getInstance(currencyCode);
    }
    private static ResourceBundle getCurrencyBundle(final Locale locale) {
        return AccessController.doPrivileged(new PrivilegedAction<ResourceBundle>() {
            public ResourceBundle run() {
                String bundle = "org.apache.harmony.luni.internal.locale.Currency";
                return ResourceBundle.getBundle(bundle, locale);
            }
        });
    }
}
