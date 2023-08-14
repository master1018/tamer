public final class java_lang_reflect_Field extends AbstractTest<Field> {
    public int f1;
    public int f2;
    public static void main(String[] args) {
        new java_lang_reflect_Field().test(true);
    }
    protected Field getObject() {
        try {
            return java_lang_reflect_Field.class.getField("f1");
        } catch (NoSuchFieldException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    protected Field getAnotherObject() {
        try {
            return java_lang_reflect_Field.class.getField("f2");
        } catch (NoSuchFieldException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
}
