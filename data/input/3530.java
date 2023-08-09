public class RemoveStaticProvider {
    public static void main(String argv[]) throws Exception {
        Security.removeProvider("SunJCE");
        try {
            KeyAgreement ka = KeyAgreement.getInstance("DH", "SunJCE");
            throw new Exception("Hmm... didn't get expected exception!");
        } catch (java.security.NoSuchProviderException nsa){
            System.out.println("Passed -- as expected: " + nsa);
        }
    }
}
