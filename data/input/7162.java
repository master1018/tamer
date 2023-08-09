public class CollatorProviderTest extends ProviderTest {
    com.foo.CollatorProviderImpl cp = new com.foo.CollatorProviderImpl();
    List<Locale> availloc = Arrays.asList(Collator.getAvailableLocales());
    List<Locale> providerloc = Arrays.asList(cp.getAvailableLocales());
    List<Locale> jreloc = Arrays.asList(LocaleData.getAvailableLocales());
    public static void main(String[] s) {
        new CollatorProviderTest();
    }
    CollatorProviderTest() {
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
        Collator def = Collator.getInstance(new Locale(""));
        String defrules = ((RuleBasedCollator)def).getRules();
        for (Locale target: availloc) {
            ResourceBundle rb = LocaleData.getCollationData(target);
            boolean jreSupportsLocale = jreloc.contains(target);
            Collator result = Collator.getInstance(target);
            Collator providersResult = null;
            if (providerloc.contains(target)) {
                providersResult = cp.getInstance(target);
            }
            Collator jresResult = null;
            if (jreSupportsLocale) {
                try {
                    String rules = rb.getString("Rule");
                    jresResult = new RuleBasedCollator(defrules+rules);
                    jresResult.setDecomposition(Collator.NO_DECOMPOSITION);
                } catch (MissingResourceException mre) {
                } catch (ParseException pe) {
                }
            }
            checkValidity(target, jresResult, providersResult, result, jreSupportsLocale);
        }
    }
}
