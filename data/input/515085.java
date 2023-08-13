public class AndroidClip implements Clip {
    private MediaPlayer player;
    private AndroidAudioInputStream stream;
    public int getFrameLength() {
        throw new UnsupportedOperationException();
    }
    public long getMicrosecondLength() {
        throw new UnsupportedOperationException();
    }
    public void loop(int count) {
        throw new UnsupportedOperationException();
    }
    public void open(AudioFormat format, byte[] data, int offset, int bufferSize)
            throws LineUnavailableException {
        InputStream stream = new ByteArrayInputStream(data, offset, bufferSize);
        open();
        try {
            this.stream = (AndroidAudioInputStream)(new AndroidAudioFileReader().getAudioInputStream(stream));
        } catch (Exception ex) {
            throw new LineUnavailableException(ex.toString());
        }
    }
    public void open(AudioInputStream stream) throws LineUnavailableException, IOException {
        open();
        if (!(stream instanceof AndroidAudioInputStream)) {
            try {
                stream = new AndroidAudioFileReader().getAudioInputStream(stream);
            } catch (Exception ex) {
                throw new LineUnavailableException(ex.toString());
            }
        }
        this.stream = (AndroidAudioInputStream)stream;
    }
    public void setFramePosition(int frames) {
        throw new UnsupportedOperationException();
    }
    public void setLoopPoints(int start, int end) {
        throw new UnsupportedOperationException();
    }
    public void setMicrosecondPosition(long microseconds) {
        if (!isOpen()) {
            throw new IllegalStateException("Clip must be open");
        }
        player.seekTo((int)(microseconds / 1000));
    }
    public int available() {
        throw new UnsupportedOperationException();
    }
    public void drain() {
    }
    public void flush() {
    }
    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }
    public AudioFormat getFormat() {
        throw new UnsupportedOperationException();
    }
    public int getFramePosition() {
        throw new UnsupportedOperationException();
    }
    public float getLevel() {
        throw new UnsupportedOperationException();
    }
    public long getLongFramePosition() {
        throw new UnsupportedOperationException();
    }
    public long getMicrosecondPosition() {
        if (isOpen()) {
            return player.getCurrentPosition() * 1000;
        } else {
            return 0;
        }
    }
    public boolean isActive() {
        return false;
    }
    public boolean isRunning() {
        return player != null && player.isPlaying();
    }
    public void start() {
        if (!isOpen()) {
            throw new IllegalStateException("Clip must be open");
        }
        if (stream == null) {
            throw new IllegalStateException("Need an AudioInputStream to play");
        }
        if (!isRunning()) {
            try {
                String s = this.stream.getURL().toExternalForm();
                if (s.startsWith("file:")) {
                    s = s.substring(5);
                }
                player.setDataSource(s);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepare();
            } catch (IOException ex) {
                throw new RuntimeException(ex.toString());
            }
            player.start();
        }
    }
    public void stop() {
        if (!isOpen()) {
            throw new IllegalStateException("Clip must be open");
        }
        if (isRunning()) {
            player.stop();
        }
    }
    public void addLineListener(LineListener listener) {
        throw new UnsupportedOperationException();
    }
    public void close() {
        if (isOpen()) {
            stop();
            player = null;
        }
    }
    public Control getControl(Type control) {
        throw new IllegalArgumentException("No controls available");
    }
    public Control[] getControls() {
        return new Control[0];
    }
    public javax.sound.sampled.Line.Info getLineInfo() {
        return new Line.Info(this.getClass());
    }
    public boolean isControlSupported(Type control) {
        return false;
    }
    public boolean isOpen() {
        return player != null;
    }
    public void open() throws LineUnavailableException {
        try {
            player = new MediaPlayer();
        } catch (Exception ex) {
            throw new LineUnavailableException(ex.toString());
        }
    }
    public void removeLineListener(LineListener listener) {
        throw new UnsupportedOperationException();
    }
}
