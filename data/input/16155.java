public class ControlFactoryTest {
    private static interface Factory {
        public ResourceBundle.Control getControl(List<String> formats);
        public String name();
    }
    static int errors;
    public static void main(String[] args) {
        testControlFactory(new Factory() {
                public ResourceBundle.Control getControl(List<String> formats) {
                    return ResourceBundle.Control.getControl(formats);
                }
                public String name() { return "getControl"; }
            }, Locale.getDefault());
        testControlFactory(new Factory() {
                public ResourceBundle.Control getControl(List<String> formats) {
                    return ResourceBundle.Control.getNoFallbackControl(formats);
                }
                public String name() { return "getNoFallbackControl"; }
            }, null);
        if (errors > 0) {
            throw new RuntimeException("FAILED: " + errors + " error(s)");
        }
    }
    private static void testControlFactory(Factory factory, Locale loc) {
        testGetControl(factory, loc, FORMAT_DEFAULT, "java.class", "java.properties");
        testGetControl(factory, loc, FORMAT_CLASS, "java.class");
        testGetControl(factory, loc, FORMAT_PROPERTIES, "java.properties");
        String[][] data = {
            { "java.class", "java.properties", "java.xml" },
            { "java.class", "java.props" },
            { "java.properties", "java.class" },
            { "java.foo", "java.properties" },
            { "java.foo" },
            { null },
        };
        for (String[] fmts : data) {
            try {
                List<String> fmt = Arrays.asList(fmts);
                ResourceBundle.Control control = factory.getControl(fmt);
                error("getControl: %s%n", fmt);
            } catch (IllegalArgumentException e) {
            }
        }
        try {
            ResourceBundle.Control control = factory.getControl(null);
            error("%s: doesn't throw NPE.%n", factory.name());
        } catch (NullPointerException npe) {
        }
    }
    private static void testGetControl(Factory factory,
                                       Locale loc,
                                       final List<String> FORMATS,
                                       String... fmtStrings) {
        final ResourceBundle.Control CONTROL = factory.getControl(FORMATS);
        List<String> fmt = CONTROL.getFormats("any");
        if (fmt != FORMATS) {
            error("%s: returns %s, expected %s.%n",
                  factory.name(), fmt, FORMATS);
        }
        ResourceBundle.Control control = null;
        for (int i = 0; i < 10; i++) {
            fmt = Arrays.asList(fmtStrings);
            control = factory.getControl(fmt);
            if (control != CONTROL) {
                error("%s: doesn't return the singleton: got %s, expected %s%n",
                      factory.name(), control, CONTROL);
            }
        }
        Locale defaultLocale = Locale.getDefault();
        Locale nonDefaultLocale = defaultLocale.equals(Locale.US) ? Locale.JAPAN : Locale.US;
        if (loc != null) {
            Locale l = CONTROL.getFallbackLocale("any", nonDefaultLocale);
            if (!defaultLocale.equals(l)) {
                error("%s: getFallbackLocale doesn't return default locale. got %s, expected %s%n",
                      factory.name(), toString(l), toString(defaultLocale));
            }
            l = CONTROL.getFallbackLocale("any", defaultLocale);
            if (l != null) {
                error("%s: getFallbackLocale doesn't return null. got %s%n",
                      factory.name(), toString(l));
            }
        } else {
            Locale l = CONTROL.getFallbackLocale("any", nonDefaultLocale);
            if (l != null) {
                error("%s: getFallbackLocale doesn't return null. got %s%n",
                      factory.name(), toString(l));
            }
            l = CONTROL.getFallbackLocale("any", defaultLocale);
            if (l != null) {
                error("%s: getFallbackLocale doesn't return null. got %s%n",
                      factory.name(), toString(l));
            }
        }
    }
    private static String toString(Locale loc) {
        if (loc == null)
            return "null";
        return "\"" + loc.getLanguage() + "_" + loc.getCountry() + "_" + loc.getVariant() + "\"";
    }
    private static void error(String msg) {
        System.out.println(msg);
        errors++;
    }
    private static void error(String fmt, Object... args) {
        System.out.printf(fmt, args);
        errors++;
    }
}
