public class AudioInputStream extends InputStream {
    protected AudioFormat format;
    protected long frameLength;
    protected long framePos;
    protected int frameSize;
    private InputStream stream;
    private TargetDataLine line;
    private byte[] oneByte = new byte[1];
    private long marketFramePos;
    public AudioInputStream(InputStream stream, AudioFormat format, long length) {
        this.stream = stream;
        this.format = format;
        this.frameLength = length;
        this.frameSize = format.getFrameSize();
    }
    public AudioInputStream(TargetDataLine line) {
        this.line = line;
        this.format = line.getFormat();
        this.frameLength = AudioSystem.NOT_SPECIFIED; 
        this.frameSize = this.format.getFrameSize();
    }
    public AudioFormat getFormat() {
        return format;
    }
    public long getFrameLength() {
        return frameLength;
    }
    public int read() throws IOException {
        if (frameSize != 1) {
            throw new IOException(Messages.getString("sound.0C")); 
        }
        int res;
        if (stream != null) { 
            if (framePos == frameLength) {
                return 0;
            }
            res = stream.read();
            if (res == -1) {
                return -1;
            }
            framePos += 1;
            return res;
        } else { 
            if (line.read(oneByte, 0, 1) == 0) {
                return -1;
            }
            framePos = line.getLongFramePosition();
            return oneByte[0];
        }
    }
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    public int read(byte[] b, int off, int len) throws IOException {
        int l = Math.min(len, (int) ((frameLength - framePos) * frameSize));
        l = l - (l % frameSize);
        if (l == 0) {
            return 0;
        }
        int res;
        if (stream != null) { 
            res = stream.read(b, off, l);
            if (res == -1) {
                return -1;
            }
            framePos = framePos + res / frameSize;
            return res;
        } else { 
            res = line.read(b, off, l);
            if (res == 0) {
                return -1;
            }
            framePos = line.getLongFramePosition();
            return res;
        }
    }
    public long skip(long n) throws IOException {
        if (n < frameSize) {
            return 0;
        }
        byte[] skipBuf = new byte[frameSize];
        long skipped = 0;
        while (skipped < n) {
            int read = read(skipBuf, 0, frameSize);
            if (read == -1) {
                return skipped;
            }
            skipped += read;
            if (n - skipped < frameSize) {
                return skipped;
            }
        }
        return skipped;
    }
    public int available() throws IOException {
        if (stream != null) { 
            return Math.min(stream.available(),
                    (int)((frameLength - framePos) * frameSize));
        } else { 
            return line.available();
        }
    }
    public void close() throws IOException {
        if (stream != null) { 
            stream.close();
        } else { 
            line.close();
        }
    }
    public void mark(int readlimit) {
        if (stream != null) { 
            stream.mark(readlimit);
            marketFramePos = framePos;
        } else { 
        }
    }
    public void reset() throws IOException {
        if (stream != null) { 
            stream.reset();
            framePos = marketFramePos;
        } else { 
        }
    }
    public boolean markSupported() {
        if (stream != null) { 
            return stream.markSupported();
        } else { 
            return false;
        }
    }
}
