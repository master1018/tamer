public abstract class MidiMessage implements Cloneable {
    protected byte[] data;
    protected int length;
    protected MidiMessage(byte[] data) {
        if (data == null) {
            length = 0;
        } else {
            length = data.length;
            this.data = data;
        }
    }
    @Override
    public abstract Object clone();
    public int getLength() {
        return length;
    }
    public byte[] getMessage() {
        if (data == null) {
            throw new NullPointerException();
        }
        return data.clone();
    }
    public int getStatus() {
        if ((data == null) || (length == 0)) {
            return 0;
        }
        return data[0] & 0xFF;
    }
    protected void setMessage(byte[] data, int length) throws InvalidMidiDataException {
        if ((length < 0) || (length > data.length)) {
            throw new IndexOutOfBoundsException(Messages.getString("sound.03", length)); 
        }
        this.data = new byte[length];
        if (length != 0) {
            for (int i = 0; i < length; i++) {
                this.data[i] = data[i];
            }
        }
        this.length = length;
    }
}
