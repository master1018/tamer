public final class VersionCheck {
    private final static Pattern sPluginVersionPattern = Pattern.compile(
            "^plugin.version=(\\d+)\\.(\\d+)\\.(\\d+).*$"); 
    public static boolean checkVersion(String osSdkPath, CheckSdkErrorHandler errorHandler) {
        AdtPlugin plugin = AdtPlugin.getDefault();
        String osLibs = osSdkPath + SdkConstants.OS_SDK_TOOLS_LIB_FOLDER;
        int minMajorVersion = -1;
        int minMinorVersion = -1;
        int minMicroVersion = -1;
        try {
            FileReader reader = new FileReader(osLibs + SdkConstants.FN_PLUGIN_PROP);
            BufferedReader bReader = new BufferedReader(reader);
            String line;
            while ((line = bReader.readLine()) != null) {
                Matcher m = sPluginVersionPattern.matcher(line);
                if (m.matches()) {
                    minMajorVersion = Integer.parseInt(m.group(1));
                    minMinorVersion = Integer.parseInt(m.group(2));
                    minMicroVersion = Integer.parseInt(m.group(3));
                    break;
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        if (minMajorVersion == -1 || minMinorVersion == -1 || minMicroVersion ==-1) {
            return errorHandler.handleWarning(Messages.VersionCheck_Plugin_Version_Failed);
        }
        String versionString = (String) plugin.getBundle().getHeaders().get(
                Constants.BUNDLE_VERSION);
        Version version = new Version(versionString);
        boolean valid = true;
        if (version.getMajor() < minMajorVersion) {
            valid = false;
        } else if (version.getMajor() == minMajorVersion) {
            if (version.getMinor() < minMinorVersion) {
                valid = false;
            } else if (version.getMinor() == minMinorVersion) {
                if (version.getMicro() < minMicroVersion) {
                    valid = false;
                }
            }
        }
        if (valid == false) {
            return errorHandler.handleWarning(
                    String.format(Messages.VersionCheck_Plugin_Too_Old,
                            minMajorVersion, minMinorVersion, minMicroVersion, versionString));
        }
        return true; 
    }
}
