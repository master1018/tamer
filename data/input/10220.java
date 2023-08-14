public class CurrencyNameProviderTest extends ProviderTest {
    public static void main(String[] s) {
        new CurrencyNameProviderTest();
    }
    CurrencyNameProviderTest() {
        test1();
        test2();
    }
    void test1() {
        com.bar.CurrencyNameProviderImpl cnp = new com.bar.CurrencyNameProviderImpl();
        Locale[] availloc = Locale.getAvailableLocales();
        Locale[] testloc = availloc.clone();
        List<Locale> providerloc = Arrays.asList(cnp.getAvailableLocales());
        for (Locale target: availloc) {
            OpenListResourceBundle rb = (OpenListResourceBundle)LocaleData.getCurrencyNames(target);
            boolean jreHasBundle = rb.getLocale().equals(target);
            for (Locale test: testloc) {
                Currency c = null;
                try {
                    c = Currency.getInstance(test);
                } catch (IllegalArgumentException iae) {}
                if (c == null) {
                    continue;
                }
                String currencyresult = c.getSymbol(target);
                String nameresult = c.getDisplayName(target);
                String providerscurrency = null;
                String providersname = null;
                if (providerloc.contains(target)) {
                    providerscurrency = cnp.getSymbol(c.getCurrencyCode(), target);
                    providersname = cnp.getDisplayName(c.getCurrencyCode(), target);
                }
                String jrescurrency = null;
                String jresname = null;
                String key = c.getCurrencyCode();
                String nameKey = key.toLowerCase(Locale.ROOT);
                if (jreHasBundle) {
                    try {
                        jrescurrency = rb.getString(key);
                    } catch (MissingResourceException mre) {
                    }
                    try {
                        jresname = rb.getString(nameKey);
                    } catch (MissingResourceException mre) {
                    }
                }
                checkValidity(target, jrescurrency, providerscurrency, currencyresult, jrescurrency!=null);
                checkValidity(target, jresname, providersname, nameresult,
                              jreHasBundle && rb.handleGetKeys().contains(nameKey));
            }
        }
    }
    final String pattern = "###,###\u00A4";
    final String YEN_IN_OSAKA = "100,000\u5186\u3084\u3002";
    final String YEN_IN_KYOTO = "100,000\u5186\u3069\u3059\u3002";
    final Locale OSAKA = new Locale("ja", "JP", "osaka");
    final Locale KYOTO = new Locale("ja", "JP", "kyoto");
    Integer i = new Integer(100000);
    String formatted;
    DecimalFormat df;
    void test2() {
        try {
            df = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(OSAKA));
            System.out.println(formatted = df.format(i));
            if(!formatted.equals(YEN_IN_OSAKA)) {
                throw new RuntimeException("formatted zone names mismatch. " +
                    "Should match with " + YEN_IN_OSAKA);
            }
            df.parse(YEN_IN_OSAKA);
            Locale.setDefault(KYOTO);
            df = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance());
            System.out.println(formatted = df.format(i));
            if(!formatted.equals(YEN_IN_KYOTO)) {
                throw new RuntimeException("formatted zone names mismatch. " +
                    "Should match with " + YEN_IN_KYOTO);
            }
            df.parse(YEN_IN_KYOTO);
        } catch (ParseException pe) {
            throw new RuntimeException("parse error occured" + pe);
        }
    }
}
