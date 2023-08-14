abstract class AbstractMidiDevice implements MidiDevice, ReferenceCountingDevice {
    private static final boolean TRACE_TRANSMITTER = false;
    private ArrayList<Receiver> receiverList;
    private TransmitterList transmitterList;
    private Object traRecLock = new Object();
    private MidiDevice.Info info;
    protected  boolean open          = false;
    private int openRefCount;
    private List openKeepingObjects;
    protected long id                   = 0;
    protected AbstractMidiDevice(MidiDevice.Info info) {
        if(Printer.trace) Printer.trace(">> AbstractMidiDevice CONSTRUCTOR");
        this.info = info;
        openRefCount = 0;
        if(Printer.trace) Printer.trace("<< AbstractMidiDevice CONSTRUCTOR completed");
    }
    public MidiDevice.Info getDeviceInfo() {
        return info;
    }
    public void open() throws MidiUnavailableException {
        if (Printer.trace) Printer.trace("> AbstractMidiDevice: open()");
        synchronized(this) {
            openRefCount = -1;
            doOpen();
        }
        if (Printer.trace) Printer.trace("< AbstractMidiDevice: open() completed");
    }
    private void openInternal(Object object) throws MidiUnavailableException {
        if (Printer.trace) Printer.trace("> AbstractMidiDevice: openInternal()");
        synchronized(this) {
            if (openRefCount != -1) {
                openRefCount++;
                getOpenKeepingObjects().add(object);
            }
            doOpen();
        }
        if (Printer.trace) Printer.trace("< AbstractMidiDevice: openInternal() completed");
    }
    private void doOpen() throws MidiUnavailableException {
        if (Printer.trace) Printer.trace("> AbstractMidiDevice: doOpen()");
        synchronized(this) {
            if (! isOpen()) {
                implOpen();
                open = true;
            }
        }
        if (Printer.trace) Printer.trace("< AbstractMidiDevice: doOpen() completed");
    }
    public void close() {
        if (Printer.trace) Printer.trace("> AbstractMidiDevice: close()");
        synchronized (this) {
            doClose();
            openRefCount = 0;
        }
        if (Printer.trace) Printer.trace("< AbstractMidiDevice: close() completed");
    }
    public void closeInternal(Object object) {
        if (Printer.trace) Printer.trace("> AbstractMidiDevice: closeInternal()");
        synchronized(this) {
            if (getOpenKeepingObjects().remove(object)) {
                if (openRefCount > 0) {
                    openRefCount--;
                    if (openRefCount == 0) {
                        doClose();
                    }
                }
            }
        }
        if (Printer.trace) Printer.trace("< AbstractMidiDevice: closeInternal() completed");
    }
    public void doClose() {
        if (Printer.trace) Printer.trace("> AbstractMidiDevice: doClose()");
        synchronized(this) {
            if (isOpen()) {
                implClose();
                open = false;
            }
        }
        if (Printer.trace) Printer.trace("< AbstractMidiDevice: doClose() completed");
    }
    public boolean isOpen() {
        return open;
    }
    protected void implClose() {
        synchronized (traRecLock) {
            if (receiverList != null) {
                for(int i = 0; i < receiverList.size(); i++) {
                    receiverList.get(i).close();
                }
                receiverList.clear();
            }
            if (transmitterList != null) {
                transmitterList.close();
            }
        }
    }
    public long getMicrosecondPosition() {
        return -1;
    }
    public final int getMaxReceivers() {
        if (hasReceivers()) {
            return -1;
        } else {
            return 0;
        }
    }
    public final int getMaxTransmitters() {
        if (hasTransmitters()) {
            return -1;
        } else {
            return 0;
        }
    }
    public final Receiver getReceiver() throws MidiUnavailableException {
        Receiver receiver;
        synchronized (traRecLock) {
            receiver = createReceiver(); 
            getReceiverList().add(receiver);
        }
        return receiver;
    }
    public final List<Receiver> getReceivers() {
        List<Receiver> recs;
        synchronized (traRecLock) {
            if (receiverList == null) {
                recs = Collections.unmodifiableList(new ArrayList<Receiver>(0));
            } else {
                recs = Collections.unmodifiableList
                    ((List<Receiver>) (receiverList.clone()));
            }
        }
        return recs;
    }
    public final Transmitter getTransmitter() throws MidiUnavailableException {
        Transmitter transmitter;
        synchronized (traRecLock) {
            transmitter = createTransmitter(); 
            getTransmitterList().add(transmitter);
        }
        return transmitter;
    }
    public final List<Transmitter> getTransmitters() {
        List<Transmitter> tras;
        synchronized (traRecLock) {
            if (transmitterList == null
                || transmitterList.transmitters.size() == 0) {
                tras = Collections.unmodifiableList(new ArrayList<Transmitter>(0));
            } else {
                tras = Collections.unmodifiableList((List<Transmitter>) (transmitterList.transmitters.clone()));
            }
        }
        return tras;
    }
    long getId() {
        return id;
    }
    public Receiver getReceiverReferenceCounting() throws MidiUnavailableException {
        Receiver receiver;
        synchronized (traRecLock) {
            receiver = getReceiver();
            AbstractMidiDevice.this.openInternal(receiver);
        }
        return receiver;
    }
    public Transmitter getTransmitterReferenceCounting() throws MidiUnavailableException {
        Transmitter transmitter;
        synchronized (traRecLock) {
            transmitter = getTransmitter();
            AbstractMidiDevice.this.openInternal(transmitter);
        }
        return transmitter;
    }
    private synchronized List getOpenKeepingObjects() {
        if (openKeepingObjects == null) {
            openKeepingObjects = new ArrayList();
        }
        return openKeepingObjects;
    }
    private List<Receiver> getReceiverList() {
        synchronized (traRecLock) {
            if (receiverList == null) {
                receiverList = new ArrayList<Receiver>();
            }
        }
        return receiverList;
    }
    protected boolean hasReceivers() {
        return false;
    }
    protected Receiver createReceiver() throws MidiUnavailableException {
        throw new MidiUnavailableException("MIDI IN receiver not available");
    }
    protected TransmitterList getTransmitterList() {
        synchronized (traRecLock) {
            if (transmitterList == null) {
                transmitterList = new TransmitterList();
            }
        }
        return transmitterList;
    }
    protected boolean hasTransmitters() {
        return false;
    }
    protected Transmitter createTransmitter() throws MidiUnavailableException {
        throw new MidiUnavailableException("MIDI OUT transmitter not available");
    }
    protected abstract void implOpen() throws MidiUnavailableException;
    protected void finalize() {
        close();
    }
    protected abstract class AbstractReceiver implements MidiDeviceReceiver {
        private boolean open = true;
        public synchronized void send(MidiMessage message, long timeStamp) {
            if (open) {
                implSend(message, timeStamp);
            } else {
                throw new IllegalStateException("Receiver is not open");
            }
        }
        protected abstract void implSend(MidiMessage message, long timeStamp);
        public void close() {
            open = false;
            synchronized (AbstractMidiDevice.this.traRecLock) {
                AbstractMidiDevice.this.getReceiverList().remove(this);
            }
            AbstractMidiDevice.this.closeInternal(this);
        }
        public MidiDevice getMidiDevice() {
            return AbstractMidiDevice.this;
        }
        protected boolean isOpen() {
            return open;
        }
    } 
    protected class BasicTransmitter implements MidiDeviceTransmitter {
        private Receiver receiver = null;
        TransmitterList tlist = null;
        protected BasicTransmitter() {
        }
        private void setTransmitterList(TransmitterList tlist) {
            this.tlist = tlist;
        }
        public void setReceiver(Receiver receiver) {
            if (tlist != null && this.receiver != receiver) {
                if (Printer.debug) Printer.debug("Transmitter "+toString()+": set receiver "+receiver);
                tlist.receiverChanged(this, this.receiver, receiver);
                this.receiver = receiver;
            }
        }
        public Receiver getReceiver() {
            return receiver;
        }
        public void close() {
            AbstractMidiDevice.this.closeInternal(this);
            if (tlist != null) {
                tlist.receiverChanged(this, this.receiver, null);
                tlist.remove(this);
                tlist = null;
            }
        }
        public MidiDevice getMidiDevice() {
            return AbstractMidiDevice.this;
        }
    } 
    class TransmitterList {
        private ArrayList<Transmitter> transmitters = new ArrayList<Transmitter>();
        private MidiOutDevice.MidiOutReceiver midiOutReceiver;
        private int optimizedReceiverCount = 0;
        private void add(Transmitter t) {
            synchronized(transmitters) {
                transmitters.add(t);
            }
            if (t instanceof BasicTransmitter) {
                ((BasicTransmitter) t).setTransmitterList(this);
            }
            if (Printer.debug) Printer.debug("--added transmitter "+t);
        }
        private void remove(Transmitter t) {
            synchronized(transmitters) {
                int index = transmitters.indexOf(t);
                if (index >= 0) {
                    transmitters.remove(index);
                    if (Printer.debug) Printer.debug("--removed transmitter "+t);
                }
            }
        }
        private void receiverChanged(BasicTransmitter t,
                                     Receiver oldR,
                                     Receiver newR) {
            synchronized(transmitters) {
                if (midiOutReceiver == oldR) {
                    midiOutReceiver = null;
                }
                if (newR != null) {
                    if ((newR instanceof MidiOutDevice.MidiOutReceiver)
                        && (midiOutReceiver == null)) {
                        midiOutReceiver = ((MidiOutDevice.MidiOutReceiver) newR);
                    }
                }
                optimizedReceiverCount =
                      ((midiOutReceiver!=null)?1:0);
            }
        }
        void close() {
            synchronized (transmitters) {
                for(int i = 0; i < transmitters.size(); i++) {
                    transmitters.get(i).close();
                }
                transmitters.clear();
            }
            if (Printer.trace) Printer.trace("TransmitterList.close() succeeded");
        }
        void sendMessage(int packedMessage, long timeStamp) {
            try {
                synchronized(transmitters) {
                    int size = transmitters.size();
                    if (optimizedReceiverCount == size) {
                        if (midiOutReceiver != null) {
                            if (TRACE_TRANSMITTER) Printer.println("Sending packed message to MidiOutReceiver");
                            midiOutReceiver.sendPackedMidiMessage(packedMessage, timeStamp);
                        }
                    } else {
                        if (TRACE_TRANSMITTER) Printer.println("Sending packed message to "+size+" transmitter's receivers");
                        for (int i = 0; i < size; i++) {
                            Receiver receiver = ((Transmitter)transmitters.get(i)).getReceiver();
                            if (receiver != null) {
                                if (optimizedReceiverCount > 0) {
                                    if (receiver instanceof MidiOutDevice.MidiOutReceiver) {
                                        ((MidiOutDevice.MidiOutReceiver) receiver).sendPackedMidiMessage(packedMessage, timeStamp);
                                    } else {
                                        receiver.send(new FastShortMessage(packedMessage), timeStamp);
                                    }
                                } else {
                                    receiver.send(new FastShortMessage(packedMessage), timeStamp);
                                }
                            }
                        }
                    }
                }
            } catch (InvalidMidiDataException e) {
            }
        }
        void sendMessage(byte[] data, long timeStamp) {
            try {
                synchronized(transmitters) {
                    int size = transmitters.size();
                    if (TRACE_TRANSMITTER) Printer.println("Sending long message to "+size+" transmitter's receivers");
                    for (int i = 0; i < size; i++) {
                        Receiver receiver = ((Transmitter)transmitters.get(i)).getReceiver();
                        if (receiver != null) {
                            receiver.send(new FastSysexMessage(data), timeStamp);
                        }
                    }
                }
            } catch (InvalidMidiDataException e) {
                return;
            }
        }
        void sendMessage(MidiMessage message, long timeStamp) {
            if (message instanceof FastShortMessage) {
                sendMessage(((FastShortMessage) message).getPackedMsg(), timeStamp);
                return;
            }
            synchronized(transmitters) {
                int size = transmitters.size();
                if (optimizedReceiverCount == size) {
                    if (midiOutReceiver != null) {
                        if (TRACE_TRANSMITTER) Printer.println("Sending MIDI message to MidiOutReceiver");
                        midiOutReceiver.send(message, timeStamp);
                    }
                } else {
                    if (TRACE_TRANSMITTER) Printer.println("Sending MIDI message to "+size+" transmitter's receivers");
                    for (int i = 0; i < size; i++) {
                        Receiver receiver = ((Transmitter)transmitters.get(i)).getReceiver();
                        if (receiver != null) {
                            receiver.send(message, timeStamp);
                        }
                    }
                }
            }
        }
    } 
}
