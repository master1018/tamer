public class ChannelUpdateEvent extends ManagerEvent {
    private static final long serialVersionUID = 3141630567125429466L;
    private String channelType;
    private String channel;
    private String uniqueId;
    private String sipCallId;
    private String sipFullContact;
    private String peerName;
    private String gtalkSid;
    private String iax2CallNoLocal;
    private String iax2CallNoRemote;
    private String iax2Peer;
    public ChannelUpdateEvent(Object source) {
        super(source);
    }
    public String getChannelType() {
        return channelType;
    }
    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }
    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    public String getSipCallId() {
        return sipCallId;
    }
    public void setSipCallId(String sipCallId) {
        this.sipCallId = sipCallId;
    }
    public String getSipFullContact() {
        return sipFullContact;
    }
    public void setSipFullContact(String sipFullContact) {
        this.sipFullContact = sipFullContact;
    }
    public String getPeerName() {
        return peerName;
    }
    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }
    public String getGtalkSid() {
        return gtalkSid;
    }
    public void setGtalkSid(String gtalkSid) {
        this.gtalkSid = gtalkSid;
    }
    public String getIax2CallNoLocal() {
        return iax2CallNoLocal;
    }
    public void setIax2CallNoLocal(String iax2CallNoLocal) {
        this.iax2CallNoLocal = iax2CallNoLocal;
    }
    public String getIax2CallNoRemote() {
        return iax2CallNoRemote;
    }
    public void setIax2CallNoRemote(String iax2CallNoRemote) {
        this.iax2CallNoRemote = iax2CallNoRemote;
    }
    public String getIax2Peer() {
        return iax2Peer;
    }
    public void setIax2Peer(String iax2Peer) {
        this.iax2Peer = iax2Peer;
    }
}
