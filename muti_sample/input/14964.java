public class ResolverXPointer extends ResourceResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                            ResolverXPointer.class.getName());
    public boolean engineIsThreadSafe() {
           return true;
   }
   public XMLSignatureInput engineResolve(Attr uri, String BaseURI)
           throws ResourceResolverException {
      Node resultNode = null;
      Document doc = uri.getOwnerElement().getOwnerDocument();
        String uriStr=uri.getNodeValue();
         if (isXPointerSlash(uriStr)) {
            resultNode = doc;
         } else if (isXPointerId(uriStr)) {
            String id = getXPointerId(uriStr);
            resultNode =IdResolver.getElementById(doc, id);
            if (resultNode == null) {
               Object exArgs[] = { id };
               throw new ResourceResolverException(
                  "signature.Verification.MissingID", exArgs, uri, BaseURI);
            }
         }
      XMLSignatureInput result = new XMLSignatureInput(resultNode);
      result.setMIMEType("text/xml");
      if (BaseURI != null && BaseURI.length() > 0) {
          result.setSourceURI(BaseURI.concat(uri.getNodeValue()));
      } else {
          result.setSourceURI(uri.getNodeValue());
      }
      return result;
   }
   public boolean engineCanResolve(Attr uri, String BaseURI) {
      if (uri == null) {
         return false;
      }
          String uriStr =uri.getNodeValue();
      if (isXPointerSlash(uriStr) || isXPointerId(uriStr)) {
         return true;
      }
      return false;
   }
   private static boolean isXPointerSlash(String uri) {
      if (uri.equals("#xpointer(/)")) {
         return true;
      }
      return false;
   }
   private static final String XP="#xpointer(id(";
   private static final int XP_LENGTH=XP.length();
   private static boolean isXPointerId(String uri) {
      if (uri.startsWith(XP)
              && uri.endsWith("))")) {
         String idPlusDelim = uri.substring(XP_LENGTH,
                                                     uri.length()
                                                     - 2);
                 int idLen=idPlusDelim.length() -1;
         if (((idPlusDelim.charAt(0) == '"') && (idPlusDelim
                 .charAt(idLen) == '"')) || ((idPlusDelim
                 .charAt(0) == '\'') && (idPlusDelim
                 .charAt(idLen) == '\''))) {
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Id="
                      + idPlusDelim.substring(1, idLen));
            return true;
         }
      }
      return false;
   }
   private static String getXPointerId(String uri) {
      if (uri.startsWith(XP)
              && uri.endsWith("))")) {
         String idPlusDelim = uri.substring(XP_LENGTH,uri.length()
                                                     - 2);
                 int idLen=idPlusDelim.length() -1;
         if (((idPlusDelim.charAt(0) == '"') && (idPlusDelim
                 .charAt(idLen) == '"')) || ((idPlusDelim
                 .charAt(0) == '\'') && (idPlusDelim
                 .charAt(idLen) == '\''))) {
            return idPlusDelim.substring(1, idLen);
         }
      }
      return null;
   }
}
