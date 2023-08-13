public class OpenSSLSignature extends Signature {
    private int ctx;
    private int dsa;
    private int rsa;
    private String evpAlgorithm;
    private byte[] singleByte = new byte[1];
    public static OpenSSLSignature getInstance(String algorithm) throws NoSuchAlgorithmException {
        return new OpenSSLSignature(algorithm);
    }
    private OpenSSLSignature(String algorithm) throws NoSuchAlgorithmException {
        super(algorithm);
        int i = algorithm.indexOf("with"); 
        if (i == -1) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        if ("MD2withRSA".equalsIgnoreCase(algorithm) ||
                "MD2withRSAEncryption".equalsIgnoreCase(algorithm) ||
                "1.2.840.113549.1.1.2".equalsIgnoreCase(algorithm) ||
                "MD2/RSA".equalsIgnoreCase(algorithm)) {
            throw new NoSuchAlgorithmException("MD2withRSA");
        }
        if ("1.3.14.3.2.26with1.2.840.10040.4.1".equals(algorithm)
                || "SHA1withDSA".equals(algorithm)
                || "SHAwithDSA".equals(algorithm)) {
            evpAlgorithm = "DSA-SHA";
        } else {
            evpAlgorithm = algorithm.substring(0, i).replace("-", "").toUpperCase();
        }
        ctx = NativeCrypto.EVP_new();
    }
    @Override
    protected void engineUpdate(byte input) {
        singleByte[0] = input;
        engineUpdate(singleByte, 0, 1);
    }
    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        if (state == SIGN) {
            throw new UnsupportedOperationException();
        } else {
            NativeCrypto.EVP_VerifyUpdate(ctx, input, offset, len);
        }
    }
    @Override
    protected Object engineGetParameter(String param) throws InvalidParameterException {
        return null;
    }
    @Override
    protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
        throw new UnsupportedOperationException();
    }
    @Override
    protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
        if (publicKey instanceof DSAPublicKey) {
            try {
                DSAPublicKey dsaPublicKey = (DSAPublicKey)publicKey;
                DSAParams dsaParams = dsaPublicKey.getParams();
                dsa = NativeCrypto.EVP_PKEY_new_DSA(dsaParams.getP().toByteArray(), 
                        dsaParams.getQ().toByteArray(), dsaParams.getG().toByteArray(),
                        dsaPublicKey.getY().toByteArray(), null);
            } catch (Exception ex) {
                throw new InvalidKeyException(ex.toString());
            }
        } else if (publicKey instanceof RSAPublicKey) {
            try {
                RSAPublicKey rsaPublicKey = (RSAPublicKey)publicKey;
                rsa = NativeCrypto.EVP_PKEY_new_RSA(rsaPublicKey.getModulus().toByteArray(),
                        rsaPublicKey.getPublicExponent().toByteArray(), null, null, null);
            } catch (Exception ex) {
                throw new InvalidKeyException(ex.toString());
            }
        } else {
            throw new InvalidKeyException("Need DSA or RSA public key");
        }
        try {
            NativeCrypto.EVP_VerifyInit(ctx, evpAlgorithm);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    @Override
    protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
    }
    @Override
    protected byte[] engineSign() throws SignatureException {
        throw new UnsupportedOperationException();
    }
    @Override
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        int handle = (rsa != 0) ? rsa : dsa;
        if (handle == 0) {
            throw new SignatureException("Need DSA or RSA public key");
        }
        try {
            int result = NativeCrypto.EVP_VerifyFinal(ctx, sigBytes, 0, sigBytes.length, handle);
            return result == 1;
        } catch (Exception ex) {
            throw new SignatureException(ex);
        }
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (dsa != 0) {
            NativeCrypto.EVP_PKEY_free(dsa);
        }
        if (rsa != 0) {
            NativeCrypto.EVP_PKEY_free(rsa);
        }
        if (ctx != 0) {
            NativeCrypto.EVP_free(ctx);
        }
    }
}
