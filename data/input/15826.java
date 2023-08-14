public class MidiDeviceReceiverEnvelope implements MidiDeviceReceiver {
    private final MidiDevice device;
    private final Receiver receiver;
    public MidiDeviceReceiverEnvelope(MidiDevice device, Receiver receiver) {
        if (device == null || receiver == null) {
            throw new NullPointerException();
        }
        this.device = device;
        this.receiver = receiver;
    }
    public void close() {
        receiver.close();
    }
    public void send(MidiMessage message, long timeStamp) {
        receiver.send(message, timeStamp);
    }
    public MidiDevice getMidiDevice() {
        return device;
    }
    public Receiver getReceiver() {
        return receiver;
    }
}
