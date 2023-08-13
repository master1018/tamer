public class PublicConstructor {
    public static void main(String args[]) {
        StackTraceElement ste = new StackTraceElement("com.acme.Widget",
            "frobnicate", "Widget.java", 42);
        if (!(ste.getClassName().equals("com.acme.Widget")  &&
              ste.getFileName().equals("Widget.java") &&
              ste.getMethodName().equals("frobnicate") &&
              ste.getLineNumber() == 42))
            throw new RuntimeException("1");
        if (ste.isNativeMethod())
            throw new RuntimeException("2");
        StackTraceElement ste2 = new StackTraceElement("com.acme.Widget",
            "frobnicate", "Widget.java", -2);
        if (!ste2.isNativeMethod())
            throw new RuntimeException("3");
    }
}
