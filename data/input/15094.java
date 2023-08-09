public abstract class AudioFileReader {
    public abstract AudioFileFormat getAudioFileFormat(InputStream stream) throws UnsupportedAudioFileException, IOException;
    public abstract AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException;
    public abstract AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException;
    public abstract AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException;
    public abstract AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException;
    public abstract AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException;
}
