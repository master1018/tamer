public final class DataBufferDouble extends DataBuffer {
    double bankdata[][];
    double data[];
    public DataBufferDouble(int size) {
        super(STABLE, TYPE_DOUBLE, size);
        data = new double[size];
        bankdata = new double[1][];
        bankdata[0] = data;
    }
    public DataBufferDouble(int size, int numBanks) {
        super(STABLE, TYPE_DOUBLE, size, numBanks);
        bankdata = new double[numBanks][];
        for (int i= 0; i < numBanks; i++) {
            bankdata[i] = new double[size];
        }
        data = bankdata[0];
    }
    public DataBufferDouble(double dataArray[], int size) {
        super(UNTRACKABLE, TYPE_DOUBLE, size);
        data = dataArray;
        bankdata = new double[1][];
        bankdata[0] = data;
    }
    public DataBufferDouble(double dataArray[], int size, int offset) {
        super(UNTRACKABLE, TYPE_DOUBLE, size, 1, offset);
        data = dataArray;
        bankdata = new double[1][];
        bankdata[0] = data;
    }
    public DataBufferDouble(double dataArray[][], int size) {
        super(UNTRACKABLE, TYPE_DOUBLE, size, dataArray.length);
        bankdata = (double[][]) dataArray.clone();
        data = bankdata[0];
    }
    public DataBufferDouble(double dataArray[][], int size, int offsets[]) {
        super(UNTRACKABLE, TYPE_DOUBLE, size, dataArray.length, offsets);
        bankdata = (double[][]) dataArray.clone();
        data = bankdata[0];
    }
    public double[] getData() {
        theTrackable.setUntrackable();
        return data;
    }
    public double[] getData(int bank) {
        theTrackable.setUntrackable();
        return bankdata[bank];
    }
    public double[][] getBankData() {
        theTrackable.setUntrackable();
        return (double[][]) bankdata.clone();
    }
    public int getElem(int i) {
        return (int)(data[i+offset]);
    }
    public int getElem(int bank, int i) {
        return (int)(bankdata[bank][i+offsets[bank]]);
    }
    public void setElem(int i, int val) {
        data[i+offset] = (double)val;
        theTrackable.markDirty();
    }
    public void setElem(int bank, int i, int val) {
        bankdata[bank][i+offsets[bank]] = (double)val;
        theTrackable.markDirty();
    }
    public float getElemFloat(int i) {
        return (float)data[i+offset];
    }
    public float getElemFloat(int bank, int i) {
        return (float)bankdata[bank][i+offsets[bank]];
    }
    public void setElemFloat(int i, float val) {
        data[i+offset] = (double)val;
        theTrackable.markDirty();
    }
    public void setElemFloat(int bank, int i, float val) {
        bankdata[bank][i+offsets[bank]] = (double)val;
        theTrackable.markDirty();
    }
    public double getElemDouble(int i) {
        return data[i+offset];
    }
    public double getElemDouble(int bank, int i) {
        return bankdata[bank][i+offsets[bank]];
    }
    public void setElemDouble(int i, double val) {
        data[i+offset] = val;
        theTrackable.markDirty();
    }
    public void setElemDouble(int bank, int i, double val) {
        bankdata[bank][i+offsets[bank]] = val;
        theTrackable.markDirty();
    }
}
