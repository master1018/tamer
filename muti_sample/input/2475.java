public class StorageResolver {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(StorageResolver.class.getName());
   List _storageResolvers = null;
   Iterator _iterator = null;
   public StorageResolver() {}
   public StorageResolver(StorageResolverSpi resolver) {
      this.add(resolver);
   }
   public void add(StorageResolverSpi resolver) {
           if (_storageResolvers==null)
                   _storageResolvers=new ArrayList();
      this._storageResolvers.add(resolver);
      this._iterator = null;
   }
   public StorageResolver(KeyStore keyStore) {
      this.add(keyStore);
   }
   public void add(KeyStore keyStore) {
      try {
         this.add(new KeyStoreResolver(keyStore));
      } catch (StorageResolverException ex) {
         log.log(java.util.logging.Level.SEVERE, "Could not add KeyStore because of: ", ex);
      }
   }
   public StorageResolver(X509Certificate x509certificate) {
      this.add(x509certificate);
   }
   public void add(X509Certificate x509certificate) {
      this.add(new SingleCertificateResolver(x509certificate));
   }
   public Iterator getIterator() {
      if (this._iterator == null) {
         if (_storageResolvers==null)
                   _storageResolvers=new ArrayList();
         this._iterator = new StorageResolverIterator(this._storageResolvers.iterator());
      }
      return this._iterator;
   }
   public boolean hasNext() {
      if (this._iterator == null) {
          if (_storageResolvers==null)
                   _storageResolvers=new ArrayList();
         this._iterator = new StorageResolverIterator(this._storageResolvers.iterator());
      }
      return this._iterator.hasNext();
   }
   public X509Certificate next() {
      return (X509Certificate) this._iterator.next();
   }
   static class StorageResolverIterator implements Iterator {
      Iterator _resolvers = null;
      public StorageResolverIterator(Iterator resolvers) {
         this._resolvers = resolvers;
      }
      public boolean hasNext() {
          return _resolvers.hasNext();
      }
      public Object next() {
          return _resolvers.next();
      }
      public void remove() {
         throw new UnsupportedOperationException(
            "Can't remove keys from KeyStore");
      }
   }
}
