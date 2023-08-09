public abstract class MidiDeviceProvider {
    public boolean isDeviceSupported(MidiDevice.Info info) {
        MidiDevice.Info infos[] = getDeviceInfo();
        for(int i=0; i<infos.length; i++) {
            if( info.equals( infos[i] ) ) {
                return true;
            }
        }
        return false;
    }
    public abstract MidiDevice.Info[] getDeviceInfo();
    public abstract MidiDevice getDevice(MidiDevice.Info info);
}
