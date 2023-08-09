class X509KeySelector extends KeySelector {
    private KeyStore ks;
    private boolean trusted = true;
    X509KeySelector(KeyStore keyStore) throws KeyStoreException {
        this(keyStore, true);
    }
    X509KeySelector(KeyStore keyStore, boolean trusted)
        throws KeyStoreException {
        if (keyStore == null) {
            throw new NullPointerException("keyStore is null");
        }
        this.trusted = trusted;
        this.ks = keyStore;
        this.ks.size();
    }
    public KeySelectorResult select(KeyInfo keyInfo,
        KeySelector.Purpose purpose, AlgorithmMethod method,
        XMLCryptoContext context) throws KeySelectorException {
        SignatureMethod sm = (SignatureMethod) method;
        try {
            if (keyInfo == null || ks.size() == 0) {
                return new SimpleKeySelectorResult(null);
            }
            Iterator i = keyInfo.getContent().iterator();
            while (i.hasNext()) {
                XMLStructure kiType = (XMLStructure) i.next();
                if (kiType instanceof X509Data) {
                    X509Data xd = (X509Data) kiType;
                    KeySelectorResult ksr = x509DataSelect(xd, sm);
                    if (ksr != null) {
                        return ksr;
                    }
                } else if (kiType instanceof KeyName) {
                    KeyName kn = (KeyName) kiType;
                    Certificate cert = ks.getCertificate(kn.getName());
                    if (cert != null && algEquals(sm.getAlgorithm(),
                        cert.getPublicKey().getAlgorithm())) {
                        return new SimpleKeySelectorResult(cert.getPublicKey());
                    }
                } else if (kiType instanceof RetrievalMethod) {
                    RetrievalMethod rm = (RetrievalMethod) kiType;
                    try {
                        KeySelectorResult ksr = null;
                        if (rm.getType().equals
                            (X509Data.RAW_X509_CERTIFICATE_TYPE)) {
                            OctetStreamData data = (OctetStreamData)
                                rm.dereference(context);
                            CertificateFactory cf =
                                CertificateFactory.getInstance("X.509");
                            X509Certificate cert = (X509Certificate)
                                cf.generateCertificate(data.getOctetStream());
                            ksr = certSelect(cert, sm);
                        } else if (rm.getType().equals(X509Data.TYPE)) {
                            X509Data xd = (X509Data) ((DOMRetrievalMethod) rm).
                                dereferenceAsXMLStructure(context);
                            ksr = x509DataSelect(xd, sm);
                        } else {
                            continue;
                        }
                        if (ksr != null) {
                            return ksr;
                        }
                    } catch (Exception e) {
                        throw new KeySelectorException(e);
                    }
                }
            }
        } catch (KeyStoreException kse) {
            throw new KeySelectorException(kse);
        }
        return new SimpleKeySelectorResult(null);
    }
    private KeySelectorResult keyStoreSelect(CertSelector cs)
        throws KeyStoreException {
        Enumeration aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = (String) aliases.nextElement();
            Certificate cert = ks.getCertificate(alias);
            if (cert != null && cs.match(cert)) {
                return new SimpleKeySelectorResult(cert.getPublicKey());
            }
        }
        return null;
    }
    private KeySelectorResult certSelect(X509Certificate xcert,
        SignatureMethod sm) throws KeyStoreException {
        boolean[] keyUsage = xcert.getKeyUsage();
        if (keyUsage != null && keyUsage[0] == false) {
            return null;
        }
        String alias = ks.getCertificateAlias(xcert);
        if (alias != null) {
            PublicKey pk = ks.getCertificate(alias).getPublicKey();
            if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                return new SimpleKeySelectorResult(pk);
            }
        }
        return null;
    }
    private String getPKAlgorithmOID(String algURI) {
        if (algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
            return "1.2.840.10040.4.1";
        } else if (algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
            return "1.2.840.113549.1.1";
        } else {
            return null;
        }
    }
    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private final Key key;
        SimpleKeySelectorResult(Key key) { this.key = key; }
        public Key getKey() { return key; }
    }
    private boolean algEquals(String algURI, String algName) {
        if (algName.equalsIgnoreCase("DSA") &&
            algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
            return true;
        } else if (algName.equalsIgnoreCase("RSA") &&
            algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
            return true;
        } else {
            return false;
        }
    }
    private KeySelectorResult x509DataSelect(X509Data xd, SignatureMethod sm)
        throws KeyStoreException, KeySelectorException {
        String algOID = getPKAlgorithmOID(sm.getAlgorithm());
        X509CertSelector subjectcs = new X509CertSelector();
        try {
            subjectcs.setSubjectPublicKeyAlgID(algOID);
        } catch (IOException ioe) {
            throw new KeySelectorException(ioe);
        }
        Collection certs = new ArrayList();
        Iterator xi = xd.getContent().iterator();
        while (xi.hasNext()) {
            Object o = xi.next();
            if (o instanceof X509IssuerSerial) {
                X509IssuerSerial xis = (X509IssuerSerial) o;
                try {
                    subjectcs.setSerialNumber(xis.getSerialNumber());
                    String issuer = new X500Principal(xis.getIssuerName()).getName();
                    if (issuer.endsWith("\n")) {
                        issuer = new String
                            (issuer.toCharArray(), 0, issuer.length()-1);
                    }
                    subjectcs.setIssuer(issuer);
                } catch (IOException ioe) {
                    throw new KeySelectorException(ioe);
                }
            } else if (o instanceof String) {
                String sn = (String) o;
                try {
                    String subject = new X500Principal(sn).getName();
                    if (subject.endsWith("\n")) {
                        subject = new String
                            (subject.toCharArray(), 0, subject.length()-1);
                    }
                    subjectcs.setSubject(subject);
                } catch (IOException ioe) {
                    throw new KeySelectorException(ioe);
                }
            } else if (o instanceof byte[]) {
                byte[] ski = (byte[]) o;
                byte[] encodedSki = new byte[ski.length+2];
                encodedSki[0] = 0x04; 
                encodedSki[1] = (byte) ski.length; 
                System.arraycopy(ski, 0, encodedSki, 2, ski.length);
                subjectcs.setSubjectKeyIdentifier(encodedSki);
            } else if (o instanceof X509Certificate) {
                certs.add((X509Certificate) o);
            } else {
                continue;
            }
        }
        KeySelectorResult ksr = keyStoreSelect(subjectcs);
        if (ksr != null) {
            return ksr;
        }
        if (!certs.isEmpty() && !trusted) {
            Iterator i = certs.iterator();
            while (i.hasNext()) {
                X509Certificate cert = (X509Certificate) i.next();
                if (subjectcs.match(cert)) {
                    return new SimpleKeySelectorResult(cert.getPublicKey());
                }
            }
        }
        return null;
    }
}
