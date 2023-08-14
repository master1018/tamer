public final class DataBufferShort extends DataBuffer {
    short data[][];
    public DataBufferShort(short dataArrays[][], int size, int offsets[]) {
        super(TYPE_SHORT, size, dataArrays.length, offsets);
        data = dataArrays.clone();
    }
    public DataBufferShort(short dataArrays[][], int size) {
        super(TYPE_SHORT, size, dataArrays.length);
        data = dataArrays.clone();
    }
    public DataBufferShort(short dataArray[], int size, int offset) {
        super(TYPE_SHORT, size, 1, offset);
        data = new short[1][];
        data[0] = dataArray;
    }
    public DataBufferShort(short dataArray[], int size) {
        super(TYPE_SHORT, size);
        data = new short[1][];
        data[0] = dataArray;
    }
    public DataBufferShort(int size, int numBanks) {
        super(TYPE_SHORT, size, numBanks);
        data = new short[numBanks][];
        int i = 0;
        while (i < numBanks) {
            data[i++] = new short[size];
        }
    }
    public DataBufferShort(int size) {
        super(TYPE_SHORT, size);
        data = new short[1][];
        data[0] = new short[size];
    }
    @Override
    public void setElem(int bank, int i, int val) {
        data[bank][offsets[bank] + i] = (short)val;
        notifyChanged();
    }
    @Override
    public void setElem(int i, int val) {
        data[0][offset + i] = (short)val;
        notifyChanged();
    }
    @Override
    public int getElem(int bank, int i) {
        return (data[bank][offsets[bank] + i]);
    }
    public short[] getData(int bank) {
        notifyTaken();
        return data[bank];
    }
    @Override
    public int getElem(int i) {
        return (data[0][offset + i]);
    }
    public short[][] getBankData() {
        notifyTaken();
        return data.clone();
    }
    public short[] getData() {
        notifyTaken();
        return data[0];
    }
}
