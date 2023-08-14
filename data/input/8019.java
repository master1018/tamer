public class IllegalAccessInInvoke {
    public static void main(String[] argv) {
        Class[] argTypes = new Class[0];
        Object[] args = new Object[0];
        Method pm = null;
        try {
            pm = Foo.class.getDeclaredMethod("privateMethod", argTypes);
        } catch (NoSuchMethodException nsme) {
            throw new
                RuntimeException("Bizzare: privateMethod *must* be there");
        }
        boolean ethrown = false;
        try {
            pm.invoke(new Foo(), args);
        } catch (IllegalAccessException iae) {
            ethrown = true;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected " + e.toString());
        }
        if (!ethrown) {
            throw new
                RuntimeException("Reflection access checks are disabled");
        }
    }
}
class Foo {
    private void privateMethod() {
    }
}
