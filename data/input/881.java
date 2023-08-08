public abstract class BindResp extends ie.omk.smpp.message.SMPPResponse {
    private String sysId;
    protected BindResp(int id) {
        super(id);
    }
    public BindResp(Bind req) {
        super(req);
    }
    public void setSystemId(String sysId) throws InvalidParameterValueException {
        if (sysId != null) {
            if (version.validateSystemId(sysId)) {
                this.sysId = sysId;
            } else {
                throw new InvalidParameterValueException("Invalid system Id", sysId);
            }
        } else {
            this.sysId = null;
            return;
        }
    }
    public String getSystemId() {
        return sysId;
    }
    public int getBodyLength() {
        return ((sysId != null) ? sysId.length() : 0) + 1;
    }
    protected void encodeBody(OutputStream out) throws java.io.IOException {
        SMPPIO.writeCString(sysId, out);
    }
    public void readBodyFrom(byte[] body, int offset) throws SMPPProtocolException {
        sysId = SMPPIO.readCString(body, offset);
    }
    public String toString() {
        return new String("bind_resp");
    }
}
