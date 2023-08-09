public final class java_lang_reflect_Method extends AbstractTest<Method> {
    public static void main(String[] args) {
        new java_lang_reflect_Method().test(true);
    }
    protected Method getObject() {
        try {
            return java_lang_reflect_Method.class.getMethod("m1");
        } catch (NoSuchMethodException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    protected Method getAnotherObject() {
        try {
            return java_lang_reflect_Method.class.getMethod("m2");
        } catch (NoSuchMethodException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    public void m1() {
    }
    public void m2() {
    }
}
