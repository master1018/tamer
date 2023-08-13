class MethodCallsReflection {
    public static void main(String args[]) throws Exception {
        (new MethodCallsReflection()).go();
    }
    static void staticCaller1(MethodCallsReflection mc) throws Exception {
        System.out.println("Called staticCaller1");
        staticExceptionCallee();
    }
    static void staticCaller2(MethodCallsReflection mc) throws Exception {
        System.out.println("Called staticCaller2");
        mc.instanceExceptionCallee();
    }
    static void staticCaller3(MethodCallsReflection mc) throws Exception {
        System.out.println("Called staticCaller3");
        Method m = MethodCallsReflection.class.getDeclaredMethod("staticExceptionCallee", new Class[0]);
        m.invoke(mc, new Object[0]);
    }
    void instanceCaller1() throws Exception {
        System.out.println("Called instanceCaller1");
        staticExceptionCallee();
    }
    void instanceCaller2() throws Exception {
        System.out.println("Called instanceCaller2");
        instanceExceptionCallee();
    }
    void instanceCaller3() throws Exception {
        System.out.println("Called instanceCaller3");
        Method m = getClass().getDeclaredMethod("instanceExceptionCallee", new Class[0]);
        m.invoke(this, new Object[0]);
    }
   static  void staticExceptionCallee() throws Exception {
        System.out.println("Called staticExceptionCallee");
        throw new IndexOutOfBoundsException ("staticExceptionCallee");
    }
    void instanceExceptionCallee() throws Exception {
        System.out.println("Called instanceExceptionCallee");
        throw new IndexOutOfBoundsException ("instanceExceptionCallee");
    }
    void go() throws Exception {
        try {
            instanceCaller1();
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Caught expected IndexOutOfBoundsException from instanceCaller1()");
        }
        try {
            instanceCaller2();
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Caught expected IndexOutOfBoundsException from instanceCaller2()");
        }
        try {
            instanceCaller3();
        } catch (InvocationTargetException ex) {
            System.out.println("Caught expected InvocationTargetException from instanceCaller3()");
        }
        try {
            staticCaller1(this);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Caught expected IndexOutOfBoundsException from staticCaller1()");
        }
        try {
            staticCaller2(this);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("Caught expected IndexOutOfBoundsException from staticCaller2()");
        }
        try {
            staticCaller3(this);
        } catch (InvocationTargetException ex) {
            System.out.println("Caught expected InvocationTargetException from staticCaller3()");
        }
    }
}
