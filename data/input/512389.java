public final class TestEnvironment {
    private TestEnvironment() {}
    public static synchronized void reset() {
        resetSystemProperties();
        resetPreferences();
        resetDefaultLocale();
        resetDefaultTimeZone();
    }
    private static void resetDefaultLocale() {
        Locale.setDefault(Locale.US);
    }
    private static void resetDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    }
    private static String getExistingSystemProperty(String name) {
        String result = System.getProperty(name);
        if (result == null) {
            throw new AssertionError("Tests require the '" + name + "' system property");
        }
        return result;
    }
    private static void resetSystemProperties() {
        String tmpDir = getExistingSystemProperty("java.io.tmpdir");
        String userName = getExistingSystemProperty("user.name");
        Properties p = new Properties();
        copyProperty(p, "android.vm.dexfile");
        copyProperty(p, "java.boot.class.path");
        copyProperty(p, "java.class.path");
        copyProperty(p, "java.io.tmpdir");
        copyProperty(p, "java.library.path");
        copyProperty(p, "os.arch");
        copyProperty(p, "os.name");
        copyProperty(p, "os.version");
        String userHome = tmpDir + "/user.home";
        String javaHome = tmpDir + "/java.home";
        String userDir = tmpDir + "/user.dir";
        makeDirectory(new File(userHome));
        makeDirectory(new File(javaHome));
        makeDirectory(new File(userDir));
        p.put("java.home", javaHome);
        p.put("user.dir", userDir);
        p.put("user.home", userHome);
        p.put("file.encoding", "UTF-8");
        p.put("file.separator", "/");
        p.put("java.class.version", "46.0");
        p.put("java.compiler", "");
        p.put("java.ext.dirs", "");
        p.put("java.net.preferIPv6Addresses", "true");
        p.put("java.runtime.name", "Android Runtime");
        p.put("java.runtime.version", "0.9");
        p.put("java.specification.name", "Dalvik Core Library");
        p.put("java.specification.vendor", "The Android Project");
        p.put("java.specification.version", "0.9");
        p.put("java.vendor", "The Android Project");
        p.put("java.vendor.url", "http:
        p.put("java.version", "0");
        p.put("java.vm.name", "Dalvik");
        p.put("java.vm.specification.name", "Dalvik Virtual Machine Specification");
        p.put("java.vm.specification.vendor", "The Android Project");
        p.put("java.vm.specification.version", "0.9");
        p.put("java.vm.vendor", "The Android Project");
        p.put("java.vm.vendor.url", "http:
        p.put("java.vm.version", "1.2.0");
        p.put("javax.net.ssl.trustStore", "/etc/security/cacerts.bks");
        p.put("line.separator", "\n");
        p.put("path.separator", ":");
        p.put("user.language", "en");
        p.put("user.name", userName);
        p.put("user.region", "US");
        System.setProperties(p);
    }
    private static void copyProperty(Properties p, String key) {
        String value = System.getProperty(key);
        if (value != null) {
            p.put(key, value);
        } else {
            p.remove(key);
        }
    }
    private static void makeDirectory(File path) {
        boolean success;
        if (!path.exists()) {
            success = path.mkdirs();
        } else if (!path.isDirectory()) {
            success = path.delete() && path.mkdirs();
        } else {
            success = true;
        }
        if (!success) {
            throw new RuntimeException("Failed to make directory " + path);
        }
    }
    private static void resetPreferences() {
        try {
            for (Preferences root : Arrays.asList(
                    Preferences.systemRoot(), Preferences.userRoot())) {
                for (String child : root.childrenNames()) {
                    root.node(child).removeNode();
                }
                root.clear();
                root.flush();
            }
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
