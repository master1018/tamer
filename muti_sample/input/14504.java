public class X509CertificatePair {
    private static final byte TAG_FORWARD = 0;
    private static final byte TAG_REVERSE = 1;
    private X509Certificate forward;
    private X509Certificate reverse;
    private byte[] encoded;
    private static final Cache cache = Cache.newSoftMemoryCache(750);
    public X509CertificatePair() {}
    public X509CertificatePair(X509Certificate forward, X509Certificate reverse)
                throws CertificateException {
        if (forward == null && reverse == null) {
            throw new CertificateException("at least one of certificate pair "
                + "must be non-null");
        }
        this.forward = forward;
        this.reverse = reverse;
        checkPair();
    }
    private X509CertificatePair(byte[] encoded)throws CertificateException {
        try {
            parse(new DerValue(encoded));
            this.encoded = encoded;
        } catch (IOException ex) {
            throw new CertificateException(ex.toString());
        }
        checkPair();
    }
    public static synchronized void clearCache() {
        cache.clear();
    }
    public static synchronized X509CertificatePair generateCertificatePair
            (byte[] encoded) throws CertificateException {
        Object key = new Cache.EqualByteArray(encoded);
        X509CertificatePair pair = (X509CertificatePair)cache.get(key);
        if (pair != null) {
            return pair;
        }
        pair = new X509CertificatePair(encoded);
        key = new Cache.EqualByteArray(pair.encoded);
        cache.put(key, pair);
        return pair;
    }
    public void setForward(X509Certificate cert) throws CertificateException {
        checkPair();
        forward = cert;
    }
    public void setReverse(X509Certificate cert) throws CertificateException {
        checkPair();
        reverse = cert;
    }
    public X509Certificate getForward() {
        return forward;
    }
    public X509Certificate getReverse() {
        return reverse;
    }
    public byte[] getEncoded() throws CertificateEncodingException {
        try {
            if (encoded == null) {
                DerOutputStream tmp = new DerOutputStream();
                emit(tmp);
                encoded = tmp.toByteArray();
            }
        } catch (IOException ex) {
            throw new CertificateEncodingException(ex.toString());
        }
        return encoded;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("X.509 Certificate Pair: [\n");
        if (forward != null)
            sb.append("  Forward: " + forward + "\n");
        if (reverse != null)
            sb.append("  Reverse: " + reverse + "\n");
        sb.append("]");
        return sb.toString();
    }
    private void parse(DerValue val)
        throws IOException, CertificateException
    {
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException
                ("Sequence tag missing for X509CertificatePair");
        }
        while (val.data != null && val.data.available() != 0) {
            DerValue opt = val.data.getDerValue();
            short tag = (byte) (opt.tag & 0x01f);
            switch (tag) {
                case TAG_FORWARD:
                    if (opt.isContextSpecific() && opt.isConstructed()) {
                        if (forward != null) {
                            throw new IOException("Duplicate forward "
                                + "certificate in X509CertificatePair");
                        }
                        opt = opt.data.getDerValue();
                        forward = X509Factory.intern
                                        (new X509CertImpl(opt.toByteArray()));
                    }
                    break;
                case TAG_REVERSE:
                    if (opt.isContextSpecific() && opt.isConstructed()) {
                        if (reverse != null) {
                            throw new IOException("Duplicate reverse "
                                + "certificate in X509CertificatePair");
                        }
                        opt = opt.data.getDerValue();
                        reverse = X509Factory.intern
                                        (new X509CertImpl(opt.toByteArray()));
                    }
                    break;
                default:
                    throw new IOException("Invalid encoding of "
                        + "X509CertificatePair");
            }
        }
        if (forward == null && reverse == null) {
            throw new CertificateException("at least one of certificate pair "
                + "must be non-null");
        }
    }
    private void emit(DerOutputStream out)
        throws IOException, CertificateEncodingException
    {
        DerOutputStream tagged = new DerOutputStream();
        if (forward != null) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putDerValue(new DerValue(forward.getEncoded()));
            tagged.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                         true, TAG_FORWARD), tmp);
        }
        if (reverse != null) {
            DerOutputStream tmp = new DerOutputStream();
            tmp.putDerValue(new DerValue(reverse.getEncoded()));
            tagged.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                         true, TAG_REVERSE), tmp);
        }
        out.write(DerValue.tag_Sequence, tagged);
    }
    private void checkPair() throws CertificateException {
        if (forward == null || reverse == null) {
            return;
        }
        X500Principal fwSubject = forward.getSubjectX500Principal();
        X500Principal fwIssuer = forward.getIssuerX500Principal();
        X500Principal rvSubject = reverse.getSubjectX500Principal();
        X500Principal rvIssuer = reverse.getIssuerX500Principal();
        if (!fwIssuer.equals(rvSubject) || !rvIssuer.equals(fwSubject)) {
            throw new CertificateException("subject and issuer names in "
                + "forward and reverse certificates do not match");
        }
        try {
            PublicKey pk = reverse.getPublicKey();
            if (!(pk instanceof DSAPublicKey) ||
                        ((DSAPublicKey)pk).getParams() != null) {
                forward.verify(pk);
            }
            pk = forward.getPublicKey();
            if (!(pk instanceof DSAPublicKey) ||
                        ((DSAPublicKey)pk).getParams() != null) {
                reverse.verify(pk);
            }
        } catch (GeneralSecurityException e) {
            throw new CertificateException("invalid signature: "
                + e.getMessage());
        }
    }
}
