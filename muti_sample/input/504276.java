public class BaseOkay implements IDoubledExtendOkay {
    public BaseOkay() {}
    public DoubledExtendOkay getExtended() {
        return new DoubledExtendOkay();
    }
    public static String doStuff(DoubledExtendOkay dt) {
        return dt.getStr();
    }
}
interface IDoubledExtendOkay {
    public DoubledExtendOkay getExtended();
}
