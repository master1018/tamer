public class RSAOtherPrimeInfo {
    private final BigInteger prime;
    private final BigInteger primeExponent;
    private final BigInteger crtCoefficient;
    public RSAOtherPrimeInfo(BigInteger prime,
            BigInteger primeExponent, BigInteger crtCoefficient) {
        if (prime == null) {
            throw new NullPointerException(Messages.getString("security.83", "prime")); 
        }
        if (primeExponent == null) {
            throw new NullPointerException(Messages.getString("security.83", "primeExponent")); 
        }
        if (crtCoefficient == null) {
            throw new NullPointerException(Messages.getString("security.83", "crtCoefficient")); 
        }
        this.prime = prime;
        this.primeExponent = primeExponent;
        this.crtCoefficient = crtCoefficient;
    }
    public final BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }
    public final BigInteger getPrime() {
        return prime;
    }
    public final BigInteger getExponent() {
        return primeExponent;
    }
}
