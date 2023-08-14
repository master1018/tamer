public final class DataBufferUShort extends DataBuffer
{
    short data[];
    short bankdata[][];
    public DataBufferUShort(int size) {
        super(STABLE, TYPE_USHORT, size);
        data = new short[size];
        bankdata = new short[1][];
        bankdata[0] = data;
    }
    public DataBufferUShort(int size, int numBanks) {
        super(STABLE, TYPE_USHORT, size, numBanks);
        bankdata = new short[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new short[size];
        }
        data = bankdata[0];
    }
    public DataBufferUShort(short dataArray[], int size) {
        super(UNTRACKABLE, TYPE_USHORT, size);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        data = dataArray;
        bankdata = new short[1][];
        bankdata[0] = data;
    }
    public DataBufferUShort(short dataArray[], int size, int offset) {
        super(UNTRACKABLE, TYPE_USHORT, size, 1, offset);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        if ((size+offset) > dataArray.length) {
            throw new IllegalArgumentException("Length of dataArray is less "+
                                               " than size+offset.");
        }
        data = dataArray;
        bankdata = new short[1][];
        bankdata[0] = data;
    }
    public DataBufferUShort(short dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_USHORT, size, dataArray.length);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        for (int i=0; i < dataArray.length; i++) {
            if (dataArray[i] == null) {
                throw new NullPointerException("dataArray["+i+"] is null");
            }
        }
        bankdata = (short[][]) dataArray.clone();
        data = bankdata[0];
    }
    public DataBufferUShort(short dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_USHORT, size, dataArray.length, offsets);
        if (dataArray == null) {
            throw new NullPointerException("dataArray is null");
        }
        for (int i=0; i < dataArray.length; i++) {
            if (dataArray[i] == null) {
                throw new NullPointerException("dataArray["+i+"] is null");
            }
            if ((size+offsets[i]) > dataArray[i].length) {
                throw new IllegalArgumentException("Length of dataArray["+i+
                                                   "] is less than size+"+
                                                   "offsets["+i+"].");
            }
        }
        bankdata = (short[][]) dataArray.clone();
        data = bankdata[0];
    }
    public short[] getData() {
        theTrackable.setUntrackable();
        return data;
    }
    public short[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }
    public short[][] getBankData() {
        theTrackable.setUntrackable();
        return (short[][]) bankdata.clone();
    }
    public int getElem(int i) {
        return (int)(data[i+offset]&0xffff);
    }
    public int getElem(int bank, int i) {
        return (int)(bankdata[bank][i+offsets[bank]]&0xffff);
    }
    public void setElem(int i, int val) {
        data[i+offset] = (short)(val&0xffff);
        theTrackable.markDirty();
    }
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (short)(val&0xffff);
        theTrackable.markDirty();
    }
}
