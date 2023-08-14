public class B6401598 {
        static class MyHandler implements HttpHandler {
                public MyHandler() {
                }
                public void handle(HttpExchange arg0) throws IOException {
                        try {
                                InputStream is = arg0.getRequestBody();
                                OutputStream os = arg0.getResponseBody();
                                DataInputStream dis = new DataInputStream(is);
                                short input = dis.readShort();
                                while (dis.read() != -1) ;
                                dis.close();
                                DataOutputStream dos = new DataOutputStream(os);
                                arg0.sendResponseHeaders(200, 0);
                                dos.writeShort(input);
                                dos.flush();
                                dos.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                                error = true;
                        }
                }
        }
        static int port;
        static boolean error = false;
        static ExecutorService exec;
        static HttpServer server;
        public static void main(String[] args) {
                try {
                        server = HttpServer.create(new InetSocketAddress(0), 400);
                        server.createContext("/server/", new MyHandler());
                        exec = Executors.newFixedThreadPool(3);
                        server.setExecutor(exec);
                        port = server.getAddress().getPort();
                        server.start();
                        short counter;
                        for (counter = 0; counter < 1000; counter++) {
                                HttpURLConnection connection = getHttpURLConnection(new URL("http:
                                OutputStream os = connection.getOutputStream();
                                DataOutputStream dos = new DataOutputStream(os);
                                dos.writeShort(counter);
                                dos.flush();
                                dos.close();
                                counter++;
                                InputStream is = connection.getInputStream();
                                DataInputStream dis = new DataInputStream(is);
                                short ret = dis.readShort();
                                dis.close();
                        }
                        System.out.println ("Stopping");
                        server.stop (1);
                        exec.shutdown();
                } catch (IOException e) {
                        e.printStackTrace();
                        server.stop (1);
                        exec.shutdown();
                }
        }
        static HttpURLConnection getHttpURLConnection(URL url, int timeout) throws IOException {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(40000);
                httpURLConnection.setReadTimeout(timeout);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setAllowUserInteraction(false);
                httpURLConnection.setRequestMethod("POST");
                return httpURLConnection;
        }
}
