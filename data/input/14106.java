public class LocaleNameProviderImpl extends LocaleNameProvider {
    static Locale[] avail = {Locale.JAPANESE,
                             Locale.JAPAN,
                             new Locale("ja", "JP", "osaka"),
                             new Locale("ja", "JP", "kyoto"),
                             new Locale("xx")};
    static List<Locale> availList = Arrays.asList(avail);
    public Locale[] getAvailableLocales() {
        return avail;
    }
    public String getDisplayLanguage(String lang, Locale target) {
        if (!Utils.supportsLocale(availList, target)) {
            throw new IllegalArgumentException("locale is not supported: "+target);
        }
        String ret = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("com.bar.LocaleNames", target);
            ret = rb.getString(lang);
        } catch (MissingResourceException mre) {
        }
        return ret;
    }
    public String getDisplayCountry(String ctry, Locale target) {
        if (!Utils.supportsLocale(availList, target)) {
            throw new IllegalArgumentException("locale is not supported: "+target);
        }
        String ret = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("LocaleNames", target);
            ret = rb.getString(ctry);
        } catch (MissingResourceException mre) {
        }
        return ret;
    }
    public String getDisplayVariant(String vrnt, Locale target) {
        if (!Utils.supportsLocale(availList, target)) {
            throw new IllegalArgumentException("locale is not supported: "+target);
        }
        String ret = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("LocaleNames", target);
            ret = rb.getString(vrnt);
        } catch (MissingResourceException mre) {
        }
        return ret;
    }
}
