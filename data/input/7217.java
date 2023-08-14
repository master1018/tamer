public class LocaleCategory {
    private static Locale base = null;
    private static Locale disp = null;
    private static Locale fmt = null;
    private static String enc = null;
    public static void main(String[] args) {
        Locale.Builder builder = new Locale.Builder();
        base = builder.setLanguage(System.getProperty("user.language", ""))
                      .setScript(System.getProperty("user.script", ""))
                      .setRegion(System.getProperty("user.country", ""))
                      .setVariant(System.getProperty("user.variant", "")).build();
        disp = builder.setLanguage(System.getProperty("user.language.display",
                                                      Locale.getDefault().getLanguage()))
                      .setScript(System.getProperty("user.script.display",
                                                    Locale.getDefault().getScript()))
                      .setRegion(System.getProperty("user.country.display",
                                                    Locale.getDefault().getCountry()))
                      .setVariant(System.getProperty("user.variant.display",
                                                     Locale.getDefault().getVariant())).build();
        fmt = builder.setLanguage(System.getProperty("user.language.format",
                                                     Locale.getDefault().getLanguage()))
                     .setScript(System.getProperty("user.script.format",
                                                   Locale.getDefault().getScript()))
                     .setRegion(System.getProperty("user.country.format",
                                                   Locale.getDefault().getCountry()))
                     .setVariant(System.getProperty("user.variant.format",
                                                     Locale.getDefault().getVariant())).build();
        checkDefault();
        testGetSetDefault();
    }
    static void checkDefault() {
        if (!base.equals(Locale.getDefault()) ||
            !disp.equals(Locale.getDefault(Locale.Category.DISPLAY)) ||
            !fmt.equals(Locale.getDefault(Locale.Category.FORMAT))) {
            throw new RuntimeException("Some of the return values from getDefault() do not agree with the locale derived from \"user.xxxx\" system properties");
        }
    }
    static void testGetSetDefault() {
        try {
            Locale.setDefault(null, null);
            throw new RuntimeException("setDefault(null, null) should throw a NullPointerException");
        } catch (NullPointerException npe) {}
        Locale.setDefault(Locale.CHINA);
        if (!Locale.CHINA.equals(Locale.getDefault(Locale.Category.DISPLAY)) ||
            !Locale.CHINA.equals(Locale.getDefault(Locale.Category.FORMAT))) {
            throw new RuntimeException("setDefault() should set all default locales for all categories");
        }
    }
}
