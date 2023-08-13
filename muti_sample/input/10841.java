public class ResolverLocalFilesystem extends ResourceResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                    ResolverLocalFilesystem.class.getName());
    public boolean engineIsThreadSafe() {
           return true;
   }
   public XMLSignatureInput engineResolve(Attr uri, String BaseURI)
           throws ResourceResolverException {
     try {
        URI uriNew = getNewURI(uri.getNodeValue(), BaseURI);
        URI uriNewNoFrag = new URI(uriNew);
        uriNewNoFrag.setFragment(null);
        String fileName =
           ResolverLocalFilesystem
              .translateUriToFilename(uriNewNoFrag.toString());
        FileInputStream inputStream = new FileInputStream(fileName);
        XMLSignatureInput result = new XMLSignatureInput(inputStream);
        result.setSourceURI(uriNew.toString());
        return result;
     } catch (Exception e) {
        throw new ResourceResolverException("generic.EmptyMessage", e, uri,
                                            BaseURI);
      }
   }
   private static int FILE_URI_LENGTH="file:/".length();
   private static String translateUriToFilename(String uri) {
      String subStr = uri.substring(FILE_URI_LENGTH);
      if (subStr.indexOf("%20") > -1)
      {
        int offset = 0;
        int index = 0;
        StringBuffer temp = new StringBuffer(subStr.length());
        do
        {
          index = subStr.indexOf("%20",offset);
          if (index == -1) temp.append(subStr.substring(offset));
          else
          {
            temp.append(subStr.substring(offset,index));
            temp.append(' ');
            offset = index+3;
          }
        }
        while(index != -1);
        subStr = temp.toString();
      }
      if (subStr.charAt(1) == ':') {
         return subStr;
      }
      return "/" + subStr;
   }
   public boolean engineCanResolve(Attr uri, String BaseURI) {
      if (uri == null) {
         return false;
      }
      String uriNodeValue = uri.getNodeValue();
      if (uriNodeValue.equals("") || (uriNodeValue.charAt(0)=='#') ||
          uriNodeValue.startsWith("http:")) {
         return false;
      }
      try {
                 if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "I was asked whether I can resolve " + uriNodeValue);
                 if ( uriNodeValue.startsWith("file:") ||
                                         BaseURI.startsWith("file:")) {
                    if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "I state that I can resolve " + uriNodeValue);
                    return true;
                 }
      } catch (Exception e) {}
      log.log(java.util.logging.Level.FINE, "But I can't");
      return false;
   }
   private static URI getNewURI(String uri, String BaseURI)
           throws URI.MalformedURIException {
      if ((BaseURI == null) || "".equals(BaseURI)) {
         return new URI(uri);
      }
      return new URI(new URI(BaseURI), uri);
   }
}
