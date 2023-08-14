public final class java_lang_Character extends AbstractTest<Character> {
    public static void main(String[] args) {
        new java_lang_Character().test(true);
    }
    protected Character getObject() {
        return Character.valueOf('\\');
    }
    protected Character getAnotherObject() {
        return Character.valueOf('\uFFFF');
    }
}
