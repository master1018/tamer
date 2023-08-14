public class B6296310
{
   static SimpleHttpTransaction httpTrans;
   static HttpServer server;
   public static void main(String[] args)
   {
      ResponseCache.setDefault(new MyCacheHandler());
      startHttpServer();
      makeHttpCall();
   }
   public static void startHttpServer() {
      try {
         httpTrans = new SimpleHttpTransaction();
         server = new HttpServer(httpTrans, 1, 10, 0);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   public static void makeHttpCall() {
      try {
         System.out.println("http server listen on: " + server.getLocalPort());
         URL url = new URL("http" , InetAddress.getLocalHost().getHostAddress(),
                            server.getLocalPort(), "/");
         HttpURLConnection uc = (HttpURLConnection)url.openConnection();
         System.out.println(uc.getResponseCode());
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         server.terminate();
      }
   }
}
class SimpleHttpTransaction implements HttpCallback
{
   public void request(HttpTransaction trans) {
      try {
         trans.setResponseEntityBody("");
         trans.sendResponse(200, "OK");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
class MyCacheHandler extends ResponseCache
{
   public CacheResponse get(URI uri, String rqstMethod, Map rqstHeaders)
   {
      return null;
   }
   public CacheRequest put(URI uri, URLConnection conn)
   {
      return new MyCacheRequest();
   }
}
class MyCacheRequest extends CacheRequest
{
   public void abort() {}
   public OutputStream getBody() throws IOException {
       return null;
   }
}
