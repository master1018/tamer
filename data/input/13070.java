public class Versions {
    static String getProperty(String prop) throws Exception {
        String value = System.getProperty(prop);
        if (value == null)
            throw new Exception("No such system property: " + prop);
        System.out.printf("%s=%s%n", prop, value);
        return value;
    }
    static ClassLoader cl;
    static void checkClassVersion(int major, int minor, boolean expectSupported)
        throws Exception
    {
        final String className  = "ClassVersionTest";
        final String classFile  = className + ".class";
        final DataOutputStream dos =
            new DataOutputStream(new FileOutputStream(classFile));
        dos.writeLong((0xCafeBabel << 32) + (minor << 16) + major);
        dos.close();
        boolean supported = true;
        try {
            Class.forName(className, false, cl);
        } catch (UnsupportedClassVersionError e) {
            supported = false;
        } catch (Throwable t) {
        }
        new File(classFile).delete();
        if (supported != expectSupported)
            throw new Exception("Forgot to update java.class.version?");
    }
    public static void main(String [] args) throws Exception {
        String classVersion   = getProperty("java.class.version");
        String javaVersion    = getProperty("java.version");
        String VMVersion      = getProperty("java.vm.version");
        String runtimeVersion = getProperty("java.runtime.version");
        String specVersion    = getProperty("java.specification.version");
        if (! (javaVersion.startsWith(specVersion) &&
               runtimeVersion.startsWith(specVersion)))
            throw new Exception("Invalid version-related system properties");
        String[] versions = classVersion.split("\\.");
        int majorVersion = Integer.parseInt(versions[0]);
        int minorVersion = Integer.parseInt(versions[1]);
        System.out.printf("majorVersion=%s%n",majorVersion);
        System.out.printf("minorVersion=%s%n",minorVersion);
        cl = new URLClassLoader(new URL[]{new File("./").toURL()}, null);
        checkClassVersion(majorVersion    , minorVersion    , true );
        checkClassVersion(majorVersion + 1, minorVersion    , false);
        checkClassVersion(majorVersion    , minorVersion + 1, false);
    }
}
