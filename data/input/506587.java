public class T_areturn_12 {
    private TSuper test() {
        return new TChild();
    }
    public boolean run() {
        TSuper t = test();
        return (t instanceof TChild);
    }
}
