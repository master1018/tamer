public class EllipticCurve {
    private final ECField field;
    private final BigInteger a;
    private final BigInteger b;
    private final byte[] seed;
    private volatile int hash;
    public EllipticCurve(ECField field, BigInteger a, BigInteger b, byte[] seed) {
        this.field = field;
        if (this.field == null) {
            throw new NullPointerException(Messages.getString("security.7A")); 
        }
        this.a = a;
        if (this.a == null) {
            throw new NullPointerException(Messages.getString("security.7B")); 
        }
        this.b = b;
        if (this.b == null) {
            throw new NullPointerException(Messages.getString("security.7C")); 
        }
        if (seed == null) {
            this.seed = null;
        } else {
            this.seed = new byte[seed.length];
            System.arraycopy(seed, 0, this.seed, 0, this.seed.length);
        }
        if (this.field instanceof ECFieldFp) {
            BigInteger p = ((ECFieldFp) this.field).getP();
            if (this.a.signum() < 0 || this.a.compareTo(p) >= 0) {
                throw new IllegalArgumentException(Messages.getString("security.7D")); 
            }
            if (this.b.signum() < 0 || this.b.compareTo(p) >= 0) {
                throw new IllegalArgumentException(Messages.getString("security.7E")); 
            }
        } else if (this.field instanceof ECFieldF2m) {
            int fieldSizeInBits = this.field.getFieldSize();
            if (!(this.a.bitLength() <= fieldSizeInBits)) {
                throw new IllegalArgumentException(Messages.getString("security.7D")); 
            }
            if (!(this.b.bitLength() <= fieldSizeInBits)) {
                throw new IllegalArgumentException(Messages.getString("security.7E")); 
            }
        }
    }
    public EllipticCurve(ECField field, BigInteger a, BigInteger b) {
        this(field, a, b, null);
    }
    public BigInteger getA() {
        return a;
    }
    public BigInteger getB() {
        return b;
    }
    public ECField getField() {
        return field;
    }
    public byte[] getSeed() {
        if (seed == null) {
            return null;
        } else {
            byte[] ret = new byte[seed.length];
            System.arraycopy(seed, 0, ret, 0, ret.length);
            return ret;
        }
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EllipticCurve)) {
            return false;
        }
        EllipticCurve otherEc = (EllipticCurve) other;
        return this.field.equals(otherEc.field) && this.a.equals(otherEc.a)
                && this.b.equals(otherEc.b)
                && Arrays.equals(this.seed, otherEc.seed);
    }
    public int hashCode() {
        if (hash == 0) {
            int hash0 = 11;
            hash0 = hash0 * 31 + field.hashCode();
            hash0 = hash0 * 31 + a.hashCode();
            hash0 = hash0 * 31 + b.hashCode();
            if (seed != null) {
                for (int i = 0; i < seed.length; i++) {
                    hash0 = hash0 * 31 + seed[i];
                }
            } else {
                hash0 = hash0 * 31;
            }
            hash = hash0;
        }
        return hash;
    }
}
