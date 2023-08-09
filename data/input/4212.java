public class JdiLoadedByCustomLoader {
    public static void main(String args[]) throws Exception {
        File f1 = new File(args[0]);
        String home = System.getProperty("java.home");
        String tools = ".." + File.separatorChar + "lib" +
            File.separatorChar + "tools.jar";
        File f2 = (new File(home, tools)).getCanonicalFile();
        URL[] urls = { f1.toURL(), f2.toURL() };
        URLClassLoader cl = new URLClassLoader(urls);
        Class c = Class.forName("ListConnectors", true, cl);
        Method m = c.getDeclaredMethod("list");
        Object o = c.newInstance();
        m.invoke(o);
    }
}
