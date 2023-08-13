public class MidiOutDeviceProvider extends AbstractMidiDeviceProvider {
    static Info[] infos = null;
    static MidiDevice[] devices = null;
    private static boolean enabled;
    static {
        Platform.initialize();
        enabled = Platform.isMidiIOEnabled();
    }
    public MidiOutDeviceProvider() {
        if (Printer.trace) Printer.trace("MidiOutDeviceProvider: constructor");
    }
    AbstractMidiDeviceProvider.Info createInfo(int index) {
        if (!enabled) {
            return null;
        }
        return new MidiOutDeviceInfo(index, MidiOutDeviceProvider.class);
    }
    MidiDevice createDevice(AbstractMidiDeviceProvider.Info info) {
        if (enabled && (info instanceof MidiOutDeviceInfo)) {
            return new MidiOutDevice(info);
        }
        return null;
    }
    int getNumDevices() {
        if (!enabled) {
            if (Printer.debug)Printer.debug("MidiOutDevice not enabled, returning 0 devices");
            return 0;
        }
        return nGetNumDevices();
    }
    MidiDevice[] getDeviceCache() { return devices; }
    void setDeviceCache(MidiDevice[] devices) { this.devices = devices; }
    Info[] getInfoCache() { return infos; }
    void setInfoCache(Info[] infos) { this.infos = infos; }
    static class MidiOutDeviceInfo extends AbstractMidiDeviceProvider.Info {
        private Class providerClass;
        private MidiOutDeviceInfo(int index, Class providerClass) {
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
