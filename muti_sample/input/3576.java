public class RedefineMethodAddInvokeTarget {
    public void test(int counter) throws Exception {
        Method method = getClass().getDeclaredMethod("myMethod" +
            (counter == 0 ? "" : counter));
        method.setAccessible(true);
        method.invoke(this);
    }
    public void myMethod() {
        System.out.println("Hello from the non-EMCP again myMethod()!");
    }
    private final void myMethod1() {
        System.out.println("Hello from myMethod1()!");
        System.out.println("Calling myMethod() from myMethod1():");
        myMethod();
    }
    private final void myMethod2() {
        System.out.println("Hello from myMethod2()!");
        System.out.println("Calling myMethod1() from myMethod2():");
        myMethod1();
    }
}
