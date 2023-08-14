public abstract class MidiDeviceProvider {
    public abstract MidiDevice getDevice(MidiDevice.Info info);
    public abstract MidiDevice.Info[] getDeviceInfo();
    public boolean isDeviceSupported(MidiDevice.Info info) {
        MidiDevice.Info[] devices = getDeviceInfo();
        for (Info element : devices) {
            if (info.equals(element)) {
                return true;
            }
        }
        return false;
    }
}
