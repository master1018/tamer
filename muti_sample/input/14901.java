public class ResponseCacheTest implements Runnable {
    ServerSocket ss;
    static URL url1;
    static URL url2;
    static String FNPrefix, OutFNPrefix;
    static List<Closeable> streams = new ArrayList<>();
    static List<File> files = new ArrayList<>();
    public void run() {
        Socket s = null;
        FileInputStream fis = null;
        try {
            s = ss.accept();
            InputStream is = s.getInputStream ();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String x;
            while ((x=r.readLine()) != null) {
                if (x.length() ==0) {
                    break;
                }
            }
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            File file2 = new File(FNPrefix+"file2.1");
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
            out.print("Content-Length: "+file2.length()+"\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            fis = new FileInputStream(file2);
            byte[] buf = new byte[(int)file2.length()];
            int len;
            while ((len = fis.read(buf)) != -1) {
                out.print(new String(buf));
            }
            out.flush();
            s.close();
            ss.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { ss.close(); } catch (IOException unused) {}
            try { s.close(); } catch (IOException unused) {}
            try { fis.close(); } catch (IOException unused) {}
        }
    }
static class NameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    ResponseCacheTest() throws Exception {
        ss = new ServerSocket(0);
        (new Thread(this)).start();
        url1 = new URL("http:
        HttpURLConnection http = (HttpURLConnection)url1.openConnection();
        InputStream is = null;
        System.out.println("request headers: "+http.getRequestProperties());
        System.out.println("responsecode is :"+http.getResponseCode());
        Map<String,List<String>> headers1 = http.getHeaderFields();
        try {
            is = http.getInputStream();
        } catch (IOException ioex) {
            throw new RuntimeException(ioex.getMessage());
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String x;
        File fileout = new File(OutFNPrefix+"file1");
        PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    new FileOutputStream(fileout)));
        while ((x=r.readLine()) != null) {
            out.print(x+"\n");
        }
        out.flush();
        out.close();
        http.disconnect();
        url2 = new URL("http:
                       Integer.toString(ss.getLocalPort())+"/file2.1");
        http = (HttpURLConnection)url2.openConnection();
        System.out.println("responsecode2 is :"+http.getResponseCode());
        Map<String,List<String>> headers2 = http.getHeaderFields();
        try {
            is = http.getInputStream();
        } catch (IOException ioex) {
            throw new RuntimeException(ioex.getMessage());
        }
        r = new BufferedReader(new InputStreamReader(is));
        fileout = new File(OutFNPrefix+"file2.2");
        out = new PrintStream(
                                 new BufferedOutputStream(
                                    new FileOutputStream(fileout)));
        while ((x=r.readLine()) != null) {
            out.print(x+"\n");
        }
        out.flush();
        out.close();
        File file1 = new File(OutFNPrefix+"file1");
        File file2 = new File(OutFNPrefix+"file2.2");
        files.add(file1);
        files.add(file2);
        System.out.println("headers1"+headers1+"\nheaders2="+headers2);
        if (!headers1.equals(headers2) || file1.length() != file2.length()) {
            throw new RuntimeException("test failed");
        }
    }
    public static void main(String args[]) throws Exception {
        try {
            ResponseCache.setDefault(new MyResponseCache());
            FNPrefix = System.getProperty("test.src", ".")+"/";
            OutFNPrefix = System.getProperty("test.scratch", ".")+"/";
            new ResponseCacheTest();
        } finally{
            ResponseCache.setDefault(null);
            for (Closeable c: streams) {
                try { c.close(); } catch (IOException unused) {}
            }
            for (File f: files) {
                f.delete();
            }
        }
    }
    static class MyResponseCache extends ResponseCache {
        public CacheResponse
        get(URI uri, String rqstMethod, Map<String,List<String>> rqstHeaders)
            throws IOException {
            if (uri.equals(ParseUtil.toURI(url1))) {
                return new MyCacheResponse(FNPrefix+"file1.cache");
            }
            return null;
        }
        public CacheRequest put(URI uri, URLConnection conn)  throws IOException {
            return new MyCacheRequest(OutFNPrefix+"file2.cache", conn.getHeaderFields());
        }
    }
    static class MyCacheResponse extends CacheResponse {
        FileInputStream fis;
        Map<String,List<String>> headers;
        public MyCacheResponse(String filename) {
            try {
                fis = new FileInputStream(new File(filename));
                streams.add(fis);
                ObjectInputStream ois = new ObjectInputStream(fis);
                headers = (Map<String,List<String>>)ois.readObject();
            } catch (Exception ex) {
            }
        }
        public InputStream getBody() throws IOException {
            return fis;
        }
        public Map<String,List<String>> getHeaders() throws IOException {
            return headers;
        }
    }
    static class MyCacheRequest extends CacheRequest {
        FileOutputStream fos;
        public MyCacheRequest(String filename, Map<String,List<String>> rspHeaders) {
            try {
                File file = new File(filename);
                fos = new FileOutputStream(file);
                streams.add(fos);
                files.add(file);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(rspHeaders);
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
        public OutputStream getBody() throws IOException {
            return fos;
        }
        public void abort() {
        }
    }
}
