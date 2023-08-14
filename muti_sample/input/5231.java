public class RealTimeSequencerProvider extends MidiDeviceProvider {
    public MidiDevice.Info[] getDeviceInfo() {
        MidiDevice.Info[] localArray = { RealTimeSequencer.info };
        return localArray;
    }
    public MidiDevice getDevice(MidiDevice.Info info) {
        if ((info != null) && (!info.equals(RealTimeSequencer.info))) {
            return null;
        }
        try {
            return new RealTimeSequencer();
        } catch (MidiUnavailableException e) {
            return null;
        }
    }
}
