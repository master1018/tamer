public class RSAMultiPrimePrivateCrtKeySpec extends RSAPrivateKeySpec {
    private final BigInteger publicExponent;
    private final BigInteger primeP;
    private final BigInteger primeQ;
    private final BigInteger primeExponentP;
    private final BigInteger primeExponentQ;
    private final BigInteger crtCoefficient;
    private final RSAOtherPrimeInfo[] otherPrimeInfo;
    public RSAMultiPrimePrivateCrtKeySpec(
            BigInteger modulus,
            BigInteger publicExponent,
            BigInteger privateExponent,
            BigInteger primeP,
            BigInteger primeQ,
            BigInteger primeExponentP,
            BigInteger primeExponentQ,
            BigInteger crtCoefficient,
            RSAOtherPrimeInfo[] otherPrimeInfo) {
        super(modulus, privateExponent);
        if (modulus == null) {
            throw new NullPointerException(Messages.getString("security.83", "modulus")); 
        }
        if (privateExponent == null) {
            throw new NullPointerException(Messages.getString("security.83", "privateExponent")); 
        }
        if (publicExponent == null) {
            throw new NullPointerException(Messages.getString("security.83", "publicExponent")); 
        }
        if (primeP == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeP")); 
        }
        if (primeQ == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeQ")); 
        }
        if (primeExponentP == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeExponentP")); 
        }
        if (primeExponentQ == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeExponentQ")); 
        }
        if (crtCoefficient == null) {
            throw new NullPointerException(Messages.getString("security.83", "crtCoefficient")); 
        }
        if (otherPrimeInfo != null) {
            if (otherPrimeInfo.length == 0) {
                throw new IllegalArgumentException(
                Messages.getString("security.85")); 
            }
            this.otherPrimeInfo = new RSAOtherPrimeInfo[otherPrimeInfo.length];
            System.arraycopy(otherPrimeInfo, 0,
                    this.otherPrimeInfo, 0, this.otherPrimeInfo.length);
        } else {
            this.otherPrimeInfo = null;
        }
        this.publicExponent = publicExponent;
        this.primeP = primeP;
        this.primeQ = primeQ;
        this.primeExponentP = primeExponentP;
        this.primeExponentQ = primeExponentQ;
        this.crtCoefficient = crtCoefficient;
    }
    public BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }
    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        if (otherPrimeInfo == null) {
            return null;
        } else {
            RSAOtherPrimeInfo[] ret =
                new RSAOtherPrimeInfo[otherPrimeInfo.length];
            System.arraycopy(otherPrimeInfo, 0, ret, 0, ret.length);
            return ret;
        }
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
}
