public class CheckInstanceof {
    public static void main(Object obj) {
        if (obj instanceof otherpkg.Package)
            System.out.println("yes!");
        else
            System.out.println("no!");
    }
}
