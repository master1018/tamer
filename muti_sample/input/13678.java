public class Order {
    @VisitOrder(2)
    static double d;
    @VisitOrder(3)
    private Order() {}
    @VisitOrder(4)
    int i;
    @VisitOrder(5)
    static class InnerOrder {
        @VisitOrder(6)
        InnerOrder(){}
        @VisitOrder(7)
        String toString() {return "";}
    }
    @VisitOrder(8)
    String toString() {return "";}
    @VisitOrder(9)
    InnerOrder io;
    @VisitOrder(10)
    String foo() {return toString();}
}
