public class FailOverTest {
    public static void main(String args[]) throws Exception {
        Provider p1 =  new com.p1.Provider1();
        Provider p2 =  new com.p2.Provider2();
        Security.insertProviderAt(p1, 1);
        Security.insertProviderAt(p2, 2);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DUMMY");
        skf.translateKey((SecretKey)null);
        if (skf.getProvider() != p2) {
            throw new Exception("Should have gotten Provider 2");
        } else {
            System.out.println("Test Passed!");
        }
    }
}
