public class UnsupportedList extends SIPHeaderList<Unsupported> {
    private static final long serialVersionUID = -4052610269407058661L;
    public UnsupportedList() {
        super(Unsupported.class, UnsupportedHeader.NAME);
    }
    public Object clone() {
        UnsupportedList retval = new UnsupportedList();
        return retval.clonehlist(this.hlist);
    }
}
