public abstract class AudioFloatInputStream {
    private static class BytaArrayAudioFloatInputStream
            extends AudioFloatInputStream {
        private int pos = 0;
        private int markpos = 0;
        private AudioFloatConverter converter;
        private AudioFormat format;
        private byte[] buffer;
        private int buffer_offset;
        private int buffer_len;
        private int framesize_pc;
        public BytaArrayAudioFloatInputStream(AudioFloatConverter converter,
                byte[] buffer, int offset, int len) {
            this.converter = converter;
            this.format = converter.getFormat();
            this.buffer = buffer;
            this.buffer_offset = offset;
            framesize_pc = format.getFrameSize() / format.getChannels();
            this.buffer_len = len / framesize_pc;
        }
        public AudioFormat getFormat() {
            return format;
        }
        public long getFrameLength() {
            return buffer_len;
        }
        public int read(float[] b, int off, int len) throws IOException {
            if (b == null)
                throw new NullPointerException();
            if (off < 0 || len < 0 || len > b.length - off)
                throw new IndexOutOfBoundsException();
            if (pos >= buffer_len)
                return -1;
            if (len == 0)
                return 0;
            if (pos + len > buffer_len)
                len = buffer_len - pos;
            converter.toFloatArray(buffer, buffer_offset + pos * framesize_pc,
                    b, off, len);
            pos += len;
            return len;
        }
        public long skip(long len) throws IOException {
            if (pos >= buffer_len)
                return -1;
            if (len <= 0)
                return 0;
            if (pos + len > buffer_len)
                len = buffer_len - pos;
            pos += len;
            return len;
        }
        public int available() throws IOException {
            return buffer_len - pos;
        }
        public void close() throws IOException {
        }
        public void mark(int readlimit) {
            markpos = pos;
        }
        public boolean markSupported() {
            return true;
        }
        public void reset() throws IOException {
            pos = markpos;
        }
    }
    private static class DirectAudioFloatInputStream
            extends AudioFloatInputStream {
        private AudioInputStream stream;
        private AudioFloatConverter converter;
        private int framesize_pc; 
        private byte[] buffer;
        public DirectAudioFloatInputStream(AudioInputStream stream) {
            converter = AudioFloatConverter.getConverter(stream.getFormat());
            if (converter == null) {
                AudioFormat format = stream.getFormat();
                AudioFormat newformat;
                AudioFormat[] formats = AudioSystem.getTargetFormats(
                        AudioFormat.Encoding.PCM_SIGNED, format);
                if (formats.length != 0) {
                    newformat = formats[0];
                } else {
                    float samplerate = format.getSampleRate();
                    int samplesizeinbits = format.getSampleSizeInBits();
                    int framesize = format.getFrameSize();
                    float framerate = format.getFrameRate();
                    samplesizeinbits = 16;
                    framesize = format.getChannels() * (samplesizeinbits / 8);
                    framerate = samplerate;
                    newformat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED, samplerate,
                            samplesizeinbits, format.getChannels(), framesize,
                            framerate, false);
                }
                stream = AudioSystem.getAudioInputStream(newformat, stream);
                converter = AudioFloatConverter.getConverter(stream.getFormat());
            }
            framesize_pc = stream.getFormat().getFrameSize()
                    / stream.getFormat().getChannels();
            this.stream = stream;
        }
        public AudioFormat getFormat() {
            return stream.getFormat();
        }
        public long getFrameLength() {
            return stream.getFrameLength();
        }
        public int read(float[] b, int off, int len) throws IOException {
            int b_len = len * framesize_pc;
            if (buffer == null || buffer.length < b_len)
                buffer = new byte[b_len];
            int ret = stream.read(buffer, 0, b_len);
            if (ret == -1)
                return -1;
            converter.toFloatArray(buffer, b, off, ret / framesize_pc);
            return ret / framesize_pc;
        }
        public long skip(long len) throws IOException {
            long b_len = len * framesize_pc;
            long ret = stream.skip(b_len);
            if (ret == -1)
                return -1;
            return ret / framesize_pc;
        }
        public int available() throws IOException {
            return stream.available() / framesize_pc;
        }
        public void close() throws IOException {
            stream.close();
        }
        public void mark(int readlimit) {
            stream.mark(readlimit * framesize_pc);
        }
        public boolean markSupported() {
            return stream.markSupported();
        }
        public void reset() throws IOException {
            stream.reset();
        }
    }
    public static AudioFloatInputStream getInputStream(URL url)
            throws UnsupportedAudioFileException, IOException {
        return new DirectAudioFloatInputStream(AudioSystem
                .getAudioInputStream(url));
    }
    public static AudioFloatInputStream getInputStream(File file)
            throws UnsupportedAudioFileException, IOException {
        return new DirectAudioFloatInputStream(AudioSystem
                .getAudioInputStream(file));
    }
    public static AudioFloatInputStream getInputStream(InputStream stream)
            throws UnsupportedAudioFileException, IOException {
        return new DirectAudioFloatInputStream(AudioSystem
                .getAudioInputStream(stream));
    }
    public static AudioFloatInputStream getInputStream(
            AudioInputStream stream) {
        return new DirectAudioFloatInputStream(stream);
    }
    public static AudioFloatInputStream getInputStream(AudioFormat format,
            byte[] buffer, int offset, int len) {
        AudioFloatConverter converter = AudioFloatConverter
                .getConverter(format);
        if (converter != null)
            return new BytaArrayAudioFloatInputStream(converter, buffer,
                    offset, len);
        InputStream stream = new ByteArrayInputStream(buffer, offset, len);
        long aLen = format.getFrameSize() == AudioSystem.NOT_SPECIFIED
                ? AudioSystem.NOT_SPECIFIED : len / format.getFrameSize();
        AudioInputStream astream = new AudioInputStream(stream, format, aLen);
        return getInputStream(astream);
    }
    public abstract AudioFormat getFormat();
    public abstract long getFrameLength();
    public abstract int read(float[] b, int off, int len) throws IOException;
    public int read(float[] b) throws IOException {
        return read(b, 0, b.length);
    }
    public float read() throws IOException {
        float[] b = new float[1];
        int ret = read(b, 0, 1);
        if (ret == -1 || ret == 0)
            return 0;
        return b[0];
    }
    public abstract long skip(long len) throws IOException;
    public abstract int available() throws IOException;
    public abstract void close() throws IOException;
    public abstract void mark(int readlimit);
    public abstract boolean markSupported();
    public abstract void reset() throws IOException;
}
