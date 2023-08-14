public class SysexMessage extends MidiMessage {
    public static final int SYSTEM_EXCLUSIVE                    = 0xF0; 
    public static final int SPECIAL_SYSTEM_EXCLUSIVE    = 0xF7; 
    public SysexMessage() {
        this(new byte[2]);
        data[0] = (byte) (SYSTEM_EXCLUSIVE & 0xFF);
        data[1] = (byte) (ShortMessage.END_OF_EXCLUSIVE & 0xFF);
    }
    public SysexMessage(byte[] data, int length)
            throws InvalidMidiDataException {
        super(null);
        setMessage(data, length);
    }
    public SysexMessage(int status, byte[] data, int length)
            throws InvalidMidiDataException {
        super(null);
        setMessage(status, data, length);
    }
    protected SysexMessage(byte[] data) {
        super(data);
    }
    public void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        int status = (data[0] & 0xFF);
        if ((status != 0xF0) && (status != 0xF7)) {
            throw new InvalidMidiDataException("Invalid status byte for sysex message: 0x" + Integer.toHexString(status));
        }
        super.setMessage(data, length);
    }
    public void setMessage(int status, byte[] data, int length) throws InvalidMidiDataException {
        if ( (status != 0xF0) && (status != 0xF7) ) {
            throw new InvalidMidiDataException("Invalid status byte for sysex message: 0x" + Integer.toHexString(status));
        }
        if (length < 0 || length > data.length) {
            throw new IndexOutOfBoundsException("length out of bounds: "+length);
        }
        this.length = length + 1;
        if (this.data==null || this.data.length < this.length) {
            this.data = new byte[this.length];
        }
        this.data[0] = (byte) (status & 0xFF);
        if (length > 0) {
            System.arraycopy(data, 0, this.data, 1, length);
        }
    }
    public byte[] getData() {
        byte[] returnedArray = new byte[length - 1];
        System.arraycopy(data, 1, returnedArray, 0, (length - 1));
        return returnedArray;
    }
    public Object clone() {
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, newData.length);
        SysexMessage event = new SysexMessage(newData);
        return event;
    }
}
