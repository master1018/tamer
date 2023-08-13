public class ECFieldF2m implements ECField {
    private static final int TPB_MID_LEN = 1;
    private static final int PPB_MID_LEN = 3;
    private static final int TPB_LEN = TPB_MID_LEN + 2;
    private static final int PPB_LEN = PPB_MID_LEN + 2;
    private final int m;
    private final BigInteger rp;
    private final int[] ks;
    public ECFieldF2m(int m) {
        this.m = m;
        if (this.m <= 0) {
            throw new IllegalArgumentException(Messages.getString("security.75")); 
        }
        this.rp = null;
        this.ks = null;
    }
    public ECFieldF2m(int m, BigInteger rp) {
        this.m = m;
        if (this.m <= 0) {
            throw new IllegalArgumentException(Messages.getString("security.75")); 
        }
        this.rp = rp;
        if (this.rp == null) {
            throw new NullPointerException(Messages.getString("security.76")); 
        }
        int rp_bc = this.rp.bitCount();
        if ((this.rp.bitLength() != (m+1)) ||
            (rp_bc != TPB_LEN && rp_bc != PPB_LEN) ||
            (!this.rp.testBit(0) || !this.rp.testBit(m)) ) {
            throw new IllegalArgumentException(Messages.getString("security.77")); 
        }
        ks = new int[rp_bc-2];
        BigInteger rpTmp = rp.clearBit(0);
        for (int i=ks.length-1; i>=0; i-- ) {
            ks[i] = rpTmp.getLowestSetBit();
            rpTmp = rpTmp.clearBit(ks[i]);
        }
    }
    public ECFieldF2m(int m, int[] ks) {
        this.m = m;
        if (this.m <= 0) {
            throw new IllegalArgumentException(Messages.getString("security.75")); 
        }
        this.ks = new int[ks.length];
        System.arraycopy(ks, 0, this.ks, 0, this.ks.length);
        if (this.ks.length != TPB_MID_LEN && this.ks.length != PPB_MID_LEN) {
            throw new IllegalArgumentException(Messages.getString("security.78")); 
        }
        boolean checkFailed = false;
        int prev = this.m;
        for (int i=0; i<this.ks.length; i++) {
            if (this.ks[i] < prev) {
                prev = this.ks[i];
                continue;
            }
            checkFailed = true;
            break;
        }
        if (checkFailed || prev < 1) {
            throw new IllegalArgumentException(Messages.getString("security.79")); 
        }
        BigInteger rpTmp = BigInteger.ONE.setBit(this.m);
        for (int i=0; i<this.ks.length; i++) {
            rpTmp = rpTmp.setBit(this.ks[i]);
        }
        rp = rpTmp;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ECFieldF2m) {
            ECFieldF2m o = (ECFieldF2m)obj;
            if (this.m == o.m) {
                if (this.rp == null) {
                    if (o.rp == null) {
                        return true;
                    }
                } else {
                    return Arrays.equals(this.ks, o.ks);
                }
            }
        }
        return false;
    }
    public int getFieldSize() {
        return m;
    }
    public int getM() {
        return m;
    }
    public int[] getMidTermsOfReductionPolynomial() {
        if (ks == null) {
            return null;
        } else {
            int[] ret = new int[ks.length];
            System.arraycopy(ks, 0, ret, 0, ret.length);
            return ret;
        }
    }
    public BigInteger getReductionPolynomial() {
        return rp;
    }
    public int hashCode() {
        return rp == null ? m : m + rp.hashCode();
    }
}
