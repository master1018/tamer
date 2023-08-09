public class DirectAudioDeviceProvider extends MixerProvider {
    private static DirectAudioDeviceInfo[] infos;
    private static DirectAudioDevice[] devices;
    static {
        Platform.initialize();
    }
    public DirectAudioDeviceProvider() {
        if (Platform.isDirectAudioEnabled()) {
            init();
        } else {
            infos = new DirectAudioDeviceInfo[0];
            devices = new DirectAudioDevice[0];
        }
    }
    private synchronized static void init() {
        int numDevices = nGetNumDevices();
        if (infos == null || infos.length != numDevices) {
            if (Printer.trace) Printer.trace("DirectAudioDeviceProvider: init()");
            infos = new DirectAudioDeviceInfo[numDevices];
            devices = new DirectAudioDevice[numDevices];
            for (int i = 0; i < infos.length; i++) {
                infos[i] = nNewDirectAudioDeviceInfo(i);
            }
            if (Printer.trace) Printer.trace("DirectAudioDeviceProvider: init(): found numDevices: " + numDevices);
        }
    }
    public Mixer.Info[] getMixerInfo() {
        Mixer.Info[] localArray = new Mixer.Info[infos.length];
        System.arraycopy(infos, 0, localArray, 0, infos.length);
        return localArray;
    }
    public Mixer getMixer(Mixer.Info info) {
        if (info == null) {
            for (int i = 0; i < infos.length; i++) {
                Mixer mixer = getDevice(infos[i]);
                if (mixer.getSourceLineInfo().length > 0) {
                    return mixer;
                }
            }
        }
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].equals(info)) {
                return getDevice(infos[i]);
            }
        }
        throw new IllegalArgumentException("Mixer " + info.toString() + " not supported by this provider.");
    }
    private Mixer getDevice(DirectAudioDeviceInfo info) {
        int index = info.getIndex();
        if (devices[index] == null) {
            devices[index] = new DirectAudioDevice(info);
        }
        return devices[index];
    }
    static class DirectAudioDeviceInfo extends Mixer.Info {
        private int index;
        private int maxSimulLines;
        private int deviceID;
        private DirectAudioDeviceInfo(int index, int deviceID, int maxSimulLines,
                                      String name, String vendor,
                                      String description, String version) {
            super(name, vendor, "Direct Audio Device: "+description, version);
            this.index = index;
            this.maxSimulLines = maxSimulLines;
            this.deviceID = deviceID;
        }
        int getIndex() {
            return index;
        }
        int getMaxSimulLines() {
            return maxSimulLines;
        }
        int getDeviceID() {
            return deviceID;
        }
    } 
    private static native int nGetNumDevices();
    private static native DirectAudioDeviceInfo nNewDirectAudioDeviceInfo(int deviceIndex);
}
