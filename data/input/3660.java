public class ChainingConstructors {
    private static final String MSG = "msg";
    private static Exception cause = new Exception("cause");
    public static void main(String[] args) throws Exception {
        SecurityException se = new SecurityException(cause);
        if (!se.getCause().equals(cause)) {
            throw new SecurityException("Test 1 failed");
        }
        se = new SecurityException(MSG, cause);
        if (!se.getMessage().equals(MSG) || !se.getCause().equals(cause)) {
            throw new SecurityException("Test 1 failed");
        }
        DigestException de = new DigestException(cause);
        if (!de.getCause().equals(cause)) {
            throw new SecurityException("Test 2 failed");
        }
        de = new DigestException(MSG, cause);
        if (!de.getMessage().equals(MSG) || !de.getCause().equals(cause)) {
            throw new SecurityException("Test 2 failed");
        }
        GeneralSecurityException gse = new GeneralSecurityException(cause);
        if (!gse.getCause().equals(cause)) {
            throw new SecurityException("Test 3 failed");
        }
        gse = new GeneralSecurityException(MSG, cause);
        if (!gse.getMessage().equals(MSG) || !gse.getCause().equals(cause)) {
            throw new SecurityException("Test 3 failed");
        }
        InvalidAlgorithmParameterException iape =
                new InvalidAlgorithmParameterException(cause);
        if (!iape.getCause().equals(cause)) {
            throw new SecurityException("Test 4 failed");
        }
        iape = new InvalidAlgorithmParameterException(MSG, cause);
        if (!iape.getMessage().equals(MSG) || !iape.getCause().equals(cause)) {
            throw new SecurityException("Test 4 failed");
        }
        InvalidKeyException ike = new InvalidKeyException(cause);
        if (!ike.getCause().equals(cause)) {
            throw new SecurityException("Test 5 failed");
        }
        ike = new InvalidKeyException(MSG, cause);
        if (!ike.getMessage().equals(MSG) || !ike.getCause().equals(cause)) {
            throw new SecurityException("Test 5 failed");
        }
        InvalidKeySpecException ikse = new InvalidKeySpecException(cause);
        if (!ikse.getCause().equals(cause)) {
            throw new SecurityException("Test 6 failed");
        }
        ikse = new InvalidKeySpecException(MSG, cause);
        if (!ikse.getMessage().equals(MSG) || !ikse.getCause().equals(cause)) {
            throw new SecurityException("Test 6 failed");
        }
        KeyException ke = new KeyException(cause);
        if (!ke.getCause().equals(cause)) {
            throw new SecurityException("Test 7 failed");
        }
        ke = new KeyException(MSG, cause);
        if (!ke.getMessage().equals(MSG) || !ke.getCause().equals(cause)) {
            throw new SecurityException("Test 7 failed");
        }
        KeyManagementException kme = new KeyManagementException(cause);
        if (!kme.getCause().equals(cause)) {
            throw new SecurityException("Test 8 failed");
        }
        kme = new KeyManagementException(MSG, cause);
        if (!kme.getMessage().equals(MSG) || !kme.getCause().equals(cause)) {
            throw new SecurityException("Test 8 failed");
        }
        KeyStoreException kse = new KeyStoreException(cause);
        if (!kse.getCause().equals(cause)) {
            throw new SecurityException("Test 9 failed");
        }
        kse = new KeyStoreException(MSG, cause);
        if (!kse.getMessage().equals(MSG) || !kse.getCause().equals(cause)) {
            throw new SecurityException("Test 9 failed");
        }
        NoSuchAlgorithmException nsae = new NoSuchAlgorithmException(cause);
        if (!nsae.getCause().equals(cause)) {
            throw new SecurityException("Test 10 failed");
        }
        nsae = new NoSuchAlgorithmException(MSG, cause);
        if (!nsae.getMessage().equals(MSG) || !nsae.getCause().equals(cause)) {
            throw new SecurityException("Test 10 failed");
        }
        ProviderException pe = new ProviderException(cause);
        if (!pe.getCause().equals(cause)) {
            throw new SecurityException("Test 11 failed");
        }
        pe = new ProviderException(MSG, cause);
        if (!pe.getMessage().equals(MSG) || !pe.getCause().equals(cause)) {
            throw new SecurityException("Test 11 failed");
        }
        SignatureException sige = new SignatureException(cause);
        if (!sige.getCause().equals(cause)) {
            throw new SecurityException("Test 12 failed");
        }
        sige = new SignatureException(MSG, cause);
        if (!sige.getMessage().equals(MSG) || !sige.getCause().equals(cause)) {
            throw new SecurityException("Test 12 failed");
        }
        CRLException crle = new CRLException(cause);
        if (!crle.getCause().equals(cause)) {
            throw new SecurityException("Test 13 failed");
        }
        crle = new CRLException(MSG, cause);
        if (!crle.getMessage().equals(MSG) || !crle.getCause().equals(cause)) {
            throw new SecurityException("Test 13 failed");
        }
        CertificateException ce = new CertificateException(cause);
        if (!ce.getCause().equals(cause)) {
            throw new SecurityException("Test 14 failed");
        }
        ce = new CertificateException(MSG, cause);
        if (!ce.getMessage().equals(MSG) || !ce.getCause().equals(cause)) {
            throw new SecurityException("Test 14 failed");
        }
        CertificateParsingException cpe =
                new CertificateParsingException(cause);
        if (!cpe.getCause().equals(cause)) {
            throw new SecurityException("Test 15 failed");
        }
        cpe = new CertificateParsingException(MSG, cause);
        if (!cpe.getMessage().equals(MSG) || !cpe.getCause().equals(cause)) {
            throw new SecurityException("Test 15 failed");
        }
        CertificateEncodingException cee =
                new CertificateEncodingException(cause);
        if (!cee.getCause().equals(cause)) {
            throw new SecurityException("Test 16 failed");
        }
        cee = new CertificateEncodingException(MSG, cause);
        if (!cee.getMessage().equals(MSG) || !cee.getCause().equals(cause)) {
            throw new SecurityException("Test 16 failed");
        }
    }
}
