public class CollectionCertStore extends CertStoreSpi {
    private Collection<?> coll;
    public CollectionCertStore(CertStoreParameters params)
        throws InvalidAlgorithmParameterException
    {
        super(params);
        if (!(params instanceof CollectionCertStoreParameters))
            throw new InvalidAlgorithmParameterException(
                "parameters must be CollectionCertStoreParameters");
        coll = ((CollectionCertStoreParameters) params).getCollection();
    }
    public Collection<Certificate> engineGetCertificates
            (CertSelector selector) throws CertStoreException {
        if (coll == null) {
            throw new CertStoreException("Collection is null");
        }
        for (int c = 0; c < 10; c++) {
            try {
                HashSet<Certificate> result = new HashSet<Certificate>();
                Iterator<?> i = coll.iterator();
                if (selector != null) {
                    while (i.hasNext()) {
                        Object o = i.next();
                        if ((o instanceof Certificate) &&
                            selector.match((Certificate) o))
                            result.add((Certificate)o);
                    }
                } else {
                    while (i.hasNext()) {
                        Object o = i.next();
                        if (o instanceof Certificate)
                            result.add((Certificate)o);
                    }
                }
                return(result);
            } catch (ConcurrentModificationException e) { }
        }
        throw new ConcurrentModificationException("Too many "
            + "ConcurrentModificationExceptions");
    }
    public Collection<CRL> engineGetCRLs(CRLSelector selector)
        throws CertStoreException
    {
        if (coll == null)
            throw new CertStoreException("Collection is null");
        for (int c = 0; c < 10; c++) {
            try {
                HashSet<CRL> result = new HashSet<CRL>();
                Iterator<?> i = coll.iterator();
                if (selector != null) {
                    while (i.hasNext()) {
                        Object o = i.next();
                        if ((o instanceof CRL) && selector.match((CRL) o))
                            result.add((CRL)o);
                    }
                } else {
                    while (i.hasNext()) {
                        Object o = i.next();
                        if (o instanceof CRL)
                            result.add((CRL)o);
                    }
                }
                return(result);
            } catch (ConcurrentModificationException e) { }
        }
        throw new ConcurrentModificationException("Too many "
            + "ConcurrentModificationExceptions");
    }
}
