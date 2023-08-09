public class TableResult implements Callback {
    public int ncolumns;
    public int nrows;
    public String column[];
    public String types[];
    public Vector rows;
    public TableResult() {
    clear();
    }
    public void clear() {
    column = new String[0];
    types = null;
    rows = new Vector();
    ncolumns = nrows = 0;
    }
    public void columns(String coldata[]) {
    column = coldata;
    ncolumns = column.length;
    }
    public void types(String types[]) {
    this.types = types;
    }
    public boolean newrow(String rowdata[]) {
    if (rowdata != null) {
        rows.addElement(rowdata);
        nrows++;
    }
    return false;
    }
    public String toString() {
    StringBuffer sb = new StringBuffer();
    int i;
    for (i = 0; i < ncolumns; i++) {
        sb.append(column[i] == null ? "NULL" : column[i]);
        sb.append('|');
    }
    sb.append('\n');
    for (i = 0; i < nrows; i++) {
        int k;
        String row[] = (String[]) rows.elementAt(i);
        for (k = 0; k < ncolumns; k++) {
        sb.append(row[k] == null ? "NULL" : row[k]);
        sb.append('|');
        }
        sb.append('\n');
    }
    return sb.toString();
    }
}
