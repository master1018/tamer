public class AndroidSequencer implements Sequencer {
    private class Info extends MidiDevice.Info {
        public Info() {
            super("Android Sequencer", "Android Sequencer", "The Android Project", "1.0");        
        }
    }
    private MediaPlayer player;
    private AndroidSequence sequence;
    public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
        throw new UnsupportedOperationException();
    }
    public boolean addMetaEventListener(MetaEventListener listener) {
        throw new UnsupportedOperationException();
    }
    public int getLoopCount() {
        throw new UnsupportedOperationException();
    }
    public long getLoopEndPoint() {
        throw new UnsupportedOperationException();
    }
    public long getLoopStartPoint() {
        throw new UnsupportedOperationException();
    }
    public SyncMode getMasterSyncMode() {
        throw new UnsupportedOperationException();
    }
    public SyncMode[] getMasterSyncModes() {
        throw new UnsupportedOperationException();
    }
    public long getMicrosecondLength() {
        throw new UnsupportedOperationException();
    }
    public long getMicrosecondPosition() {
        throw new UnsupportedOperationException();
    }
    public Sequence getSequence() {
        return sequence;
    }
    public SyncMode getSlaveSyncMode() {
        throw new UnsupportedOperationException();
    }
    public SyncMode[] getSlaveSyncModes() {
        throw new UnsupportedOperationException();
    }
    public float getTempoFactor() {
        throw new UnsupportedOperationException();
    }
    public float getTempoInBPM() {
        throw new UnsupportedOperationException();
    }
    public float getTempoInMPQ() {
        throw new UnsupportedOperationException();
    }
    public long getTickLength() {
        throw new UnsupportedOperationException();
    }
    public long getTickPosition() {
        throw new UnsupportedOperationException();
    }
    public boolean getTrackMute(int track) {
        throw new UnsupportedOperationException();
    }
    public boolean getTrackSolo(int track) {
        throw new UnsupportedOperationException();
    }
    public boolean isRecording() {
        return false;
    }
    public boolean isRunning() {
        return player != null && player.isPlaying();
    }
    public void recordDisable(Track track) {
        throw new UnsupportedOperationException();
    }
    public void recordEnable(Track track, int channel) {
        throw new UnsupportedOperationException();
    }
    public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
        throw new UnsupportedOperationException();
    }
    public void removeMetaEventListener(MetaEventListener listener) {
        throw new UnsupportedOperationException();
    }
    public void setLoopCount(int count) {
        throw new UnsupportedOperationException();
    }
    public void setLoopEndPoint(long tick) {
        throw new UnsupportedOperationException();
    }
    public void setLoopStartPoint(long tick) {
        throw new UnsupportedOperationException();
    }
    public void setMasterSyncMode(SyncMode sync) {
        throw new UnsupportedOperationException();
    }
    public void setMicrosecondPosition(long microseconds) {
        throw new UnsupportedOperationException();
    }
    public void setSequence(InputStream stream) throws IOException, InvalidMidiDataException {
        setSequence(new AndroidMidiFileReader().getSequence(stream));
    }
    public void setSequence(Sequence sequence) throws InvalidMidiDataException {
        if (!(sequence instanceof AndroidSequence)) {
            throw new InvalidMidiDataException("Sequence must be an AndroidSequence");
        }
        if (isRunning()) {
            stop();
        }
        this.sequence = (AndroidSequence)sequence;
    }
    public void setSlaveSyncMode(SyncMode sync) {
        throw new UnsupportedOperationException();
    }
    public void setTempoFactor(float factor) {
        throw new UnsupportedOperationException();
    }
    public void setTempoInBPM(float bpm) {
        throw new UnsupportedOperationException();
    }
    public void setTempoInMPQ(float mpq) {
        throw new UnsupportedOperationException();
    }
    public void setTickPosition(long tick) {
        throw new UnsupportedOperationException();
    }
    public void setTrackMute(int track, boolean mute) {
        throw new UnsupportedOperationException();
    }
    public void setTrackSolo(int track, boolean solo) {
        throw new UnsupportedOperationException();
    }
    public void start() {
        if (!isOpen()) {
            throw new IllegalStateException("Sequencer must be open");
        }
        if (sequence == null) {
            throw new IllegalStateException("Need a Sequence to play");
        }
        if (!isRunning()) {
            try {
                String s = this.sequence.getURL().toExternalForm();
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
    public void startRecording() {
        throw new UnsupportedOperationException();
    }
    public void stop() {
        if (!isOpen()) {
            throw new IllegalStateException("Sequencer must be open");
        }
        if (isRunning()) {
            player.stop();
        }
    }
    public void stopRecording() {
        throw new UnsupportedOperationException();
    }
    public void close() {
        if (isOpen()) {
            stop();
            player = null;
        }
    }
    public Info getDeviceInfo() {
        return new Info();
    }
    public int getMaxReceivers() {
        return 0;
    }
    public int getMaxTransmitters() {
        return 0;
    }
    public Receiver getReceiver() throws MidiUnavailableException {
        throw new MidiUnavailableException("No receiver available");
    }
    public List<Receiver> getReceivers() {
        return new ArrayList<Receiver>();
    }
    public Transmitter getTransmitter() throws MidiUnavailableException {
        throw new MidiUnavailableException("No receiver available");
    }
    public List<Transmitter> getTransmitters() {
        return new ArrayList<Transmitter>();
    }
    public boolean isOpen() {
        return player != null;
    }
    public void open() throws MidiUnavailableException {
        try {
            player = new MediaPlayer();
        } catch (Exception ex) {
            throw new MidiUnavailableException(ex.toString());
        }
    }
}
