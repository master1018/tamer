public abstract class CurrencyNameProvider extends LocaleServiceProvider {
    protected CurrencyNameProvider() {
    }
    public abstract String getSymbol(String currencyCode, Locale locale);
    public String getDisplayName(String currencyCode, Locale locale) {
        if (currencyCode == null || locale == null) {
            throw new NullPointerException();
        }
        char[] charray = currencyCode.toCharArray();
        if (charray.length != 3) {
            throw new IllegalArgumentException("The currencyCode is not in the form of three upper-case letters.");
        }
        for (char c : charray) {
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("The currencyCode is not in the form of three upper-case letters.");
            }
        }
        List<Locale> avail = Arrays.asList(getAvailableLocales());
        if (!avail.contains(locale)) {
            throw new IllegalArgumentException("The locale is not available");
        }
        return null;
    }
}
