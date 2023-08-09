public class DoubledImplement2 implements ICommon2 {
    public DoubledImplement2() {
        System.out.println("Ctor: doubled implement, type 1");
    }
    public DoubledImplement2 getDoubledInstance2() {
        return new DoubledImplement2();
    }
    public void one() {
        System.out.println("DoubledImplement2 one");
    }
}
