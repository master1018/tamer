public class X509SKIResolver extends KeyResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(X509SKIResolver.class.getName());
   public PublicKey engineLookupAndResolvePublicKey(
           Element element, String BaseURI, StorageResolver storage)
              throws KeyResolverException {
      X509Certificate cert = this.engineLookupResolveX509Certificate(element,
                                BaseURI, storage);
      if (cert != null) {
         return cert.getPublicKey();
      }
      return null;
   }
   public X509Certificate engineLookupResolveX509Certificate(
           Element element, String BaseURI, StorageResolver storage)
              throws KeyResolverException {
           if (log.isLoggable(java.util.logging.Level.FINE)) {
             log.log(java.util.logging.Level.FINE, "Can I resolve " + element.getTagName() + "?");
           }
           if (!XMLUtils.elementIsInSignatureSpace(element,
              Constants._TAG_X509DATA)) {
                 log.log(java.util.logging.Level.FINE, "I can't");
                 return null;
           }
           XMLX509SKI x509childObject[] = null;
           Element x509childNodes[] = null;
           x509childNodes = XMLUtils.selectDsNodes(element.getFirstChild(),
                          Constants._TAG_X509SKI);
           if (!((x509childNodes != null)
                         && (x509childNodes.length > 0))) {
                   log.log(java.util.logging.Level.FINE, "I can't");
                return null;
           }
           try {
         if (storage == null) {
            Object exArgs[] = { Constants._TAG_X509SKI };
            KeyResolverException ex =
               new KeyResolverException("KeyResolver.needStorageResolver",
                                        exArgs);
            log.log(java.util.logging.Level.INFO, "", ex);
            throw ex;
         }
         x509childObject = new XMLX509SKI[x509childNodes.length];
         for (int i = 0; i < x509childNodes.length; i++) {
            x509childObject[i] =
               new XMLX509SKI(x509childNodes[i], BaseURI);
         }
         while (storage.hasNext()) {
            X509Certificate cert = storage.next();
            XMLX509SKI certSKI = new XMLX509SKI(element.getOwnerDocument(), cert);
            for (int i = 0; i < x509childObject.length; i++) {
               if (certSKI.equals(x509childObject[i])) {
                  log.log(java.util.logging.Level.FINE, "Return PublicKey from "
                            + cert.getSubjectDN().getName());
                  return cert;
               }
            }
         }
      } catch (XMLSecurityException ex) {
         throw new KeyResolverException("empty", ex);
      }
      return null;
   }
   public javax.crypto.SecretKey engineLookupAndResolveSecretKey(
           Element element, String BaseURI, StorageResolver storage)
    {
      return null;
   }
}
