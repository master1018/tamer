public class ResolverFragment extends ResourceResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                            ResolverFragment.class.getName());
   public boolean engineIsThreadSafe() {
           return true;
   }
   public XMLSignatureInput engineResolve(Attr uri, String BaseURI)
       throws ResourceResolverException
   {
      String uriNodeValue = uri.getNodeValue();
      Document doc = uri.getOwnerElement().getOwnerDocument();
      Node selectedElem = null;
      if (uriNodeValue.equals("")) {
         log.log(java.util.logging.Level.FINE, "ResolverFragment with empty URI (means complete document)");
         selectedElem = doc;
      } else {
         String id = uriNodeValue.substring(1);
         selectedElem = IdResolver.getElementById(doc, id);
         if (selectedElem==null) {
                Object exArgs[] = { id };
            throw new ResourceResolverException(
               "signature.Verification.MissingID", exArgs, uri, BaseURI);
         }
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Try to catch an Element with ID " + id + " and Element was " + selectedElem);
      }
      XMLSignatureInput result = new XMLSignatureInput(selectedElem);
      result.setExcludeComments(true);
      result.setMIMEType("text/xml");
          result.setSourceURI((BaseURI != null) ? BaseURI.concat(uri.getNodeValue()) :
                  uri.getNodeValue());
      return result;
   }
   public boolean engineCanResolve(Attr uri, String BaseURI) {
      if (uri == null) {
         log.log(java.util.logging.Level.FINE, "Quick fail for null uri");
         return false;
      }
      String uriNodeValue = uri.getNodeValue();
      if  (uriNodeValue.equals("") ||
             (
            (uriNodeValue.charAt(0)=='#')
              && !((uriNodeValue.charAt(1)=='x') && uriNodeValue.startsWith("#xpointer("))
              )
           ){
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "State I can resolve reference: \"" + uriNodeValue + "\"");
         return true;
      }
      if (log.isLoggable(java.util.logging.Level.FINE))
        log.log(java.util.logging.Level.FINE, "Do not seem to be able to resolve reference: \"" + uriNodeValue + "\"");
      return false;
   }
}
