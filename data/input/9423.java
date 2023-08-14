public class CertsInFilesystemDirectoryResolver extends StorageResolverSpi {
    static java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(
                    CertsInFilesystemDirectoryResolver.class.getName());
   String _merlinsCertificatesDir = null;
   private List _certs = new ArrayList();
   Iterator _iterator = null;
   public CertsInFilesystemDirectoryResolver(String directoryName)
           throws StorageResolverException {
      this._merlinsCertificatesDir = directoryName;
      this.readCertsFromHarddrive();
      this._iterator = new FilesystemIterator(this._certs);
   }
   private void readCertsFromHarddrive() throws StorageResolverException {
      File certDir = new File(this._merlinsCertificatesDir);
      ArrayList al = new ArrayList();
      String[] names = certDir.list();
      for (int i = 0; i < names.length; i++) {
         String currentFileName = names[i];
         if (currentFileName.endsWith(".crt")) {
            al.add(names[i]);
         }
      }
      CertificateFactory cf = null;
      try {
         cf = CertificateFactory.getInstance("X.509");
      } catch (CertificateException ex) {
         throw new StorageResolverException("empty", ex);
      }
      if (cf == null) {
         throw new StorageResolverException("empty");
      }
      for (int i = 0; i < al.size(); i++) {
         String filename = certDir.getAbsolutePath() + File.separator
                           + (String) al.get(i);
         File file = new File(filename);
         boolean added = false;
         String dn = null;
         try {
            FileInputStream fis = new FileInputStream(file);
            X509Certificate cert =
               (X509Certificate) cf.generateCertificate(fis);
            fis.close();
            cert.checkValidity();
            this._certs.add(cert);
            dn = cert.getSubjectDN().getName();
            added = true;
         } catch (FileNotFoundException ex) {
            log.log(java.util.logging.Level.FINE, "Could not add certificate from file " + filename, ex);
         } catch (IOException ex) {
            log.log(java.util.logging.Level.FINE, "Could not add certificate from file " + filename, ex);
         } catch (CertificateNotYetValidException ex) {
            log.log(java.util.logging.Level.FINE, "Could not add certificate from file " + filename, ex);
         } catch (CertificateExpiredException ex) {
            log.log(java.util.logging.Level.FINE, "Could not add certificate from file " + filename, ex);
         } catch (CertificateException ex) {
            log.log(java.util.logging.Level.FINE, "Could not add certificate from file " + filename, ex);
         }
         if (added) {
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Added certificate: " + dn);
         }
      }
   }
   public Iterator getIterator() {
      return this._iterator;
   }
   private static class FilesystemIterator implements Iterator {
      List _certs = null;
      int _i;
      public FilesystemIterator(List certs) {
         this._certs = certs;
         this._i = 0;
      }
      public boolean hasNext() {
         return (this._i < this._certs.size());
      }
      public Object next() {
         return this._certs.get(this._i++);
      }
      public void remove() {
         throw new UnsupportedOperationException(
            "Can't remove keys from KeyStore");
      }
   }
   public static void main(String unused[]) throws Exception {
      CertsInFilesystemDirectoryResolver krs =
         new CertsInFilesystemDirectoryResolver(
            "data/ie/baltimore/merlin-examples/merlin-xmldsig-eighteen/certs");
      for (Iterator i = krs.getIterator(); i.hasNext(); ) {
         X509Certificate cert = (X509Certificate) i.next();
         byte[] ski =
            com.sun.org.apache.xml.internal.security.keys.content.x509.XMLX509SKI
               .getSKIBytesFromCert(cert);
         System.out.println();
         System.out.println("Base64(SKI())=                 \""
                            + Base64.encode(ski) + "\"");
         System.out.println("cert.getSerialNumber()=        \""
                            + cert.getSerialNumber().toString() + "\"");
         System.out.println("cert.getSubjectDN().getName()= \""
                            + cert.getSubjectDN().getName() + "\"");
         System.out.println("cert.getIssuerDN().getName()=  \""
                            + cert.getIssuerDN().getName() + "\"");
      }
   }
}
