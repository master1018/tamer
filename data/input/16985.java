public class X509CertificateResolver extends KeyResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(X509CertificateResolver.class.getName());
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
      try {
          Element[] els=XMLUtils.selectDsNodes(element.getFirstChild(),
                  Constants._TAG_X509CERTIFICATE);
         if ((els == null) || (els.length == 0)) {
                 Element el=XMLUtils.selectDsNode(element.getFirstChild(),
                     Constants._TAG_X509DATA,0);
             if (el!=null) {
                 return engineLookupResolveX509Certificate(el, BaseURI, storage);
             }
                 return null;
         }
         for (int i = 0; i < els.length; i++) {
                 XMLX509Certificate xmlCert=new XMLX509Certificate(els[i], BaseURI);
                 X509Certificate cert = xmlCert.getX509Certificate();
            if (cert!=null) {
                return cert;
            }
         }
         return null;
      } catch (XMLSecurityException ex) {
         log.log(java.util.logging.Level.FINE, "XMLSecurityException", ex);
         throw new KeyResolverException("generic.EmptyMessage", ex);
      }
   }
   public javax.crypto.SecretKey engineLookupAndResolveSecretKey(
           Element element, String BaseURI, StorageResolver storage)
   {
      return null;
   }
}
