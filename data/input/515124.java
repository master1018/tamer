public abstract class MidiFileWriter {
    public abstract int[] getMidiFileTypes();
    public abstract int[] getMidiFileTypes(Sequence sequence);
    public boolean isFileTypeSupported(int fileType) {
        int[] supported = getMidiFileTypes();
        for (int element : supported) {
            if (fileType == element) {
                return true;
            }
        }
        return false;
    }
    public boolean isFileTypeSupported(int fileType, Sequence sequence) {
        int[] supported = getMidiFileTypes(sequence);
        for (int element : supported) {
            if (fileType == element) {
                return true;
            }
        }
        return false;
    }
    public abstract int write(Sequence in, int fileType, File out)
            throws IOException;
    public abstract int write(Sequence in, int fileType, OutputStream out)
            throws IOException;
}
