public class DataPusher implements Runnable {
    private static final int AUTO_CLOSE_TIME = 5000;
    private static final boolean DEBUG = false;
    private SourceDataLine source = null;
    private AudioFormat format = null;
    private AudioInputStream ais = null;
    private byte[] audioData = null;
    private int audioDataByteLength = 0;
    private int pos;
    private int newPos = -1;
    private boolean looping;
    private Thread pushThread = null;
    private int wantedState;
    private int threadState;
    private final int STATE_NONE = 0;
    private final int STATE_PLAYING = 1;
    private final int STATE_WAITING = 2;
    private final int STATE_STOPPING = 3;
    private final int STATE_STOPPED = 4;
    private final int BUFFER_SIZE = 16384;
    public DataPusher(SourceDataLine sourceLine, AudioFormat format, byte[] audioData, int byteLength) {
        this.audioData = audioData;
        this.audioDataByteLength = byteLength;
        this.format = format;
        this.source = sourceLine;
    }
    public DataPusher(SourceDataLine sourceLine, AudioInputStream ais) {
        this.ais = ais;
        this.format = ais.getFormat();
        this.source = sourceLine;
    }
    public synchronized void start() {
        start(false);
    }
    public synchronized void start(boolean loop) {
        if (DEBUG || Printer.debug) Printer.debug("> DataPusher.start(loop="+loop+")");
        try {
            if (threadState == STATE_STOPPING) {
                if (DEBUG || Printer.trace)Printer.trace("DataPusher.start(): calling stop()");
                stop();
            }
            looping = loop;
            newPos = 0;
            wantedState = STATE_PLAYING;
            if (!source.isOpen()) {
                if (DEBUG || Printer.trace)Printer.trace("DataPusher: source.open()");
                source.open(format);
            }
            if (DEBUG || Printer.trace)Printer.trace("DataPusher: source.flush()");
            source.flush();
            if (DEBUG || Printer.trace)Printer.trace("DataPusher: source.start()");
            source.start();
            if (pushThread == null) {
                if (DEBUG || Printer.debug) Printer.debug("DataPusher.start(): Starting push");
                pushThread = JSSecurityManager.createThread(this,
                                                            null,   
                                                            false,  
                                                            -1,    
                                                            true); 
            }
            notifyAll();
        } catch (Exception e) {
            if (DEBUG || Printer.err) e.printStackTrace();
        }
        if (DEBUG || Printer.debug) Printer.debug("< DataPusher.start(loop="+loop+")");
    }
    public synchronized void stop() {
        if (DEBUG || Printer.debug) Printer.debug("> DataPusher.stop()");
        if (threadState == STATE_STOPPING
            || threadState == STATE_STOPPED
            || pushThread == null) {
            if (DEBUG || Printer.debug) Printer.debug("DataPusher.stop(): nothing to do");
            return;
        }
        if (DEBUG || Printer.debug) Printer.debug("DataPusher.stop(): Stopping push");
        wantedState = STATE_WAITING;
        if (source != null) {
            if (DEBUG || Printer.trace)Printer.trace("DataPusher: source.flush()");
            source.flush();
        }
        notifyAll();
        int maxWaitCount = 50; 
        while ((maxWaitCount-- >= 0) && (threadState == STATE_PLAYING)) {
            try {
                wait(100);
            } catch (InterruptedException e) {  }
        }
        if (DEBUG || Printer.debug) Printer.debug("< DataPusher.stop()");
    }
    synchronized void close() {
        if (source != null) {
                if (DEBUG || Printer.trace)Printer.trace("DataPusher.close(): source.close()");
                source.close();
        }
    }
    public void run() {
        byte[] buffer = null;
        boolean useStream = (ais != null);
        if (useStream) {
            buffer = new byte[BUFFER_SIZE];
        } else {
            buffer = audioData;
        }
        while (wantedState != STATE_STOPPING) {
                if (wantedState == STATE_WAITING) {
                    if (DEBUG || Printer.debug)Printer.debug("DataPusher.run(): waiting 5 seconds");
                    try {
                        synchronized(this) {
                                threadState = STATE_WAITING;
                                wantedState = STATE_STOPPING;
                                wait(AUTO_CLOSE_TIME);
                        }
                    } catch (InterruptedException ie) {}
                    if (DEBUG || Printer.debug)Printer.debug("DataPusher.run(): waiting finished");
                    continue;
                }
                if (newPos >= 0) {
                        pos = newPos;
                        newPos = -1;
                }
                threadState = STATE_PLAYING;
                int toWrite = BUFFER_SIZE;
                if (useStream) {
                    try {
                        pos = 0; 
                        toWrite = ais.read(buffer, 0, buffer.length);
                    } catch (java.io.IOException ioe) {
                        toWrite = -1;
                    }
                } else {
                    if (toWrite > audioDataByteLength - pos) {
                        toWrite = audioDataByteLength - pos;
                    }
                    if (toWrite == 0) {
                        toWrite = -1; 
                    }
                }
                if (toWrite < 0) {
                    if (DEBUG || Printer.debug) Printer.debug("DataPusher.run(): Found end of stream");
                        if (!useStream && looping) {
                            if (DEBUG || Printer.debug)Printer.debug("DataPusher.run(): setting pos back to 0");
                            pos = 0;
                            continue;
                        }
                    if (DEBUG || Printer.debug)Printer.debug("DataPusher.run(): calling drain()");
                    wantedState = STATE_WAITING;
                    source.drain();
                    continue;
                }
                if (DEBUG || Printer.debug) Printer.debug("> DataPusher.run(): Writing " + toWrite + " bytes");
                    int bytesWritten = source.write(buffer, pos, toWrite);
                    pos += bytesWritten;
                if (DEBUG || Printer.debug) Printer.debug("< DataPusher.run(): Wrote " + bytesWritten + " bytes");
        }
        threadState = STATE_STOPPING;
        if (DEBUG || Printer.debug)Printer.debug("DataPusher: closing device");
        if (Printer.trace)Printer.trace("DataPusher: source.flush()");
        source.flush();
        if (DEBUG || Printer.trace)Printer.trace("DataPusher: source.stop()");
        source.stop();
        if (DEBUG || Printer.trace)Printer.trace("DataPusher: source.flush()");
        source.flush();
        if (DEBUG || Printer.trace)Printer.trace("DataPusher: source.close()");
        source.close();
        threadState = STATE_STOPPED;
        synchronized (this) {
                pushThread = null;
                notifyAll();
        }
        if (DEBUG || Printer.debug)Printer.debug("DataPusher:end of thread");
    }
} 
