public class MaxTime {
    private static boolean debugFlag = false;
    private static void debug(String s) {
        if (debugFlag) {
            System.err.println(s);
        }
    }
    private static void usage() {
        System.err.println(
          "Usage:\n" +
          "  java  MaxTime  <jarfile> <timeout>\n" +
          "\n" +
          "  <jarfile> is a jar file specifying an application to run\n" +
          "  <timeout> is an integer number of seconds to allow the app to run." );
        System.exit(1);
    }
    public static void main(String[] args) {
        int timeoutPeriod = 0;
        String mainClass = null;
        if (args.length != 2)
            usage();
        try {
            timeoutPeriod = Integer.parseInt(args[1]);
        } catch (Exception e) {
            usage();
        }
        try {
            mainClass = new JarFile(args[0]).getManifest()
                .getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        } catch (FileNotFoundException e) {
            System.err.println("Can't find " + args[0]);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }
        final int timeout = timeoutPeriod * 1000;
        new Thread() {
            public void run() {
                try {
                    debug("Before timeout!");
                    Thread.sleep(timeout);
                    debug("After timeout!");
                } catch (InterruptedException e) {
                }
                debug("Exit caused by timeout!");
                System.exit(0);
            }
        }.start();
        try {
            ClassLoader cl = new URLClassLoader(
                                   new URL[] {new URL("file:"+args[0])});
            Class clazz = cl.loadClass(mainClass);
            String[] objs = new String[0];
            Method mainMethod = clazz.getMethod("main", new Class[]{objs.getClass()});
            mainMethod.invoke(null, new Object[]{objs});
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(3);
        }
    }
}
