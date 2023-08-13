public class SysexMessage extends MidiMessage {
    public static final int SPECIAL_SYSTEM_EXCLUSIVE = 247;
    public static final int SYSTEM_EXCLUSIVE = 240;
    public SysexMessage() {
        super(new byte[] {-16, -9});
    }
    protected SysexMessage(byte[] data) {
        super(data);
    }
    @Override
    public Object clone() {
        return new SysexMessage(this.getMessage());
    }
    public byte[] getData() {
        byte[] bt = new byte[super.length - 1];
        for(int i = 1; i < super.length; i++) {
            bt[i-1] = super.data[i];
        }
        return bt;
    }
    @Override
    public void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        if(((data[0] & 0xFF) != SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) &&
                ((data[0] & 0xFF) != SysexMessage.SYSTEM_EXCLUSIVE)) {
            throw new InvalidMidiDataException(Messages.getString("sound.09",  
                    data[0] & 0xFF));
        }
        super.setMessage(data, length);
    }
    public void setMessage(int status, byte[] data, int length) throws InvalidMidiDataException {
        if((status != SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) &&
                (status != SysexMessage.SYSTEM_EXCLUSIVE)) {
            throw new InvalidMidiDataException(Messages.getString("sound.09",  
                    status));
        }
        if((length < 0) || (length > data.length)) {
            throw new IndexOutOfBoundsException(Messages.getString("sound.03", length)); 
        }
        byte[] bt = new byte[length + 1];
        bt[0] = (byte) status;
        for(int i = 0; i < length; i++) {
            bt[i+1] = data[i];
        }
        super.setMessage(bt, length + 1);
    }
}
