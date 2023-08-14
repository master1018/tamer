public class DeleteTempJar
{
    public static void realMain(String args[]) throws Exception
    {
        final File zf = File.createTempFile("deletetemp", ".jar");
        zf.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(zf);
             JarOutputStream jos = new JarOutputStream(fos))
        {
            JarEntry je = new JarEntry("entry");
            jos.putNextEntry(je);
            jos.write("hello, world".getBytes("ASCII"));
        }
        HttpServer server = HttpServer.create(
                new InetSocketAddress((InetAddress) null, 0), 0);
        HttpContext context = server.createContext("/",
            new HttpHandler() {
                public void handle(HttpExchange e) {
                    try (FileInputStream fis = new FileInputStream(zf)) {
                        e.sendResponseHeaders(200, zf.length());
                        OutputStream os = e.getResponseBody();
                        byte[] buf = new byte[1024];
                        int count = 0;
                        while ((count = fis.read(buf)) != -1) {
                            os.write(buf, 0, count);
                        }
                    } catch (Exception ex) {
                        unexpected(ex);
                    } finally {
                        e.close();
                    }
                }
            });
        server.start();
        URL url = new URL("jar:http:
                          + new Integer(server.getAddress().getPort()).toString()
                          + "/deletetemp.jar!/");
        JarURLConnection c = (JarURLConnection)url.openConnection();
        JarFile f = c.getJarFile();
        check(f.getEntry("entry") != null);
        System.out.println(f.getName());
        server.stop(0);
    }
    static volatile int passed = 0, failed = 0;
    static boolean pass() {passed++; return true;}
    static boolean fail() {failed++; Thread.dumpStack(); return false;}
    static boolean fail(String msg) {System.out.println(msg); return fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static boolean check(boolean cond) {if (cond) pass(); else fail(); return cond;}
    static boolean equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) return pass();
        else return fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
