public class test {
    public void initDecrypt(Key key, AlgorithmParameterSpec params) throws InvalidKeyException {
        if (key instanceof RSAPrivateCrtKey || key instanceof RSAPrivateKey) {
            rsaPrivateKey_ = (RSAPrivateKey) key;
            rsaPublicKey_ = null;
            secureRandom_ = new SecureRandom();
            try {
                md_ = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException nsae) {
                System.err.println("SHA-1 Algorithm not found");
                nsae.printStackTrace();
            }
            cipherBlockSize_ = ((rsaPrivateKey_.getModulus().bitLength()) + 7) / 8;
            blockSize_ = cipherBlockSize_ - 2 * (md_.getDigestLength()) - 2;
        } else {
            throw new InvalidKeyException("Key is not a RSAPrivateKey!");
        }
        if (params instanceof RSAOAEPAlgorithmParameterSpec) {
            ASN1Sequence paramSeq = (ASN1Sequence) ((RSAOAEPAlgorithmParameterSpec) params).getParameters();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DEREncoder dec = new DEREncoder(baos);
                paramSeq.encode(dec);
                byte[] paramBytes = baos.toByteArray();
                dec.close();
                md_.update(paramBytes);
                pHash_ = md_.digest();
            } catch (IOException ioe) {
                System.out.println("shouldn't happen");
                ioe.printStackTrace();
            } catch (ASN1Exception ae) {
                System.out.println("shouldn't happen");
                ae.printStackTrace();
            }
        }
    }
}
