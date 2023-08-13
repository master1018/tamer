public abstract class MidiFileReader {
    public abstract MidiFileFormat getMidiFileFormat(File file)
            throws InvalidMidiDataException, IOException;
    public abstract MidiFileFormat getMidiFileFormat(InputStream stream)
            throws InvalidMidiDataException, IOException;
    public abstract MidiFileFormat getMidiFileFormat(URL url)
            throws InvalidMidiDataException, IOException;
    public abstract Sequence getSequence(File file)
            throws InvalidMidiDataException, IOException;
    public abstract Sequence getSequence(InputStream stream)
            throws InvalidMidiDataException, IOException;
    public abstract Sequence getSequence(URL url)
            throws InvalidMidiDataException, IOException;
}
