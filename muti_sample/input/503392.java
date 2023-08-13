public class ECFieldFp implements ECField {
    private final BigInteger p;
    public ECFieldFp(BigInteger p) {
        this.p = p;
        if (this.p == null) {
            throw new NullPointerException(Messages.getString("security.83", "p")); 
        }
        if (this.p.signum() != 1) {
            throw new IllegalArgumentException(Messages.getString("security.86", "p")); 
        }
    }
    public int getFieldSize() {
        return p.bitLength();
    }
    public BigInteger getP() {
        return p;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ECFieldFp) {
            return (this.p.equals(((ECFieldFp)obj).p));
        }
        return false;
    }
    public int hashCode() {
        return p.hashCode();
    }
}
