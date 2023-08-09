public class DoubledExtend extends Base {
    public DoubledExtend() {
    }
    @Override
    public DoubledExtend getExtended() {
        System.out.println("getExtended 1");
        return new DoubledExtend();
    }
    public String getStr() {
        return "DoubledExtend 1";
    }
}
