public class CookieManagerTest {
    static CookieHttpTransaction httpTrans;
    static HttpServer server;
    public static void main(String[] args) throws Exception {
        startHttpServer();
        makeHttpCall();
        if (httpTrans.badRequest) {
            throw new RuntimeException("Test failed : bad cookie header");
        }
    }
    public static void startHttpServer() {
        try {
            httpTrans = new CookieHttpTransaction();
            server = new HttpServer(httpTrans, 1, 1, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void makeHttpCall() {
        try {
            System.out.println("http server listen on: " + server.getLocalPort());
            CookieHandler.setDefault(new CookieManager());
            for (int i = 0; i < CookieHttpTransaction.testCount; i++) {
                System.out.println("====== CookieManager test " + (i+1) + " ======");
                ((CookieManager)CookieHandler.getDefault()).setCookiePolicy(CookieHttpTransaction.testPolicies[i]);
                ((CookieManager)CookieHandler.getDefault()).getCookieStore().removeAll();
                URL url = new URL("http" , InetAddress.getLocalHost().getHostAddress(),
                                    server.getLocalPort(), CookieHttpTransaction.testCases[i][0].serverPath);
                HttpURLConnection uc = (HttpURLConnection)url.openConnection();
                uc.getResponseCode();
                uc.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.terminate();
        }
    }
}
class CookieHttpTransaction implements HttpCallback {
    public static boolean badRequest = false;
    public static final int testCount = 6;
    private String localHostAddr = "127.0.0.1";
    public static class CookieTestCase {
        public String headerToken;
        public String cookieToSend;
        public String cookieToRecv;
        public String serverPath;
        public CookieTestCase(String h, String cts, String ctr, String sp) {
            headerToken = h;
            cookieToSend = cts;
            cookieToRecv = ctr;
            serverPath = sp;
        }
    };
    public static CookieTestCase[][] testCases = null;  
    public static CookiePolicy[] testPolicies = null;   
    CookieHttpTransaction() {
        testCases = new CookieTestCase[testCount][];
        testPolicies = new CookiePolicy[testCount];
        try {
            localHostAddr = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        };
        int count = 0;
        testPolicies[count] = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        testCases[count++] = new CookieTestCase[]{
                new CookieTestCase("Set-Cookie",
                "CUSTOMER=WILE:BOB; path=/; expires=Wednesday, 09-Nov-2030 23:12:40 GMT;" + "domain=." + localHostAddr,
                "CUSTOMER=WILE:BOB",
                "/"
                ),
                new CookieTestCase("Set-Cookie",
                "PART_NUMBER=ROCKET_LAUNCHER_0001; path=/;" + "domain=." + localHostAddr,
                "CUSTOMER=WILE:BOB; PART_NUMBER=ROCKET_LAUNCHER_0001",
                "/"
                ),
                new CookieTestCase("Set-Cookie",
                "SHIPPING=FEDEX; path=/foo;" + "domain=." + localHostAddr,
                "CUSTOMER=WILE:BOB; PART_NUMBER=ROCKET_LAUNCHER_0001",
                "/"
                ),
                new CookieTestCase("Set-Cookie",
                "SHIPPING=FEDEX; path=/foo;" + "domain=." + localHostAddr,
                "CUSTOMER=WILE:BOB; PART_NUMBER=ROCKET_LAUNCHER_0001; SHIPPING=FEDEX",
                "/foo"
                )
                };
        testPolicies[count] = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        testCases[count++] = new CookieTestCase[]{
                new CookieTestCase("Set-Cookie",
                "PART_NUMBER=ROCKET_LAUNCHER_0001; path=/;" + "domain=." + localHostAddr,
                "PART_NUMBER=ROCKET_LAUNCHER_0001",
                "/"
                ),
                new CookieTestCase("Set-Cookie",
                "PART_NUMBER=RIDING_ROCKET_0023; path=/ammo;" + "domain=." + localHostAddr,
                "PART_NUMBER=RIDING_ROCKET_0023; PART_NUMBER=ROCKET_LAUNCHER_0001",
                "/ammo"
                )
                };
        testPolicies[count] = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        testCases[count++] = new CookieTestCase[]{
                new CookieTestCase("Set-Cookie2",
                "Customer=\"WILE_E_COYOTE\"; Version=\"1\"; Path=\"/acme\";" + "domain=." + localHostAddr,
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\";$Path=\"/acme\";$Domain=\"." + localHostAddr + "\"",
                "/acme/login"
                ),
                new CookieTestCase("Set-Cookie2",
                "Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\";Path=\"/acme\";" + "domain=." + localHostAddr,
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\";$Path=\"/acme\";" + "$Domain=\"." + localHostAddr  + "\"" + "; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";" + "$Domain=\"." + localHostAddr +  "\"",
                "/acme/pickitem"
                ),
                new CookieTestCase("Set-Cookie2",
                "Shipping=\"FedEx\"; Version=\"1\"; Path=\"/acme\";" + "domain=." + localHostAddr,
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\";$Path=\"/acme\";" + "$Domain=\"." + localHostAddr  + "\"" + "; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";" + "$Domain=\"." + localHostAddr  + "\"" + "; Shipping=\"FedEx\";$Path=\"/acme\";" + "$Domain=\"." + localHostAddr + "\"",
                "/acme/shipping"
                )
                };
        testPolicies[count] = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        testCases[count++] = new CookieTestCase[]{
                new CookieTestCase("Set-Cookie2",
                "Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\"; Path=\"/acme\";" + "domain=." + localHostAddr,
                "$Version=\"1\"; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";$Domain=\"." + localHostAddr + "\"",
                "/acme/ammo"
                ),
                new CookieTestCase("Set-Cookie2",
                "Part_Number=\"Riding_Rocket_0023\"; Version=\"1\"; Path=\"/acme/ammo\";" + "domain=." + localHostAddr,
                "$Version=\"1\"; Part_Number=\"Riding_Rocket_0023\";$Path=\"/acme/ammo\";$Domain=\"." + localHostAddr  + "\"" + "; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";" + "$Domain=\"." + localHostAddr + "\"",
                "/acme/ammo"
                ),
                new CookieTestCase("",
                "",
                "$Version=\"1\"; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";" + "$Domain=\"." + localHostAddr + "\"",
                "/acme/parts"
                )
                };
        testPolicies[count] = CookiePolicy.ACCEPT_ORIGINAL_SERVER;
        testCases[count++] = new CookieTestCase[]{
                new CookieTestCase("Set-Cookie2",
                "Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\"; Path=\"/acme\";" + "domain=." + localHostAddr,
                "$Version=\"1\"; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";$Domain=\"." + localHostAddr + "\"",
                "/acme"
                ),
                new CookieTestCase("Set-Cookie2",
                "Part_Number=\"Rocket_Launcher_2000\"; Version=\"1\"; Path=\"/acme\";" + "domain=." + localHostAddr,
                "$Version=\"1\"; Part_Number=\"Rocket_Launcher_2000\";$Path=\"/acme\";$Domain=\"." + localHostAddr + "\"",
                "/acme"
                )
                };
        testPolicies[count] = CookiePolicy.ACCEPT_ALL;
        testCases[count++] = new CookieTestCase[]{
                new CookieTestCase("Set-Cookie2",
                "Customer=\"WILE_E_COYOTE\"; Version=\"1\"; Path=\"/acme\"",
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\";$Path=\"/acme\";$Domain=\""+localHostAddr+"\"",
                "/acme/login"
                ),
                new CookieTestCase("Set-Cookie2",
                "Part_Number=\"Rocket_Launcher_0001\"; Version=\"1\";Path=\"/acme\"",
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\";$Path=\"/acme\";$Domain=\""+localHostAddr+"\"" + "; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";$Domain=\""+localHostAddr+"\"",
                "/acme/pickitem"
                ),
                new CookieTestCase("Set-Cookie2",
                "Shipping=\"FedEx\"; Version=\"1\"; Path=\"/acme\"",
                "$Version=\"1\"; Customer=\"WILE_E_COYOTE\";$Path=\"/acme\";$Domain=\""+localHostAddr+"\"" + "; Part_Number=\"Rocket_Launcher_0001\";$Path=\"/acme\";$Domain=\""+localHostAddr+"\"" + "; Shipping=\"FedEx\";$Path=\"/acme\";$Domain=\""+localHostAddr+"\"",
                "/acme/shipping"
                )
                };
        assert count == testCount;
    }
    private int testcaseDone = 0;
    private int testDone = 0;
    public void request(HttpTransaction trans) {
        try {
            if (testDone < testCases[testcaseDone].length) {
                if (testDone > 0) checkResquest(trans);
                trans.addResponseHeader("Location", testCases[testcaseDone][testDone].serverPath);
                trans.addResponseHeader(testCases[testcaseDone][testDone].headerToken,
                                        testCases[testcaseDone][testDone].cookieToSend);
                testDone++;
                trans.sendResponse(302, "Moved Temporarily");
            } else {
                if (testDone > 0) checkResquest(trans);
                testcaseDone++;
                testDone = 0;
                trans.sendResponse(200, "OK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void checkResquest(HttpTransaction trans) {
        String cookieHeader = null;
        assert testDone > 0;
        cookieHeader = trans.getRequestHeader("Cookie");
        if (cookieHeader != null &&
            cookieHeader.equalsIgnoreCase(testCases[testcaseDone][testDone-1].cookieToRecv))
        {
            System.out.printf("%15s %s\n", "PASSED:", cookieHeader);
        } else {
            System.out.printf("%15s %s\n", "FAILED:", cookieHeader);
            System.out.printf("%15s %s\n\n", "should be:", testCases[testcaseDone][testDone-1].cookieToRecv);
            badRequest = true;
        }
    }
}
