public class RowSetWarning extends SQLException {
     private RowSetWarning rwarning;
    public RowSetWarning(String reason) {
        super(reason);
    }
    public RowSetWarning() {
        super();
    }
    public RowSetWarning(java.lang.String reason, java.lang.String SQLState) {
        super(reason, SQLState);
    }
    public RowSetWarning(java.lang.String reason, java.lang.String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    public RowSetWarning getNextWarning() {
        return rwarning;
    }
    public void setNextWarning(RowSetWarning warning) {
        rwarning = warning;
    }
    static final long serialVersionUID = 6678332766434564774L;
}
