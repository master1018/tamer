public final class java_lang_String extends AbstractTest<String> {
    public static void main(String[] args) {
        new java_lang_String().test(true);
    }
    protected String getObject() {
        return "\u0000\ud800\udc00\uFFFF";
    }
    protected String getAnotherObject() {
        int length = 0x10000;
        StringBuilder sb = new StringBuilder(length);
        while (0 < length--) {
            sb.append((char) length);
        }
        return sb.toString();
    }
}
