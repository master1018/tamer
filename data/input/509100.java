public class Base {
    public Base() {}
    public DoubledExtend getExtended() {
        return new DoubledExtend();
    }
    public static String doStuff(DoubledExtend dt) {
        return dt.getStr();
    }
}
