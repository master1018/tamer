public class SoftProvider extends MidiDeviceProvider {
    protected final static Info softinfo = SoftSynthesizer.info;
    private static Info[] softinfos = {softinfo};
    public MidiDevice.Info[] getDeviceInfo() {
        return Arrays.copyOf(softinfos, softinfos.length);
    }
    public MidiDevice getDevice(MidiDevice.Info info) {
        if (info == softinfo) {
            return new SoftSynthesizer();
        }
        return null;
    }
}
