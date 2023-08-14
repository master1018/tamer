public class RedefineMethodAddInvokeTarget {
    public void test(int counter) throws Exception {
        Method method = getClass().getDeclaredMethod("myMethod" +
            (counter == 0 ? "" : counter));
        method.setAccessible(true);
        method.invoke(this);
    }
    public void myMethod() {
        System.out.println("Hello from the non-EMCP myMethod()!");
    }
    private final void myMethod1() {
        System.out.println("Hello from myMethod1()!");
        System.out.println("Calling myMethod() from myMethod1():");
        myMethod();
    }
}
