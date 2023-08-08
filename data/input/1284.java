public class BindTransmitter extends BindRequest {
    public BindTransmitter() {
        super(Data.BIND_TRANSMITTER);
    }
    protected Response createResponse() {
        return new BindTransmitterResp();
    }
    public boolean isTransmitter() {
        return true;
    }
    public boolean isReceiver() {
        return false;
    }
}
