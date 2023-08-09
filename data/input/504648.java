public abstract class AudioFileWriter {
    public abstract AudioFileFormat.Type[] getAudioFileTypes();
    public abstract AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream stream);
    public boolean isFileTypeSupported(AudioFileFormat.Type fileType) {
        AudioFileFormat.Type[] supported = getAudioFileTypes();
        for (Type element : supported) {
            if (fileType.equals(element)) {
                return true;
            }
        }
        return false;
    }
    public boolean isFileTypeSupported(AudioFileFormat.Type fileType, AudioInputStream stream) {
        AudioFileFormat.Type[] supported = getAudioFileTypes(stream);
        for (Type element : supported) {
            if (fileType.equals(element)) {
                return true;
            }
        }
        return false;
    }
    public abstract int write(AudioInputStream stream,
            AudioFileFormat.Type fileType, File out) throws IOException;
    public abstract int write(AudioInputStream stream,
            AudioFileFormat.Type fileType, OutputStream out) throws IOException;
}
