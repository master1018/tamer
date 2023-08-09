public final class CurrencyNames_zh_HK extends OpenListResourceBundle {
    public CurrencyNames_zh_HK() {
        ResourceBundle bundle = LocaleData.getCurrencyNames(Locale.TAIWAN);
        setParent(bundle);
    }
    protected final Object[][] getContents() {
        return new Object[][] {
            {"HKD", "HK$"},
            {"TWD", "TWD"},
        };
    }
}
