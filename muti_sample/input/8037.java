public class ContextInsulation {
    public static void main(String[] args) throws Exception {
        TestLibrary.suggestSecurityManager(null);
        ServiceConfiguration.installServiceConfigurationFile();
        CodeSource codesource = new CodeSource(null, (Certificate[]) null);
        Permissions perms = null;
        ProtectionDomain pd = new ProtectionDomain(codesource, perms);
        AccessControlContext acc =
            new AccessControlContext(new ProtectionDomain[] { pd });
        java.security.AccessController.doPrivileged(
        new java.security.PrivilegedExceptionAction() {
            public Object run() throws Exception {
                TestProvider.exerciseTestProvider(
                    TestProvider2.loadClassReturn,
                    TestProvider2.loadProxyClassReturn,
                    TestProvider2.getClassLoaderReturn,
                    TestProvider2.getClassAnnotationReturn,
                    TestProvider2.invocations);
                return null;
            }
        }, acc);
    }
}
