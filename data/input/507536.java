public class ECPrivateKeySpec implements KeySpec {
    private final BigInteger s;
    private final ECParameterSpec params;
    public ECPrivateKeySpec(BigInteger s, ECParameterSpec params) {
        this.s = s;
        this.params = params;
        if (this.s == null) {
            throw new NullPointerException(Messages.getString("security.83", "s")); 
        }
        if (this.params == null) {
            throw new NullPointerException(Messages.getString("security.83", "params")); 
        }
    }
    public ECParameterSpec getParams() {
        return params;
    }
    public BigInteger getS() {
        return s;
    }
}
