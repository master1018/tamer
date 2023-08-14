class MidiInDevice extends AbstractMidiDevice implements Runnable {
    private Thread midiInThread = null;
    MidiInDevice(AbstractMidiDeviceProvider.Info info) {
        super(info);
        if(Printer.trace) Printer.trace("MidiInDevice CONSTRUCTOR");
    }
    protected synchronized void implOpen() throws MidiUnavailableException {
        if (Printer.trace) Printer.trace("> MidiInDevice: implOpen()");
        int index = ((MidiInDeviceProvider.MidiInDeviceInfo)getDeviceInfo()).getIndex();
        id = nOpen(index); 
        if (id == 0) {
            throw new MidiUnavailableException("Unable to open native device");
        }
        if (midiInThread == null) {
            midiInThread = JSSecurityManager.createThread(this,
                                                    "Java Sound MidiInDevice Thread",   
                                                    false,  
                                                    -1,    
                                                    true); 
        }
        nStart(id); 
        if (Printer.trace) Printer.trace("< MidiInDevice: implOpen() completed");
    }
    protected synchronized void implClose() {
        if (Printer.trace) Printer.trace("> MidiInDevice: implClose()");
        long oldId = id;
        id = 0;
        super.implClose();
        nStop(oldId);
        if (midiInThread != null) {
            try {
                midiInThread.join(1000);
            } catch (InterruptedException e) {
            }
        }
        nClose(oldId);
        if (Printer.trace) Printer.trace("< MidiInDevice: implClose() completed");
    }
    public long getMicrosecondPosition() {
        long timestamp = -1;
        if (isOpen()) {
            timestamp = nGetTimeStamp(id);
        }
        return timestamp;
    }
    protected boolean hasTransmitters() {
        return true;
    }
    protected Transmitter createTransmitter() {
        return new MidiInTransmitter();
    }
    private class MidiInTransmitter extends BasicTransmitter {
        private MidiInTransmitter() {
            super();
        }
    }
    public void run() {
        while (id!=0) {
            nGetMessages(id);
            if (id!=0) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {}
            }
        }
        if(Printer.verbose) Printer.verbose("MidiInDevice Thread exit");
        midiInThread = null;
    }
    void callbackShortMessage(int packedMsg, long timeStamp) {
        if (packedMsg == 0 || id == 0) {
            return;
        }
        getTransmitterList().sendMessage(packedMsg, timeStamp);
    }
    void callbackLongMessage(byte[] data, long timeStamp) {
        if (id == 0 || data == null) {
            return;
        }
        getTransmitterList().sendMessage(data, timeStamp);
    }
    private native long nOpen(int index) throws MidiUnavailableException;
    private native void nClose(long id);
    private native void nStart(long id) throws MidiUnavailableException;
    private native void nStop(long id);
    private native long nGetTimeStamp(long id);
    private native void nGetMessages(long id);
}
