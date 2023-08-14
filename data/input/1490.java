public class X509IssuerSerialResolver extends KeyResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                    X509IssuerSerialResolver.class.getName());
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
         if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Can I resolve " + element.getTagName() + "?");
          X509Data x509data = null;
          try {
             x509data = new X509Data(element, BaseURI);
           } catch (XMLSignatureException ex) {
              log.log(java.util.logging.Level.FINE, "I can't");
         return null;
           } catch (XMLSecurityException ex) {
              log.log(java.util.logging.Level.FINE, "I can't");
          return null;
           }
           if (x509data == null) {
              log.log(java.util.logging.Level.FINE, "I can't");
              return null;
           }
           if (!x509data.containsIssuerSerial()) {
                    return null;
           }
      try {
         if (storage == null) {
            Object exArgs[] = { Constants._TAG_X509ISSUERSERIAL };
            KeyResolverException ex =
               new KeyResolverException("KeyResolver.needStorageResolver",
                                        exArgs);
            log.log(java.util.logging.Level.INFO, "", ex);
            throw ex;
         }
         int noOfISS = x509data.lengthIssuerSerial();
         while (storage.hasNext()) {
            X509Certificate cert = storage.next();
            XMLX509IssuerSerial certSerial = new XMLX509IssuerSerial(element.getOwnerDocument(), cert);
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Found Certificate Issuer: "
                      + certSerial.getIssuerName());
                log.log(java.util.logging.Level.FINE, "Found Certificate Serial: "
                      + certSerial.getSerialNumber().toString());
            }
            for (int i=0; i<noOfISS; i++) {
               XMLX509IssuerSerial xmliss = x509data.itemIssuerSerial(i);
               if (log.isLoggable(java.util.logging.Level.FINE)) {
                    log.log(java.util.logging.Level.FINE, "Found Element Issuer:     "
                         + xmliss.getIssuerName());
                    log.log(java.util.logging.Level.FINE, "Found Element Serial:     "
                         + xmliss.getSerialNumber().toString());
               }
               if (certSerial.equals(xmliss)) {
                  log.log(java.util.logging.Level.FINE, "match !!! ");
                  return cert;
               }
                log.log(java.util.logging.Level.FINE, "no match...");
            }
         }
         return null;
      } catch (XMLSecurityException ex) {
         log.log(java.util.logging.Level.FINE, "XMLSecurityException", ex);
         throw new KeyResolverException("generic.EmptyMessage", ex);
      }
   }
   public javax.crypto.SecretKey engineLookupAndResolveSecretKey(
           Element element, String BaseURI, StorageResolver storage) {
      return null;
   }
}
