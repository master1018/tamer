class FastSysexMessage extends SysexMessage {
    FastSysexMessage(byte[] data) throws InvalidMidiDataException {
        super(data);
        if (data.length==0 || (((data[0] & 0xFF) != 0xF0) && ((data[0] & 0xFF) != 0xF7))) {
            super.setMessage(data, data.length); 
        }
    }
    byte[] getReadOnlyMessage() {
        return data;
    }
    public void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        if ((data.length == 0) || (((data[0] & 0xFF) != 0xF0) && ((data[0] & 0xFF) != 0xF7))) {
            super.setMessage(data, data.length); 
        }
        this.length = length;
        this.data = new byte[this.length];
        System.arraycopy(data, 0, this.data, 0, length);
    }
} 
