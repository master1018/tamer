public class MidiInDeviceProvider extends AbstractMidiDeviceProvider {
    static Info[] infos = null;
    static MidiDevice[] devices = null;
    private static boolean enabled;
    static {
        Platform.initialize();
        enabled = Platform.isMidiIOEnabled();
    }
    public MidiInDeviceProvider() {
        if (Printer.trace) Printer.trace("MidiInDeviceProvider: constructor");
    }
    AbstractMidiDeviceProvider.Info createInfo(int index) {
        if (!enabled) {
            return null;
        }
        return new MidiInDeviceInfo(index, MidiInDeviceProvider.class);
    }
    MidiDevice createDevice(AbstractMidiDeviceProvider.Info info) {
        if (enabled && (info instanceof MidiInDeviceInfo)) {
            return new MidiInDevice(info);
        }
        return null;
    }
    int getNumDevices() {
        if (!enabled) {
            if (Printer.debug)Printer.debug("MidiInDevice not enabled, returning 0 devices");
            return 0;
        }
        int numDevices = nGetNumDevices();
        if (Printer.debug)Printer.debug("MidiInDeviceProvider.getNumDevices(): devices: " + numDevices);
        return numDevices;
    }
    MidiDevice[] getDeviceCache() { return devices; }
    void setDeviceCache(MidiDevice[] devices) { this.devices = devices; }
    Info[] getInfoCache() { return infos; }
    void setInfoCache(Info[] infos) { this.infos = infos; }
    static class MidiInDeviceInfo extends AbstractMidiDeviceProvider.Info {
        private Class providerClass;
        private MidiInDeviceInfo(int index, Class providerClass) {
            super(nGetName(index), nGetVendor(index), nGetDescription(index), nGetVersion(index), index);
            this.providerClass = providerClass;
        }
    } 
    private static native int nGetNumDevices();
    private static native String nGetName(int index);
    private static native String nGetVendor(int index);
    private static native String nGetDescription(int index);
    private static native String nGetVersion(int index);
}
