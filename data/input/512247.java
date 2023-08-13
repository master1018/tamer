public class SQLWarning extends SQLException implements Serializable {
    private static final long serialVersionUID = 3917336774604784856L;
    public SQLWarning() {
        super();
    }
    public SQLWarning(String theReason) {
        super(theReason);
    }
    public SQLWarning(String theReason, String theSQLState) {
        super(theReason, theSQLState);
    }
    public SQLWarning(String theReason, String theSQLState, int theErrorCode) {
        super(theReason, theSQLState, theErrorCode);
    }
    public SQLWarning getNextWarning() {
        SQLException next = super.getNextException();
        if (next == null) {
            return null;
        }
        if (next instanceof SQLWarning) {
            return (SQLWarning) next;
        }
        throw new Error(Messages.getString("sql.8")); 
    }
    public void setNextWarning(SQLWarning w) {
        super.setNextException(w);
    }
}
