public class Abort extends Error {
    private static final long serialVersionUID = 0;
    public Abort(Throwable cause) {
        super(cause);
    }
    public Abort() {
        super();
    }
}
