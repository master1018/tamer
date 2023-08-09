public class SharedSecrets {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static JavaUtilJarAccess javaUtilJarAccess;
    private static JavaLangAccess javaLangAccess;
    private static JavaIOAccess javaIOAccess;
    private static JavaNetAccess javaNetAccess;
    private static JavaNioAccess javaNioAccess;
    private static JavaIOFileDescriptorAccess javaIOFileDescriptorAccess;
    private static JavaSecurityProtectionDomainAccess javaSecurityProtectionDomainAccess;
    private static JavaSecurityAccess javaSecurityAccess;
    private static JavaxSecurityAuthKerberosAccess javaxSecurityAuthKerberosAccess;
    public static JavaUtilJarAccess javaUtilJarAccess() {
        if (javaUtilJarAccess == null) {
            unsafe.ensureClassInitialized(JarFile.class);
        }
        return javaUtilJarAccess;
    }
    public static void setJavaUtilJarAccess(JavaUtilJarAccess access) {
        javaUtilJarAccess = access;
    }
    public static void setJavaLangAccess(JavaLangAccess jla) {
        javaLangAccess = jla;
    }
    public static JavaLangAccess getJavaLangAccess() {
        return javaLangAccess;
    }
    public static void setJavaNetAccess(JavaNetAccess jna) {
        javaNetAccess = jna;
    }
    public static JavaNetAccess getJavaNetAccess() {
        return javaNetAccess;
    }
    public static void setJavaNioAccess(JavaNioAccess jna) {
        javaNioAccess = jna;
    }
    public static JavaNioAccess getJavaNioAccess() {
        if (javaNioAccess == null) {
            unsafe.ensureClassInitialized(java.nio.ByteOrder.class);
        }
        return javaNioAccess;
    }
    public static void setJavaIOAccess(JavaIOAccess jia) {
        javaIOAccess = jia;
    }
    public static JavaIOAccess getJavaIOAccess() {
        if (javaIOAccess == null) {
            unsafe.ensureClassInitialized(Console.class);
        }
        return javaIOAccess;
    }
    public static void setJavaIOFileDescriptorAccess(JavaIOFileDescriptorAccess jiofda) {
        javaIOFileDescriptorAccess = jiofda;
    }
    public static JavaIOFileDescriptorAccess getJavaIOFileDescriptorAccess() {
        if (javaIOFileDescriptorAccess == null)
            unsafe.ensureClassInitialized(FileDescriptor.class);
        return javaIOFileDescriptorAccess;
    }
    public static void setJavaSecurityProtectionDomainAccess
        (JavaSecurityProtectionDomainAccess jspda) {
            javaSecurityProtectionDomainAccess = jspda;
    }
    public static JavaSecurityProtectionDomainAccess
        getJavaSecurityProtectionDomainAccess() {
            if (javaSecurityProtectionDomainAccess == null)
                unsafe.ensureClassInitialized(ProtectionDomain.class);
            return javaSecurityProtectionDomainAccess;
    }
    public static void setJavaSecurityAccess(JavaSecurityAccess jsa) {
        javaSecurityAccess = jsa;
    }
    public static JavaSecurityAccess getJavaSecurityAccess() {
        if (javaSecurityAccess == null) {
            unsafe.ensureClassInitialized(AccessController.class);
        }
        return javaSecurityAccess;
    }
    public static void setJavaxSecurityAuthKerberosAccess
            (JavaxSecurityAuthKerberosAccess jsaka) {
        javaxSecurityAuthKerberosAccess = jsaka;
    }
    public static JavaxSecurityAuthKerberosAccess
            getJavaxSecurityAuthKerberosAccess() {
        if (javaxSecurityAuthKerberosAccess == null)
            unsafe.ensureClassInitialized(KeyTab.class);
        return javaxSecurityAuthKerberosAccess;
    }
}
