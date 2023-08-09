class FastShortMessage extends ShortMessage {
    private int packedMsg;
    public FastShortMessage(int packedMsg) throws InvalidMidiDataException {
        this.packedMsg = packedMsg;
        getDataLength(packedMsg & 0xFF); 
    }
    public FastShortMessage(ShortMessage msg) {
        this.packedMsg = msg.getStatus()
            | (msg.getData1() << 8)
            | (msg.getData2() << 16);
    }
    int getPackedMsg() {
        return packedMsg;
    }
    public byte[] getMessage() {
        int length = 0;
        try {
            length = getDataLength(packedMsg & 0xFF) + 1;
        } catch (InvalidMidiDataException imde) {
        }
        byte[] returnedArray = new byte[length];
        if (length>0) {
            returnedArray[0] = (byte) (packedMsg & 0xFF);
            if (length>1) {
                returnedArray[1] = (byte) ((packedMsg & 0xFF00) >> 8);
                if (length>2) {
                    returnedArray[2] = (byte) ((packedMsg & 0xFF0000) >> 16);
                }
            }
        }
        return returnedArray;
    }
    public int getLength() {
        try {
            return getDataLength(packedMsg & 0xFF) + 1;
        } catch (InvalidMidiDataException imde) {
        }
        return 0;
    }
    public void setMessage(int status) throws InvalidMidiDataException {
        int dataLength = getDataLength(status); 
        if (dataLength != 0) {
            super.setMessage(status); 
        }
        packedMsg = (packedMsg & 0xFFFF00) | (status & 0xFF);
    }
    public void setMessage(int status, int data1, int data2) throws InvalidMidiDataException {
        getDataLength(status); 
        packedMsg = (status & 0xFF) | ((data1 & 0xFF) << 8) | ((data2 & 0xFF) << 16);
    }
    public void setMessage(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
        getDataLength(command); 
        packedMsg = (command & 0xF0) | (channel & 0x0F) | ((data1 & 0xFF) << 8) | ((data2 & 0xFF) << 16);
    }
    public int getChannel() {
        return packedMsg & 0x0F;
    }
    public int getCommand() {
        return packedMsg & 0xF0;
    }
    public int getData1() {
        return (packedMsg & 0xFF00) >> 8;
    }
    public int getData2() {
        return (packedMsg & 0xFF0000) >> 16;
    }
    public int getStatus() {
        return packedMsg & 0xFF;
    }
    public Object clone() {
        try {
            return new FastShortMessage(packedMsg);
        } catch (InvalidMidiDataException imde) {
        }
        return null;
    }
} 
