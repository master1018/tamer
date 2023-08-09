public class ForStaticInnerClass {
    static class Static {
    }
    public static void main(String[] args) throws Exception {
        if (!Modifier.isStatic(Static.class.getModifiers()))
            throw new Exception("VM lost static modifier of innerclass.");
    }
}
