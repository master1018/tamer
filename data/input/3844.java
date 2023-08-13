public final class DataBufferInt extends DataBuffer
{
    int data[];
    int bankdata[][];
    public DataBufferInt(int size) {
        super(STABLE, TYPE_INT, size);
        data = new int[size];
        bankdata = new int[1][];
        bankdata[0] = data;
    }
    public DataBufferInt(int size, int numBanks) {
        super(STABLE, TYPE_INT, size, numBanks);
        bankdata = new int[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new int[size];
        }
        data = bankdata[0];
    }
    public DataBufferInt(int dataArray[], int size) {
        super(UNTRACKABLE, TYPE_INT, size);
        data = dataArray;
        bankdata = new int[1][];
        bankdata[0] = data;
    }
    public DataBufferInt(int dataArray[], int size, int offset) {
        super(UNTRACKABLE, TYPE_INT, size, 1, offset);
        data = dataArray;
        bankdata = new int[1][];
        bankdata[0] = data;
    }
    public DataBufferInt(int dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_INT, size, dataArray.length);
        bankdata = (int [][]) dataArray.clone();
        data = bankdata[0];
    }
    public DataBufferInt(int dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_INT, size, dataArray.length, offsets);
        bankdata = (int [][]) dataArray.clone();
        data = bankdata[0];
    }
    public int[] getData() {
        theTrackable.setUntrackable();
        return data;
    }
    public int[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }
    public int[][] getBankData() {
        theTrackable.setUntrackable();
        return (int [][]) bankdata.clone();
    }
    public int getElem(int i) {
        return data[i+offset];
    }
    public int getElem(int bank, int i) {
        return bankdata[bank][i+offsets[bank]];
    }
    public void setElem(int i, int val) {
        data[i+offset] = val;
        theTrackable.markDirty();
    }
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (int)val;
        theTrackable.markDirty();
    }
}
