public class BreakIteratorProviderTest extends ProviderTest {
    com.foo.BreakIteratorProviderImpl bip = new com.foo.BreakIteratorProviderImpl();
    List<Locale> availloc = Arrays.asList(BreakIterator.getAvailableLocales());
    List<Locale> providerloc = Arrays.asList(bip.getAvailableLocales());
    List<Locale> jreloc = Arrays.asList(LocaleData.getAvailableLocales());
    private static final int CHARACTER_INDEX = 0;
    private static final int WORD_INDEX = 1;
    private static final int LINE_INDEX = 2;
    private static final int SENTENCE_INDEX = 3;
    public static void main(String[] s) {
        new BreakIteratorProviderTest();
    }
    BreakIteratorProviderTest() {
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
            ResourceBundle rb = ResourceBundle.getBundle(
                        "sun.text.resources.BreakIteratorInfo", target);
            String[] classNames = rb.getStringArray("BreakIteratorClasses");
            boolean jreSupportsLocale = jreloc.contains(target);
            String[] result = new String[4];
            result[0] = BreakIterator.getCharacterInstance(target).getClass().getName();
            result[1] = BreakIterator.getWordInstance(target).getClass().getName();
            result[2] = BreakIterator.getLineInstance(target).getClass().getName();
            result[3] = BreakIterator.getSentenceInstance(target).getClass().getName();
            String[] providersResult = new String[4];
            if (providerloc.contains(target)) {
                providersResult[0] = bip.getCharacterInstance(target).getClass().getName();
                providersResult[1] = bip.getWordInstance(target).getClass().getName();
                providersResult[2] = bip.getLineInstance(target).getClass().getName();
                providersResult[3] = bip.getSentenceInstance(target).getClass().getName();
            }
            String[] jresResult = new String[4];
            if (jreSupportsLocale) {
                for (int i = 0; i < 4; i++) {
                    jresResult[i] = "java.text."+classNames[i];
                }
            }
            for (int i = 0; i < 4; i++) {
                checkValidity(target, jresResult[i], providersResult[i], result[i], jreSupportsLocale);
            }
        }
    }
}
