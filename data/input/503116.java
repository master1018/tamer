public class ShortMessage extends MidiMessage {
    public static final int ACTIVE_SENSING = 254;
    public static final int CHANNEL_PRESSURE = 208;
    public static final int CONTINUE = 251;
    public static final int CONTROL_CHANGE = 176;
    public static final int END_OF_EXCLUSIVE = 247;
    public static final int MIDI_TIME_CODE = 241;
    public static final int NOTE_OFF = 128;
    public static final int NOTE_ON = 144;
    public static final int PITCH_BEND = 224;
    public static final int POLY_PRESSURE = 160;
    public static final int PROGRAM_CHANGE = 192;
    public static final int SONG_POSITION_POINTER = 242;
    public static final int SONG_SELECT = 243;
    public static final int START = 250;
    public static final int STOP = 252;
    public static final int SYSTEM_RESET = 255;
    public static final int TIMING_CLOCK = 248;
    public static final int TUNE_REQUEST = 246;
    public ShortMessage() {
        super(new byte[] {-112, 64, 127});
    }
    protected ShortMessage(byte[] data) {
        super(data);
    }
    @Override
    public Object clone() {
        return new ShortMessage(this.getMessage());
    }
    public int getChannel() {
        if ((data == null) || (data.length == 0)) {
            return 0;
        }
        return data[0] & 0x0F;
    }
    public int getCommand() {
        if ((data == null) || (data.length == 0)) {
            return 0;
        }
        return (data[0] & 0xFF) - getChannel();
    }
    public int getData1() {
        if ((data == null) || (data.length == 0)) {
            return 0;
        } else if (data.length < 2) {
            return 0;
        } else {
            return data[1] & 0xFF;
        }
    }
    public int getData2() {
        if ((data == null) || (data.length == 0)) {
            return 0;
        } else if (data.length < 3) {
            return 0;
        } else {
            return data[2] & 0xFF;
        }
    }
    protected final int getDataLength(int status)
            throws InvalidMidiDataException {
        if (status < 0) {
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); 
        }
        if (((status % 256) >= 0) && ((status % 256) <= 127)) {
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); 
        }
        if (((status / 256) == 0)
                && ((status == 240) || (status == 244) || (status == 245))) {
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); 
        }
        if (((status / 256) != 0) && ((status % 256) >= 244)
                && ((status % 256) <= 255)) {
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); 
        }
        if ((status / 256) == 0) {
            if ((status == 241) || (status == 243)) {
                return 1;
            } else if (status == 242) {
                return 2;
            } else if ((status >= 246) && (status <= 255)) {
                return 0;
            }
        }
        if (((status % 256) >= 128) && ((status % 256) <= 191)) {
            return 2;
        } else if (((status % 256) >= 192) && ((status % 256) <= 223)) {
            return 1;
        } else {
            return 2;
        }
    }
    public void setMessage(int status) throws InvalidMidiDataException {
        if ((status < 246) || (status > 255)) {
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); 
        }
        super.setMessage(new byte[] {(byte) status}, 1);
    }
    public void setMessage(int status, int data1, int data2)
            throws InvalidMidiDataException {
        if ((status < 246) || (status > 255)) {
            throw new InvalidMidiDataException(Messages.getString("sound.04", status)); 
        }
        super.setMessage(new byte[] {(byte) status}, 1);
    }
    public void setMessage(int command, int channel, int data1, int data2)
            throws InvalidMidiDataException {
        if ((command < 128) || (command > 239)) {
            throw new InvalidMidiDataException(Messages.getString("sound.05", command)); 
        }
        if ((channel < 0) || (channel > 15)) {
            throw new InvalidMidiDataException(Messages.getString("sound.06", channel)); 
        }
        if ((getDataLength(command) >= 1) && ((data1 < 0) || (data1 > 127))) {
            throw new InvalidMidiDataException(Messages.getString("sound.07", data1)); 
        }
        if ((getDataLength(command) == 2) && ((data2 < 0) || (data2 > 127))) {
            throw new InvalidMidiDataException(Messages.getString("sound.08", data2)); 
        }
        int tcom = command - (command % 16);
        if (getDataLength(command) == 1) {
            super.setMessage(new byte[] {(byte) (tcom + channel), (byte) data1}, 2);
        } else {
            super.setMessage(new byte[] {(byte) (tcom + channel), (byte) data1, 
                    (byte) data2}, 3);
        }
    }
}
