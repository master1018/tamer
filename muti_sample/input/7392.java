public class ORBProperties {
    public static final String ORB_CLASS =
        "org.omg.CORBA.ORBClass=com.sun.corba.se.impl.orb.ORBImpl";
    public static final String ORB_SINGLETON_CLASS =
        "org.omg.CORBA.ORBSingletonClass=com.sun.corba.se.impl.orb.ORBSingleton";
    public static void main (String[] args) {
        try {
            String javaHome = System.getProperty("java.home");
            File propFile = new File(javaHome + File.separator
                                     + "lib" + File.separator
                                     + "orb.properties");
            if (propFile.exists())
                return;
            FileOutputStream out = new FileOutputStream(propFile);
            PrintWriter pw = new PrintWriter(out);
            try {
                pw.println(ORB_CLASS);
                pw.println(ORB_SINGLETON_CLASS);
            } finally {
                pw.close();
                out.close();
            }
        } catch (Exception ex) { }
    }
}
