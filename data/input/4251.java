public class TestLibrary {
    public final static int REGISTRY_PORT = 2006;
    public final static int RMID_PORT = 1098;
    static void mesg(Object mesg) {
        System.err.println("TEST_LIBRARY: " + mesg.toString());
    }
    public static void bomb(String message, Exception e) {
        String testFailed = "TEST FAILED: ";
        if ((message == null) && (e == null)) {
            testFailed += " No relevant information";
        } else if (e == null) {
            testFailed += message;
        }
        System.err.println(testFailed);
        if (e != null) {
            System.err.println("Test failed with: " +
                               e.getMessage());
            e.printStackTrace(System.err);
        }
        throw new TestFailedException(testFailed, e);
    }
    public static void bomb(String message) {
        bomb(message, null);
    }
    public static void bomb(Exception e) {
        bomb(null, e);
    }
    private static boolean getBoolean(String name) {
        return (new Boolean(getProperty(name, "false")).booleanValue());
    }
    private static Integer getInteger(String name) {
        int val = 0;
        Integer value = null;
        String propVal = getProperty(name, null);
        if (propVal == null) {
            return null;
        }
        try {
            value = new Integer(Integer.parseInt(propVal));
        } catch (NumberFormatException nfe) {
        }
        return value;
    }
    public static String getProperty(String property, String defaultVal) {
        final String prop = property;
        final String def = defaultVal;
        return ((String) java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction() {
                public Object run() {
                    return System.getProperty(prop, def);
                }
            }));
    }
    public static void setBoolean(String property, boolean value) {
        setProperty(property, (new Boolean(value)).toString());
    }
    public static void setInteger(String property, int value) {
        setProperty(property, Integer.toString(value));
    }
    public static void setProperty(String property, String value) {
        final String prop = property;
        final String val = value;
        java.security.AccessController.doPrivileged
            (new java.security.PrivilegedAction() {
                public Object run() {
                    System.setProperty(prop, val);
                    return null;
                }
        });
    }
    public static void printEnvironment() {
        printEnvironment(System.err);
    }
    public static void printEnvironment(PrintStream out) {
        out.println("-------------------Test environment----------" +
                    "---------");
        for(Enumeration keys = System.getProperties().keys();
            keys.hasMoreElements();) {
            String property = (String) keys.nextElement();
            out.println(property + " = " + getProperty(property, null));
        }
        out.println("---------------------------------------------" +
                    "---------");
    }
    public static URL installClassInCodebase(String className,
                                             String codebase)
        throws MalformedURLException
    {
        return installClassInCodebase(className, codebase, true);
    }
    public static URL installClassInCodebase(String className,
                                             String codebase,
                                             boolean delete)
        throws MalformedURLException
    {
        String classFileName = className + ".class";
        File dstDir = (new File(getProperty("user.dir", "."), codebase));
        if (!dstDir.exists()) {
            if (!dstDir.mkdir()) {
                throw new RuntimeException(
                    "could not create codebase directory");
            }
        }
        File dstFile = new File(dstDir, classFileName);
        URL codebaseURL = dstDir.toURL();
        File srcDir = new File(getProperty("test.classes", "."));
        File srcFile = new File(srcDir, classFileName);
        mesg(srcFile);
        mesg(dstFile);
        if (!dstFile.exists()) {
            if (!srcFile.exists()) {
                throw new RuntimeException(
                    "could not find class file to install in codebase " +
                    "(try rebuilding the test): " + srcFile);
            }
            try {
                copyFile(srcFile, dstFile);
            } catch (IOException e) {
                throw new RuntimeException(
                    "could not install class file in codebase");
            }
            mesg("Installed class \"" + className +
                "\" in codebase " + codebaseURL);
        }
        if (srcFile.exists()) {
            if (delete && !srcFile.delete()) {
                throw new RuntimeException(
                    "could not delete duplicate class file in CLASSPATH");
            }
        }
        return codebaseURL;
    }
    public static void copyFile(File srcFile, File dstFile)
        throws IOException
    {
        FileInputStream src = new FileInputStream(srcFile);
        FileOutputStream dst = new FileOutputStream(dstFile);
        byte[] buf = new byte[32768];
        while (true) {
            int count = src.read(buf);
            if (count < 0) {
                break;
            }
            dst.write(buf, 0, count);
        }
        dst.close();
        src.close();
    }
    public static void unexport(Remote obj) {
        if (obj != null) {
            try {
                mesg("unexporting object...");
                UnicastRemoteObject.unexportObject(obj, true);
            } catch (NoSuchObjectException munch) {
            } catch (Exception e) {
                e.getMessage();
                e.printStackTrace();
            }
        }
    }
    public static void suggestSecurityManager(String managerClassName) {
        SecurityManager manager = null;
        if (System.getSecurityManager() == null) {
            try {
                if (managerClassName == null) {
                    managerClassName = TestParams.defaultSecurityManager;
                }
                manager = ((SecurityManager) Class.
                           forName(managerClassName).newInstance());
            } catch (ClassNotFoundException cnfe) {
                bomb("Security manager could not be found: " +
                     managerClassName, cnfe);
            } catch (Exception e) {
                bomb("Error creating security manager. ", e);
            }
            System.setSecurityManager(manager);
        }
    }
    public String stackTraceToString(Exception e) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        e.printStackTrace(ps);
        return bos.toString();
    }
    private static Properties props;
    private static synchronized Properties getExtraProperties() {
        if (props != null) {
            return props;
        }
        props = new Properties();
        File f = new File(".." + File.separator + ".." + File.separator +
                          "test.props");
        if (!f.exists()) {
            return props;
        }
        try {
            FileInputStream in = new FileInputStream(f);
            try {
                props.load(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("extra property setup failed", e);
        }
        return props;
    }
    public static String getExtraProperty(String property, String defaultVal) {
        return getExtraProperties().getProperty(property, defaultVal);
    }
}
