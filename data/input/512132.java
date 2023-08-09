public class JPEGImageReadParam extends ImageReadParam {
    private JPEGQTable qTables[];
    private JPEGHuffmanTable dcHuffmanTables[];
    private JPEGHuffmanTable acHuffmanTables[];
    public JPEGImageReadParam() {
    }
    public boolean areTablesSet() {
        return qTables != null;
    }
    public void setDecodeTables(JPEGQTable[] qTables, JPEGHuffmanTable[] DCHuffmanTables,
            JPEGHuffmanTable[] ACHuffmanTables) {
        if (qTables == null || DCHuffmanTables == null || ACHuffmanTables == null) {
            throw new IllegalArgumentException("Invalid JPEG table arrays");
        }
        if (DCHuffmanTables.length != ACHuffmanTables.length) {
            throw new IllegalArgumentException("Invalid JPEG table arrays");
        }
        if (qTables.length > 4 || DCHuffmanTables.length > 4) {
            throw new IllegalArgumentException("Invalid JPEG table arrays");
        }
        this.qTables = qTables.clone();
        dcHuffmanTables = DCHuffmanTables.clone();
        acHuffmanTables = ACHuffmanTables.clone();
    }
    public void unsetDecodeTables() {
        qTables = null;
        dcHuffmanTables = null;
        acHuffmanTables = null;
    }
    public JPEGQTable[] getQTables() {
        return qTables == null ? null : qTables.clone();
    }
    public JPEGHuffmanTable[] getDCHuffmanTables() {
        return dcHuffmanTables == null ? null : dcHuffmanTables.clone();
    }
    public JPEGHuffmanTable[] getACHuffmanTables() {
        return acHuffmanTables == null ? null : acHuffmanTables.clone();
    }
}
