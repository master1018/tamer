public class ClassLoad {
     public static void main(String[] args) throws Exception {
         boolean error = true;
         HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
         HttpHandler handler = new HttpHandler() {
             public void handle(HttpExchange t) throws IOException {
                 InputStream is = t.getRequestBody();
                 while (is.read() != -1);
                 t.sendResponseHeaders (404, -1);
                 t.close();
             }
         };
         server.createContext("/", handler);
         server.start();
         try {
             URL url = new URL("http:
             String name = args.length >= 2 ? args[1] : "foo.bar.Baz";
             ClassLoader loader = new URLClassLoader(new URL[] { url });
             System.out.println(url);
             Class c = loader.loadClass(name);
             System.out.println("Loaded class \"" + c.getName() + "\".");
         } catch (ClassNotFoundException ex) {
             System.out.println(ex);
             error = false;
         } finally {
             server.stop(0);
         }
         if (error)
             throw new RuntimeException("No ClassNotFoundException generated");
    }
}
