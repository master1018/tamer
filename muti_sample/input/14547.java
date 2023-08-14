public class DateFormatProviderTest extends ProviderTest {
    com.foo.DateFormatProviderImpl dfp = new com.foo.DateFormatProviderImpl();
    List<Locale> availloc = Arrays.asList(DateFormat.getAvailableLocales());
    List<Locale> providerloc = Arrays.asList(dfp.getAvailableLocales());
    List<Locale> jreloc = Arrays.asList(LocaleData.getAvailableLocales());
    public static void main(String[] s) {
        new DateFormatProviderTest();
    }
    DateFormatProviderTest() {
        availableLocalesTest();
        objectValidityTest();
        extendedVariantTest();
        messageFormatTest();
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
            Calendar cal = Calendar.getInstance(target);
            String key = "DateTimePatterns";
            if (!cal.getClass().getName().equals("java.util.GregorianCalendar")) {
                key = cal.getClass().getName() + "." + key;
            }
            ResourceBundle rb = LocaleData.getDateFormatData(target);
            boolean jreSupportsLocale = jreloc.contains(target);
            String[] jreDateTimePatterns = null;
            if (jreSupportsLocale) {
                try {
                    jreDateTimePatterns = (String[])rb.getObject(key);
                } catch (MissingResourceException mre) {}
            }
            for (int style = DateFormat.FULL; style <= DateFormat.SHORT; style ++) {
                DateFormat result = DateFormat.getDateTimeInstance(style, style, target);
                DateFormat providersResult = null;
                if (providerloc.contains(target)) {
                    providersResult = dfp.getDateTimeInstance(style, style, target);
                }
                DateFormat jresResult = null;
                if (jreSupportsLocale) {
                    Object[] dateTimeArgs = {jreDateTimePatterns[style],
                                             jreDateTimePatterns[style + 4]};
                    String pattern = MessageFormat.format(jreDateTimePatterns[8], dateTimeArgs);
                    jresResult = new SimpleDateFormat(pattern, target);
                }
                checkValidity(target, jresResult, providersResult, result, jreSupportsLocale);
            }
        }
    }
    void extendedVariantTest() {
        Locale[] testlocs = {new Locale("ja", "JP", "osaka_extended"),
                             new Locale("ja", "JP", "osaka_extended_further"),
                             new Locale("ja", "JP", "osaka_")};
        for (Locale test: testlocs) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, test);
            DateFormat provider = dfp.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, test);
            if (!df.equals(provider)) {
                throw new RuntimeException("variant fallback failed. test locale: "+test);
            }
        }
    }
    private static final String[] TYPES = {
        "date",
        "time"
    };
    private static final String[] MODIFIERS = {
        "",
        "short",
        "medium", 
        "long",
        "full"
    };
    void messageFormatTest() {
        for (Locale target : providerloc) {
            for (String type : TYPES) {
                for (String modifier : MODIFIERS) {
                    String pattern, expected;
                    if (modifier.equals("")) {
                        pattern = String.format("%s={0,%s}", type, type);
                    } else {
                        pattern = String.format("%s={0,%s,%s}", type, type, modifier);
                    }
                    if (modifier.equals("medium")) {
                        expected = String.format("%s={0,%s}", type, type);
                    } else {
                        expected = pattern;
                    }
                    MessageFormat mf = new MessageFormat(pattern, target);
                    Format[] fmts = mf.getFormats();
                    if (fmts[0] instanceof SimpleDateFormat) {
                        continue;
                    }
                    String toPattern = mf.toPattern();
                    if (!toPattern.equals(expected)) {
                        throw new RuntimeException("messageFormatTest: got '" + toPattern
                                                   + "', expected '" + expected + "'");
                    }
                }
            }
        }
    }
}
