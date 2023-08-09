public class ECPoint {
    public static final ECPoint POINT_INFINITY = new ECPoint();
    private final BigInteger affineX;
    private final BigInteger affineY;
    private ECPoint() {
        affineX = null;
        affineY = null;
    }
    public ECPoint(BigInteger affineX, BigInteger affineY) {
        this.affineX = affineX;
        if (this.affineX == null) {
            throw new NullPointerException(Messages.getString("security.83", "X")); 
        }
        this.affineY = affineY;
        if (this.affineY == null) {
            throw new NullPointerException(Messages.getString("security.83", "Y")); 
        }
    }
    public BigInteger getAffineX() {
        return affineX;
    }
    public BigInteger getAffineY() {
        return affineY;
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof ECPoint) {
            if (this.affineX != null) {
                ECPoint otherPoint = (ECPoint)other;
                return this.affineX.equals(otherPoint.affineX) &&
                       this.affineY.equals(otherPoint.affineY);
            } else {
                return other == POINT_INFINITY;
            }
        }
        return false;
    }
    public int hashCode() {
        if (this.affineX != null) {
            return affineX.hashCode() * 31 + affineY.hashCode();
        }
        return 11;
    }
}
