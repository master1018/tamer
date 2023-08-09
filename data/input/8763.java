public class ChunkedErrorStream
{
    com.sun.net.httpserver.HttpServer httpServer;
    static {
        System.getProperties().setProperty("sun.net.http.errorstream.enableBuffering", "true");
    }
    public static void main(String[] args) {
        new ChunkedErrorStream();
    }
    public ChunkedErrorStream() {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }  finally {
            httpServer.stop(1);
        }
    }
    void doClient() {
        for (int times=0; times<3; times++) {
            HttpURLConnection uc = null;
            try {
                InetSocketAddress address = httpServer.getAddress();
                String URLStr = "http:
                if (times == 0) {
                    URLStr += "first";
                } else {
                    URLStr += "second";
                }
                System.out.println("Trying " + URLStr);
                URL url = new URL(URLStr);
                uc = (HttpURLConnection)url.openConnection();
                uc.getInputStream();
                throw new RuntimeException("Failed: getInputStream should throw and IOException");
            }  catch (IOException e) {
                if (e instanceof SocketTimeoutException) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed: SocketTimeoutException should not happen");
                }
                InputStream es = uc.getErrorStream();
                byte[] ba = new byte[1024];
                int count = 0, ret;
                try {
                    while ((ret = es.read(ba)) != -1)
                        count += ret;
                    es.close();
                } catch  (IOException ioe) {
                    ioe.printStackTrace();
                }
                if (count == 0)
                    throw new RuntimeException("Failed: ErrorStream returning 0 bytes");
                if (times >= 1 && count != (4096+10))
                    throw new RuntimeException("Failed: ErrorStream returning " + count +
                                                 " bytes. Expecting " + (4096+10));
                System.out.println("Read " + count + " bytes from the errorStream");
            }
        }
    }
    void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/test/first", new FirstHandler());
        httpServer.createContext("/test/second", new SecondHandler());
        httpServer.start();
    }
    class FirstHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            byte[] ba = new byte[1024];
            while (is.read(ba) != -1);
            is.close();
            Headers resHeaders = t.getResponseHeaders();
            resHeaders.add("Connection", "close");
            t.sendResponseHeaders(404, 0);
            OutputStream os = t.getResponseBody();
            byte b = 'a';
            for (int i=0; i<2048; i++)
                os.write(b);
            os.close();
            t.close();
        }
    }
    static class SecondHandler implements HttpHandler {
        static int count = 0;
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            byte[] ba = new byte[1024];
            while (is.read(ba) != -1);
            is.close();
            if (count > 0) {
                System.out.println("server sleeping...");
                try { Thread.sleep(1000); } catch(InterruptedException e) {}
            }
            count++;
            t.sendResponseHeaders(404, 0);
            OutputStream os = t.getResponseBody();
            byte b = 'a';
            for (int i=0; i<(4096+10); i++)
                os.write(b);
            os.close();
            t.close();
        }
    }
}
