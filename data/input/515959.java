public class DataTruncation extends SQLWarning implements Serializable {
    private static final long serialVersionUID = 6464298989504059473L;
    private int index = 0;
    private boolean parameter = false;
    private boolean read = false;
    private int dataSize = 0;
    private int transferSize = 0;
    private static final String THE_REASON = "Data truncation"; 
    private static final String THE_SQLSTATE = "01004"; 
    private static final int THE_ERROR_CODE = 0;
    public DataTruncation(int index, boolean parameter, boolean read,
            int dataSize, int transferSize) {
        super(THE_REASON, THE_SQLSTATE, THE_ERROR_CODE);
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
    }
    public int getDataSize() {
        return dataSize;
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
    public int getTransferSize() {
        return transferSize;
    }
}
