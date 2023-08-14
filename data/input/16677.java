public class SALauncherLoader extends URLClassLoader {
    public String findLibrary(String name) {
        name = System.mapLibraryName(name);
        for (int i = 0; i < libpaths.length; i++) {
            File file = new File(new File(libpaths[i]), name);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
    public SALauncherLoader(ClassLoader parent) {
        super(getClassPath(), parent);
        String salibpath = System.getProperty("sa.library.path");
        if (salibpath != null) {
            libpaths = salibpath.split(File.pathSeparator);
        } else {
            libpaths = new String[0];
        }
    }
    public synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        int i = name.lastIndexOf('.');
        if (i != -1) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPackageAccess(name.substring(0, i));
            }
        }
        Class clazz = findLoadedClass(name);
        if (clazz != null) return clazz;
        try {
            return findClass(name);
        } catch (ClassNotFoundException cnfe) {
            return (super.loadClass(name, resolve));
        }
    }
    protected PermissionCollection getPermissions(CodeSource codesource) {
        PermissionCollection perms = super.getPermissions(codesource);
        perms.add(new RuntimePermission("exitVM"));
        return perms;
    }
    private String[] libpaths;
    private static URL[] getClassPath() {
        final String s = System.getProperty("java.class.path");
        final File[] path = (s == null) ? new File[0] : getClassPath(s);
        return pathToURLs(path);
    }
    private static URL[] pathToURLs(File[] path) {
        URL[] urls = new URL[path.length];
        for (int i = 0; i < path.length; i++) {
            urls[i] = getFileURL(path[i]);
        }
        return urls;
    }
    private static File[] getClassPath(String cp) {
        String[] tmp = cp.split(File.pathSeparator);
        File[] paths = new File[tmp.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new File(tmp[i].equals("")? "." : tmp[i]);
        }
        return paths;
    }
    private static URL getFileURL(File file) {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException mue) {
            throw new InternalError(mue.getMessage());
        }
    }
}
