public class DLSSoundbankReader extends SoundbankReader {
    public Soundbank getSoundbank(URL url)
            throws InvalidMidiDataException, IOException {
        try {
            return new DLSSoundbank(url);
        } catch (RIFFInvalidFormatException e) {
            return null;
        } catch(IOException ioe) {
            return null;
        }
    }
    public Soundbank getSoundbank(InputStream stream)
            throws InvalidMidiDataException, IOException {
        try {
            stream.mark(512);
            return new DLSSoundbank(stream);
        } catch (RIFFInvalidFormatException e) {
            stream.reset();
            return null;
        }
    }
    public Soundbank getSoundbank(File file)
            throws InvalidMidiDataException, IOException {
        try {
            return new DLSSoundbank(file);
        } catch (RIFFInvalidFormatException e) {
            return null;
        }
    }
}
