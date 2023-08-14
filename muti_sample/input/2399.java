public class NumberFormatProviderImpl extends NumberFormatProvider {
    static Locale[] avail = {
        Locale.JAPAN,
        new Locale("ja", "JP", "osaka"),
        new Locale("ja", "JP", "kyoto"),
        new Locale("zz")};
    static String[] dialect = {
        "\u3067\u3059\u3002",
        "\u3084\u3002",
        "\u3069\u3059\u3002",
        "-zz"
    };
    static String[] patterns = {
        "#,##0.###{0};-#,##0.###{1}", 
        "#{0};(#){1}", 
        "\u00A4#,##0{0};-\u00A4#,##0{1}", 
        "#,##0%{0}" 
    };
    static final int NUMBERSTYLE = 0;
    static final int INTEGERSTYLE = 1;
    static final int CURRENCYSTYLE = 2;
    static final int PERCENTSTYLE = 3;
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public NumberFormat getCurrencyInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                String pattern =
                    MessageFormat.format(patterns[CURRENCYSTYLE],
                                         dialect[i],
                                         dialect[i]);
                FooNumberFormat nf = new FooNumberFormat(pattern,
                    DecimalFormatSymbols.getInstance(locale));
                adjustForCurrencyDefaultFractionDigits(nf);
                return nf;
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public NumberFormat getIntegerInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                String pattern =
                    MessageFormat.format(patterns[INTEGERSTYLE],
                                         dialect[i],
                                         dialect[i]);
                FooNumberFormat nf = new FooNumberFormat(pattern,
                    DecimalFormatSymbols.getInstance(locale));
                nf.setMaximumFractionDigits(0);
                nf.setDecimalSeparatorAlwaysShown(false);
                nf.setParseIntegerOnly(true);
                return nf;
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public NumberFormat getNumberInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                String pattern =
                    MessageFormat.format(patterns[NUMBERSTYLE],
                                         dialect[i],
                                         dialect[i]);
                return new FooNumberFormat(pattern,
                    DecimalFormatSymbols.getInstance(locale));
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    public NumberFormat getPercentInstance(Locale locale) {
        for (int i = 0; i < avail.length; i ++) {
            if (Utils.supportsLocale(avail[i], locale)) {
                String pattern =
                    MessageFormat.format(patterns[PERCENTSTYLE],
                                         dialect[i]);
                return new FooNumberFormat(pattern,
                    DecimalFormatSymbols.getInstance(locale));
            }
        }
        throw new IllegalArgumentException("locale is not supported: "+locale);
    }
    void adjustForCurrencyDefaultFractionDigits(FooNumberFormat nf) {
        DecimalFormatSymbols dfs = nf.getDecimalFormatSymbols();
        Currency currency = dfs.getCurrency();
        if (currency == null) {
            try {
                currency = Currency.getInstance(dfs.getInternationalCurrencySymbol());
            } catch (IllegalArgumentException e) {
            }
        }
        if (currency != null) {
            int digits = currency.getDefaultFractionDigits();
            if (digits != -1) {
                int oldMinDigits = nf.getMinimumFractionDigits();
                if (oldMinDigits == nf.getMaximumFractionDigits()) {
                    nf.setMinimumFractionDigits(digits);
                    nf.setMaximumFractionDigits(digits);
                } else {
                    nf.setMinimumFractionDigits(Math.min(digits, oldMinDigits));
                    nf.setMaximumFractionDigits(digits);
                }
            }
        }
    }
}
