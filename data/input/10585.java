public class MidiDeviceTransmitterEnvelope implements MidiDeviceTransmitter {
    private final MidiDevice device;
    private final Transmitter transmitter;
    public MidiDeviceTransmitterEnvelope(MidiDevice device, Transmitter transmitter) {
        if (device == null || transmitter == null) {
            throw new NullPointerException();
        }
        this.device = device;
        this.transmitter = transmitter;
    }
    public void setReceiver(Receiver receiver) {
        transmitter.setReceiver(receiver);
    }
    public Receiver getReceiver() {
        return transmitter.getReceiver();
    }
    public void close() {
        transmitter.close();
    }
    public MidiDevice getMidiDevice() {
        return device;
    }
    public Transmitter getTransmitter() {
        return transmitter;
    }
}
