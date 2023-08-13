public class DecimalFormatSymbolsProviderTest extends ProviderTest {
    com.foo.DecimalFormatSymbolsProviderImpl dfsp = new com.foo.DecimalFormatSymbolsProviderImpl();
    List<Locale> availloc = Arrays.asList(DecimalFormatSymbols.getAvailableLocales());
    List<Locale> providerloc = Arrays.asList(dfsp.getAvailableLocales());
    List<Locale> jreloc = Arrays.asList(LocaleData.getAvailableLocales());
    public static void main(String[] s) {
        new DecimalFormatSymbolsProviderTest();
    }
    DecimalFormatSymbolsProviderTest() {
        availableLocalesTest();
        objectValidityTest();
    }
    void availableLocalesTest() {
        Set<Locale> localesFromAPI = new HashSet<Locale>(availloc);
        Set<Locale> localesExpected = new HashSet<Locale>(jreloc);
        localesExpected.addAll(providerloc);
        if (localesFromAPI.equals(localesExpected)) {
            System.out.println("availableLocalesTest passed.");
        } else {
            throw new RuntimeException("availableLocalesTest failed");
        }
    }
    void objectValidityTest() {
        for (Locale target: availloc) {
            ResourceBundle rb = LocaleData.getNumberFormatData(target);
            boolean jreSupportsLocale = jreloc.contains(target);
            String[] jres = new String[2];
            if (jreSupportsLocale) {
                try {
                    String[] tmp = rb.getStringArray("NumberElements");
                    jres[0] = tmp[9]; 
                    jres[1] = tmp[10]; 
                } catch (MissingResourceException mre) {}
            }
            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(target);
            String[] result = new String[2];
            result[0] = dfs.getInfinity();
            result[1] = dfs.getNaN();
            DecimalFormatSymbols providersDfs= null;
            String[] providers = new String[2];
            if (providerloc.contains(target)) {
                providersDfs = dfsp.getInstance(target);
                providers[0] = providersDfs.getInfinity();
                providers[1] = providersDfs.getNaN();
            }
            for (int i = 0; i < result.length; i ++) {
                checkValidity(target, jres[i], providers[i], result[i], jreSupportsLocale);
            }
        }
    }
}
