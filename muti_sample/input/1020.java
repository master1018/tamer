public class Bug6287579 {
    static final Locale ROOT = new Locale("");
    static final String[] baseNames = {
        "sun.text.resources.BreakIteratorInfo",
        "sun.text.resources.FormatData",
        "sun.text.resources.CollationData",
        "sun.util.resources.LocaleNames",
        "sun.util.resources.TimeZoneNames",
        "sun.awt.resources.awt",
    };
    public static void main(String[] args) throws Exception {
        int errors = 0;
        List<Locale> locales = new ArrayList<Locale>();
        locales.addAll(Arrays.asList(Locale.getAvailableLocales()));
        locales.add(ROOT);
        for (Locale locale : locales) {
            for (String base : baseNames) {
                String className = getResourceName(base, locale);
                errors += checkGetContents(className);
            }
        }
        if (errors > 0) {
            throw new RuntimeException(errors + " errors found");
        }
    }
    static int checkGetContents(String className) throws Exception {
        int err = 0;
        try {
            Class clazz = Class.forName(className);
            Method getContentsMethod = clazz.getDeclaredMethod("getContents",
                                                               (Class[]) null);
            if (!Modifier.isProtected(getContentsMethod.getModifiers())) {
                System.err.println(className + ": not protected");
                err++;
            }
            getContentsMethod.setAccessible(true);
            Object bundle = clazz.newInstance();
            Object o1 = getContentsMethod.invoke(bundle, (Object[]) null);
            Object o2 = getContentsMethod.invoke(bundle, (Object[]) null);
            if (o1 == o2) {
                System.err.println(className + ": same instance returned");
                err++;
            }
        } catch (ClassNotFoundException ce) {
        } catch (NoSuchMethodException me) {
            System.out.println(className + ": no declared getContents()");
        }
        return err;
    }
    static String getResourceName(String base, Locale locale) {
        if (locale.equals(ROOT)) {
            return base;
        }
        StringBuilder sb = new StringBuilder(base);
        sb.append('_').append(locale.getLanguage());
        if (locale.getCountry().length() > 0
            || locale.getVariant().length() > 0) {
            sb.append('_').append(locale.getCountry());
        }
        if (locale.getVariant().length() > 0) {
            sb.append('_').append(locale.getVariant());
        }
        return sb.toString();
    }
}
