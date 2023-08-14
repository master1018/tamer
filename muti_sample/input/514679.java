public class RSAMultiPrimePrivateCrtKeyImpl implements RSAMultiPrimePrivateCrtKey {
    static final long serialVersionUID = 123;
    private BigInteger crtCoefficient = null;
    private BigInteger publicExponent = null;
    private BigInteger primeExponentP = null;
    private BigInteger primeExponentQ = null;
    private BigInteger primeP = null;
    private BigInteger primeQ = null;
    private RSAOtherPrimeInfo[] otherPrimeInfo = null;
    public RSAMultiPrimePrivateCrtKeyImpl(BigInteger publicExp,
                                          BigInteger primeExpP, 
                                          BigInteger primeExpQ, 
                                          BigInteger prP, 
                                          BigInteger prQ, 
                                          BigInteger crtCft, 
                                          RSAOtherPrimeInfo[] otherPrmInfo) {
        publicExponent = publicExp;
        primeExponentP = primeExpP;
        primeExponentQ = primeExpQ;
        primeP = prP;
        primeQ = prQ;
        crtCoefficient = crtCft;
        otherPrimeInfo = otherPrmInfo;
    }
    public BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }
    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        return otherPrimeInfo;
    }
    public BigInteger getPrimeExponentP() {
        return primeExponentP;
    }
    public BigInteger getPrimeExponentQ() {
        return primeExponentQ;
    }
    public BigInteger getPrimeP() {
        return primeP;
    }
    public BigInteger getPrimeQ() {
        return primeQ;
    }
    public BigInteger getPublicExponent() {
        return publicExponent;
    }
    public BigInteger getPrivateExponent() {
        return null;
    }
    public String getFormat() {
        return null;
    }
    public byte[] getEncoded() {
        return null;
    }
    public String getAlgorithm() {
        return null;
    }
    public BigInteger getModulus() {
        return null;
    }
}