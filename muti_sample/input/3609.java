public class ClassnameCharTest implements HttpCallback {
    private static String FNPrefix;
    private String[] respBody = new String[52];
    private byte[][] bufs = new byte[52][8*1024];
    private static MessageDigest md5;
    private static byte[] file1Mac, file2Mac;
    public void request (HttpTransaction req) {
        try {
            String filename = req.getRequestURI().getPath();
            System.out.println("getRequestURI = "+req.getRequestURI());
            System.out.println("filename = "+filename);
            FileInputStream fis = new FileInputStream(FNPrefix+filename);
            req.setResponseEntityBody(fis);
            req.sendResponse(200, "OK");
            req.orderlyClose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static HttpServer server;
    public static void test () throws Exception {
        try {
            FNPrefix = System.getProperty("test.classes", ".")+"/";
            server = new HttpServer (new ClassnameCharTest(), 1, 10, 0);
            System.out.println ("Server: listening on port: " + server.getLocalPort());
            URL base = new URL("http:
            MyAppletClassLoader acl = new MyAppletClassLoader(base);
            Class class1 = acl.findClass("fo o");
            System.out.println("class1 = "+class1);
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
        server.terminate();
    }
    public static void main(String[] args) throws Exception {
        test();
    }
    public static void except (String s) {
        server.terminate();
        throw new RuntimeException (s);
    }
}
class MyAppletClassLoader extends AppletClassLoader {
    MyAppletClassLoader(URL base) {
        super(base);
    }
    public Class findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
