public abstract class AbstractMinaProtocolEncoder extends ProtocolEncoderAdapter {
    protected Bundle bundle;
    protected Logger logger = Logger.getLogger(getClass());
    public AbstractMinaProtocolEncoder(Bundle bundle) {
        this.bundle = bundle;
    }
    protected abstract byte[] buildPacket(Device device);
    protected abstract byte[] buildPacketType(TreeMap<String, byte[]> map);
    protected abstract byte[] buildPacketData(TreeMap<String, byte[]> map);
    protected abstract byte buildPacketCheck(byte[] bytes);
    protected void transmitPacket(byte[] packet, ProtocolEncoderOutput output) {
        IoBuffer buffer = IoBuffer.allocate(1).setAutoExpand(true);
        buffer.put(packet);
        buffer.flip();
        output.write(buffer);
    }
}
