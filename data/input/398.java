public class ParamRetrieve extends SMPPPacket {
    private static final long serialVersionUID = 2L;
    private String paramName;
    public ParamRetrieve() {
        super(CommandId.PARAM_RETRIEVE);
    }
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    public String getParamName() {
        return paramName;
    }
    @Override
    public boolean equals(Object obj) {
        boolean equals = super.equals(obj);
        if (equals) {
            ParamRetrieve other = (ParamRetrieve) obj;
            equals |= safeCompare(paramName, other.paramName);
        }
        return equals;
    }
    @Override
    public int hashCode() {
        int hc = super.hashCode();
        hc += (paramName != null) ? paramName.hashCode() : 0;
        return hc;
    }
    @Override
    protected void toString(StringBuilder buffer) {
        buffer.append("paramName=").append(paramName);
    }
    @Override
    protected void validateMandatory(SMPPVersion smppVersion) {
        smppVersion.validateParamName(paramName);
    }
    @Override
    protected void readMandatory(PacketDecoder decoder) {
        paramName = decoder.readCString();
    }
    @Override
    protected void writeMandatory(PacketEncoder encoder) throws IOException {
        encoder.writeCString(paramName);
    }
    @Override
    protected int getMandatorySize() {
        return 1 + sizeOf(paramName);
    }
}
