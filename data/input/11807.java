public class InvalidIntfCast {
    public static void main(String[] args) {
        I i = null;
        J j = (J) i;
    }
}
