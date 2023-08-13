public class PackagePrivateTest {
    public static void main(String[] args) {
        ResourceBundle rb;
        try {
            rb = ResourceBundle.getBundle("PackagePrivateRB");
            throw new RuntimeException(
                       "doesn't throw MissingResourceException with the default Control");
        } catch (MissingResourceException e) {
        }
        ResourceBundle.clearCache();
        rb = ResourceBundle.getBundle("PackagePrivateRB",
                new ResourceBundle.Control() {
                    @Override
                    public List<String> getFormats(String baseName) {
                        return FORMAT_CLASS;
                    }
                    @Override
                    public ResourceBundle newBundle(String baseName,
                                                    Locale locale,
                                                    String format,
                                                    ClassLoader loader,
                                                    boolean reload)
                        throws IllegalAccessException,
                               InstantiationException, IOException {
                        String bn = toBundleName(baseName, locale);
                        if ("java.class".equals(format)) {
                            try {
                                Class<? extends ResourceBundle> cl =
                                    (Class<? extends ResourceBundle>) loader.loadClass(bn);
                                return  cl.newInstance();
                            } catch (ClassNotFoundException e) {
                            }
                            return null;
                        }
                        throw new IllegalArgumentException("unknown format: " + format);
                    }
                });
        String s = rb.getString("type");
        if (!s.equals("class (package1.PackagePrivateRB)")) {
            throw new RuntimeException("wrong type: got " + s + ", expected '" +
                                       "class (package1.PackagePrivateRB)'");
        }
    }
}
