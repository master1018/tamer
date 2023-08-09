public class LoadingStrategiesTest {
    static int errors;
    public static void main(String[] args) {
        ResourceBundle rb;
        String s;
        rb = ResourceBundle.getBundle("Chinese", Locale.TAIWAN, new ChineseControl());
        s = rb.getString("data");
        check("chinese with Locale.TAIWAN", s, "root");
        rb = ResourceBundle.getBundle("Chinese", Locale.CHINA, new ChineseControl());
        s = rb.getString("data");
        check("chinese with Locale.CHINA", s, "zh");
        preparePerLocalePackageProperties();
        rb = ResourceBundle.getBundle("test.package.Messages", Locale.US,
                                      new PerLocalePackageControl());
        s = rb.getString("data");
        check("Per-locale package with Locale.US", s, "");
        rb = ResourceBundle.getBundle("test.package.Messages", Locale.GERMAN,
                                      new PerLocalePackageControl());
        s = rb.getString("data");
        check("Per-locale package with Locale.GERMAN", s, "de");
        rb = ResourceBundle.getBundle("test.package.Messages", Locale.JAPAN,
                                      new PerLocalePackageControl());
        s = rb.getString("data");
        check("Per-locale package with Locale.JAPAN", s, "ja_JP");
        if (errors > 0) {
            throw new RuntimeException("FAILED: " + errors + " error(s)");
        }
    }
    private static void check(String msg, String got, String expected) {
        if (!got.equals(expected)) {
            error("%s: got \"%s\", expected \"%s\"%n", msg, got, expected);
        }
    }
    private static class ChineseControl extends ResourceBundle.Control {
        @Override
        public List<Locale> getCandidateLocales(String baseName,
                                                Locale locale) {
            if (locale.equals(Locale.TAIWAN)) {
                return Arrays.asList(locale,
                                     new Locale(""));
            }
            return super.getCandidateLocales(baseName, locale);
        }
    }
    private static class PerLocalePackageControl extends ResourceBundle.Control {
        @Override
        public String toBundleName(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            String loc = super.toBundleName("", locale);
            if (loc.length() > 0) {
                return baseName.replaceFirst("^([\\w\\.]+)\\.(\\w+)$",
                                             "$1." + loc.substring(1) + ".$2");
            }
            return baseName;
        }
        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            if (baseName == null || locale == null) {
                throw new NullPointerException();
            }
            return null;
        }
    }
    private static void preparePerLocalePackageProperties() {
        final String DEL = File.separator;
        try {
            String dir = System.getProperty("test.classes", ".");
            String[] subdirs = { "", "de", "ja_JP" };
            for (String subdir : subdirs) {
                StringBuilder sb = new StringBuilder();
                sb.append(dir).append(DEL).append("test").append(DEL).append("package");
                if (subdir.length() > 0) {
                    sb.append(DEL).append(subdir);
                }
                File path = new File(sb.toString());
                path.mkdirs();
                File propsfile = new File(path, "Messages.properties");
                OutputStream os = new FileOutputStream(propsfile);
                Properties props = new Properties();
                props.setProperty("data", subdir);
                props.store(os, null);
                System.out.println("Created: " + propsfile);
                os.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't set up per-locale properties", e);
        }
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
