public class IndexedPKIXParameters extends PKIXParameters {
    final Map<Bytes, TrustAnchor> encodings
            = new HashMap<Bytes, TrustAnchor>();
    final Map<X500Principal, TrustAnchor> bySubject
            = new HashMap<X500Principal, TrustAnchor>();
    final Map<X500Principal, List<TrustAnchor>> byCA
            = new HashMap<X500Principal, List<TrustAnchor>>();
    public IndexedPKIXParameters(Set<TrustAnchor> anchors)
            throws KeyStoreException, InvalidAlgorithmParameterException,
            CertificateEncodingException {
        super(anchors);
        for (TrustAnchor anchor : anchors) {
            X509Certificate cert = anchor.getTrustedCert();
            Bytes encoded = new Bytes(cert.getEncoded());
            encodings.put(encoded, anchor);
            X500Principal subject = cert.getSubjectX500Principal();
            if (bySubject.put(subject, anchor) != null) {
                throw new KeyStoreException("Two certs have the same subject: "
                        + subject);
            }
            X500Principal ca = anchor.getCA();
            List<TrustAnchor> caAnchors = byCA.get(ca);
            if (caAnchors == null) {
                caAnchors = new ArrayList<TrustAnchor>();
                byCA.put(ca, caAnchors);
            }
            caAnchors.add(anchor);
        }
    }
    TrustAnchor findTrustAnchor(X509Certificate cert, CertPath certPath,
            int index) throws CertPathValidatorException {
        Exception verificationException = null;
        X500Principal issuer = cert.getIssuerX500Principal();
        List<TrustAnchor> anchors = byCA.get(issuer);
        if (anchors != null) {
            for (TrustAnchor caAnchor : anchors) {
                try {
                    cert.verify(caAnchor.getCAPublicKey());
                    return caAnchor;
                } catch (Exception e) {
                    verificationException = e;
                }
            }
        }
        TrustAnchor anchor = bySubject.get(issuer);
        if (anchor != null) {
            try {
                cert.verify(anchor.getTrustedCert().getPublicKey());
                return anchor;
            } catch (Exception e) {
                verificationException = e;
            }
        }
        try {
            Bytes encoded = new Bytes(cert.getEncoded());
            anchor = encodings.get(encoded);
            if (anchor != null) {
                return anchor;
            }
        } catch (Exception e) {
            Logger.getLogger(IndexedPKIXParameters.class.getName()).log(
                    Level.WARNING, "Error encoding cert.", e);
        }
        if (verificationException != null) {
            throw new CertPathValidatorException("TrustAnchor found but"
                    + " certificate verification failed.",
                    verificationException, certPath, index);
        }
        return null;
    }
    public boolean isDirectlyTrusted(X509Certificate cert) {
        try {
            Bytes encoded = new Bytes(cert.getEncoded());
            return encodings.containsKey(encoded);
        } catch (Exception e) {
            Logger.getLogger(IndexedPKIXParameters.class.getName()).log(
                    Level.WARNING, "Error encoding cert.", e);
            return false;
        }
    }
    static class Bytes {
        final byte[] bytes;
        final int hash;
        Bytes(byte[] bytes) {
            this.bytes = bytes;
            this.hash = Arrays.hashCode(bytes);
        }
        @Override public int hashCode() {
            return hash;
        }
        @Override public boolean equals(Object o) {
            return Arrays.equals(bytes, ((Bytes) o).bytes);
        }
    }
}
