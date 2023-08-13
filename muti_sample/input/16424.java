public class WaveFloatFileWriter extends AudioFileWriter {
    public Type[] getAudioFileTypes() {
        return new Type[] { Type.WAVE };
    }
    public Type[] getAudioFileTypes(AudioInputStream stream) {
        if (!stream.getFormat().getEncoding().equals(Encoding.PCM_FLOAT))
            return new Type[0];
        return new Type[] { Type.WAVE };
    }
    private void checkFormat(AudioFileFormat.Type type, AudioInputStream stream) {
        if (!Type.WAVE.equals(type))
            throw new IllegalArgumentException("File type " + type
                    + " not supported.");
        if (!stream.getFormat().getEncoding().equals(Encoding.PCM_FLOAT))
            throw new IllegalArgumentException("File format "
                    + stream.getFormat() + " not supported.");
    }
    public void write(AudioInputStream stream, RIFFWriter writer)
            throws IOException {
        RIFFWriter fmt_chunk = writer.writeChunk("fmt ");
        AudioFormat format = stream.getFormat();
        fmt_chunk.writeUnsignedShort(3); 
        fmt_chunk.writeUnsignedShort(format.getChannels());
        fmt_chunk.writeUnsignedInt((int) format.getSampleRate());
        fmt_chunk.writeUnsignedInt(((int) format.getFrameRate())
                * format.getFrameSize());
        fmt_chunk.writeUnsignedShort(format.getFrameSize());
        fmt_chunk.writeUnsignedShort(format.getSampleSizeInBits());
        fmt_chunk.close();
        RIFFWriter data_chunk = writer.writeChunk("data");
        byte[] buff = new byte[1024];
        int len;
        while ((len = stream.read(buff, 0, buff.length)) != -1)
            data_chunk.write(buff, 0, len);
        data_chunk.close();
    }
    private static class NoCloseOutputStream extends OutputStream {
        OutputStream out;
        public NoCloseOutputStream(OutputStream out) {
            this.out = out;
        }
        public void write(int b) throws IOException {
            out.write(b);
        }
        public void flush() throws IOException {
            out.flush();
        }
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
        public void write(byte[] b) throws IOException {
            out.write(b);
        }
    }
    private AudioInputStream toLittleEndian(AudioInputStream ais) {
        AudioFormat format = ais.getFormat();
        AudioFormat targetFormat = new AudioFormat(format.getEncoding(), format
                .getSampleRate(), format.getSampleSizeInBits(), format
                .getChannels(), format.getFrameSize(), format.getFrameRate(),
                false);
        return AudioSystem.getAudioInputStream(targetFormat, ais);
    }
    public int write(AudioInputStream stream, Type fileType, OutputStream out)
            throws IOException {
        checkFormat(fileType, stream);
        if (stream.getFormat().isBigEndian())
            stream = toLittleEndian(stream);
        RIFFWriter writer = new RIFFWriter(new NoCloseOutputStream(out), "WAVE");
        write(stream, writer);
        int fpointer = (int) writer.getFilePointer();
        writer.close();
        return fpointer;
    }
    public int write(AudioInputStream stream, Type fileType, File out)
            throws IOException {
        checkFormat(fileType, stream);
        if (stream.getFormat().isBigEndian())
            stream = toLittleEndian(stream);
        RIFFWriter writer = new RIFFWriter(out, "WAVE");
        write(stream, writer);
        int fpointer = (int) writer.getFilePointer();
        writer.close();
        return fpointer;
    }
}
