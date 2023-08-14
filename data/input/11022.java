public class MetaMessage extends MidiMessage {
    public static final int META                                                = 0xFF; 
    private static byte[] defaultMessage                                = { (byte)META, 0 };
    private int dataLength = 0;
    public MetaMessage() {
        this(defaultMessage);
    }
    public MetaMessage(int type, byte[] data, int length)
            throws InvalidMidiDataException {
        super(null);
        setMessage(type, data, length); 
    }
    protected MetaMessage(byte[] data) {
        super(data);
        if (data.length>=3) {
            dataLength=data.length-3;
            int pos=2;
            while (pos<data.length && (data[pos] & 0x80)!=0) {
                dataLength--; pos++;
            }
        }
    }
    public void setMessage(int type, byte[] data, int length) throws InvalidMidiDataException {
        if (type >= 128 || type < 0) {
            throw new InvalidMidiDataException("Invalid meta event with type " + type);
        }
        if ((length > 0 && length > data.length) || length < 0) {
            throw new InvalidMidiDataException("length out of bounds: "+length);
        }
        this.length = 2 + getVarIntLength(length) + length;
        this.dataLength = length;
        this.data = new byte[this.length];
        this.data[0] = (byte) META;        
        this.data[1] = (byte) type;        
        writeVarInt(this.data, 2, length); 
        if (length > 0) {
            System.arraycopy(data, 0, this.data, this.length - this.dataLength, this.dataLength);
        }
    }
    public int getType() {
        if (length>=2) {
            return data[1] & 0xFF;
        }
        return 0;
    }
    public byte[] getData() {
        byte[] returnedArray = new byte[dataLength];
        System.arraycopy(data, (length - dataLength), returnedArray, 0, dataLength);
        return returnedArray;
    }
    public Object clone() {
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, newData.length);
        MetaMessage event = new MetaMessage(newData);
        return event;
    }
    private int getVarIntLength(long value) {
        int length = 0;
        do {
            value = value >> 7;
            length++;
        } while (value > 0);
        return length;
    }
    private final static long mask = 0x7F;
    private void writeVarInt(byte[] data, int off, long value) {
        int shift=63; 
        while ((shift > 0) && ((value & (mask << shift)) == 0)) shift-=7;
        while (shift > 0) {
            data[off++]=(byte) (((value & (mask << shift)) >> shift) | 0x80);
            shift-=7;
        }
        data[off] = (byte) (value & mask);
    }
}
