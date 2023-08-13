public class T_return_object_13 {
    private TInterface test() {
        return new TChild();
    }
    public boolean run() {
        TInterface t = test();
        return (t instanceof TChild);
    }
}
