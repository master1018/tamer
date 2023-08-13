public final class DataBufferUShort extends DataBuffer {
    short data[][];
    public DataBufferUShort(short dataArrays[][], int size, int offsets[]) {
        super(TYPE_USHORT, size, dataArrays.length, offsets);
        for (int i = 0; i < dataArrays.length; i++) {
            if (dataArrays[i].length < offsets[i] + size) {
                throw new IllegalArgumentException(Messages.getString("awt.28D", i, i)); 
            }
        }
        data = dataArrays.clone();
    }
    public DataBufferUShort(short dataArrays[][], int size) {
        super(TYPE_USHORT, size, dataArrays.length);
        data = dataArrays.clone();
    }
    public DataBufferUShort(short dataArray[], int size, int offset) {
        super(TYPE_USHORT, size, 1, offset);
        if (dataArray.length < size + offset) {
            throw new IllegalArgumentException(Messages.getString("awt.28E")); 
        }
        data = new short[1][];
        data[0] = dataArray;
    }
    public DataBufferUShort(short dataArray[], int size) {
        super(TYPE_USHORT, size);
        data = new short[1][];
        data[0] = dataArray;
    }
    public DataBufferUShort(int size, int numBanks) {
        super(TYPE_USHORT, size, numBanks);
        data = new short[numBanks][];
        int i = 0;
        while (i < numBanks) {
            data[i++] = new short[size];
        }
    }
    public DataBufferUShort(int size) {
        super(TYPE_USHORT, size);
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
        return (data[bank][offsets[bank] + i]) & 0xffff;
    }
    public short[] getData(int bank) {
        notifyTaken();
        return data[bank];
    }
    @Override
    public int getElem(int i) {
        return (data[0][offset + i]) & 0xffff;
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
