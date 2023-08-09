public class NIOCharsetAvailabilityTest {
    public static void main(String[] args) throws Exception {
        Set charsets = new HashSet();
        addCharsets(charsets, "sun.nio.cs");
        addCharsets(charsets, "sun.nio.cs.ext");
        Collection availableCharsets = Charset.availableCharsets().values();
        Iterator iter = availableCharsets.iterator();
        while (iter.hasNext()) {
            charsets.remove(((Charset) iter.next()).getClass());
        }
        charsets.remove(Class.forName("sun.nio.cs.Unicode"));
        charsets.remove(Class.forName("sun.nio.cs.ext.ISO2022"));
        charsets.remove(Class.forName("sun.nio.cs.ext.ISO2022_CN_GB"));
        charsets.remove(Class.forName("sun.nio.cs.ext.ISO2022_CN_CNS"));
        iter = charsets.iterator();
        while (iter.hasNext()) {
            System.out.println("Unused Charset subclass: " + ((Class) iter.next()).getName());
        }
        if (charsets.size() > 0) {
            throw new RuntimeException();
        }
    }
    private static Vector classPathSegments = new Vector();
    private static void addCharsets(Set charsets, final String packageName)
            throws Exception {
        String classPath =
            (String) java.security.AccessController.doPrivileged(
             new sun.security.action.GetPropertyAction("sun.boot.class.path"));
        String s =
            (String) java.security.AccessController.doPrivileged(
             new sun.security.action.GetPropertyAction("java.class.path"));
        if (s != null && s.length() != 0) {
            classPath += File.pathSeparator + s;
        }
        while (classPath != null && classPath.length() != 0) {
            int i = classPath.lastIndexOf(java.io.File.pathSeparatorChar);
            String dir = classPath.substring(i + 1);
            if (i == -1) {
                classPath = null;
            } else {
                classPath = classPath.substring(0, i);
            }
            classPathSegments.insertElementAt(dir, 0);
        }
        ClassLoader appLoader = Launcher.getLauncher().getClassLoader();
        URLClassLoader extLoader = (URLClassLoader) appLoader.getParent();
        URL[] urls = extLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            try {
                URI uri = new URI(urls[i].toString());
                classPathSegments.insertElementAt(uri.getPath(), 0);
            } catch (URISyntaxException e) {
            }
        }
        String[] classList = (String[])
            java.security.AccessController.doPrivileged(
                                    new java.security.PrivilegedAction() {
                public Object run() {
                    return getClassList(packageName, "");
                }
            });
        for (int i = 0; i < classList.length; i++) {
            try {
                Class clazz = Class.forName(packageName + "." + classList[i]);
                Class superclazz = clazz.getSuperclass();
                while (superclazz != null && !superclazz.equals(Object.class)) {
                    if (superclazz.equals(Charset.class)) {
                        charsets.add(clazz);
                        break;
                    } else {
                        superclazz = superclazz.getSuperclass();
                    }
                }
            } catch (ClassNotFoundException e) {
            }
        }
    }
    private static final char ZIPSEPARATOR = '/';
    private static String[] getClassList(String pkgName, String prefix) {
        Vector listBuffer = new Vector();
        String packagePath = pkgName.replace('.', File.separatorChar)
            + File.separatorChar;
        String zipPackagePath = pkgName.replace('.', ZIPSEPARATOR)
            + ZIPSEPARATOR;
        for (int i = 0; i < classPathSegments.size(); i++){
            String onePath = (String) classPathSegments.elementAt(i);
            File f = new File(onePath);
            if (!f.exists())
                continue;
            if (f.isFile())
                scanFile(f, zipPackagePath, listBuffer, prefix);
            else if (f.isDirectory()) {
                String fullPath;
                if (onePath.endsWith(File.separator))
                    fullPath = onePath + packagePath;
                else
                    fullPath = onePath + File.separatorChar + packagePath;
                File dir = new File(fullPath);
                if (dir.exists() && dir.isDirectory())
                    scanDir(dir, listBuffer, prefix);
            }
        }
        String[] classNames = new String[listBuffer.size()];
        listBuffer.copyInto(classNames);
        return classNames;
    }
    private static void addClass (String className, Vector listBuffer, String prefix) {
        if (className != null && className.startsWith(prefix)
                    && !listBuffer.contains(className))
            listBuffer.addElement(className);
    }
    private static String midString(String str, String pre, String suf) {
        String midStr;
        if (str.startsWith(pre) && str.endsWith(suf))
            midStr = str.substring(pre.length(), str.length() - suf.length());
        else
            midStr = null;
        return midStr;
    }
    private static void scanDir(File dir, Vector listBuffer, String prefix) {
        String[] fileList = dir.list();
        for (int i = 0; i < fileList.length; i++) {
            addClass(midString(fileList[i], "", ".class"), listBuffer, prefix);
        }
    }
    private static void scanFile(File f, String packagePath, Vector listBuffer,
                String prefix) {
        try {
            ZipInputStream zipFile = new ZipInputStream(new FileInputStream(f));
            ZipEntry entry;
            while ((entry = zipFile.getNextEntry()) != null) {
                String eName = entry.getName();
                if (eName.startsWith(packagePath)) {
                    if (eName.endsWith(".class")) {
                        addClass(midString(eName, packagePath, ".class"),
                                listBuffer, prefix);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found:" + e);
        } catch (IOException e) {
            System.out.println("file IO Exception:" + e);
        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }
    }
}
