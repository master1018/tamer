public class ResolveFrom {
    private class Inner {
        int i;
    }
    public static void main(String argv[]) throws Exception {
        int m = ResolveFrom.class.getModifiers();
        System.out.println("ResolveFrom has modifiers = " +
                           Modifier.toString(m));
    }
}
