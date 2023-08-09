public class InnerTest {
    private int mSomeField;
    private MyStaticInnerClass mInnerInstance;
    private MyIntEnum mTheIntEnum;
    private MyGenerics1<int[][], InnerTest, MyIntEnum, float[]> mGeneric1;
    public class NotStaticInner2 extends NotStaticInner1 {
    }
    public class NotStaticInner1 {
        public void someThing() {
            mSomeField = 2;
            mInnerInstance = null;
        }
    }
    private static class MyStaticInnerClass {
    }
    private static class DerivingClass extends InnerTest {
    }
    public enum MyIntEnum {
        VALUE0(0),
        VALUE1(1),
        VALUE2(2);
        MyIntEnum(int myInt) {
            this.myInt = myInt;
        }
        final int myInt;
    }
    public static class MyGenerics1<T, U, V, W> {
        public MyGenerics1() {
            int a = 1;
        }
    }
    public <X> void genericMethod1(X a, X[] a) {
    }
    public <X, Y> void genericMethod2(X a, List<Y> b) {
    }
    public <X, Y> void genericMethod3(X a, List<Y extends InnerTest> b) {
    }
    public <T extends InnerTest> void genericMethod4(T[] a, Collection<T> b, Collection<?> c) {
        Iterator<T> i = b.iterator();
    }
    public void someMethod(InnerTest self) {
        mSomeField = self.mSomeField;
        MyStaticInnerClass m = new MyStaticInnerClass();
        mInnerInstance = m;
        mTheIntEnum = null;
        mGeneric1 = new MyGenerics1();
        genericMethod(new DerivingClass[0], new ArrayList<DerivingClass>(), new ArrayList<InnerTest>());
    }
}
