public final class Resources {
    private static ResourceBundle resources = null;
    private static ResourceBundle resourcesExt = null;
    static {
        try {
            resources =
                ResourceBundle.getBundle("sun.rmi.rmic.resources.rmic");
        } catch (MissingResourceException e) {
        }
        try {
            resourcesExt =
                ResourceBundle.getBundle("sun.rmi.rmic.resources.rmicext");
        } catch (MissingResourceException e) {
        }
    }
    private Resources() { throw new AssertionError(); }
    public static String getText(String key, String... args) {
        String format = getString(key);
        if (format == null) {
            format = "missing resource key: key = \"" + key + "\", " +
                "arguments = \"{0}\", \"{1}\", \"{2}\"";
        }
        return MessageFormat.format(format, args);
    }
    private static String getString(String key) {
        if (resourcesExt != null) {
            try {
                return resourcesExt.getString(key);
            } catch (MissingResourceException e) {
            }
        }
        if (resources != null) {
            try {
                return resources.getString(key);
            } catch (MissingResourceException e) {
                return null;
            }
        }
        return "missing resource bundle: key = \"" + key + "\", " +
            "arguments = \"{0}\", \"{1}\", \"{2}\"";
    }
}
