public abstract class SnmpParams implements SnmpDefinitions {
    private int protocolVersion = snmpVersionOne;
    SnmpParams(int version) {
        protocolVersion = version;
    }
    SnmpParams() {}
    public abstract boolean allowSnmpSets();
    public int getProtocolVersion() {
        return protocolVersion ;
    }
    public void setProtocolVersion(int protocolversion) {
        this.protocolVersion = protocolversion ;
    }
}
