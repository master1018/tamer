public final class AndroidLocation {
    public static final String FOLDER_AVD = "avd";
    public static final class AndroidLocationException extends Exception {
        private static final long serialVersionUID = 1L;
        public AndroidLocationException(String string) {
            super(string);
        }
    }
    private static String sPrefsLocation = null;
    public final static String getFolder() throws AndroidLocationException {
        if (sPrefsLocation == null) {
            String home = findValidPath("ANDROID_SDK_HOME", "user.home", "HOME");
            if (home == null) {
                throw new AndroidLocationException(
                        "Unable to get the Android SDK home directory.\n" +
                        "Make sure the environment variable ANDROID_SDK_HOME is set up.");
            } else {
                sPrefsLocation = home + File.separator + ".android" + File.separator;
            }
        }
        File f = new File(sPrefsLocation);
        if (f.exists() == false) {
            try {
                f.mkdir();
            } catch (SecurityException e) {
                AndroidLocationException e2 = new AndroidLocationException(String.format(
                        "Unable to create folder '%1$s'. " +
                        "This is the path of preference folder expected by the Android tools.",
                        sPrefsLocation));
                e2.initCause(e);
                throw e2;
            }
        } else if (f.isFile()) {
            throw new AndroidLocationException(sPrefsLocation +
                    " is not a directory! " +
                    "This is the path of preference folder expected by the Android tools.");
        }
        return sPrefsLocation;
    }
    private static String findValidPath(String... names) {
        for (String name : names) {
            String path;
            if (name.indexOf('.') != -1) {
                path = System.getProperty(name);
            } else {
                path = System.getenv(name);
            }
            if (path != null) {
                File f = new File(path);
                if (f.isDirectory()) {
                    return path;
                }
            }
        }
        return null;
    }
}
