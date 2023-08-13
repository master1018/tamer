public class RSAKeyValueResolver extends KeyResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                        RSAKeyValueResolver.class.getName());
   public PublicKey engineLookupAndResolvePublicKey(
           Element element, String BaseURI, StorageResolver storage) {
           if (log.isLoggable(java.util.logging.Level.FINE))
                        log.log(java.util.logging.Level.FINE, "Can I resolve " + element.getTagName());
      if (element == null) {
         return null;
      }
          boolean isKeyValue = XMLUtils.elementIsInSignatureSpace(element,
                                              Constants._TAG_KEYVALUE);
          Element rsaKeyElement=null;
          if (isKeyValue) {
                   rsaKeyElement = XMLUtils.selectDsNode(element.getFirstChild(),
                                    Constants._TAG_RSAKEYVALUE, 0);
          } else if (XMLUtils.elementIsInSignatureSpace(element,
              Constants._TAG_RSAKEYVALUE)) {
         rsaKeyElement = element;
          }
      if (rsaKeyElement == null) {
         return null;
      }
      try {
         RSAKeyValue rsaKeyValue = new RSAKeyValue(rsaKeyElement,
                                                   BaseURI);
         return rsaKeyValue.getPublicKey();
      } catch (XMLSecurityException ex) {
         log.log(java.util.logging.Level.FINE, "XMLSecurityException", ex);
      }
      return null;
   }
   public X509Certificate engineLookupResolveX509Certificate(
           Element element, String BaseURI, StorageResolver storage) {
      return null;
   }
   public javax.crypto.SecretKey engineLookupAndResolveSecretKey(
           Element element, String BaseURI, StorageResolver storage) {
      return null;
   }
}
