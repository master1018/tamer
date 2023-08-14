public class ResolverDirectHTTP extends ResourceResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                            ResolverDirectHTTP.class.getName());
   private static final String properties[] =
        { "http.proxy.host", "http.proxy.port",
          "http.proxy.username",
          "http.proxy.password",
          "http.basic.username",
          "http.basic.password" };
   private static final int HttpProxyHost = 0;
   private static final int HttpProxyPort = 1;
   private static final int HttpProxyUser = 2;
   private static final int HttpProxyPass = 3;
   private static final int HttpBasicUser = 4;
   private static final int HttpBasicPass = 5;
   public boolean engineIsThreadSafe() {
           return true;
   }
   public XMLSignatureInput engineResolve(Attr uri, String BaseURI)
           throws ResourceResolverException {
      try {
         boolean useProxy = false;
         String proxyHost =
            engineGetProperty(ResolverDirectHTTP
               .properties[ResolverDirectHTTP.HttpProxyHost]);
         String proxyPort =
            engineGetProperty(ResolverDirectHTTP
               .properties[ResolverDirectHTTP.HttpProxyPort]);
         if ((proxyHost != null) && (proxyPort != null)) {
            useProxy = true;
         }
         String oldProxySet = null;
         String oldProxyHost = null;
         String oldProxyPort = null;
         if (useProxy) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Use of HTTP proxy enabled: " + proxyHost + ":"
                      + proxyPort);
            }
            oldProxySet = System.getProperty("http.proxySet");
            oldProxyHost = System.getProperty("http.proxyHost");
            oldProxyPort = System.getProperty("http.proxyPort");
            System.setProperty("http.proxySet", "true");
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
         }
         boolean switchBackProxy = ((oldProxySet != null)
                                    && (oldProxyHost != null)
                                    && (oldProxyPort != null));
         URI uriNew = getNewURI(uri.getNodeValue(), BaseURI);
         URI uriNewNoFrag = new URI(uriNew);
         uriNewNoFrag.setFragment(null);
         URL url = new URL(uriNewNoFrag.toString());
         URLConnection urlConnection = url.openConnection();
         {
            String proxyUser =
               engineGetProperty(ResolverDirectHTTP
                  .properties[ResolverDirectHTTP.HttpProxyUser]);
            String proxyPass =
               engineGetProperty(ResolverDirectHTTP
                  .properties[ResolverDirectHTTP.HttpProxyPass]);
            if ((proxyUser != null) && (proxyPass != null)) {
               String password = proxyUser + ":" + proxyPass;
               String encodedPassword = Base64.encode(password.getBytes());
               urlConnection.setRequestProperty("Proxy-Authorization",
                                                encodedPassword);
            }
         }
         {
            String auth = urlConnection.getHeaderField("WWW-Authenticate");
            if (auth != null) {
               if (auth.startsWith("Basic")) {
                  String user =
                     engineGetProperty(ResolverDirectHTTP
                        .properties[ResolverDirectHTTP.HttpBasicUser]);
                  String pass =
                     engineGetProperty(ResolverDirectHTTP
                        .properties[ResolverDirectHTTP.HttpBasicPass]);
                  if ((user != null) && (pass != null)) {
                     urlConnection = url.openConnection();
                     String password = user + ":" + pass;
                     String encodedPassword =
                        Base64.encode(password.getBytes());
                     urlConnection.setRequestProperty("Authorization",
                                                      "Basic "
                                                      + encodedPassword);
                  }
               }
            }
         }
         String mimeType = urlConnection.getHeaderField("Content-Type");
         InputStream inputStream = urlConnection.getInputStream();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte buf[] = new byte[4096];
         int read = 0;
         int summarized = 0;
         while ((read = inputStream.read(buf)) >= 0) {
            baos.write(buf, 0, read);
            summarized += read;
         }
         log.log(java.util.logging.Level.FINE, "Fetched " + summarized + " bytes from URI "
                   + uriNew.toString());
         XMLSignatureInput result = new XMLSignatureInput(baos.toByteArray());
         result.setSourceURI(uriNew.toString());
         result.setMIMEType(mimeType);
         if (useProxy && switchBackProxy) {
            System.setProperty("http.proxySet", oldProxySet);
            System.setProperty("http.proxyHost", oldProxyHost);
            System.setProperty("http.proxyPort", oldProxyPort);
         }
         return result;
      } catch (MalformedURLException ex) {
         throw new ResourceResolverException("generic.EmptyMessage", ex, uri,
                                             BaseURI);
      } catch (IOException ex) {
         throw new ResourceResolverException("generic.EmptyMessage", ex, uri,
                                             BaseURI);
      }
   }
   public boolean engineCanResolve(Attr uri, String BaseURI) {
      if (uri == null) {
         log.log(java.util.logging.Level.FINE, "quick fail, uri == null");
         return false;
      }
      String uriNodeValue = uri.getNodeValue();
      if (uriNodeValue.equals("") || (uriNodeValue.charAt(0)=='#')) {
         log.log(java.util.logging.Level.FINE, "quick fail for empty URIs and local ones");
         return false;
      }
      if (log.isLoggable(java.util.logging.Level.FINE)) {
         log.log(java.util.logging.Level.FINE, "I was asked whether I can resolve " + uriNodeValue);
      }
      if ( uriNodeValue.startsWith("http:") ||
                                (BaseURI!=null && BaseURI.startsWith("http:") )) {
         if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I state that I can resolve " + uriNodeValue);
         }
         return true;
      }
      if (log.isLoggable(java.util.logging.Level.FINE)) {
         log.log(java.util.logging.Level.FINE, "I state that I can't resolve " + uriNodeValue);
      }
      return false;
   }
   public String[] engineGetPropertyKeys() {
      return (String[]) ResolverDirectHTTP.properties.clone();
   }
   private URI getNewURI(String uri, String BaseURI)
           throws URI.MalformedURIException {
      if ((BaseURI == null) || "".equals(BaseURI)) {
         return new URI(uri);
      }
      return new URI(new URI(BaseURI), uri);
   }
}
