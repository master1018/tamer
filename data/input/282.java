public class TenpayConfig {
    public static final String PAYMENT_URL = "http:
    public static final String QUERY_URL = "http:
    public static final String RETURN_URL = "/shop/payment!tenpayReturn.action";
    public static final String SHOW_URL = "/shop/payment!result.action";
    public enum TenpayType {
        direct, partnerMaterial, partnerVirtual
    }
    public static final CurrencyType[] currencyType = { CurrencyType.CNY };
    private TenpayType tenpayType;
    private String bargainorId;
    private String key;
    public TenpayType getTenpayType() {
        return tenpayType;
    }
    public void setTenpayType(TenpayType tenpayType) {
        this.tenpayType = tenpayType;
    }
    public String getBargainorId() {
        return bargainorId;
    }
    public void setBargainorId(String bargainorId) {
        this.bargainorId = bargainorId;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
}
