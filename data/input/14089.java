public class MidiEvent {
    private final MidiMessage message;
    private long tick;
    public MidiEvent(MidiMessage message, long tick) {
        this.message = message;
        this.tick = tick;
    }
    public MidiMessage getMessage() {
        return message;
    }
    public void setTick(long tick) {
        this.tick = tick;
    }
    public long getTick() {
        return tick;
    }
}
