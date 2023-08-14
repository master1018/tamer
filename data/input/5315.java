public class TesterClient {
    private static final Logger log = Logger.getLogger("test.xembed.TesterClient");
    private static Method test;
    private static boolean passed = false;
    public static void main(String[] args) throws Throwable {
        Class cl = Class.forName("sun.awt.X11.XEmbedServerTester");
        test = cl.getMethod(args[0], new Class[0]);
        long window = Long.parseLong(args[1]);
        Rectangle r[] = new Rectangle[(args.length-2)/4];
        for (int i = 0; i < r.length; i++) {
            r[i] = new Rectangle(Integer.parseInt(args[2+i*4]), Integer.parseInt(args[2+i*4+1]),
                                 Integer.parseInt(args[2+i*4+2]), Integer.parseInt(args[2+i*4+3]));
        }
        startClient(r, window);
    }
    public static void startClient(Rectangle bounds[], long window) throws Throwable {
        Method m_getTester = Class.forName("sun.awt.X11.XEmbedServerTester").
            getMethod("getTester", new Class[] {bounds.getClass(), Long.TYPE});
        final Object tester = m_getTester.invoke(null, new Object[] {bounds, window});
        try {
            log.info("Starting test " + test.getName());
            test.invoke(tester, (Object[])null);
            log.info("Test " + test.getName() + " PASSED.");
            passed = true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Test " + test.getName() + " FAILED.", e);
        }
        System.exit(passed?0:1);
    }
}
