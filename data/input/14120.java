public class SctpSendFailed extends SendFailedNotification
    implements SctpNotification
{
    private Association association;
    private int assocId;
    private SocketAddress address;
    private ByteBuffer buffer;
    private int errorCode;
    private int streamNumber;
    private SctpSendFailed(int assocId,
                           SocketAddress address,
                           ByteBuffer buffer,
                           int errorCode,
                           int streamNumber) {
        this.assocId = assocId;
        this.errorCode = errorCode;
        this.streamNumber = streamNumber;
        this.address = address;
        this.buffer = buffer;
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
    public Association association() {
        return association;
    }
    @Override
    public SocketAddress address() {
        assert address != null;
        return address;
    }
    @Override
    public ByteBuffer buffer() {
        assert buffer != null;
        return buffer;
    }
    @Override
    public int errorCode() {
        return errorCode;
    }
    @Override
    public int streamNumber() {
        return streamNumber;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(" [");
        sb.append("Association:").append(association);
        sb.append(", Address: ").append(address);
        sb.append(", buffer: ").append(buffer);
        sb.append(", errorCode: ").append(errorCode);
        sb.append(", streamNumber: ").append(streamNumber);
        sb.append("]");
        return sb.toString();
    }
}
