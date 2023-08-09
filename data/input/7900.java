public class ProtectedInnerClass2 extends p1.ProtectedInnerClass1
{
    class Bug extends Foo {
        String getBug() { return getBar(); }
    }
    public static void main(String[] args) {
        ProtectedInnerClass2 x = new ProtectedInnerClass2();
        Bug y = x.new Bug();
        System.out.println(y.getBug());
    }
}
