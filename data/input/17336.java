public class SctpResultContainer {
    static final int NOTHING = 0;
    static final int MESSAGE = 1;
    static final int SEND_FAILED = 2;
    static final int ASSOCIATION_CHANGED = 3;
    static final int PEER_ADDRESS_CHANGED = 4;
    static final int SHUTDOWN = 5;
    private Object value;
    private int type;
    int type() {
        return type;
    }
    boolean hasSomething() {
        return type() != NOTHING;
    }
    boolean isNotification() {
        return type() != MESSAGE && type() != NOTHING ? true : false;
    }
    void clear() {
        type = NOTHING;
        value = null;
    }
    SctpNotification notification() {
        assert type() != MESSAGE && type() != NOTHING;
        return (SctpNotification) value;
    }
    SctpMessageInfoImpl getMessageInfo() {
        assert type() == MESSAGE;
        if (value instanceof SctpMessageInfoImpl)
            return (SctpMessageInfoImpl) value;
        return null;
    }
    SctpSendFailed getSendFailed() {
        assert type() == SEND_FAILED;
        if (value instanceof SctpSendFailed)
            return (SctpSendFailed) value;
        return null;
    }
    SctpAssocChange getAssociationChanged() {
        assert type() == ASSOCIATION_CHANGED;
        if (value instanceof SctpAssocChange)
            return (SctpAssocChange) value;
        return null;
    }
    SctpPeerAddrChange getPeerAddressChanged() {
        assert type() == PEER_ADDRESS_CHANGED;
        if (value instanceof SctpPeerAddrChange)
            return (SctpPeerAddrChange) value;
        return null;
    }
    SctpShutdown getShutdown() {
        assert type() == SHUTDOWN;
        if (value instanceof SctpShutdown)
            return (SctpShutdown) value;
        return null;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ");
        switch (type) {
            case NOTHING:              sb.append("NOTHING");             break;
            case MESSAGE:              sb.append("MESSAGE");             break;
            case SEND_FAILED:          sb.append("SEND FAILED");         break;
            case ASSOCIATION_CHANGED:  sb.append("ASSOCIATION CHANGE");  break;
            case PEER_ADDRESS_CHANGED: sb.append("PEER ADDRESS CHANGE"); break;
            case SHUTDOWN:             sb.append("SHUTDOWN");            break;
            default :                  sb.append("Unknown result type");
        }
        sb.append(", Value: ");
        sb.append((value == null) ? "null" : value.toString());
        return sb.toString();
    }
}
