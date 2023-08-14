public abstract class SoundbankReader {
    public abstract Soundbank getSoundbank(URL url) throws InvalidMidiDataException, IOException;
    public abstract Soundbank getSoundbank(InputStream stream) throws InvalidMidiDataException, IOException;
    public abstract Soundbank getSoundbank(File file) throws InvalidMidiDataException, IOException;
}
