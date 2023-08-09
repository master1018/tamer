public class TrustManagerImpl implements X509TrustManager {
    private CertPathValidator validator;
    private PKIXParameters params;
    private Exception err = null;
    private CertificateFactory factory;
    public TrustManagerImpl(KeyStore ks) {
        try {
            validator = CertPathValidator.getInstance("PKIX");
            factory = CertificateFactory.getInstance("X509");
            byte[] nameConstrains = null;
            Set<TrustAnchor> trusted = new HashSet<TrustAnchor>();
            for (Enumeration<String> en = ks.aliases(); en.hasMoreElements();) {
                final String alias = en.nextElement();
                final X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
                if (cert != null) {
                    trusted.add(new TrustAnchor(cert, nameConstrains));
                }
            }
            params = new PKIXParameters(trusted);
            params.setRevocationEnabled(false);
        } catch (Exception e) {
            err = e;
        }
    }
    public void indexTrustAnchors() throws CertificateEncodingException,
            InvalidAlgorithmParameterException, KeyStoreException {
        params = new IndexedPKIXParameters(params.getTrustAnchors());
        params.setRevocationEnabled(false);
    }
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        if (chain == null || chain.length == 0 || authType == null
                || authType.length() == 0) {
            throw new IllegalArgumentException("null or zero-length parameter");
        }
        if (err != null) {
            throw new CertificateException(err);
        }
        if (isDirectlyTrustedCert(chain)) {
            return;
        }
        try {
            CertPath certPath = factory.generateCertPath(Arrays.asList(chain));
            if (!Arrays.equals(chain[0].getEncoded(),
                    ((X509Certificate)certPath.getCertificates().get(0))
                    .getEncoded())) {
                throw new CertificateException("Certificate chain error");
            }
            validator.validate(certPath, params);
        } catch (InvalidAlgorithmParameterException e) {
            throw new CertificateException(e);
        } catch (CertPathValidatorException e) {
            throw new CertificateException(e);
        }
    }
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        if (chain == null || chain.length == 0 || authType == null
                || authType.length() == 0) {
            throw new IllegalArgumentException(
                    "null or zero-length parameter");
        }
        if (err != null) {
            throw new CertificateException(err);
        }
        CertificateException ce = null;
        try {
            CertPath certPath = factory.generateCertPath(
                    Arrays.asList(chain));
            if (!Arrays.equals(chain[0].getEncoded(),
                    certPath.getCertificates().get(0).getEncoded())) {
                throw new CertificateException("Certificate chain error");
            }
            validator.validate(certPath, params);
        } catch (InvalidAlgorithmParameterException e) {
            ce = new CertificateException(e);
        } catch (CertPathValidatorException e) {
            ce = new CertificateException(e);
        }
        if (ce != null) {
            if (!isDirectlyTrustedCert(chain)) {
                throw ce;
            }
        }
    }
    private boolean isDirectlyTrustedCert(X509Certificate[] chain) {
        byte[] questionable;
        if (chain.length == 1) {
            if (params instanceof IndexedPKIXParameters) {
                IndexedPKIXParameters index = (IndexedPKIXParameters) params;
                return index.isDirectlyTrusted(chain[0]);
            } else {
                try {
                    questionable = chain[0].getEncoded();
                    Set<TrustAnchor> anchors = params.getTrustAnchors();
                    for (TrustAnchor trustAnchor : anchors) {
                        byte[] trusted = trustAnchor.getTrustedCert()
                                .getEncoded();
                        if (Arrays.equals(questionable, trusted)) {
                            return true;
                        }
                    }
                } catch (CertificateEncodingException e) {
                }
            }
        }
        return false;
    }
    public X509Certificate[] getAcceptedIssuers() {
        if (params == null) {
            return new X509Certificate[0];
        }
        Set<TrustAnchor> anchors = params.getTrustAnchors();
        X509Certificate[] certs = new X509Certificate[anchors.size()];
        int i = 0;
        for (Iterator<TrustAnchor> it = anchors.iterator(); it.hasNext();) {
            certs[i++] = it.next().getTrustedCert();
        }
        return certs;
    }
}
