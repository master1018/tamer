public abstract class MidiMessage implements Cloneable {
    protected byte[] data;
    protected int length = 0;
    protected MidiMessage(byte[] data) {
        this.data = data;
        if (data != null) {
            this.length = data.length;
        }
    }
    protected void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        if (length < 0 || (length > 0 && length > data.length)) {
            throw new IndexOutOfBoundsException("length out of bounds: "+length);
        }
        this.length = length;
        if (this.data == null || this.data.length < this.length) {
            this.data = new byte[this.length];
        }
        System.arraycopy(data, 0, this.data, 0, length);
    }
    public byte[] getMessage() {
        byte[] returnedArray = new byte[length];
        System.arraycopy(data, 0, returnedArray, 0, length);
        return returnedArray;
    }
    public int getStatus() {
        if (length > 0) {
            return (data[0] & 0xFF);
        }
        return 0;
    }
    public int getLength() {
        return length;
    }
    public abstract Object clone();
}
