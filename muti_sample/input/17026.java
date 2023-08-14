public class KeyStoreResolver extends StorageResolverSpi {
   KeyStore _keyStore = null;
   Iterator _iterator = null;
   public KeyStoreResolver(KeyStore keyStore) throws StorageResolverException {
      this._keyStore = keyStore;
      this._iterator = new KeyStoreIterator(this._keyStore);
   }
   public Iterator getIterator() {
      return this._iterator;
   }
   static class KeyStoreIterator implements Iterator {
      KeyStore _keyStore = null;
      Enumeration _aliases = null;
      public KeyStoreIterator(KeyStore keyStore)
              throws StorageResolverException {
         try {
            this._keyStore = keyStore;
            this._aliases = this._keyStore.aliases();
         } catch (KeyStoreException ex) {
            throw new StorageResolverException("generic.EmptyMessage", ex);
         }
      }
      public boolean hasNext() {
         return this._aliases.hasMoreElements();
      }
      public Object next() {
         String alias = (String) this._aliases.nextElement();
         try {
            return this._keyStore.getCertificate(alias);
         } catch (KeyStoreException ex) {
            return null;
         }
      }
      public void remove() {
         throw new UnsupportedOperationException(
            "Can't remove keys from KeyStore");
      }
   }
   public static void main(String unused[]) throws Exception {
      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(
         new java.io.FileInputStream(
         "data/com/sun/org/apache/xml/internal/security/samples/input/keystore.jks"),
            "xmlsecurity".toCharArray());
      KeyStoreResolver krs = new KeyStoreResolver(ks);
      for (Iterator i = krs.getIterator(); i.hasNext(); ) {
         X509Certificate cert = (X509Certificate) i.next();
         byte[] ski =
            com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509SKI
               .getSKIBytesFromCert(cert);
         System.out.println(com.sun.org.apache.xml.internal.security.utils.Base64.encode(ski));
      }
   }
}
