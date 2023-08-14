public class AllPerm {
    private static final Class[] ARGS = new Class[] { };
    private static ProtectionDomain allPermClassDomain;
    public static void main(String[]args) throws Exception {
        File file = new File(System.getProperty("test.src"), "AllPerm.jar");
        URL[] urls = new URL[] { file.toURL() };
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        AllPermLoader loader = new AllPermLoader(urls, parent);
        Object o = loader.loadClass("AllPermClass").newInstance();
        Method doCheck = o.getClass().getMethod("doCheck", ARGS);
        allPermClassDomain = o.getClass().getProtectionDomain();
        Policy.setPolicy(new AllPermPolicy());
        System.setSecurityManager(new SecurityManager());
        doCheck.invoke(o, ARGS);
    }
    private static class AllPermLoader extends URLClassLoader {
        public AllPermLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        protected PermissionCollection getPermissions(CodeSource codesource) {
            Permissions perms = new Permissions();
            perms.add(new AllPermission());
            return perms;
        }
    }
    private static class AllPermPolicy extends Policy {
        public boolean implies(ProtectionDomain domain, Permission permission) {
            if (domain == allPermClassDomain) {
                throw new SecurityException
                        ("Unexpected call into AllPermPolicy");
            }
            return true;
        }
    }
}
