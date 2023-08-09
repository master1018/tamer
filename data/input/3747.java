class MethodCalls {
    public static void main(String args[]) throws Exception {
        (new MethodCalls()).go();
    }
    static void staticCaller(MethodCalls mc) throws Exception {
        System.out.println("Called staticCaller");
        staticCallee();
        mc.instanceCallee();
        Method m = MethodCalls.class.getDeclaredMethod("staticCallee", new Class[0]);
        m.invoke(mc, new Object[0]);
    }
    void instanceCaller() throws Exception {
        System.out.println("Called instanceCaller");
        staticCallee();
        instanceCallee();
        Method m = getClass().getDeclaredMethod("instanceCallee", new Class[0]);
        m.invoke(this, new Object[0]);
    }
    static void staticCallee() {
        System.out.println("Called staticCallee");
    }
    void instanceCallee() {
        System.out.println("Called instanceCallee");
    }
    void go() throws Exception {
        instanceCaller();
        staticCaller(this);
    }
}
