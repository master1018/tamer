public class SctpPeerAddrChange extends PeerAddressChangeNotification
    implements SctpNotification
{
    private final static int SCTP_ADDR_AVAILABLE = 1;
    private final static int SCTP_ADDR_UNREACHABLE = 2;
    private final static int SCTP_ADDR_REMOVED = 3;
    private final static int SCTP_ADDR_ADDED = 4;
    private final static int SCTP_ADDR_MADE_PRIM = 5;
    private final static int SCTP_ADDR_CONFIRMED =6;
    private Association association;
    private int assocId;
    private SocketAddress address;
    private AddressChangeEvent event;
    private SctpPeerAddrChange(int assocId, SocketAddress address, int intEvent) {
        switch (intEvent) {
            case SCTP_ADDR_AVAILABLE :
                this.event = AddressChangeEvent.ADDR_AVAILABLE;
                break;
            case SCTP_ADDR_UNREACHABLE :
                this.event = AddressChangeEvent.ADDR_UNREACHABLE;
                break;
            case SCTP_ADDR_REMOVED :
                this.event = AddressChangeEvent.ADDR_REMOVED;
                break;
            case SCTP_ADDR_ADDED :
                this.event = AddressChangeEvent.ADDR_ADDED;
                break;
            case SCTP_ADDR_MADE_PRIM :
                this.event = AddressChangeEvent.ADDR_MADE_PRIMARY;
                break;
            case SCTP_ADDR_CONFIRMED :
                this.event = AddressChangeEvent.ADDR_CONFIRMED;
                break;
            default:
                throw new AssertionError("Unknown event type");
        }
        this.assocId = assocId;
        this.address = address;
    }
    @Override
    public int assocId() {
        return assocId;
    }
    @Override
    public void setAssociation(Association association) {
        this.association = association;
    }
    @Override
    public SocketAddress address() {
        assert address != null;
        return address;
    }
    @Override
    public Association association() {
        assert association != null;
        return association;
    }
    @Override
    public AddressChangeEvent event() {
        assert event != null;
        return event;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(" [");
        sb.append("Address: ").append(address);
        sb.append(", Association:").append(association);
        sb.append(", Event: ").append(event).append("]");
        return sb.toString();
    }
}
