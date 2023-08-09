public class BadControlsTest {
    public static void main(String[] args) {
        ResourceBundle.Control control;
        control = new ResourceBundle.Control() {
                public List<String> getFormats(String name) {
                    return null;
                }
            };
        testControl(control, "getFormats returns null");
        control = new ResourceBundle.Control() {
                public List<String> getFormats(String name) {
                    return Arrays.asList("java.class", null, "java.properties");
                }
            };
        testControl(control, "getFormats returns a List containing null");
        control = new ResourceBundle.Control() {
                public List<Locale> getCandidateLocales(String name, Locale loc) {
                    return null;
                }
            };
        testControl(control, "getCandidateLocales returns null");
        control = new ResourceBundle.Control() {
                public List<Locale> getCandidateLocales(String name, Locale loc) {
                    return Arrays.asList(Locale.US, null, Locale.ENGLISH);
                }
            };
        testControl(control, "getCandidateLocales returns a List containing null");
        long[] badTtls = {
            TTL_NO_EXPIRATION_CONTROL - 1,
            -10000,
            Long.MIN_VALUE
        };
        for (final long ttl : badTtls) {
            control = new ResourceBundle.Control() {
                    public long getTimeToLive(String name, Locale loc) {
                        return ttl;
                    }
                };
            testControl(control, "getTimeToLive returns " + ttl);
        }
        control = new ResourceBundle.Control() {
                public String toBundleName(String name, Locale loc) {
                    return null;
                }
            };
        try {
            ResourceBundle rb = ResourceBundle.getBundle("StressOut", control);
            throw new RuntimeException("toBundleName returns null");
        } catch (MissingResourceException e) {
            if (!(e.getCause() instanceof NullPointerException)) {
                throw new RuntimeException("toBundleName returns null. The cause isn't NPE.");
            }
        }
        control = null;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("StressOut", control);
            throw new RuntimeException("getBundle doesn't throw NPE with null Control");
        } catch (NullPointerException e) {
        }
    }
    private static void testControl(ResourceBundle.Control control, String testTitle) {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("StressOut", control);
            throw new RuntimeException(testTitle);
        } catch (IllegalArgumentException e) {
            System.out.println(testTitle + ": PASSED (" + e.getMessage() + ")");
        }
    }
}
