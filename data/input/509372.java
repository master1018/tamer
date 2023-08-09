public class PublicAccess {
    public static void main() {
        String shouldFail = SemiPrivate.mPrivvy;
        System.out.println("Got " + shouldFail);
    }
}
