public class Mutant {
    public int disappearingField = 3;
    public static int disappearingStaticField = 4;
    public void disappearingMethod() {
        System.out.println("bye");
    }
    public static void disappearingStaticMethod() {
        System.out.println("kthxbai");
    }
    public int inaccessibleField = 5;
    public static int inaccessibleStaticField = 6;
    public void inaccessibleMethod() {
        System.out.println("no");
    }
    public static void inaccessibleStaticMethod() {
        System.out.println("nay");
    }
}
