public final class DataBufferInt extends DataBuffer {
    int data[][];
    public DataBufferInt(int dataArrays[][], int size, int offsets[]) {
        super(TYPE_INT, size, dataArrays.length, offsets);
        data = dataArrays.clone();
    }
    public DataBufferInt(int dataArrays[][], int size) {
        super(TYPE_INT, size, dataArrays.length);
        data = dataArrays.clone();
    }
    public DataBufferInt(int dataArray[], int size, int offset) {
        super(TYPE_INT, size, 1, offset);
        data = new int[1][];
        data[0] = dataArray;
    }
    public DataBufferInt(int dataArray[], int size) {
        super(TYPE_INT, size);
        data = new int[1][];
        data[0] = dataArray;
    }
    public DataBufferInt(int size, int numBanks) {
        super(TYPE_INT, size, numBanks);
        data = new int[numBanks][];
        int i = 0;
        while (i < numBanks) {
            data[i++] = new int[size];
        }
    }
    public DataBufferInt(int size) {
        super(TYPE_INT, size);
        data = new int[1][];
        data[0] = new int[size];
    }
    @Override
    public void setElem(int bank, int i, int val) {
        data[bank][offsets[bank] + i] = val;
        notifyChanged();
    }
    @Override
    public void setElem(int i, int val) {
        data[0][offset + i] = val;
        notifyChanged();
    }
    @Override
    public int getElem(int bank, int i) {
        return data[bank][offsets[bank] + i];
    }
    public int[] getData(int bank) {
        notifyTaken();
        return data[bank];
    }
    @Override
    public int getElem(int i) {
        return data[0][offset + i];
    }
    public int[][] getBankData() {
        notifyTaken();
        return data.clone();
    }
    public int[] getData() {
        notifyTaken();
        return data[0];
    }
}
