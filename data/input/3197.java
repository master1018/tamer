public class RealmException extends KrbException {
    private static final long serialVersionUID = -9100385213693792864L;
    public RealmException(int i) {
        super(i);
    }
    public RealmException(String s) {
        super(s);
    }
    public RealmException(int i, String s) {
        super(i,s);
    }
}
