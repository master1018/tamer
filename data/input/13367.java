public class Bug6807534 {
    static final CurrencyNameProvider cnp = new CurrencyNameProviderImpl();
    public static void main(String[] args) throws Exception {
        try {
            cnp.getDisplayName(null, Locale.US);
            throwException("NPE was not thrown with null currencyCode");
        } catch (NullPointerException npe) {}
        try {
            cnp.getDisplayName("USD", null);
            throwException("NPE was not thrown with null locale");
        } catch (NullPointerException npe) {}
        try {
            cnp.getDisplayName("INVALID", Locale.US);
            throwException("IllegalArgumentException was not thrown with invalid currency code");
        } catch (IllegalArgumentException iae) {}
        try {
            cnp.getDisplayName("inv", Locale.US);
            throwException("IllegalArgumentException was not thrown with invalid currency code");
        } catch (IllegalArgumentException iae) {}
        try {
            cnp.getDisplayName("USD", Locale.JAPAN);
            throwException("IllegalArgumentException was not thrown with non-supported locale");
        } catch (IllegalArgumentException iae) {}
    }
    static void throwException(String msg) {
        throw new RuntimeException("test failed. "+msg);
    }
    static class CurrencyNameProviderImpl extends CurrencyNameProvider {
        public String getSymbol(String currencyCode, Locale locale) {
            return "";
        }
        public Locale[] getAvailableLocales() {
            Locale[] avail = new Locale[1];
            avail[0] = Locale.US;
            return avail;
        }
    }
}
