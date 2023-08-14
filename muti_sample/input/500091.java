public abstract class MixerProvider {
    public abstract Mixer getMixer(Mixer.Info info);
    public abstract Mixer.Info[] getMixerInfo();
    public boolean isMixerSupported(Mixer.Info info) {
        Mixer.Info[] devices = getMixerInfo();
        for (Info element : devices) {
            if (info.equals(element)) {
                return true;
            }
        }
        return false;
    }
}
