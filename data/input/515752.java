public class ECPublicKeySpec implements KeySpec {
    private final ECPoint w;
    private final ECParameterSpec params;
    public ECPublicKeySpec(ECPoint w, ECParameterSpec params) {
        this.w = w;
        this.params = params;
        if (this.w == null) {
            throw new NullPointerException(Messages.getString("security.83", "w")); 
        }
        if (this.params == null) {
            throw new NullPointerException(Messages.getString("security.83", "params")); 
        }
        if (this.w.equals(ECPoint.POINT_INFINITY)) {
            throw new IllegalArgumentException(
                Messages.getString("security.84")); 
        }
    }
    public ECParameterSpec getParams() {
        return params;
    }
    public ECPoint getW() {
        return w;
    }
}
