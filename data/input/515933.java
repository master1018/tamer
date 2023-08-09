public class JUnitLaunchConfigDelegate extends JUnitLaunchConfigurationDelegate {
    private static final String JUNIT_JAR = "junit.jar"; 
    @Override
    public String[][] getBootpathExt(ILaunchConfiguration configuration) throws CoreException {
        String[][] bootpath = super.getBootpathExt(configuration);
        return fixBootpathExt(bootpath);
    }
    @Override
    public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {
        String[] classpath = super.getClasspath(configuration);
        return fixClasspath(classpath, getJavaProjectName(configuration));
    }
    public static String[][] fixBootpathExt(String[][] bootpath) {
        for (int i = 0; i < bootpath.length; i++) {
            if (bootpath[i] != null) {
                if (bootpath[i][0].endsWith(SdkConstants.FN_FRAMEWORK_LIBRARY)) {
                    bootpath[i] = null;
                }
            }
        }
        return bootpath;
    }
    public static String[] fixClasspath(String[] classpath, String projectName) {
        for (int i = 0; i < classpath.length; i++) {
            if (classpath[i].endsWith(JUNIT_JAR)) { 
                return classpath;
            }
        }
        try {
            String jarLocation = getJunitJarLocation();
            String[] newClasspath = new String[classpath.length + 1];
            System.arraycopy(classpath, 0, newClasspath, 0, classpath.length);
            newClasspath[newClasspath.length - 1] = jarLocation;
            classpath = newClasspath;
        } catch (IOException e) {
            AdtPlugin.log(e, "Could not find a valid junit.jar");
            AdtPlugin.printErrorToConsole(projectName,
                    "Could not find a valid junit.jar");
        }
        return classpath;
    }
    public static String getJunitJarLocation() throws IOException {
        Bundle bundle = Platform.getBundle("org.junit"); 
        if (bundle == null) {
            throw new IOException("Cannot find org.junit bundle");
        }
        URL jarUrl = bundle.getEntry(AndroidConstants.WS_SEP + JUNIT_JAR);
        return FileLocator.resolve(jarUrl).getFile();
    }
}
