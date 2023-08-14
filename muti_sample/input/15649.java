abstract class AbstractMixer extends AbstractLine implements Mixer {
    protected static final int PCM  = 0;
    protected static final int ULAW = 1;
    protected static final int ALAW = 2;
    private final Mixer.Info mixerInfo;
    protected Line.Info[] sourceLineInfo;
    protected Line.Info[] targetLineInfo;
    private boolean started = false;
    private boolean manuallyOpened = false;
    protected Vector sourceLines = new Vector();
    protected Vector targetLines = new Vector();
    protected AbstractMixer(Mixer.Info mixerInfo,
                            Control[] controls,
                            Line.Info[] sourceLineInfo,
                            Line.Info[] targetLineInfo) {
        super(new Line.Info(Mixer.class), null, controls);
        this.mixer = this;
        if (controls == null) {
            controls = new Control[0];
        }
        this.mixerInfo = mixerInfo;
        this.sourceLineInfo = sourceLineInfo;
        this.targetLineInfo = targetLineInfo;
    }
    public Mixer.Info getMixerInfo() {
        return mixerInfo;
    }
    public Line.Info[] getSourceLineInfo() {
        Line.Info[] localArray = new Line.Info[sourceLineInfo.length];
        System.arraycopy(sourceLineInfo, 0, localArray, 0, sourceLineInfo.length);
        return localArray;
    }
    public Line.Info[] getTargetLineInfo() {
        Line.Info[] localArray = new Line.Info[targetLineInfo.length];
        System.arraycopy(targetLineInfo, 0, localArray, 0, targetLineInfo.length);
        return localArray;
    }
    public Line.Info[] getSourceLineInfo(Line.Info info) {
        int i;
        Vector vec = new Vector();
        for (i = 0; i < sourceLineInfo.length; i++) {
            if (info.matches(sourceLineInfo[i])) {
                vec.addElement(sourceLineInfo[i]);
            }
        }
        Line.Info[] returnedArray = new Line.Info[vec.size()];
        for (i = 0; i < returnedArray.length; i++) {
            returnedArray[i] = (Line.Info)vec.elementAt(i);
        }
        return returnedArray;
    }
    public Line.Info[] getTargetLineInfo(Line.Info info) {
        int i;
        Vector vec = new Vector();
        for (i = 0; i < targetLineInfo.length; i++) {
            if (info.matches(targetLineInfo[i])) {
                vec.addElement(targetLineInfo[i]);
            }
        }
        Line.Info[] returnedArray = new Line.Info[vec.size()];
        for (i = 0; i < returnedArray.length; i++) {
            returnedArray[i] = (Line.Info)vec.elementAt(i);
        }
        return returnedArray;
    }
    public boolean isLineSupported(Line.Info info) {
        int i;
        for (i = 0; i < sourceLineInfo.length; i++) {
            if (info.matches(sourceLineInfo[i])) {
                return true;
            }
        }
        for (i = 0; i < targetLineInfo.length; i++) {
            if (info.matches(targetLineInfo[i])) {
                return true;
            }
        }
        return false;
    }
    public abstract Line getLine(Line.Info info) throws LineUnavailableException;
    public abstract int getMaxLines(Line.Info info);
    protected abstract void implOpen() throws LineUnavailableException;
    protected abstract void implStart();
    protected abstract void implStop();
    protected abstract void implClose();
    public Line[] getSourceLines() {
        Line[] localLines;
        synchronized(sourceLines) {
            localLines = new Line[sourceLines.size()];
            for (int i = 0; i < localLines.length; i++) {
                localLines[i] = (Line)sourceLines.elementAt(i);
            }
        }
        return localLines;
    }
    public Line[] getTargetLines() {
        Line[] localLines;
        synchronized(targetLines) {
            localLines = new Line[targetLines.size()];
            for (int i = 0; i < localLines.length; i++) {
                localLines[i] = (Line)targetLines.elementAt(i);
            }
        }
        return localLines;
    }
    public void synchronize(Line[] lines, boolean maintainSync) {
        throw new IllegalArgumentException("Synchronization not supported by this mixer.");
    }
    public void unsynchronize(Line[] lines) {
        throw new IllegalArgumentException("Synchronization not supported by this mixer.");
    }
    public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
        return false;
    }
    public synchronized void open() throws LineUnavailableException {
        open(true);
    }
    protected synchronized void open(boolean manual) throws LineUnavailableException {
        if (Printer.trace) Printer.trace(">> AbstractMixer: open()");
        if (!isOpen()) {
            implOpen();
            setOpen(true);
            if (manual) {
                manuallyOpened = true;
            }
        }
        if (Printer.trace) Printer.trace("<< AbstractMixer: open() succeeded");
    }
    protected synchronized void open(Line line) throws LineUnavailableException {
        if (Printer.trace) Printer.trace(">> AbstractMixer: open(line = " + line + ")");
        if (this.equals(line)) {
            if (Printer.trace) Printer.trace("<< AbstractMixer: open(" + line + ") nothing done");
            return;
        }
        if (isSourceLine(line.getLineInfo())) {
            if (! sourceLines.contains(line) ) {
                open(false);
                sourceLines.addElement(line);
            }
        } else {
            if(isTargetLine(line.getLineInfo())) {
                if (! targetLines.contains(line) ) {
                    open(false);
                    targetLines.addElement(line);
                }
            } else {
                if (Printer.err) Printer.err("Unknown line received for AbstractMixer.open(Line): " + line);
            }
        }
        if (Printer.trace) Printer.trace("<< AbstractMixer: open(" + line + ") completed");
    }
    protected synchronized void close(Line line) {
        if (Printer.trace) Printer.trace(">> AbstractMixer: close(" + line + ")");
        if (this.equals(line)) {
            if (Printer.trace) Printer.trace("<< AbstractMixer: close(" + line + ") nothing done");
            return;
        }
        sourceLines.removeElement(line);
        targetLines.removeElement(line);
        if (Printer.debug) Printer.debug("AbstractMixer: close(line): sourceLines.size() now: " + sourceLines.size());
        if (Printer.debug) Printer.debug("AbstractMixer: close(line): targetLines.size() now: " + targetLines.size());
        if (sourceLines.isEmpty() && targetLines.isEmpty() && !manuallyOpened) {
            if (Printer.trace) Printer.trace("AbstractMixer: close(" + line + "): need to close the mixer");
            close();
        }
        if (Printer.trace) Printer.trace("<< AbstractMixer: close(" + line + ") succeeded");
    }
    public synchronized void close() {
        if (Printer.trace) Printer.trace(">> AbstractMixer: close()");
        if (isOpen()) {
            Line[] localLines = getSourceLines();
            for (int i = 0; i<localLines.length; i++) {
                localLines[i].close();
            }
            localLines = getTargetLines();
            for (int i = 0; i<localLines.length; i++) {
                localLines[i].close();
            }
            implClose();
            setOpen(false);
        }
        manuallyOpened = false;
        if (Printer.trace) Printer.trace("<< AbstractMixer: close() succeeded");
    }
    protected synchronized void start(Line line) {
        if (Printer.trace) Printer.trace(">> AbstractMixer: start(" + line + ")");
        if (this.equals(line)) {
            if (Printer.trace) Printer.trace("<< AbstractMixer: start(" + line + ") nothing done");
            return;
        }
        if (!started) {
            if (Printer.debug) Printer.debug("AbstractMixer: start(line): starting the mixer");
            implStart();
            started = true;
        }
        if (Printer.trace) Printer.trace("<< AbstractMixer: start(" + line + ") succeeded");
    }
    protected synchronized void stop(Line line) {
        if (Printer.trace) Printer.trace(">> AbstractMixer: stop(" + line + ")");
        if (this.equals(line)) {
            if (Printer.trace) Printer.trace("<< AbstractMixer: stop(" + line + ") nothing done");
            return;
        }
        Vector localSourceLines = (Vector)sourceLines.clone();
        for (int i = 0; i < localSourceLines.size(); i++) {
            if (localSourceLines.elementAt(i) instanceof AbstractDataLine) {
                AbstractDataLine sourceLine = (AbstractDataLine)localSourceLines.elementAt(i);
                if ( sourceLine.isStartedRunning() && (!sourceLine.equals(line)) ) {
                    if (Printer.trace) Printer.trace("<< AbstractMixer: stop(" + line + ") found running sourceLine: " + sourceLine);
                    return;
                }
            }
        }
        Vector localTargetLines = (Vector)targetLines.clone();
        for (int i = 0; i < localTargetLines.size(); i++) {
            if (localTargetLines.elementAt(i) instanceof AbstractDataLine) {
                AbstractDataLine targetLine = (AbstractDataLine)localTargetLines.elementAt(i);
                if ( targetLine.isStartedRunning() && (!targetLine.equals(line)) ) {
                    if (Printer.trace) Printer.trace("<< AbstractMixer: stop(" + line + ") found running targetLine: " + targetLine);
                    return;
                }
            }
        }
        if (Printer.debug) Printer.debug("AbstractMixer: stop(line): stopping the mixer");
        started = false;
        implStop();
        if (Printer.trace) Printer.trace("<< AbstractMixer: stop(" + line + ") succeeded");
    }
    boolean isSourceLine(Line.Info info) {
        for (int i = 0; i < sourceLineInfo.length; i++) {
            if (info.matches(sourceLineInfo[i])) {
                return true;
            }
        }
        return false;
    }
    boolean isTargetLine(Line.Info info) {
        for (int i = 0; i < targetLineInfo.length; i++) {
            if (info.matches(targetLineInfo[i])) {
                return true;
            }
        }
        return false;
    }
    Line.Info getLineInfo(Line.Info info) {
        if (info == null) {
            return null;
        }
        for (int i = 0; i < sourceLineInfo.length; i++) {
            if (info.matches(sourceLineInfo[i])) {
                return sourceLineInfo[i];
            }
        }
        for (int i = 0; i < targetLineInfo.length; i++) {
            if (info.matches(targetLineInfo[i])) {
                return targetLineInfo[i];
            }
        }
        return null;
    }
}
