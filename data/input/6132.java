public class JavaSoundAudioClip implements AudioClip, MetaEventListener, LineListener {
    private static final boolean DEBUG = false;
    private static final int BUFFER_SIZE = 16384; 
    private long lastPlayCall = 0;
    private static final int MINIMUM_PLAY_DELAY = 30;
    private byte loadedAudio[] = null;
    private int loadedAudioByteLength = 0;
    private AudioFormat loadedAudioFormat = null;
    private AutoClosingClip clip = null;
    private boolean clipLooping = false;
    private DataPusher datapusher = null;
    private Sequencer sequencer = null;
    private Sequence sequence = null;
    private boolean sequencerloop = false;
    private final static long CLIP_THRESHOLD = 1048576;
    private final static int STREAM_BUFFER_SIZE = 1024;
    public JavaSoundAudioClip(InputStream in) throws IOException {
        if (DEBUG || Printer.debug)Printer.debug("JavaSoundAudioClip.<init>");
        BufferedInputStream bis = new BufferedInputStream(in, STREAM_BUFFER_SIZE);
        bis.mark(STREAM_BUFFER_SIZE);
        boolean success = false;
        try {
            AudioInputStream as = AudioSystem.getAudioInputStream(bis);
            success = loadAudioData(as);
            if (success) {
                success = false;
                if (loadedAudioByteLength < CLIP_THRESHOLD) {
                    success = createClip();
                }
                if (!success) {
                    success = createSourceDataLine();
                }
            }
        } catch (UnsupportedAudioFileException e) {
            try {
                MidiFileFormat mff = MidiSystem.getMidiFileFormat(bis);
                success = createSequencer(bis);
            } catch (InvalidMidiDataException e1) {
                success = false;
            }
        }
        if (!success) {
            throw new IOException("Unable to create AudioClip from input stream");
        }
    }
    public synchronized void play() {
        startImpl(false);
    }
    public synchronized void loop() {
        startImpl(true);
    }
    private synchronized void startImpl(boolean loop) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastPlayCall;
        if (diff < MINIMUM_PLAY_DELAY) {
            if (DEBUG || Printer.debug) Printer.debug("JavaSoundAudioClip.startImpl(loop="+loop+"): abort - too rapdly");
            return;
        }
        lastPlayCall = currentTime;
        if (DEBUG || Printer.debug) Printer.debug("JavaSoundAudioClip.startImpl(loop="+loop+")");
        try {
            if (clip != null) {
                if (!clip.isOpen()) {
                    if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip: clip.open()");
                    clip.open(loadedAudioFormat, loadedAudio, 0, loadedAudioByteLength);
                } else {
                    if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip: clip.flush()");
                    clip.flush();
                    if (loop != clipLooping) {
                        if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip: clip.stop()");
                        clip.stop();
                    }
                }
                clip.setFramePosition(0);
                if (loop) {
                    if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip: clip.loop()");
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip: clip.start()");
                    clip.start();
                }
                clipLooping = loop;
                if (DEBUG || Printer.debug)Printer.debug("Clip should be playing/looping");
            } else if (datapusher != null ) {
                datapusher.start(loop);
                if (DEBUG || Printer.debug)Printer.debug("Stream should be playing/looping");
            } else if (sequencer != null) {
                sequencerloop = loop;
                if (sequencer.isRunning()) {
                    sequencer.setMicrosecondPosition(0);
                }
                if (!sequencer.isOpen()) {
                    try {
                        sequencer.open();
                        sequencer.setSequence(sequence);
                    } catch (InvalidMidiDataException e1) {
                        if (DEBUG || Printer.err)e1.printStackTrace();
                    } catch (MidiUnavailableException e2) {
                        if (DEBUG || Printer.err)e2.printStackTrace();
                    }
                }
                sequencer.addMetaEventListener(this);
                try {
                    sequencer.start();
                } catch (Exception e) {
                    if (DEBUG || Printer.err) e.printStackTrace();
                }
                if (DEBUG || Printer.debug)Printer.debug("Sequencer should be playing/looping");
            }
        } catch (Exception e) {
            if (DEBUG || Printer.err)e.printStackTrace();
        }
    }
    public synchronized void stop() {
        if (DEBUG || Printer.debug)Printer.debug("JavaSoundAudioClip->stop()");
        lastPlayCall = 0;
        if (clip != null) {
            try {
                if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip: clip.flush()");
                clip.flush();
            } catch (Exception e1) {
                if (Printer.err) e1.printStackTrace();
            }
            try {
                if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip: clip.stop()");
                clip.stop();
            } catch (Exception e2) {
                if (Printer.err) e2.printStackTrace();
            }
            if (DEBUG || Printer.debug)Printer.debug("Clip should be stopped");
        } else if (datapusher != null) {
            datapusher.stop();
            if (DEBUG || Printer.debug)Printer.debug("Stream should be stopped");
        } else if (sequencer != null) {
            try {
                sequencerloop = false;
                sequencer.addMetaEventListener(this);
                sequencer.stop();
            } catch (Exception e3) {
                if (Printer.err) e3.printStackTrace();
            }
            try {
                sequencer.close();
            } catch (Exception e4) {
                if (Printer.err) e4.printStackTrace();
            }
            if (DEBUG || Printer.debug)Printer.debug("Sequencer should be stopped");
        }
    }
    public synchronized void update(LineEvent event) {
        if (DEBUG || Printer.debug) Printer.debug("line event received: "+event);
    }
    public synchronized void meta( MetaMessage message ) {
        if (DEBUG || Printer.debug)Printer.debug("META EVENT RECEIVED!!!!! ");
        if( message.getType() == 47 ) {
            if (sequencerloop){
                sequencer.setMicrosecondPosition(0);
                loop();
            } else {
                stop();
            }
        }
    }
    public String toString() {
        return getClass().toString();
    }
    protected void finalize() {
        if (clip != null) {
            if (DEBUG || Printer.trace)Printer.trace("JavaSoundAudioClip.finalize: clip.close()");
            clip.close();
        }
        if (datapusher != null) {
            datapusher.close();
        }
        if (sequencer != null) {
            sequencer.close();
        }
    }
    private boolean loadAudioData(AudioInputStream as)  throws IOException, UnsupportedAudioFileException {
        if (DEBUG || Printer.debug)Printer.debug("JavaSoundAudioClip->openAsClip()");
        as = Toolkit.getPCMConvertedAudioInputStream(as);
        if (as == null) {
            return false;
        }
        loadedAudioFormat = as.getFormat();
        long frameLen = as.getFrameLength();
        int frameSize = loadedAudioFormat.getFrameSize();
        long byteLen = AudioSystem.NOT_SPECIFIED;
        if (frameLen != AudioSystem.NOT_SPECIFIED
            && frameLen > 0
            && frameSize != AudioSystem.NOT_SPECIFIED
            && frameSize > 0) {
            byteLen = frameLen * frameSize;
        }
        if (byteLen != AudioSystem.NOT_SPECIFIED) {
            readStream(as, byteLen);
        } else {
            readStream(as);
        }
        return true;
    }
    private void readStream(AudioInputStream as, long byteLen) throws IOException {
        int intLen;
        if (byteLen > 2147483647) {
            intLen = 2147483647;
        } else {
            intLen = (int) byteLen;
        }
        loadedAudio = new byte[intLen];
        loadedAudioByteLength = 0;
        while (true) {
            int bytesRead = as.read(loadedAudio, loadedAudioByteLength, intLen - loadedAudioByteLength);
            if (bytesRead <= 0) {
                as.close();
                break;
            }
            loadedAudioByteLength += bytesRead;
        }
    }
    private void readStream(AudioInputStream as) throws IOException {
        DirectBAOS baos = new DirectBAOS();
        byte buffer[] = new byte[16384];
        int bytesRead = 0;
        int totalBytesRead = 0;
        while( true ) {
            bytesRead = as.read(buffer, 0, buffer.length);
            if (bytesRead <= 0) {
                as.close();
                break;
            }
            totalBytesRead += bytesRead;
            baos.write(buffer, 0, bytesRead);
        }
        loadedAudio = baos.getInternalBuffer();
        loadedAudioByteLength = totalBytesRead;
    }
    private boolean createClip() {
        if (DEBUG || Printer.debug)Printer.debug("JavaSoundAudioClip.createClip()");
        try {
            DataLine.Info info = new DataLine.Info(Clip.class, loadedAudioFormat);
            if (!(AudioSystem.isLineSupported(info)) ) {
                if (DEBUG || Printer.err)Printer.err("Clip not supported: "+loadedAudioFormat);
                return false;
            }
            Object line = AudioSystem.getLine(info);
            if (!(line instanceof AutoClosingClip)) {
                if (DEBUG || Printer.err)Printer.err("Clip is not auto closing!"+clip);
                return false;
            }
            clip = (AutoClosingClip) line;
            clip.setAutoClosing(true);
            if (DEBUG || Printer.debug) clip.addLineListener(this);
        } catch (Exception e) {
            if (DEBUG || Printer.err)e.printStackTrace();
            return false;
        }
        if (clip==null) {
            return false;
        }
        if (DEBUG || Printer.debug)Printer.debug("Loaded clip.");
        return true;
    }
    private boolean createSourceDataLine() {
        if (DEBUG || Printer.debug)Printer.debug("JavaSoundAudioClip.createSourceDataLine()");
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, loadedAudioFormat);
            if (!(AudioSystem.isLineSupported(info)) ) {
                if (DEBUG || Printer.err)Printer.err("Line not supported: "+loadedAudioFormat);
                return false;
            }
            SourceDataLine source = (SourceDataLine) AudioSystem.getLine(info);
            datapusher = new DataPusher(source, loadedAudioFormat, loadedAudio, loadedAudioByteLength);
        } catch (Exception e) {
            if (DEBUG || Printer.err)e.printStackTrace();
            return false;
        }
        if (datapusher==null) {
            return false;
        }
        if (DEBUG || Printer.debug)Printer.debug("Created SourceDataLine.");
        return true;
    }
    private boolean createSequencer(BufferedInputStream in) throws IOException {
        if (DEBUG || Printer.debug)Printer.debug("JavaSoundAudioClip.createSequencer()");
        try {
            sequencer = MidiSystem.getSequencer( );
        } catch(MidiUnavailableException me) {
            if (DEBUG || Printer.err)me.printStackTrace();
            return false;
        }
        if (sequencer==null) {
            return false;
        }
        try {
            sequence = MidiSystem.getSequence(in);
            if (sequence == null) {
                return false;
            }
        } catch (InvalidMidiDataException e) {
            if (DEBUG || Printer.err)e.printStackTrace();
            return false;
        }
        if (DEBUG || Printer.debug)Printer.debug("Created Sequencer.");
        return true;
    }
    private static class DirectBAOS extends ByteArrayOutputStream {
        public DirectBAOS() {
            super();
        }
        public byte[] getInternalBuffer() {
            return buf;
        }
    } 
}
