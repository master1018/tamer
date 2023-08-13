class MidiOutDevice extends AbstractMidiDevice {
    MidiOutDevice(AbstractMidiDeviceProvider.Info info) {
                super(info);
                if(Printer.trace) Printer.trace("MidiOutDevice CONSTRUCTOR");
    }
    protected synchronized void implOpen() throws MidiUnavailableException {
        if (Printer.trace) Printer.trace("> MidiOutDevice: implOpen()");
        int index = ((AbstractMidiDeviceProvider.Info)getDeviceInfo()).getIndex();
        id = nOpen(index); 
        if (id == 0) {
            throw new MidiUnavailableException("Unable to open native device");
        }
        if (Printer.trace) Printer.trace("< MidiOutDevice: implOpen(): completed.");
    }
    protected synchronized void implClose() {
        if (Printer.trace) Printer.trace("> MidiOutDevice: implClose()");
        long oldId = id;
        id = 0;
        super.implClose();
        nClose(oldId);
        if (Printer.trace) Printer.trace("< MidiOutDevice: implClose(): completed");
    }
    public long getMicrosecondPosition() {
        long timestamp = -1;
        if (isOpen()) {
            timestamp = nGetTimeStamp(id);
        }
        return timestamp;
    }
    protected boolean hasReceivers() {
        return true;
    }
    protected Receiver createReceiver() {
        return new MidiOutReceiver();
    }
    class MidiOutReceiver extends AbstractReceiver {
        protected void implSend(MidiMessage message, long timeStamp) {
            int length = message.getLength();
            int status = message.getStatus();
            if (length <= 3 && status != 0xF0 && status != 0xF7) {
                int packedMsg;
                if (message instanceof ShortMessage) {
                    if (message instanceof FastShortMessage) {
                        packedMsg = ((FastShortMessage) message).getPackedMsg();
                    } else {
                        ShortMessage msg = (ShortMessage) message;
                        packedMsg = (status & 0xFF)
                            | ((msg.getData1() & 0xFF) << 8)
                            | ((msg.getData2() & 0xFF) << 16);
                    }
                } else {
                    packedMsg = 0;
                    byte[] data = message.getMessage();
                    if (length>0) {
                        packedMsg = data[0] & 0xFF;
                        if (length>1) {
                            if (status == 0xFF) {
                                return;
                            }
                            packedMsg |= (data[1] & 0xFF) << 8;
                            if (length>2) {
                                packedMsg |= (data[2] & 0xFF) << 16;
                            }
                        }
                    }
                }
                nSendShortMessage(id, packedMsg, timeStamp);
            } else {
                if (message instanceof FastSysexMessage) {
                    nSendLongMessage(id, ((FastSysexMessage) message).getReadOnlyMessage(),
                                     length, timeStamp);
                } else {
                    nSendLongMessage(id, message.getMessage(), length, timeStamp);
                }
            }
        }
        synchronized void sendPackedMidiMessage(int packedMsg, long timeStamp) {
            if (isOpen() && id != 0) {
                nSendShortMessage(id, packedMsg, timeStamp);
            }
        }
    } 
    private native long nOpen(int index) throws MidiUnavailableException;
    private native void nClose(long id);
    private native void nSendShortMessage(long id, int packedMsg, long timeStamp);
    private native void nSendLongMessage(long id, byte[] data, int size, long timeStamp);
    private native long nGetTimeStamp(long id);
} 
