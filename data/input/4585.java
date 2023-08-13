public class DateFormatSymbolsProviderTest extends ProviderTest {
    com.foo.DateFormatSymbolsProviderImpl dfsp = new com.foo.DateFormatSymbolsProviderImpl();
    List<Locale> availloc = Arrays.asList(DateFormatSymbols.getAvailableLocales());
    List<Locale> providerloc = Arrays.asList(dfsp.getAvailableLocales());
    List<Locale> jreloc = Arrays.asList(LocaleData.getAvailableLocales());
    public static void main(String[] s) {
        new DateFormatSymbolsProviderTest();
    }
    DateFormatSymbolsProviderTest() {
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
            ResourceBundle rb = LocaleData.getDateFormatData(target);
            boolean jreSupportsLocale = jreloc.contains(target);
            String[][] jres = new String[6][];
            if (jreSupportsLocale) {
                try {
                    jres[0] = (String[])rb.getObject("MonthNames");
                    jres[1] = (String[])rb.getObject("MonthAbbreviations");
                    jres[2] = (String[])rb.getObject("DayNames");
                    jres[3] = (String[])rb.getObject("DayAbbreviations");
                    jres[4] = (String[])rb.getObject("AmPmMarkers");
                    jres[5] = (String[])rb.getObject("Eras");
                } catch (MissingResourceException mre) {}
            }
            DateFormatSymbols dfs = DateFormatSymbols.getInstance(target);
            String[][] result = new String[6][];
            result[0] = dfs.getMonths();
            result[1] = dfs.getShortMonths();
            String[] tmp = dfs.getWeekdays();
            result[2] = new String[7];
            System.arraycopy(tmp, 1, result[2], 0, result[2].length);
            tmp = dfs.getShortWeekdays();
            result[3] = new String[7];
            System.arraycopy(tmp, 1, result[3], 0, result[3].length);
            result[4] = dfs.getAmPmStrings();
            result[5] = dfs.getEras();
            DateFormatSymbols providersDfs= null;
            String[][] providers = new String[6][];
            if (providerloc.contains(target)) {
                providersDfs = dfsp.getInstance(target);
                providers[0] = providersDfs.getMonths();
                providers[1] = providersDfs.getShortMonths();
                tmp = dfs.getWeekdays();
                providers[2] = new String[7];
                System.arraycopy(tmp, 1, providers[2], 0, providers[2].length);
                tmp = dfs.getShortWeekdays();
                providers[3] = new String[7];
                System.arraycopy(tmp, 1, providers[3], 0, providers[3].length);
                providers[4] = providersDfs.getAmPmStrings();
                providers[5] = providersDfs.getEras();
            }
            for (int i = 0; i < result.length; i ++) {
                for (int j = 0; j < result[i].length; j++) {
                    String jresStr =
                        (jres[i] != null ? jres[i][j] : null);
                    String providersStr =
                        (providers[i] != null ? providers[i][j] : null);
                    String resultStr =
                        (result[i] != null ? result[i][j] : null);
                    checkValidity(target, jresStr, providersStr, resultStr, jreSupportsLocale);
                }
            }
        }
    }
}
