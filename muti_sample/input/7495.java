public class IndexedCollectionCertStore extends CertStoreSpi {
    private Map<X500Principal, Object> certSubjects;
    private Map<X500Principal, Object> crlIssuers;
    private Set<Certificate> otherCertificates;
    private Set<CRL> otherCRLs;
    public IndexedCollectionCertStore(CertStoreParameters params)
            throws InvalidAlgorithmParameterException {
        super(params);
        if (!(params instanceof CollectionCertStoreParameters)) {
            throw new InvalidAlgorithmParameterException(
                "parameters must be CollectionCertStoreParameters");
        }
        Collection<?> coll = ((CollectionCertStoreParameters)params).getCollection();
        if (coll == null) {
            throw new InvalidAlgorithmParameterException
                                        ("Collection must not be null");
        }
        buildIndex(coll);
    }
    private void buildIndex(Collection<?> coll) {
        certSubjects = new HashMap<X500Principal, Object>();
        crlIssuers = new HashMap<X500Principal, Object>();
        otherCertificates = null;
        otherCRLs = null;
        for (Object obj : coll) {
            if (obj instanceof X509Certificate) {
                indexCertificate((X509Certificate)obj);
            } else if (obj instanceof X509CRL) {
                indexCRL((X509CRL)obj);
            } else if (obj instanceof Certificate) {
                if (otherCertificates == null) {
                    otherCertificates = new HashSet<Certificate>();
                }
                otherCertificates.add((Certificate)obj);
            } else if (obj instanceof CRL) {
                if (otherCRLs == null) {
                    otherCRLs = new HashSet<CRL>();
                }
                otherCRLs.add((CRL)obj);
            } else {
            }
        }
        if (otherCertificates == null) {
            otherCertificates = Collections.<Certificate>emptySet();
        }
        if (otherCRLs == null) {
            otherCRLs = Collections.<CRL>emptySet();
        }
    }
    private void indexCertificate(X509Certificate cert) {
        X500Principal subject = cert.getSubjectX500Principal();
        Object oldEntry = certSubjects.put(subject, cert);
        if (oldEntry != null) { 
            if (oldEntry instanceof X509Certificate) {
                if (cert.equals(oldEntry)) {
                    return;
                }
                List<X509Certificate> list = new ArrayList<X509Certificate>(2);
                list.add(cert);
                list.add((X509Certificate)oldEntry);
                certSubjects.put(subject, list);
            } else {
                List<X509Certificate> list = (List<X509Certificate>)oldEntry;
                if (list.contains(cert) == false) {
                    list.add(cert);
                }
                certSubjects.put(subject, list);
            }
        }
    }
    private void indexCRL(X509CRL crl) {
        X500Principal issuer = crl.getIssuerX500Principal();
        Object oldEntry = crlIssuers.put(issuer, crl);
        if (oldEntry != null) { 
            if (oldEntry instanceof X509CRL) {
                if (crl.equals(oldEntry)) {
                    return;
                }
                List<X509CRL> list = new ArrayList<X509CRL>(2);
                list.add(crl);
                list.add((X509CRL)oldEntry);
                crlIssuers.put(issuer, list);
            } else {
                List<X509CRL> list = (List<X509CRL>)oldEntry;
                if (list.contains(crl) == false) {
                    list.add(crl);
                }
                crlIssuers.put(issuer, list);
            }
        }
    }
    public Collection<? extends Certificate> engineGetCertificates(CertSelector selector)
            throws CertStoreException {
        if (selector == null) {
            Set<Certificate> matches = new HashSet<Certificate>();
            matchX509Certs(new X509CertSelector(), matches);
            matches.addAll(otherCertificates);
            return matches;
        }
        if (selector instanceof X509CertSelector == false) {
            Set<Certificate> matches = new HashSet<Certificate>();
            matchX509Certs(selector, matches);
            for (Certificate cert : otherCertificates) {
                if (selector.match(cert)) {
                    matches.add(cert);
                }
            }
            return matches;
        }
        if (certSubjects.isEmpty()) {
            return Collections.<X509Certificate>emptySet();
        }
        X509CertSelector x509Selector = (X509CertSelector)selector;
        X500Principal subject;
        X509Certificate matchCert = x509Selector.getCertificate();
        if (matchCert != null) {
            subject = matchCert.getSubjectX500Principal();
        } else {
            subject = x509Selector.getSubject();
        }
        if (subject != null) {
            Object entry = certSubjects.get(subject);
            if (entry == null) {
                return Collections.<X509Certificate>emptySet();
            }
            if (entry instanceof X509Certificate) {
                X509Certificate x509Entry = (X509Certificate)entry;
                if (x509Selector.match(x509Entry)) {
                    return Collections.singleton(x509Entry);
                } else {
                    return Collections.<X509Certificate>emptySet();
                }
            } else {
                List<X509Certificate> list = (List<X509Certificate>)entry;
                Set<X509Certificate> matches = new HashSet<X509Certificate>(16);
                for (X509Certificate cert : list) {
                    if (x509Selector.match(cert)) {
                        matches.add(cert);
                    }
                }
                return matches;
            }
        }
        Set<Certificate> matches = new HashSet<Certificate>(16);
        matchX509Certs(x509Selector, matches);
        return matches;
    }
    private void matchX509Certs(CertSelector selector,
        Collection<Certificate> matches) {
        for (Object obj : certSubjects.values()) {
            if (obj instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate)obj;
                if (selector.match(cert)) {
                    matches.add(cert);
                }
            } else {
                List<X509Certificate> list = (List<X509Certificate>)obj;
                for (X509Certificate cert : list) {
                    if (selector.match(cert)) {
                        matches.add(cert);
                    }
                }
            }
        }
    }
    public Collection<CRL> engineGetCRLs(CRLSelector selector)
            throws CertStoreException {
        if (selector == null) {
            Set<CRL> matches = new HashSet<CRL>();
            matchX509CRLs(new X509CRLSelector(), matches);
            matches.addAll(otherCRLs);
            return matches;
        }
        if (selector instanceof X509CRLSelector == false) {
            Set<CRL> matches = new HashSet<CRL>();
            matchX509CRLs(selector, matches);
            for (CRL crl : otherCRLs) {
                if (selector.match(crl)) {
                    matches.add(crl);
                }
            }
            return matches;
        }
        if (crlIssuers.isEmpty()) {
            return Collections.<CRL>emptySet();
        }
        X509CRLSelector x509Selector = (X509CRLSelector)selector;
        Collection<X500Principal> issuers = x509Selector.getIssuers();
        if (issuers != null) {
            HashSet<CRL> matches = new HashSet<CRL>(16);
            for (X500Principal issuer : issuers) {
                Object entry = crlIssuers.get(issuer);
                if (entry == null) {
                } else if (entry instanceof X509CRL) {
                    X509CRL crl = (X509CRL)entry;
                    if (x509Selector.match(crl)) {
                        matches.add(crl);
                    }
                } else { 
                    List<X509CRL> list = (List<X509CRL>)entry;
                    for (X509CRL crl : list) {
                        if (x509Selector.match(crl)) {
                            matches.add(crl);
                        }
                    }
                }
            }
            return matches;
        }
        Set<CRL> matches = new HashSet<CRL>(16);
        matchX509CRLs(x509Selector, matches);
        return matches;
    }
    private void matchX509CRLs(CRLSelector selector, Collection<CRL> matches) {
        for (Object obj : crlIssuers.values()) {
            if (obj instanceof X509CRL) {
                X509CRL crl = (X509CRL)obj;
                if (selector.match(crl)) {
                    matches.add(crl);
                }
            } else {
                List<X509CRL> list = (List<X509CRL>)obj;
                for (X509CRL crl : list) {
                    if (selector.match(crl)) {
                        matches.add(crl);
                    }
                }
            }
        }
    }
}
