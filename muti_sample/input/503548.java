public class T_areturn_14 {
    private TChild test() {
        return new TChild();
    }
    public boolean run() {
        TChild t = test();
        return (t instanceof TChild);
    }
}
