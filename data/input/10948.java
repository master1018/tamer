public class SingleCertificateResolver extends StorageResolverSpi {
   X509Certificate _certificate = null;
   Iterator _iterator = null;
   public SingleCertificateResolver(X509Certificate x509cert) {
      this._certificate = x509cert;
      this._iterator = new InternalIterator(this._certificate);
   }
   public Iterator getIterator() {
      return this._iterator;
   }
   static class InternalIterator implements Iterator {
      boolean _alreadyReturned = false;
      X509Certificate _certificate = null;
      public InternalIterator(X509Certificate x509cert) {
         this._certificate = x509cert;
      }
      public boolean hasNext() {
         return (!this._alreadyReturned);
      }
      public Object next() {
         this._alreadyReturned = true;
         return this._certificate;
      }
      public void remove() {
         throw new UnsupportedOperationException(
            "Can't remove keys from KeyStore");
      }
   }
}
