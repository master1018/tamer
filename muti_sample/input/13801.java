public class DataTruncation extends SQLWarning {
    public DataTruncation(int index, boolean parameter,
                          boolean read, int dataSize,
                          int transferSize) {
        super("Data truncation", read == true?"01004":"22001");
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
    }
    public DataTruncation(int index, boolean parameter,
                          boolean read, int dataSize,
                          int transferSize, Throwable cause) {
        super("Data truncation", read == true?"01004":"22001",cause);
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
    }
    public int getIndex() {
        return index;
    }
    public boolean getParameter() {
        return parameter;
    }
    public boolean getRead() {
        return read;
    }
    public int getDataSize() {
        return dataSize;
    }
    public int getTransferSize() {
        return transferSize;
    }
    private int index;
    private boolean parameter;
    private boolean read;
    private int dataSize;
    private int transferSize;
    private static final long serialVersionUID = 6464298989504059473L;
}
