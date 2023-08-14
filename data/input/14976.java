public class LocaleTest {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ITALY);
        try {
            throw new XMLSecurityException("foo");
        } catch (XMLSecurityException xse) {
            System.out.println("Test PASSED");
        } catch (Throwable t) {
            System.out.println("Test FAILED");
            t.printStackTrace();
        }
    }
}
