public class Derived extends Base {
    public static void notDeclaredInBase() {
        System.out.println("notDeclaredInBase: Derived");
    }
    public void overridden() {
        System.out.println("overridden: Derived");
    }
    public void wasOverridden() {
        System.out.println("wasOverridden: Derived");
    }
    public void overrideWithPublic() {
        System.out.println("overrideWithPublic: Derived");
    }
    protected void overridePublicWithProtected() {
        System.out.println("overridePublicWithProtected: Derived");
    }
    public void overrideProtectedWithPublic() {
        System.out.println("overrideProtectedWithPublic: Derived");
    }
    private void overridePublicWithPrivate() {
        System.out.println("overridePublicWithPrivate: Derived");
    }
    public void overridePrivateWithPublic() {
        System.out.println("overridePrivateWithPublic: Derived");
    }
    public static void overrideVirtualWithStatic() {
        System.out.println("overrideVirtualWithStatic: Derived");
    }
    public void overrideStaticWithVirtual() {
        System.out.println("overrideStaticWithVirtual: Derived");
    }
}
