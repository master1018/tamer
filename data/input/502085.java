public class MutableContextWrapper extends ContextWrapper {
    public MutableContextWrapper(Context base) {
        super(base);
    }
    public void setBaseContext(Context base) {
        mBase = base;
    }
}
