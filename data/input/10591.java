public class DecimalFormatSymbolsProviderImpl extends DecimalFormatSymbolsProvider {
    static Locale[] avail = {
        new Locale("ja", "JP", "osaka"),
        new Locale("ja", "JP", "kyoto"),
        Locale.JAPAN,
        new Locale("yy", "ZZ", "UUU")
    };
    static List<Locale> availList = Arrays.asList(avail);
    static String[] dialect = {
        "\u3084\u3002",
        "\u3069\u3059\u3002",
        "\u3067\u3059\u3002",
        "-yy-ZZ-UUU"
    };
    static HashMap<Locale, FooDecimalFormatSymbols> symbols = new HashMap<Locale, FooDecimalFormatSymbols>(4);
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public DecimalFormatSymbols getInstance(Locale locale) {
        if (!Utils.supportsLocale(availList, locale)) {
            throw new IllegalArgumentException("locale is not supported: "+locale);
        }
        FooDecimalFormatSymbols fdfs = symbols.get(locale);
        if (fdfs == null) {
            for (int index = 0; index < avail.length; index ++) {
                if (Utils.supportsLocale(avail[index], locale)) {
                    fdfs = new FooDecimalFormatSymbols(index);
                    symbols.put(locale, fdfs);
                    break;
                }
            }
        }
        return fdfs;
    }
    class FooDecimalFormatSymbols extends DecimalFormatSymbols {
        String dialect = "";
        String infinity = null;
        String nan = null;
        public FooDecimalFormatSymbols(int index) {
            super(DecimalFormatSymbolsProviderImpl.this.avail[index]);
            dialect = DecimalFormatSymbolsProviderImpl.this.dialect[index];
        }
        public String getInfinity() {
            if (infinity == null) {
                infinity = super.getInfinity() + dialect;
            }
            return infinity;
        }
        public void setInfinity(String infinity) {
            this.infinity = infinity;
        }
        public String getNaN() {
            if (nan == null) {
                nan = super.getNaN() + dialect;
            }
            return nan;
        }
        public void setNaN(String nan) {
            this.nan = nan;
        }
    }
}
