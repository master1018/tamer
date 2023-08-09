public final class CurrencyNames_zh_SG extends OpenListResourceBundle {
    public CurrencyNames_zh_SG() {
        ResourceBundle bundle = LocaleData.getCurrencyNames(Locale.CHINA);
        setParent(bundle);
    }
    protected final Object[][] getContents() {
        return new Object[][] {
            {"CNY", "CNY"},
            {"SGD", "S$"},
        };
    }
}
