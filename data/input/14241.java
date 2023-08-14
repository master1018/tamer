public class NotificationInfoTest {
    private static final Set<String> failed = new TreeSet<String>();
    private static final Set<String> suspicious = new TreeSet<String>();
    public static void main(String[] args) throws Exception {
        System.out.println("Checking that all known MBeans that are " +
                           "NotificationBroadcasters have sane " +
                           "MBeanInfo.getNotifications()");
        System.out.println("Checking platform MBeans...");
        checkPlatformMBeans();
        CodeSource cs =
            javax.management.MBeanServer.class.getProtectionDomain()
            .getCodeSource();
        URL codeBase;
        if (cs == null) {
            String javaHome = System.getProperty("java.home");
            String[] candidates = {"/lib/rt.jar", "/classes/"};
            codeBase = null;
            for (String candidate : candidates) {
                File file = new File(javaHome + candidate);
                if (file.exists()) {
                    codeBase = file.toURI().toURL();
                    break;
                }
            }
            if (codeBase == null) {
                throw new Exception(
                        "Could not determine codeBase for java.home=" + javaHome);
            }
        } else
            codeBase = cs.getLocation();
        System.out.println();
        System.out.println("Looking for standard MBeans...");
        String[] classes = findStandardMBeans(codeBase);
        System.out.println("Testing standard MBeans...");
        for (int i = 0; i < classes.length; i++) {
            String name = classes[i];
            Class<?> c;
            try {
                c = Class.forName(name);
            } catch (Throwable e) {
                System.out.println(name + ": cannot load (not public?): " + e);
                continue;
            }
            if (!NotificationBroadcaster.class.isAssignableFrom(c)) {
                System.out.println(name + ": not a NotificationBroadcaster");
                continue;
            }
            if (Modifier.isAbstract(c.getModifiers())) {
                System.out.println(name + ": abstract class");
                continue;
            }
            NotificationBroadcaster mbean;
            Constructor<?> constr;
            try {
                constr = c.getConstructor();
            } catch (Exception e) {
                System.out.println(name + ": no public no-arg constructor: "
                                   + e);
                continue;
            }
            try {
                mbean = (NotificationBroadcaster) constr.newInstance();
            } catch (Exception e) {
                System.out.println(name + ": no-arg constructor failed: " + e);
                continue;
            }
            check(mbean);
        }
        System.out.println();
        System.out.println("Testing some explicit cases...");
        check(new RelationService(false));
        checkRMIConnectorServer();
        System.out.println();
        if (!suspicious.isEmpty())
            System.out.println("SUSPICIOUS CLASSES: " + suspicious);
        if (failed.isEmpty())
            System.out.println("TEST PASSED");
        else {
            System.out.println("TEST FAILED: " + failed);
            System.exit(1);
        }
    }
    private static void check(NotificationBroadcaster mbean)
            throws Exception {
        System.out.print(mbean.getClass().getName() + ": ");
        check(mbean.getClass().getName(), mbean.getNotificationInfo());
    }
    private static void checkPlatformMBeans() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> mbeanNames = mbs.queryNames(null, null);
        for (ObjectName name : mbeanNames) {
            if (!mbs.isInstanceOf(name,
                                  NotificationBroadcaster.class.getName())) {
                System.out.println(name + ": not a NotificationBroadcaster");
            } else {
                MBeanInfo mbi = mbs.getMBeanInfo(name);
                check(name.toString(), mbi.getNotifications());
            }
        }
    }
    private static void checkRMIConnectorServer() throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
        RMIConnectorServer connector = new RMIConnectorServer(url, null);
        check(connector);
    }
    private static void check(String what, MBeanNotificationInfo[] mbnis) {
        System.out.print(what + ": checking notification info: ");
        if (mbnis.length == 0) {
            System.out.println("NONE (suspicious)");
            suspicious.add(what);
            return;
        }
        for (int j = 0; j < mbnis.length; j++) {
            String notifClassName = mbnis[j].getName();
                Class notifClass;
                try {
                    notifClass = Class.forName(notifClassName);
                } catch (Exception e) {
                    System.out.print("FAILED(" + notifClassName + ": " + e +
                                     ") ");
                    failed.add(what);
                    continue;
                }
                if (!Notification.class.isAssignableFrom(notifClass)) {
                    System.out.print("FAILED(" + notifClassName +
                                     ": not a Notification) ");
                    failed.add(what);
                    continue;
                }
                System.out.print("OK(" + notifClassName + ") ");
        }
        System.out.println();
    }
    private static String[] findStandardMBeans(URL codeBase)
            throws Exception {
        Set<String> names;
        if (codeBase.getProtocol().equalsIgnoreCase("file")
            && codeBase.toString().endsWith("/"))
            names = findStandardMBeansFromDir(codeBase);
        else
            names = findStandardMBeansFromJar(codeBase);
        Set<String> standardMBeanNames = new TreeSet<String>();
        for (String name : names) {
            if (name.endsWith("MBean")) {
                String prefix = name.substring(0, name.length() - 5);
                if (names.contains(prefix))
                    standardMBeanNames.add(prefix);
            }
        }
        return standardMBeanNames.toArray(new String[0]);
    }
    private static Set<String> findStandardMBeansFromJar(URL codeBase)
            throws Exception {
        InputStream is = codeBase.openStream();
        JarInputStream jis = new JarInputStream(is);
        Set<String> names = new TreeSet<String>();
        JarEntry entry;
        while ((entry = jis.getNextJarEntry()) != null) {
            String name = entry.getName();
            if (!name.endsWith(".class"))
                continue;
            name = name.substring(0, name.length() - 6);
            name = name.replace('/', '.');
            names.add(name);
        }
        return names;
    }
    private static Set<String> findStandardMBeansFromDir(URL codeBase)
            throws Exception {
        File dir = new File(new URI(codeBase.toString()));
        Set<String> names = new TreeSet<String>();
        scanDir(dir, "", names);
        return names;
    }
    private static void scanDir(File dir, String prefix, Set<String> names)
            throws Exception {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            String name = f.getName();
            String p = (prefix.equals("")) ? name : prefix + "." + name;
            if (f.isDirectory())
                scanDir(f, p, names);
            else if (name.endsWith(".class")) {
                p = p.substring(0, p.length() - 6);
                names.add(p);
            }
        }
    }
}
