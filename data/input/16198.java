public class LocaleNameProviderTest extends ProviderTest {
    public static void main(String[] s) {
        new LocaleNameProviderTest();
    }
    LocaleNameProviderTest() {
        com.bar.LocaleNameProviderImpl lnp = new com.bar.LocaleNameProviderImpl();
        Locale[] availloc = Locale.getAvailableLocales();
        Locale[] testloc = availloc.clone();
        List<Locale> providerloc = Arrays.asList(lnp.getAvailableLocales());
        for (Locale target: availloc) {
            OpenListResourceBundle rb = LocaleData.getLocaleNames(target);
            boolean jreHasBundle = rb.getLocale().equals(target);
            for (Locale test: testloc) {
                String lang = test.getLanguage();
                String ctry = test.getCountry();
                String vrnt = test.getVariant();
                String langresult = test.getDisplayLanguage(target);
                String ctryresult = test.getDisplayCountry(target);
                String vrntresult = test.getDisplayVariant(target);
                String providerslang = null;
                String providersctry = null;
                String providersvrnt = null;
                if (providerloc.contains(target)) {
                    providerslang = lnp.getDisplayLanguage(lang, target);
                    providersctry = lnp.getDisplayCountry(ctry, target);
                    providersvrnt = lnp.getDisplayVariant(vrnt, target);
                }
                String jreslang = null;
                String jresctry = null;
                String jresvrnt = null;
                if (!lang.equals("")) {
                    try {
                        jreslang = rb.getString(lang);
                    } catch (MissingResourceException mre) {}
                }
                if (!ctry.equals("")) {
                    try {
                        jresctry = rb.getString(ctry);
                    } catch (MissingResourceException mre) {}
                }
                if (!vrnt.equals("")) {
                    try {
                        jresvrnt = rb.getString("%%"+vrnt);
                    } catch (MissingResourceException mre) {
                        jresvrnt = vrnt;
                    }
                }
                checkValidity(target, jreslang, providerslang, langresult,
                    jreHasBundle && rb.handleGetKeys().contains(lang));
                checkValidity(target, jresctry, providersctry, ctryresult,
                    jreHasBundle && rb.handleGetKeys().contains(ctry));
                checkValidity(target, jresvrnt, providersvrnt, vrntresult,
                    jreHasBundle && rb.handleGetKeys().contains("%%"+vrnt));
            }
        }
    }
}
