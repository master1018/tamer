public class B5045306
{
    static SimpleHttpTransaction httpTrans;
    static HttpServer server;
    public static void main(String[] args) throws Exception {
        startHttpServer();
        clientHttpCalls();
    }
    public static void startHttpServer() {
        try {
            httpTrans = new SimpleHttpTransaction();
            server = new HttpServer(httpTrans, 1, 10, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void clientHttpCalls() {
        try {
            System.out.println("http server listen on: " + server.getLocalPort());
            String baseURLStr = "http:
                                  server.getLocalPort() + "/";
            URL bigDataURL = new URL (baseURLStr + "firstCall");
            URL smallDataURL = new URL (baseURLStr + "secondCall");
            HttpURLConnection uc = (HttpURLConnection)bigDataURL.openConnection();
            InputStream is = uc.getInputStream();
            byte[] ba = new byte[1];
            is.read(ba);
            is.close();
            try { Thread.sleep(2000); } catch (Exception e) {}
            uc = (HttpURLConnection)smallDataURL.openConnection();
            uc.getResponseCode();
            if (SimpleHttpTransaction.failed)
                throw new RuntimeException("Failed: Initial Keep Alive Connection is not being reused");
            URL part2Url = new URL (baseURLStr + "part2");
            uc = (HttpURLConnection)part2Url.openConnection();
            is = uc.getInputStream();
            is.close();
            try { Thread.sleep(2000); } catch (Exception e) {}
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            if (threadMXBean.isThreadCpuTimeSupported()) {
                long[] threads = threadMXBean.getAllThreadIds();
                ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(threads);
                for (int i=0; i<threadInfo.length; i++) {
                    if (threadInfo[i].getThreadName().equals("Keep-Alive-SocketCleaner"))  {
                        System.out.println("Found Keep-Alive-SocketCleaner thread");
                        long threadID = threadInfo[i].getThreadId();
                        long before = threadMXBean.getThreadCpuTime(threadID);
                        try { Thread.sleep(2000); } catch (Exception e) {}
                        long after = threadMXBean.getThreadCpuTime(threadID);
                        if (before ==-1 || after == -1)
                            break;  
                        long total = after - before;
                        if (total >= 1000000000)  
                            throw new RuntimeException("Failed: possible recursive loop in Keep-Alive-SocketCleaner");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.terminate();
        }
    }
}
class SimpleHttpTransaction implements HttpCallback
{
    static boolean failed = false;
    static final int RESPONSE_DATA_LENGTH = 128 * 1024;
    int port1;
    public void request(HttpTransaction trans) {
        try {
            String path = trans.getRequestURI().getPath();
            if (path.equals("/firstCall")) {
                port1 = trans.channel().socket().getPort();
                System.out.println("First connection on client port = " + port1);
                byte[] responseBody = new byte[RESPONSE_DATA_LENGTH];
                for (int i=0; i<responseBody.length; i++)
                    responseBody[i] = 0x41;
                trans.setResponseEntityBody (responseBody, responseBody.length);
                trans.sendResponse(200, "OK");
            } else if (path.equals("/secondCall")) {
                int port2 = trans.channel().socket().getPort();
                System.out.println("Second connection on client port = " + port2);
                if (port1 != port2)
                    failed = true;
                trans.setResponseHeader ("Content-length", Integer.toString(0));
                System.out.println("server sleeping...");
                try {Thread.sleep(6000); } catch (InterruptedException e) {}
                trans.sendResponse(200, "OK");
            } else if(path.equals("/part2")) {
                System.out.println("Call to /part2");
                byte[] responseBody = new byte[RESPONSE_DATA_LENGTH];
                for (int i=0; i<responseBody.length; i++)
                    responseBody[i] = 0x41;
                trans.setResponseEntityBody (responseBody, responseBody.length);
                trans.setResponseHeader("Content-length", Integer.toString(responseBody.length+1));
                trans.sendResponse(200, "OK");
                trans.channel().socket().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
