public class ECParameterSpec implements AlgorithmParameterSpec {
    private final EllipticCurve curve;
    private final ECPoint generator;
    private final BigInteger order;
    private final int cofactor;
    public ECParameterSpec(EllipticCurve curve, ECPoint generator,
            BigInteger order, int cofactor) {
        this.curve = curve;
        this.generator = generator;
        this.order = order;
        this.cofactor = cofactor;
        if (this.curve == null) {
            throw new NullPointerException(Messages.getString("security.83", "curve")); 
        }
        if (this.generator == null) {
            throw new NullPointerException(Messages.getString("security.83", "generator")); 
        }
        if (this.order == null) {
            throw new NullPointerException(Messages.getString("security.83", "order")); 
        }
        if (!(this.order.compareTo(BigInteger.ZERO) > 0)) {
            throw new
            IllegalArgumentException(Messages.getString("security.86", "order")); 
        }
        if (!(this.cofactor > 0)) {
            throw new
            IllegalArgumentException(Messages.getString("security.86", "cofactor")); 
        }
    }
    public int getCofactor() {
        return cofactor;
    }
    public EllipticCurve getCurve() {
        return curve;
    }
    public ECPoint getGenerator() {
        return generator;
    }
    public BigInteger getOrder() {
        return order;
    }
}
