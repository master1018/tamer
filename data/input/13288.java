public final class DataBufferByte extends DataBuffer
{
    byte data[];
    byte bankdata[][];
    public DataBufferByte(int size) {
      super(STABLE, TYPE_BYTE, size);
      data = new byte[size];
      bankdata = new byte[1][];
      bankdata[0] = data;
    }
    public DataBufferByte(int size, int numBanks) {
        super(STABLE, TYPE_BYTE, size, numBanks);
        bankdata = new byte[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new byte[size];
        }
        data = bankdata[0];
    }
    public DataBufferByte(byte dataArray[], int size) {
        super(UNTRACKABLE, TYPE_BYTE, size);
        data = dataArray;
        bankdata = new byte[1][];
        bankdata[0] = data;
    }
    public DataBufferByte(byte dataArray[], int size, int offset){
        super(UNTRACKABLE, TYPE_BYTE, size, 1, offset);
        data = dataArray;
        bankdata = new byte[1][];
        bankdata[0] = data;
    }
    public DataBufferByte(byte dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_BYTE, size, dataArray.length);
        bankdata = (byte[][]) dataArray.clone();
        data = bankdata[0];
    }
    public DataBufferByte(byte dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_BYTE, size, dataArray.length, offsets);
        bankdata = (byte[][]) dataArray.clone();
        data = bankdata[0];
    }
    public byte[] getData() {
        theTrackable.setUntrackable();
        return data;
    }
    public byte[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }
    public byte[][] getBankData() {
        theTrackable.setUntrackable();
        return (byte[][]) bankdata.clone();
    }
    public int getElem(int i) {
        return (int)(data[i+offset]) & 0xff;
    }
    public int getElem(int bank, int i) {
        return (int)(bankdata[bank][i+offsets[bank]]) & 0xff;
    }
    public void setElem(int i, int val) {
        data[i+offset] = (byte)val;
        theTrackable.markDirty();
    }
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (byte)val;
        theTrackable.markDirty();
    }
}
