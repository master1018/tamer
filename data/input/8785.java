public class JPEGImageReadParam extends ImageReadParam {
    private JPEGQTable[] qTables = null;
    private JPEGHuffmanTable[] DCHuffmanTables = null;
    private JPEGHuffmanTable[] ACHuffmanTables = null;
    public JPEGImageReadParam() {
        super();
    }
    public boolean areTablesSet() {
        return (qTables != null);
    }
    public void setDecodeTables(JPEGQTable[] qTables,
                                JPEGHuffmanTable[] DCHuffmanTables,
                                JPEGHuffmanTable[] ACHuffmanTables) {
        if ((qTables == null) ||
            (DCHuffmanTables == null) ||
            (ACHuffmanTables == null) ||
            (qTables.length > 4) ||
            (DCHuffmanTables.length > 4) ||
            (ACHuffmanTables.length > 4) ||
            (DCHuffmanTables.length != ACHuffmanTables.length)) {
                throw new IllegalArgumentException
                    ("Invalid JPEG table arrays");
        }
        this.qTables = (JPEGQTable[])qTables.clone();
        this.DCHuffmanTables = (JPEGHuffmanTable[])DCHuffmanTables.clone();
        this.ACHuffmanTables = (JPEGHuffmanTable[])ACHuffmanTables.clone();
    }
    public void unsetDecodeTables() {
        this.qTables = null;
        this.DCHuffmanTables = null;
        this.ACHuffmanTables = null;
    }
    public JPEGQTable[] getQTables() {
        return (qTables != null) ? (JPEGQTable[])qTables.clone() : null;
    }
    public JPEGHuffmanTable[] getDCHuffmanTables() {
        return (DCHuffmanTables != null)
            ? (JPEGHuffmanTable[])DCHuffmanTables.clone()
            : null;
    }
    public JPEGHuffmanTable[] getACHuffmanTables() {
        return (ACHuffmanTables != null)
            ? (JPEGHuffmanTable[])ACHuffmanTables.clone()
            : null;
    }
}
