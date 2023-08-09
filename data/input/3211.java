public class RedefineMethodAddInvokeTarget {
    public void test(int counter) throws Exception {
        Method method = getClass().getDeclaredMethod("myMethod" +
            (counter == 0 ? "" : counter));
        method.setAccessible(true);
        method.invoke(this);
    }
    public void myMethod() {
        System.out.println("Hello from the original myMethod()!");
    }
}
