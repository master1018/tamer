abstract class AbstractDataLine extends AbstractLine implements DataLine {
    protected  AudioFormat defaultFormat;
    protected  int defaultBufferSize;
    protected Object lock = new Object();
    protected AudioFormat format;
    protected int bufferSize;
    protected boolean running = false;
    private boolean started = false;
    private boolean active = false;
    protected AbstractDataLine(DataLine.Info info, AbstractMixer mixer, Control[] controls) {
        this(info, mixer, controls, null, AudioSystem.NOT_SPECIFIED);
    }
    protected AbstractDataLine(DataLine.Info info, AbstractMixer mixer, Control[] controls, AudioFormat format, int bufferSize) {
        super(info, mixer, controls);
        if (format != null) {
            defaultFormat = format;
        } else {
            defaultFormat = new AudioFormat(44100.0f, 16, 2, true, Platform.isBigEndian());
        }
        if (bufferSize > 0) {
            defaultBufferSize = bufferSize;
        } else {
            defaultBufferSize = ((int) (defaultFormat.getFrameRate() / 2)) * defaultFormat.getFrameSize();
        }
        this.format = defaultFormat;
        this.bufferSize = defaultBufferSize;
    }
    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
        synchronized (mixer) {
            if (Printer.trace) Printer.trace("> AbstractDataLine.open(format, bufferSize) (class: "+getClass().getName());
            if (!isOpen()) {
                Toolkit.isFullySpecifiedAudioFormat(format);
                if (Printer.debug) Printer.debug("  need to open the mixer...");
                mixer.open(this);
                try {
                    implOpen(format, bufferSize);
                    setOpen(true);
                } catch (LineUnavailableException e) {
                    mixer.close(this);
                    throw e;
                }
            } else {
                if (Printer.debug) Printer.debug("  dataline already open");
                if (!format.matches(getFormat())) {
                    throw new IllegalStateException("Line is already open with format " + getFormat() +
                                                    " and bufferSize " + getBufferSize());
                }
                if (bufferSize > 0) {
                    setBufferSize(bufferSize);
                }
            }
            if (Printer.trace) Printer.trace("< AbstractDataLine.open(format, bufferSize) completed");
        }
    }
    public void open(AudioFormat format) throws LineUnavailableException {
        open(format, AudioSystem.NOT_SPECIFIED);
    }
    public int available() {
        return 0;
    }
    public void drain() {
        if (Printer.trace) Printer.trace("AbstractDataLine: drain");
    }
    public void flush() {
        if (Printer.trace) Printer.trace("AbstractDataLine: flush");
    }
    public void start() {
        synchronized(mixer) {
            if (Printer.trace) Printer.trace("> "+getClass().getName()+".start() - AbstractDataLine");
            if (isOpen()) {
                if (!isStartedRunning()) {
                    mixer.start(this);
                    implStart();
                    running = true;
                }
            }
        }
        synchronized(lock) {
            lock.notifyAll();
        }
        if (Printer.trace) Printer.trace("< "+getClass().getName()+".start() - AbstractDataLine");
    }
    public void stop() {
        synchronized(mixer) {
            if (Printer.trace) Printer.trace("> "+getClass().getName()+".stop() - AbstractDataLine");
            if (isOpen()) {
                if (isStartedRunning()) {
                    implStop();
                    mixer.stop(this);
                    running = false;
                    if (started && (!isActive())) {
                        setStarted(false);
                    }
                }
            }
        }
        synchronized(lock) {
            lock.notifyAll();
        }
        if (Printer.trace) Printer.trace("< "+getClass().getName()+".stop() - AbstractDataLine");
    }
    public boolean isRunning() {
        return started;
    }
    public boolean isActive() {
        return active;
    }
    public long getMicrosecondPosition() {
        long microseconds = getLongFramePosition();
        if (microseconds != AudioSystem.NOT_SPECIFIED) {
            microseconds = Toolkit.frames2micros(getFormat(), microseconds);
        }
        return microseconds;
    }
    public AudioFormat getFormat() {
        return format;
    }
    public int getBufferSize() {
        return bufferSize;
    }
    public int setBufferSize(int newSize) {
        return getBufferSize();
    }
    public float getLevel() {
        return (float)AudioSystem.NOT_SPECIFIED;
    }
    protected boolean isStartedRunning() {
        return running;
    }
    protected void setActive(boolean active) {
        if (Printer.trace) Printer.trace("> AbstractDataLine: setActive(" + active + ")");
        synchronized (this) {
            if (this.active != active) {
                this.active = active;
            }
        }
    }
    protected void setStarted(boolean started) {
        if (Printer.trace) Printer.trace("> AbstractDataLine: setStarted(" + started + ")");
        boolean sendEvents = false;
        long position = getLongFramePosition();
        synchronized (this) {
            if (this.started != started) {
                this.started = started;
                sendEvents = true;
            }
        }
        if (sendEvents) {
            if (started) {
                sendEvents(new LineEvent(this, LineEvent.Type.START, position));
            } else {
                sendEvents(new LineEvent(this, LineEvent.Type.STOP, position));
            }
        }
        if (Printer.trace) Printer.trace("< AbstractDataLine: setStarted completed");
    }
    protected void setEOM() {
        if (Printer.trace) Printer.trace("> AbstractDataLine: setEOM()");
        setStarted(false);
        if (Printer.trace) Printer.trace("< AbstractDataLine: setEOM() completed");
    }
    public void open() throws LineUnavailableException {
        if (Printer.trace) Printer.trace("> "+getClass().getName()+".open() - AbstractDataLine");
        open(format, bufferSize);
        if (Printer.trace) Printer.trace("< "+getClass().getName()+".open() - AbstractDataLine");
    }
    public void close() {
        synchronized (mixer) {
            if (Printer.trace) Printer.trace("> "+getClass().getName()+".close() - in AbstractDataLine.");
            if (isOpen()) {
                stop();
                setOpen(false);
                implClose();
                mixer.close(this);
                format = defaultFormat;
                bufferSize = defaultBufferSize;
            }
        }
        if (Printer.trace) Printer.trace("< "+getClass().getName()+".close() - in AbstractDataLine");
    }
    abstract void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException;
    abstract void implClose();
    abstract void implStart();
    abstract void implStop();
}
