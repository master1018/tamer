public class CookieHandlerTest implements Runnable {
    static Map<String,String> cookies;
    ServerSocket ss;
    public void run() {
        try {
            Socket s = ss.accept();
            InputStream is = s.getInputStream ();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            boolean flag = false;
            String x;
            while ((x=r.readLine()) != null) {
                if (x.length() ==0) {
                    break;
                }
                String header = "Cookie: ";
                if (x.startsWith(header)) {
                    if (x.equals("Cookie: "+((String)cookies.get("Cookie")))) {
                        flag = true;
                    }
                }
            }
            if (!flag) {
                throw new RuntimeException("server should see cookie in request");
            }
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Set-Cookie2: "+((String)cookies.get("Set-Cookie2")+"\r\n"));
            out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.print("<HTML>");
            out.print("<HEAD><TITLE>Testing cookie</TITLE></HEAD>");
            out.print("<BODY>OK.</BODY>");
            out.print("</HTML>");
            out.flush();
            s.close();
            ss.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    CookieHandlerTest() throws Exception {
        ss = new ServerSocket(0);
        (new Thread(this)).start();
        String uri = "http:
                     Integer.toString(ss.getLocalPort());
        URL url = new URL(uri);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        int respCode = http.getResponseCode();
        http.disconnect();
    }
    public static void main(String args[]) throws Exception {
        cookies = new HashMap<String, String>();
        cookies.put("Cookie", "$Version=\"1\"; Customer=\"WILE_E_COYOTE\"; $Path=\"/acme\"");
        cookies.put("Set-Cookie2", "$Version=\"1\"; Part_Number=\"Riding_Rocket_0023\"; $Path=\"/acme/ammo\"; Part_Number=\"Rocket_Launcher_0001\"; $Path=\"/acme\"");
        CookieHandler.setDefault(new MyCookieHandler());
        new CookieHandlerTest();
    }
    static class MyCookieHandler extends CookieHandler {
        public Map<String,List<String>>
            get(URI uri, Map<String,List<String>> requestHeaders)
            throws IOException {
            Map<String,List<String>> map = new HashMap<String,List<String>>();
            List<String> l = new ArrayList<String>();
            l.add(cookies.get("Cookie"));
            map.put("Cookie",l);
            return Collections.unmodifiableMap(map);
        }
        public void
            put(URI uri, Map<String,List<String>> responseHeaders)
            throws IOException {
            List<String> l = responseHeaders.get("Set-Cookie2");
            String value = l.get(0);
            if (!value.equals(cookies.get("Set-Cookie2"))) {
                throw new RuntimeException("cookie should be available for handle to put into cache");
               }
        }
    }
}
