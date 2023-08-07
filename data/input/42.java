public class ClusterCommandPacket extends _08BasicOutPacket {
    protected byte subCommand;
    protected int clusterId;
    protected static final byte NONE = 0x00;
    protected static final byte BOLD = 0x20;
    protected static final byte ITALIC = 0x40;
    protected static final byte UNDERLINE = (byte) 0x80;
    public ClusterCommandPacket(QQUser user) {
        super(QQ.QQ_CMD_CLUSTER_CMD, true, user);
    }
    public ClusterCommandPacket(ByteBuffer buf, int length, QQUser user) throws PacketParseException {
        super(buf, length, user);
    }
    @Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
        subCommand = buf.get();
    }
    @Override
    public String getPacketName() {
        switch(subCommand) {
            case QQ.QQ_CLUSTER_CMD_ACTIVATE_CLUSTER:
                return "Cluster Activate _08Packet";
            case QQ.QQ_CLUSTER_CMD_MODIFY_MEMBER:
                return "Cluster Modify Member _08Packet";
            case QQ.QQ_CLUSTER_CMD_CREATE_CLUSTER:
                return "Cluster Create _08Packet";
            case QQ.QQ_CLUSTER_CMD_EXIT_CLUSTER:
                return "Cluster Exit _08Packet";
            case QQ.QQ_CLUSTER_CMD_GET_CLUSTER_INFO:
                return "Cluster Get Info _08Packet";
            case QQ.QQ_CLUSTER_CMD_GET_MEMBER_INFO:
                return "Cluster Get Member Info _08Packet";
            case QQ.QQ_CLUSTER_CMD_GET_ONLINE_MEMBER:
                return "Cluster Get Online Member _08Packet";
            case QQ.QQ_CLUSTER_CMD_JOIN_CLUSTER:
                return "Cluster Join _08Packet";
            case QQ.QQ_CLUSTER_CMD_JOIN_CLUSTER_AUTH:
                return "Cluster Auth _08Packet";
            case QQ.QQ_CLUSTER_CMD_MODIFY_CLUSTER_INFO:
                return "Cluster Modify Info _08Packet";
            case QQ.QQ_CLUSTER_CMD_SEARCH_CLUSTER:
                return "Cluster Search _08Packet";
            case QQ.QQ_CLUSTER_CMD_SEND_IM_EX:
                return "Cluster Send IM Ex _08Packet";
            case QQ.QQ_CLUSTER_CMD_MODIFY_TEMP_MEMBER:
                return "Cluster Modify Temp Cluster Member _08Packet";
            case QQ.QQ_CLUSTER_CMD_GET_TEMP_INFO:
                return "Cluster Get Temp Cluster Info _08Packet";
            case QQ.QQ_CLUSTER_CMD_ACTIVATE_TEMP:
                return "Cluster Activate Temp Cluster _08Packet";
            case QQ.QQ_CLUSTER_CMD_EXIT_TEMP:
                return "Cluster Exit Temp Cluster _08Packet";
            case QQ.QQ_CLUSTER_CMD_CREATE_TEMP:
                return "Cluster Create Temp Cluster _08Packet";
            default:
                return "Unknown Cluster Command _08Packet";
        }
    }
    public byte getSubCommand() {
        return subCommand;
    }
    public void setSubCommand(byte subCommand) {
        this.subCommand = subCommand;
    }
    public int getClusterId() {
        return clusterId;
    }
    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }
    @Override
    protected void putBody(ByteBuffer buf) {
    }
}
