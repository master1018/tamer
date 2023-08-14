public abstract class MidiFileWriter {
    public abstract int[] getMidiFileTypes();
    public abstract int[] getMidiFileTypes(Sequence sequence);
    public boolean isFileTypeSupported(int fileType) {
        int types[] = getMidiFileTypes();
        for(int i=0; i<types.length; i++) {
            if( fileType == types[i] ) {
                return true;
            }
        }
        return false;
    }
    public boolean isFileTypeSupported(int fileType, Sequence sequence) {
        int types[] = getMidiFileTypes( sequence );
        for(int i=0; i<types.length; i++) {
            if( fileType == types[i] ) {
                return true;
            }
        }
        return false;
    }
    public abstract int write(Sequence in, int fileType, OutputStream out) throws IOException;
    public abstract int write(Sequence in, int fileType, File out) throws IOException;
}
