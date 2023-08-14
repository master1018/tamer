public class ShortMessage extends MidiMessage {
    public static final int MIDI_TIME_CODE                              = 0xF1; 
    public static final int SONG_POSITION_POINTER               = 0xF2; 
    public static final int SONG_SELECT                                 = 0xF3; 
    public static final int TUNE_REQUEST                                = 0xF6; 
    public static final int END_OF_EXCLUSIVE                    = 0xF7; 
    public static final int TIMING_CLOCK                                = 0xF8; 
    public static final int START                                               = 0xFA; 
    public static final int CONTINUE                                    = 0xFB; 
    public static final int STOP                                                = 0xFC; 
    public static final int ACTIVE_SENSING                              = 0xFE; 
    public static final int SYSTEM_RESET                                = 0xFF; 
    public static final int NOTE_OFF                                    = 0x80;  
    public static final int NOTE_ON                                             = 0x90;  
    public static final int POLY_PRESSURE                               = 0xA0;  
    public static final int CONTROL_CHANGE                              = 0xB0;  
    public static final int PROGRAM_CHANGE                              = 0xC0;  
    public static final int CHANNEL_PRESSURE                    = 0xD0;  
    public static final int PITCH_BEND                                  = 0xE0;  
    public ShortMessage() {
        this(new byte[3]);
        data[0] = (byte) (NOTE_ON & 0xFF);
        data[1] = (byte) 64;
        data[2] = (byte) 127;
        length = 3;
    }
    public ShortMessage(int status) throws InvalidMidiDataException {
        super(null);
        setMessage(status); 
    }
    public ShortMessage(int status, int data1, int data2)
            throws InvalidMidiDataException {
        super(null);
        setMessage(status, data1, data2); 
    }
    public ShortMessage(int command, int channel, int data1, int data2)
            throws InvalidMidiDataException {
        super(null);
        setMessage(command, channel, data1, data2);
    }
    protected ShortMessage(byte[] data) {
        super(data);
    }
    public void setMessage(int status) throws InvalidMidiDataException {
        int dataLength = getDataLength(status); 
        if (dataLength != 0) {
            throw new InvalidMidiDataException("Status byte; " + status + " requires " + dataLength + " data bytes");
        }
        setMessage(status, 0, 0);
    }
    public void setMessage(int status, int data1, int data2) throws InvalidMidiDataException {
        int dataLength = getDataLength(status); 
        if (dataLength > 0) {
            if (data1 < 0 || data1 > 127) {
                throw new InvalidMidiDataException("data1 out of range: " + data1);
            }
            if (dataLength > 1) {
                if (data2 < 0 || data2 > 127) {
                    throw new InvalidMidiDataException("data2 out of range: " + data2);
                }
            }
        }
        length = dataLength + 1;
        if (data == null || data.length < length) {
            data = new byte[3];
        }
        data[0] = (byte) (status & 0xFF);
        if (length > 1) {
            data[1] = (byte) (data1 & 0xFF);
            if (length > 2) {
                data[2] = (byte) (data2 & 0xFF);
            }
        }
    }
    public void setMessage(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
        if (command >= 0xF0 || command < 0x80) {
            throw new InvalidMidiDataException("command out of range: 0x" + Integer.toHexString(command));
        }
        if ((channel & 0xFFFFFFF0) != 0) { 
            throw new InvalidMidiDataException("channel out of range: " + channel);
        }
        setMessage((command & 0xF0) | (channel & 0x0F), data1, data2);
    }
    public int getChannel() {
        return (getStatus() & 0x0F);
    }
    public int getCommand() {
        return (getStatus() & 0xF0);
    }
    public int getData1() {
        if (length > 1) {
            return (data[1] & 0xFF);
        }
        return 0;
    }
    public int getData2() {
        if (length > 2) {
            return (data[2] & 0xFF);
        }
        return 0;
    }
    public Object clone() {
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, newData.length);
        ShortMessage msg = new ShortMessage(newData);
        return msg;
    }
    protected final int getDataLength(int status) throws InvalidMidiDataException {
        switch(status) {
        case 0xF6:                      
        case 0xF7:                      
        case 0xF8:                      
        case 0xF9:                      
        case 0xFA:                      
        case 0xFB:                      
        case 0xFC:                      
        case 0xFD:                      
        case 0xFE:                      
        case 0xFF:                      
            return 0;
        case 0xF1:                      
        case 0xF3:                      
            return 1;
        case 0xF2:                      
            return 2;
        default:
        }
        switch(status & 0xF0) {
        case 0x80:
        case 0x90:
        case 0xA0:
        case 0xB0:
        case 0xE0:
            return 2;
        case 0xC0:
        case 0xD0:
            return 1;
        default:
            throw new InvalidMidiDataException("Invalid status byte: " + status);
        }
    }
}
