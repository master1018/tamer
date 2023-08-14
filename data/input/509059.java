public class RowSetEvent extends EventObject implements Serializable {
    private static final long serialVersionUID = -1875450876546332005L;
    public RowSetEvent(RowSet theSource) {
        super(theSource);
    }
}
