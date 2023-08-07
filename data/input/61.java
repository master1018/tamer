public class FacilityIndicatorImpl extends AbstractISUPParameter implements FacilityIndicator {
    private int facilityIndicator = 0;
    public FacilityIndicatorImpl(byte[] b) throws ParameterException {
        super();
        decode(b);
    }
    public FacilityIndicatorImpl() {
        super();
    }
    public FacilityIndicatorImpl(int facilityIndicator) {
        super();
        this.facilityIndicator = facilityIndicator;
    }
    public int decode(byte[] b) throws ParameterException {
        if (b == null || b.length != 1) {
            throw new ParameterException("byte[] must not be null or have different size than 1");
        }
        this.facilityIndicator = b[0];
        return 1;
    }
    public byte[] encode() throws ParameterException {
        byte[] b = { (byte) this.facilityIndicator };
        return b;
    }
    public int getFacilityIndicator() {
        return facilityIndicator;
    }
    public void setFacilityIndicator(int facilityIndicator) {
        this.facilityIndicator = facilityIndicator;
    }
    public int getCode() {
        return _PARAMETER_CODE;
    }
}
