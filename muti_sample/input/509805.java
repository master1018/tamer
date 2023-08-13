public class AndroidMidiFileReader extends MidiFileReader {
    @Override
    public MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException, IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public MidiFileFormat getMidiFileFormat(InputStream stream) throws InvalidMidiDataException,
            IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException {
        throw new UnsupportedOperationException();
    }
    @Override
    public Sequence getSequence(File file) throws InvalidMidiDataException, IOException {
        return new AndroidSequence(file.toURL());
    }
    @Override
    public Sequence getSequence(InputStream stream) throws InvalidMidiDataException, IOException {
        File file = File.createTempFile("javax.sound.midi-", null);
        file.deleteOnExit();
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buffer = new byte[1024];
        int count = stream.read(buffer);
        while (count >= 0) {
            out.write(buffer, 0, count);
            count = stream.read(buffer);
        }
        out.flush();
        out.close();
        return getSequence(file);
    }
    @Override
    public Sequence getSequence(URL url) throws InvalidMidiDataException, IOException {
        return new AndroidSequence(url);
    }
}
