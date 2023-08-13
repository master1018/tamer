public class T_areturn_15 {
    private TInterface test() {
        return new TSuper();
    }
    public boolean run() {
        TInterface t = test();
        return (t instanceof TSuper);
    }
}
