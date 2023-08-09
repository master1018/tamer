public class TestStringClassJava {
    public static void main(String[] args) {
        int length = 0x1000;
        StringBuilder sb = new StringBuilder(length);
        while (0 < length--)
            sb.append((char) length);
        new TestEditor(String.class).testJava(sb.toString());
    }
}
