public class Awt_Hang_Test {
    public static void main(String args[]) {
        java.security.SecureRandom sr = new java.security.SecureRandom();
        sr.nextBytes(new byte[5]);
    }
}
